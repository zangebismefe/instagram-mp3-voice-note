# Instagram MP3 Voice Note — Morphe research

Target: Instagram Android **452.1.0.53.64** (version code **385811006**, ARM64, APKM).

**Status: research only. No functional patch, `.mpp` bundle, or Morphe-compatible release is available yet.** Adding this repository URL to Morphe Manager will not patch Instagram at present.

The goal is to import an MP3 and send it as a **native Instagram Direct voice message**, not as a file attachment. This requires a verified version-specific send hook, audio format conversion, and device testing.

- [APK inspection findings](docs/apk-analysis.md)
- [Morphe integration plan](docs/morphe-integration.md)
- [Offline APKM inspection script](scripts/inspect_apkm.py)

The official [Morphe patch template](https://github.com/MorpheApp/morphe-patches-template) build and release configuration has been partially imported and configured. The Gradle wrapper JAR, remaining build dependencies, an actual Instagram hook, a verified `.mpp` build, and a release are still missing. Do not use this as a Morphe patch source yet.
