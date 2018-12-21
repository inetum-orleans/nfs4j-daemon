package world.gfi.nfs4j.utils;

import java.util.Arrays;

public class FileNameSanitizer {
    static final int[] illegalChars = {34, 60, 62, 124, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 58, 42, 63, 92, 47};

    static {
        Arrays.sort(illegalChars);
    }

    public static String sanitize(String name) {
        if (name == null) return null;
        StringBuilder cleanName = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            int c = (int) name.charAt(i);
            if (Arrays.binarySearch(illegalChars, c) < 0) {
                cleanName.append((char) c);
            } else {
                cleanName.append('_');
            }
        }
        return cleanName.toString();
    }
}
