package world.gfi.nfs4j.utils;

import com.sun.jna.LastErrorException;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinNT;

import java.nio.file.Path;

public class JnaWindowsUtils {
    public static long getFileId(Path path) {
        final int GENERIC_READ = 0x80000000;
        final int FILE_SHARE_READ = 0x00000001;
        final WinBase.SECURITY_ATTRIBUTES SECURITY_ATTRIBUTES = null;
        final int OPEN_EXISTING = 3;
        final int FILE_FLAG_BACKUP_SEMANTICS = 0x02000000;

        world.gfi.nfs4j.jna.windows.Kernel32.BY_HANDLE_FILE_INFORMATION lpFileInformation = new world.gfi.nfs4j.jna.windows.Kernel32.BY_HANDLE_FILE_INFORMATION();

        WinNT.HANDLE hFile = Kernel32.INSTANCE.CreateFile(path.toAbsolutePath().toFile().toString(), GENERIC_READ, FILE_SHARE_READ, SECURITY_ATTRIBUTES, OPEN_EXISTING, FILE_FLAG_BACKUP_SEMANTICS, null);

        if (hFile == WinBase.INVALID_HANDLE_VALUE) {
            throw new LastErrorException(Kernel32.INSTANCE.GetLastError());
        }

        try {
            if (!world.gfi.nfs4j.jna.windows.Kernel32.INSTANCE.GetFileInformationByHandle(hFile, lpFileInformation)) {
                throw new LastErrorException(Kernel32.INSTANCE.GetLastError());
            }

            return (((long) lpFileInformation.nFileIndexHigh.intValue()) << 32) | (lpFileInformation.nFileIndexLow.intValue() & 0xffffffffL);
        } finally {
            Kernel32.INSTANCE.CloseHandle(hFile);
        }
    }
}
