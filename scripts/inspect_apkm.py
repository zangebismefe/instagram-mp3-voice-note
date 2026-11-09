#!/usr/bin/env python3
"""Offline APKM inventory and DEX string-lead scanner. Does not decompile methods."""
import argparse
import io
import json
import zipfile

TERMS = (
    b"voice_message", b"voice_note", b"audio_message", b"direct_voice",
    b"voice_recording", b"audio_upload", b"waveform",
)

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("apkm", help="Path to your locally obtained APKM")
    args = parser.parse_args()
    with zipfile.ZipFile(args.apkm) as package:
        metadata = json.loads(package.read("info.json"))
        print("Package:", metadata.get("pname"))
        print("Version:", metadata.get("release_version"))
        print("Version code:", metadata.get("versioncode"))
        print("Architecture:", metadata.get("arches"))
        print("APK members:", [n for n in package.namelist() if n.endswith(".apk")])
        with zipfile.ZipFile(io.BytesIO(package.read("base.apk"))) as apk:
            dex_names = [n for n in apk.namelist() if n.endswith(".dex")]
            print("DEX count:", len(dex_names))
            for name in dex_names:
                content = apk.read(name).lower()
                counts = {term.decode(): content.count(term) for term in TERMS if term in content}
                if counts:
                    print(name, counts)
    print("Note: string matches are NOT verified patch hooks.")

if __name__ == "__main__":
    main()
