package utils;

import jmetal.core.SolutionSet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class FileUtils {

    public static String getFileExtension (File file) {
        String name = file.getName();
        String [] split = name.split("\\.");

        return split[split.length-1];
    }

    public static void saveFile (ArrayList<String> txt, String path) throws FileNotFoundException {
        PrintWriter pw = new PrintWriter(new File(path));

        for (String line : txt) {
            pw.println(line);
        }

        pw.close();
    }
}
