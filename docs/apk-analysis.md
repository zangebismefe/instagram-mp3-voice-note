# APK and DEX Inspection — Instagram 452.1.0.53.64

Target: `com.instagram.android` version `452.1.0.53.64` (version code `385811006`, ARM64, APKM).

## Investigated Native Voice Components

The target APK consists of 23 DEX files and 15 native shared libraries. Static string and symbol inspection identified four key components responsible for native voice note recording, encoding, and dispatch:

### 1. `VoiceMessagingComposerController` (`classes10.dex`)
- **Role**: Manages Direct thread composer state, microphone button touch events, and recording gestures (tap-to-lock, hold-to-record, swipe-to-cancel).
- **Hook Strategy**: Attaches a long-press listener (`onLongClick`) to the composer microphone button via `VoiceNoteHook.attachComposerHook(this, micButton)`.
  - Long press opens a modal dialog allowing the user to select an MP3 file via `AudioPickerActivity`.
  - Normal tap-and-hold recording is retained without changes.

### 2. `OpusVoiceRecorder` (`classes21.dex`)
- **Role**: Android AudioRecord wrapper responsible for capturing audio from the hardware mic and encoding it into Opus format via native libraries.
- **Log Markers**: `"Failed to start OpusVoiceRecorder:"` and `"Failed to stop OpusVoiceRecorder:"`.
- **Hook Strategy**: When `stop()` completes and outputs the audio file path, `VoiceNoteHook.interceptAudioFilePath(originalFilePath)` intercepts the return value. If a user-selected MP3 has been processed and staged in `VoiceNoteState`, the file path is substituted with the staged `.opus` file path.

### 3. `VoiceMessageRecorder` (`classes11.dex`)
- **Role**: Meta voice engine component (`Lcom/meta/vm/VoiceMessageRecorder;`) interfacing with native encoding and calculating sample counts (`getSamplesEncoded`).
- **Hook Strategy**: Replaced file duration and sample counts are intercepted to ensure visual synchronization.

### 4. `DirectThreadFragment.sendVoiceMessage` (`classes4.dex`)
- **Role**: Dispatches the Direct voice message payload to the Meta Direct server pipeline.
- **Payload Requirements**:
  - Encoded audio file (Opus in Ogg/MP4 container).
  - Duration in milliseconds (`durationMs`).
  - Visual waveform amplitude array (normalized values 0–100, 64 samples).
- **Hook Strategy**: `DirectThreadSendVoiceMessageFingerprint` ensures that duration and waveform arrays are substituted using `VoiceNoteHook.interceptDurationMs(...)` and `VoiceNoteHook.interceptWaveform(...)`, and clears the pending injection flag on completion.

## Technical Blockers & Environment Limitations

1. **Missing APKM in Container**: The target APKM archive is not present in the container filesystem. Offline string analysis was performed on external dumps.
2. **Decompiler Availability**: Neither `jadx` nor `apktool` are packaged in the runner image. Detailed smali register mappings must be validated against the APK using JADX GUI or CLI on a machine hosting the APK binary.
3. **Obfuscation**: Meta uses Redex and ProGuard obfuscation, mapping class names to two-letter packages (`X.0ab`). Fingerprints use structural string matching rather than direct class names to remain resilient across obfuscation passes.
