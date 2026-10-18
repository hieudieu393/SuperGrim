package ac.grim.grimac.utils.files;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LegacyDataFolderMigrationTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void movesLegacyFolderWhenNewFolderDoesNotExist() throws IOException {
        Path legacy = temporaryDirectory.resolve("GroundedGrim");
        Path current = temporaryDirectory.resolve("SuperGrim");
        Files.createDirectories(legacy);
        Files.writeString(legacy.resolve("config.yml"), "enabled: true\n");

        LegacyDataFolderMigration.migrate(legacy, current);

        assertFalse(Files.exists(legacy));
        assertEquals("enabled: true\n", Files.readString(current.resolve("config.yml")));
    }

    @Test
    void leavesDirectoriesAloneWhenThereIsNoLegacyFolder() throws IOException {
        Path legacy = temporaryDirectory.resolve("GroundedGrim");
        Path current = temporaryDirectory.resolve("SuperGrim");

        LegacyDataFolderMigration.migrate(legacy, current);

        assertFalse(Files.exists(legacy));
        assertFalse(Files.exists(current));
    }

    @Test
    void refusesToMergeWhenBothFoldersExist() throws IOException {
        Path legacy = temporaryDirectory.resolve("GroundedGrim");
        Path current = temporaryDirectory.resolve("SuperGrim");
        Files.createDirectories(legacy);
        Files.createDirectories(current);
        Files.writeString(legacy.resolve("config.yml"), "legacy: true\n");
        Files.writeString(current.resolve("config.yml"), "current: true\n");

        assertThrows(IOException.class, () -> LegacyDataFolderMigration.migrate(legacy, current));
        assertTrue(Files.exists(legacy.resolve("config.yml")));
        assertEquals("current: true\n", Files.readString(current.resolve("config.yml")));
    }
}
