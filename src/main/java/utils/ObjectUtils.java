package utils;

import java.io.*;

public class ObjectUtils {

    public static Object readerObject(String inputPath) throws IOException, ClassNotFoundException {
        FileInputStream fileInputStream = new FileInputStream(new File(inputPath));
        ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

        Object object = objectInputStream.readObject();

        return object;
    }

    public static void writerObject(Object object, String outputPath) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(new File(outputPath));
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);

        // Write object to file
        objectOutputStream.writeObject(object);

        objectOutputStream.close();
        fileOutputStream.close();
    }
}
