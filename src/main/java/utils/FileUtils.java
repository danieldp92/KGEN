package utils;

import jmetal.core.SolutionSet;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {

    public static String getFileExtension (File file) {
        String name = file.getName();
        String [] split = name.split("\\.");

        return split[split.length-1];
    }

    public static boolean pathValidation (String path) {
        File file = new File(path);

        if (file.isDirectory() || file.getParentFile().isDirectory()) {
            return true;
        }

        return false;
    }

    public static void saveFile (ArrayList<String> txt, String path) throws FileNotFoundException {
        PrintWriter pw = new PrintWriter(new File(path));

        for (String line : txt) {
            pw.println(line);
        }

        pw.close();
    }

    public static List<String> loadFile (String path) throws IOException {
        List<String> txt = null;
        BufferedReader br = null;

        try {
            txt = new ArrayList<>();
            br = new BufferedReader(new InputStreamReader(new FileInputStream(path)));

            String line = null;

            while ((line = br.readLine()) != null) {
                txt.add(line);
            }

        } finally {
            if (br != null) {
                br.close();
            }
        }

        return txt;
    }

    public static List<String> loadFile (InputStream is) throws IOException {
        List<String> txt;
        BufferedReader br = null;

        try {
            txt = new ArrayList<>();
            br = new BufferedReader(new InputStreamReader(is));

            String line = null;

            while ((line = br.readLine()) != null) {
                txt.add(line);
            }

        } finally {
            if (br != null) {
                br.close();
            }
        }

        return txt;
    }
}
