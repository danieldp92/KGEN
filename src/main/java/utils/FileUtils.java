package utils;

import java.io.File;

public class FileUtils {
    public static String getFileExtension (File file) {
        String name = file.getName();
        String [] split = name.split("\\.");

        return split[split.length-1];
    }
}
