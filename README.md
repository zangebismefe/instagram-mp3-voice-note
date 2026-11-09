# Instagram MP3 Voice Note — Morphe patch research

**Target application:** Instagram Android `452.1.0.53.64` (user-reported).
**Goal:** select a local MP3 in Instagram Direct and send it as a **native voice message**, not a document attachment.

## Current status

Research stage; **no working patch or `.mpp` build yet**. Merely adding a file picker does not implement a native Instagram voice note. The target APK's actual voice recording, upload, and message dispatch paths must be identified and tested.

## Development plan

1. Base the project on the official [Morphe patches template](https://github.com/MorpheApp/morphe-patches-template), retaining its existing build and release workflows.
2. Obtain a legally sourced APK for exactly `452.1.0.53.64` for local analysis and testing. Do not commit the APK or private user data.
3. Trace the app's voice-message encoding, upload, and Direct send path; determine whether an imported audio stream can use it.
4. Add an MP3 picker and format conversion only after the real send hook is verified.
5. Build a `.mpp` bundle, patch a test installation, and verify that the recipient sees a playable native voice note.

## Important

The GitHub repository is currently documentation-only. Do **not** add this repository as a working patch source until it contains a verified Morphe patch bundle and release.
