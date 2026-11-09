# Morphe source integration

Official template: https://github.com/MorpheApp/morphe-patches-template

The template uses the Morphe patches Gradle plugin and its original semantic-release workflow to publish `.mpp` bundles, `patches-bundle.json`, and `patches-list.json`. Its README instructs developers to work on `dev`, build with `./gradlew buildAndroid`, and publish via the provided release workflow.

**Do not add this repository as a working patch source yet.** It currently has no executable patch or release.

When a verified patch is implemented:
1. Bring in the **complete official template**, including Gradle wrapper and original workflows (do not recreate release scripts).
2. Replace template examples with a version-specific Instagram patch.
3. Build and test the bundle; keep Instagram 452.1.0.53.64 marked experimental until tested.
4. Release using the template's semantic-release workflow.
5. Add `https://github.com/zangebismefe/instagram-mp3-voice-note` in Morphe Manager as a patch source.

A repo URL alone cannot make an unimplemented patch work.
