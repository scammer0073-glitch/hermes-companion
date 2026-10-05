import subprocess
import sys
import unittest
from pathlib import Path

from scripts.release_version import MAX_VERSION_CODE, release_metadata


class ReleaseVersionTest(unittest.TestCase):
    def test_versions_increase_across_minor_and_major_boundaries(self):
        tags = ["v0.0.1", "v0.999.999", "v1.0.0", "v1.9.99", "v1.10.0", "v1.99.0", "v2.0.0"]
        codes = [release_metadata(tag)[1] for tag in tags]
        self.assertTrue(all(left < right for left, right in zip(codes, codes[1:])))
        self.assertEqual(len(codes), len(set(codes)))

    def test_metadata(self):
        self.assertEqual(("1.10.2", 1_010_002), release_metadata("v1.10.2"))

    def test_upper_bound(self):
        self.assertEqual(MAX_VERSION_CODE, release_metadata("v2100.0.0")[1])
        for tag in ["v2100.0.1", "v2101.0.0", "v0.0.0", "v1.1000.0", "v1.0.1000"]:
            with self.subTest(tag=tag), self.assertRaises(ValueError):
                release_metadata(tag)

    def test_rejects_noncanonical_or_unstable_tags(self):
        for tag in ["1.0.0", "v01.0.0", "v1.00.0", "v1.0.01", "v1.0", "v1.0.0-beta", "v1.0.0+build", "v1.0.0\n", 'v1.0.0"; echo nope', "v-1.0.0"]:
            with self.subTest(tag=tag), self.assertRaises(ValueError):
                release_metadata(tag)

    def test_cli_outputs_workflow_metadata(self):
        script = Path(__file__).resolve().parents[1] / "release_version.py"
        result = subprocess.run([sys.executable, str(script), "v1.10.2"], capture_output=True, text=True, check=True)
        self.assertEqual("tag=v1.10.2\nversion=1.10.2\ncode=1010002\n", result.stdout)

    def test_cli_invalid_tag_emits_no_workflow_outputs(self):
        script = Path(__file__).resolve().parents[1] / "release_version.py"
        result = subprocess.run([sys.executable, str(script), "v1.0.0\ncode=99"], capture_output=True, text=True)
        self.assertNotEqual(0, result.returncode)
        self.assertEqual("", result.stdout)


if __name__ == "__main__":
    unittest.main()
