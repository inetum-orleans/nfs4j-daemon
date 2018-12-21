package world.gfi.nfs4j.jna.windows;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.WinBase.FILETIME;
import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIFunctionMapper;
import com.sun.jna.win32.W32APITypeMapper;

import java.util.HashMap;
import java.util.Map;

public interface Kernel32 extends StdCallLibrary {
    Map<String, Object> WIN32API_OPTIONS = new HashMap<String, Object>() {
        private static final long serialVersionUID = 1L;

        {
            put(Library.OPTION_FUNCTION_MAPPER, W32APIFunctionMapper.UNICODE);
            put(Library.OPTION_TYPE_MAPPER, W32APITypeMapper.UNICODE);
        }
    };

    Kernel32 INSTANCE = Native.load("Kernel32", Kernel32.class, WIN32API_OPTIONS);

    @Structure.FieldOrder({"dwFileAttributes",
            "ftCreationTime",
            "ftLastAccessTime",
            "ftLastWriteTime",
            "dwVolumeSerialNumber",
            "nFileSizeHigh",
            "nFileSizeLow",
            "nNumberOfLinks",
            "nFileIndexHigh",
            "nFileIndexLow"})
    public static class BY_HANDLE_FILE_INFORMATION extends Structure {
        public DWORD dwFileAttributes;
        public FILETIME ftCreationTime;
        public FILETIME ftLastAccessTime;
        public FILETIME ftLastWriteTime;
        public DWORD dwVolumeSerialNumber;
        public DWORD nFileSizeHigh;
        public DWORD nFileSizeLow;
        public DWORD nNumberOfLinks;
        public DWORD nFileIndexHigh;
        public DWORD nFileIndexLow;
    }

    /**
     * BOOL WINAPI GetFileInformationByHandle(
     * __in   HANDLE hFile,
     * __out  LPBY_HANDLE_FILE_INFORMATION lpFileInformation
     * );
     */
    boolean GetFileInformationByHandle(
            HANDLE hFile,
            BY_HANDLE_FILE_INFORMATION lpFileInformation
    );
}
