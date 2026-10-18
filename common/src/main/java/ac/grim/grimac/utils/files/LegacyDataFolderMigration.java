package ac.grim.grimac.utils.files;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

/** Safely moves data from a previous branded plugin folder to the current one. */
public final class LegacyDataFolderMigration {
    private LegacyDataFolderMigration() {
    }

    public static boolean migrate(Path legacyFolder, Path currentFolder) throws IOException {
        if (legacyFolder.equals(currentFolder)) {
            return false;
        }

        if (!Files.exists(legacyFolder, LinkOption.NOFOLLOW_LINKS)) {
            return false;
        }

        if (Files.exists(currentFolder, LinkOption.NOFOLLOW_LINKS)) {
            throw new FileAlreadyExistsException(currentFolder.toString(), legacyFolder.toString(),
                    "Both legacy and SuperGrim data folders exist; refusing to overwrite either folder");
        }

        Files.move(legacyFolder, currentFolder);
        return true;
    }
}
