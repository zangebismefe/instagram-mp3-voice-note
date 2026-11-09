# Instagram MP3 Voice Note — Morphe patch research

Goal: allow choosing an MP3 file in Instagram Direct and sending it as a native voice note.

## Status

Research/prototype stage. **No functioning patch or `.mpp` artifact is available yet.** A native voice note requires integration with Instagram's version-specific voice upload/send flow, not just attaching an MP3.

## Next steps

1. Inspect the current Morphe patch template and patch API.
2. Identify the exact Instagram APK version for testing.
3. Investigate the voice recorder/upload/send code path in that version.
4. Implement a file picker, compatible audio conversion, and voice-message dispatch hook.
5. Build and test a patch against the specified Instagram version.

Avoid publishing account credentials, access tokens, or private Instagram messages.
