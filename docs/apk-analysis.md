# APK inspection — Instagram 452.1.0.53.64

Inspected user-provided APKMirror APKM archive locally (not committed).

- Package: `com.instagram.android`
- Version name: `452.1.0.53.64`
- Version code: `385811006`
- Architecture: `arm64-v8a`
- Minimum Android API: `28` (Android 9)
- Format: APKM with `base.apk`, `split_config.xxhdpi.apk`, `split_config.xxxhdpi.apk`
- `base.apk`: 23 DEX files, 15 native shared libraries

## Initial static string scan

Across multiple DEX files, references to `voice_message`, `voice_note`, `audio_message`, `direct_voice`, `voice_recording`, `audio_upload` and `waveform` were found. These are **search leads only**, not verified method names or proof of a usable native send hook.

## Blockers to a functional patch

1. Decompile/disassemble and map the recorder, encoding, upload, and Direct message dispatch paths.
2. Verify a specific method/fingerprint against this APK version.
3. Determine the accepted audio codec/container and metadata for native voice notes.
4. Integrate a file picker and stream conversion without disrupting normal recording.
5. Compile a Morphe `.mpp` and test against a real test account/device.

Do not mark this Instagram version as **confirmed compatible** until the native voice-note send test passes.
