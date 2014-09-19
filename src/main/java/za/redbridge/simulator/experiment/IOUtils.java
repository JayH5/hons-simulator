package za.redbridge.simulator.experiment;

import org.encog.neural.neat.NEATNetwork;

import java.io.*;

/**
 * Created by shsu on 2014/09/19.
 */
public class IOUtils {

    public static void writeNetwork(NEATNetwork network, String filename) {

        try {
            FileOutputStream fileWriter = new FileOutputStream(filename);
            ObjectOutputStream objectWriter = new ObjectOutputStream(fileWriter);

            objectWriter.writeObject(network);
        } catch (FileNotFoundException f) {
            System.out.println("File not found, aborting.");
        } catch (IOException e) {
            System.out.println("Error writing network to file.");
            e.printStackTrace();
        }
    }

    public static NEATNetwork readNetwork(String filename) {

        Object o = null;

        try {
            FileInputStream fileReader = new FileInputStream(filename);
            ObjectInputStream objectReader = new ObjectInputStream(fileReader);
            o = objectReader.readObject();
        } catch (FileNotFoundException f) {
            System.out.println("File not found, aborting.");
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
