package za.redbridge.simulator.experiment;

import org.encog.neural.neat.NEATNetwork;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * Created by shsu on 2014/09/19.
 */
public class IOUtils {

    public static void writeNetwork(NEATNetwork network, String path, String filename) {

        Path outputPath = Paths.get(path);

                try {
                    Files.createDirectories(outputPath);
                }
                catch (IOException e) {
                    e.printStackTrace();
                    System.exit(-1);
                }

        try (ObjectOutputStream objectWriter = new ObjectOutputStream(Files.newOutputStream(outputPath.resolve(filename),
                StandardOpenOption.CREATE))) {

            objectWriter.writeObject(network);
        } catch (FileNotFoundException f) {
            System.out.println("File not found, aborting.");
        } catch (IOException e) {
            System.out.println("Error writing network to file.");
            e.printStackTrace();
        }
    }

    public static NEATNetwork readNetwork(String path, String filename) {

        Object o = null;
        Path inputPath = Paths.get(path);

        try (InputStream fileReader = Files.newInputStream(inputPath.resolve(filename))){

            ObjectInputStream objectReader = new ObjectInputStream(fileReader);
            o = objectReader.readObject();
        } catch (FileNotFoundException f) {
            System.out.println("File " + filename + " not found; aborting.");
            System.exit(0);
        } catch (IOException e) {
            System.out.println("Error reading network from file.");
            e.printStackTrace();
            System.exit(0);
        } catch (ClassNotFoundException c) {
            System.out.println("Class not found.");
        }

        return (NEATNetwork) o;
    }

    public static NEATNetwork readNetwork(String fullPath) {

        Object o = null;
        Path inputPath = Paths.get(fullPath);

        try (InputStream fileReader = Files.newInputStream(inputPath)){

            ObjectInputStream objectReader = new ObjectInputStream(fileReader);
            o = objectReader.readObject();
        } catch (FileNotFoundException f) {
            System.out.println("File " + fullPath + " not found; aborting.");
            System.exit(0);
        } catch (IOException e) {
            System.out.println("Error reading network from file.");
            e.printStackTrace();
            System.exit(0);
        } catch (ClassNotFoundException c) {
            System.out.println("Class not found.");
        }

        return (NEATNetwork) o;
    }
}
