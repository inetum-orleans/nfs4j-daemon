package io.github.toilal.nsf4j.fs;

import org.dcache.nfs.vfs.DirectoryEntry;
import org.dcache.nfs.vfs.Inode;
import org.dcache.nfs.vfs.VirtualFileSystem;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Extension of {@link VirtualFileSystem} that supports attachement on {@link RootFileSystem}.
 */
public interface AttachableFileSystem extends VirtualFileSystem {
    boolean hasInode(Inode inode);

    Path getRoot();

    DirectoryEntry buildRootDirectoryEntry(String filename, long currentCookie) throws IOException;
}
