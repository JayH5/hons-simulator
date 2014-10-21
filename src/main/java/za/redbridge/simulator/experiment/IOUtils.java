package za.redbridge.simulator.experiment;

import org.encog.ml.ea.genome.Genome;
import org.encog.neural.neat.NEATNetwork;
import za.redbridge.simulator.config.MorphologyConfig;
import za.redbridge.simulator.ea.hetero.CCHIndividual;
import za.redbridge.simulator.ea.hetero.NEATTeam;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public static void writeGenome(Genome genome, String path, String filename) {

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

            objectWriter.writeObject(genome);
        } catch (FileNotFoundException f) {
            System.out.println("File not found, aborting.");
        } catch (IOException e) {
            System.out.println("Error writing network to file.");
            e.printStackTrace();
        }
    }

    public static Genome readGenome(String path) {

        Object o = null;
        Path inputPath = Paths.get(path);

        try (InputStream fileReader = Files.newInputStream(inputPath)){

            ObjectInputStream objectReader = new ObjectInputStream(fileReader);
            o = objectReader.readObject();
        } catch (FileNotFoundException f) {
            System.out.println("File " + inputPath + " not found; aborting.");
            System.exit(0);
        } catch (IOException e) {
            System.out.println("Error reading network from file.");
            e.printStackTrace();
            System.exit(0);
        } catch (ClassNotFoundException c) {
            System.out.println("Class not found.");
        }

        return (Genome) o;
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

    public static void writeTeam (MorphologyConfig morphologyConfig, NEATTeam team) {

        int i = 0;
        for (CCHIndividual individual: team.getGenotypes()) {

            IOUtils.writeGenome(individual.getGenome(), "results/" + ExperimentUtils.getIP() + "/" + morphologyConfig.getSensitivityID(), i + ".gen");
            i++;
        }
    }

    public static List<Genome> readTeam (String directory) {

        ArrayList<String> filenames = new ArrayList<>();

        List<Genome> genomes = new ArrayList<>();

        Path dir = Paths.get(directory);
        try {
            ExperimentUtils.listFiles(dir, filenames);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        for (String file: filenames) {

            Pattern genomePattern = Pattern.compile("[0-9]+[.]gen");
            Matcher matcher = genomePattern.matcher(file);

            if (matcher.find()) {
                genomes.add(readGenome(directory + file));
            }
        }

        return genomes;
    }
}
