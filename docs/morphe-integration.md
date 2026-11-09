# Morphe Source Integration & Build Status

Official template: https://github.com/MorpheApp/morphe-patches-template
Patch source URL: `https://github.com/zangebismefe/instagram-mp3-voice-note`

## Implementation Status

1. **Gradle Wrapper**:
   - `gradle/wrapper/gradle-wrapper.jar` generated and added.

2. **Android Extension (`extensions/extension`)**:
   - Package: `dev.zangebismefe.instagramvoice`
   - `AudioPickerActivity.java`: System audio file picker via `Intent.ACTION_GET_CONTENT` (`audio/*`).
   - `AudioConverter.java`: Media metadata extractor, duration calculator, and 64-point normalized waveform generator (0–100 amplitude levels).
   - `VoiceNoteState.java`: Concurrency-safe staging store coordinating injected audio files, duration, and waveform data.
   - `VoiceNoteHook.java`: Runtime hook bridge attaching long-press handlers to Direct's microphone button and intercepting the audio path, duration, and waveform data upon dispatch.
   - `AndroidManifest.xml`: Configured with translucent `AudioPickerActivity`.

3. **Bytecode Patch (`patches`)**:
   - `InstagramVoicePatch.kt`: Declared with Morphe's `bytecodePatch` DSL.
   - Bundles `extensions/extension.mpe`.
   - Fingerprints:
     - `VoiceMessagingComposerFingerprint`: Injects composer hook on mic button.
     - `OpusVoiceRecorderStopFingerprint`: Injects audio file path replacement.
     - `DirectThreadSendVoiceMessageFingerprint`: Injects duration/waveform substitution and clean up.

4. **Morphe Manager Compatibility**:
   - `patches-list.json`: Configured with version `1.0.0-experimental.1`, targeting package `com.instagram.android` version `452.1.0.53.64` (APKM).
   - `patches-bundle.json`: Release artifact index for Morphe Manager dynamic source loading.
   - `README.md`: Auto-generated via `.github/scripts/generate_patches_readme.py`.

## Technical Blockers & Next Steps for Device Testing

### 1. GitHub Packages Plugin Resolution
- **Issue**: Gradle requires authentication to resolve `app.morphe.patches:2.1.0` from `https://maven.pkg.github.com/MorpheApp/registry`.
- **Resolution**:
  - In GitHub Actions CI, this is provided automatically via `${{ secrets.GITHUB_TOKEN }}` in `.github/workflows/verify-build.yml` and `.github/workflows/release.yml`.
  - To compile locally on your workstation, configure `~/.gradle/gradle.properties`:
    ```properties
    gpr.user=YOUR_GITHUB_USERNAME
    gpr.key=YOUR_GITHUB_PERSONAL_ACCESS_TOKEN
    ```

### 2. Building the `.mpp` Artifact
Once pushed to GitHub or with local GitHub Packages credentials, compile the bundle:
```bash
./gradlew :patches:buildAndroid
```
The output `.mpp` bundle is created in `patches/build/libs/`.

### 3. Publishing an Experimental Release
- Push changes to the `dev` branch.
- The semantic-release workflow (`.github/workflows/release.yml`) will trigger, compile the `.mpp` bundle, sign it (if `MORPHE_SIGNING_KEY` is configured), and publish the experimental release tag `v1.0.0-experimental.1`.
- Once published, adding `https://github.com/zangebismefe/instagram-mp3-voice-note` to Morphe Manager will recognize the experimental patch source for device testing.
