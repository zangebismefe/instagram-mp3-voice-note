# Instagram MP3 Voice Note — Morphe Patch

Experimental Morphe bytecode patch and extension for Instagram Direct on Android.

Target: Instagram Android **452.1.0.53.64** (version code **385811006**, ARM64, APKM).

## Objective

Allows users to select a local MP3 audio file from their device storage and send it in Instagram Direct as a **native voice message** (with real playback duration and waveform visualization), rather than sending it as a file or document attachment. Normal microphone voice recording is completely preserved.

## Architecture

The project consists of two cooperating layers according to the official Morphe framework:

1. **Extension (`extensions/extension`)**:
   - `AudioPickerActivity`: Translucent activity invoking Android's Storage Access Framework (`audio/*`) to pick MP3/audio files.
   - `AudioConverter`: Extracts audio metadata, determines duration, and calculates the 64-point normalized waveform amplitude array required by Instagram Direct voice bubbles.
   - `VoiceNoteState`: Thread-safe staging manager holding the injected audio file and metadata.
   - `VoiceNoteHook`: Injects long-press handling on Direct's microphone button and hooks the audio path, duration, and waveform data upon dispatch.

2. **Bytecode Patch (`patches`)**:
   - `InstagramVoicePatch.kt`: Morphe `bytecodePatch` declared for `com.instagram.android`.
   - Injects Smali instructions calling `VoiceNoteHook` into `VoiceMessagingComposerController`, `OpusVoiceRecorder`, and `DirectThreadFragment`.
   - Bundles the compiled extension via `extendWith("extensions/extension.mpe")`.

<!-- PATCHES_START EXPANDED -->
> **[v1.0.0-dev.1](https://github.com/zangebismefe/instagram-mp3-voice-note/releases/tag/v1.0.0-dev.1)**&nbsp;&nbsp;•&nbsp;&nbsp;`dev`&nbsp;&nbsp;•&nbsp;&nbsp;1 patches total
<details open>
<summary>📦 Instagram&nbsp;&nbsp;•&nbsp;&nbsp;1 patch</summary>
<br>

**Package name:** `com.instagram.android`

**🎯 Supported versions:**

| 🧪&nbsp;452.1.0.53.64 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Instagram MP3 Voice Note](#instagram-mp3-voice-note) | Allows selecting a local MP3 audio file and sending it as a native Instagram Direct voice message instead of an attachment. |  |

</details>

<!-- PATCHES_END -->

## Documentation & Analysis

- [DEX and APK Analysis](docs/apk-analysis.md)
- [Morphe Integration & Build Status](docs/morphe-integration.md)
- [Offline APKM Inspection Script](scripts/inspect_apkm.py)

## Building the Bundle

Prerequisites:
- JDK 21
- Node.js 20+
- Access to GitHub Packages with `GITHUB_TOKEN` to resolve `app.morphe.patches` Gradle plugin.

Run:
```bash
./gradlew :patches:buildAndroid
```
The compiled patch bundle (`.mpp`) will be output to `patches/build/libs/`.
