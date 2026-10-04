"""Validate stable release tags and emit metadata for the release workflow."""

import argparse
import re

MAX_VERSION_CODE = 2_100_000_000
TAG_PATTERN = re.compile(r"v(0|[1-9][0-9]{0,3})\.(0|[1-9][0-9]{0,2})\.(0|[1-9][0-9]{0,2})")


def release_metadata(tag: str) -> tuple[str, int]:
    match = TAG_PATTERN.fullmatch(tag)
    if not match:
        raise ValueError("Use vMAJOR.MINOR.PATCH with no leading zeros; minor and patch must be 0..999.")
    major, minor, patch = map(int, match.groups())
    # Issue #16: fixed slots preserve ordering across minor and major version boundaries.
    code = major * 1_000_000 + minor * 1_000 + patch
    if not 0 < code <= MAX_VERSION_CODE:
        raise ValueError("Release versionCode must be between 1 and 2100000000.")
    return tag[1:], code


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("tag")
    args = parser.parse_args()
    try:
        version, code = release_metadata(args.tag)
    except ValueError as error:
        parser.error(str(error))
    print(f"tag={args.tag}")
    print(f"version={version}")
    print(f"code={code}")


if __name__ == "__main__":
    main()
