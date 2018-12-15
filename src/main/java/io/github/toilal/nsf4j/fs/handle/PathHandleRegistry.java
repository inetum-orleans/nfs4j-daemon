package io.github.toilal.nsf4j.fs.handle;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * Bidirectionnal mapping of Path to/from File Handle.
 */
public class PathHandleRegistry extends HandleRegistry<Path> {
    public PathHandleRegistry(UniqueHandleGenerator uniqueLongGenerator) {
        super(uniqueLongGenerator);
    }

    @Override
    protected boolean pathExists(Path path) {
        return Files.exists(path, LinkOption.NOFOLLOW_LINKS);
    }

    @Override
    public void replace(Path oldPath, Path newPath) throws IOException {
        this.replaceItem(oldPath, newPath);

        if (Files.isDirectory(newPath, LinkOption.NOFOLLOW_LINKS)) {
            Stream<Path> directoryWalker = Files.walk(newPath);
            try {
                directoryWalker.forEach((item) -> {
                    Path oldItem = oldPath.resolve(newPath.relativize(item)).normalize();
                    Path newItem = item.normalize();
                    this.replaceItem(oldItem, newItem);
                });
            } finally {
                directoryWalker.close();
            }
        }
    }
}
