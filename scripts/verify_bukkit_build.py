"""Verify the produced Bukkit jar and the actual JUnit reports using the stdlib."""

from pathlib import Path
import re
import xml.etree.ElementTree as ET
import zipfile


def verify() -> None:
    jars = list(Path("bukkit/build/libs").glob("supergrim-bukkit-*.jar"))
    assert len(jars) == 1, f"Expected one SuperGrim Bukkit artifact, found {jars}"
    with zipfile.ZipFile(jars[0]) as jar:
        metadata = jar.read("plugin.yml").decode("utf-8")
        for field, value in (
            ("name", "SuperGrim"),
            ("main", "ac.grim.grimac.platform.bukkit.GrimACBukkitLoaderPlugin"),
            ("folia-supported", "true"),
        ):
            assert re.search(rf"(?m)^{field}:\s*['\"]?{re.escape(value)}['\"]?\s*$", metadata), metadata
        assert re.search(r"(?m)^author:\s*['\"]?Onxe['\"]?\s*$|^authors:\s*\[[^\n]*\bOnxe\b[^\n]*\]|^\s*-\s*['\"]?Onxe['\"]?\s*$", metadata), metadata
        for name in (
            "ac/grim/grimac/platform/bukkit/GrimACBukkitLoaderPlugin.class",
            "ac/grim/grimac/utils/files/LegacyDataFolderMigration.class",
            "ac/grim/grimac/utils/anticheat/update/BlockPlace.class",
        ):
            assert name in jar.namelist(), f"Missing class: {name}"
        assert "fabric.mod.json" not in jar.namelist(), "Unexpected Fabric metadata"

    reports = list(Path("common/build/test-results/test").glob("TEST-*.xml"))
    assert reports, "No JUnit reports were produced"
    total = 0
    migration_tests = 0
    for report in reports:
        suite = ET.parse(report).getroot()
        count = int(suite.attrib["tests"])
        total += count
        assert int(suite.attrib.get("failures", 0)) == 0, report
        assert int(suite.attrib.get("errors", 0)) == 0, report
        assert int(suite.attrib.get("skipped", 0)) == 0, report
        if suite.attrib.get("name", "").endswith("LegacyDataFolderMigrationTest"):
            migration_tests += count
    assert total > 0 and migration_tests >= 3, "Migration regression tests did not run"
    print(f"Verified {jars[0].name}: SuperGrim / Onxe, Folia metadata, {total} tests passed")


if __name__ == "__main__":
    verify()
