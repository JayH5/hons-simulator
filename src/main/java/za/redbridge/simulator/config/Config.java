package za.redbridge.simulator.config;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by shsu on 2014/09/08.
 */
public abstract class Config {

    protected static boolean checkFieldPresent(Object field, String name) {
        if (field != null) {
            return true;
        }
        System.out.println("Field '" + name + "' not present, using default");
        return false;
    }

    public String printFile(String filepath) {

        String output = "";

        Path filePath = Paths.get(filepath);

        try (BufferedReader input = Files.newBufferedReader(filePath, Charset.defaultCharset())) {

            String line = input.readLine();
            while (line != null) {
                output += line + "\n";
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        return output;
    }
}
