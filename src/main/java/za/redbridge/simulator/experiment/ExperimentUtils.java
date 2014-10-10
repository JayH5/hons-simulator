package za.redbridge.simulator.experiment;

import za.redbridge.simulator.config.ExperimentConfig;
import za.redbridge.simulator.config.MorphologyConfig;
import za.redbridge.simulator.config.SimConfig;

import java.io.*;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by racter on 2014/09/24.
 */
public class ExperimentUtils {

    private static final String EXPERIMENT_SET_LIST = "txt/experimentSets.txt";

    //search for this experiment set, identified by its timestamp key, in the txt/experimentSets.txt file.
    public static boolean searchForExperimentSet(long timestamp) {

        File experimentSetLog = new File(EXPERIMENT_SET_LIST);

        try {
            BufferedReader input = new BufferedReader(new FileReader(experimentSetLog));

            String line = input.readLine();

            while (line != null) {

                if (line.contains(Long.toString(timestamp))) {
                    return true;
                }
            }
        }
        catch (FileNotFoundException f) {
            f.printStackTrace();
            return false;
        }
        catch (IOException i) {
            i.printStackTrace();
            return false;
        }

        return false;
    }

    //read the morphologies assigned to this IP
    public static HashMap<MorphologyConfig,String> readAssignedMorphologies(long timestamp, String IP) {

        if (IP == "???") {
            System.out.println("Invalid IP.");
            System.exit(-1);
        }

        HashMap<MorphologyConfig,String> morphologies = new HashMap<>();
        Path assignedPath = Paths.get("shared/" + IP + "/morphologies");
        ArrayList<String> fileNames = new ArrayList<>();

        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(assignedPath)) {

            for (Path path : directoryStream) {
                fileNames.add(path.toString());
            }

        } catch (IOException ex) {}


        for (String name: fileNames) {

            //TODO: trim serial
            Pattern morphologyFilePattern = Pattern.compile(Long.toString(timestamp)+"[:][0-9]+.morphology");
            Matcher fileMatcher = morphologyFilePattern.matcher(name);

            if (fileMatcher.find()) {
                morphologies.put(new MorphologyConfig(name), name);
            }
        }

        return morphologies;
    }

    //returns a list of all filenames under a path (excl directories)
    static ArrayList<String> listFiles(Path path, ArrayList<String> fileNames) throws IOException {
        DirectoryStream<Path> stream = Files.newDirectoryStream(path);
        for (Path entry : stream) {
            if (Files.isDirectory(entry)) {
                listFiles(entry, fileNames);
            }
            fileNames.add(entry.getFileName().normalize().toString());
        }

        return fileNames;
    }

    //return all the untested morphologies in the shared folder
    public static HashMap<MorphologyConfig,String> getUntestedMorphologies(long timestamp) {

        HashMap<MorphologyConfig,String> morphologies = new HashMap<>();
        Path assignedPath = Paths.get("shared/");
        final ArrayList<String> fileNames = new ArrayList<>();

        try {
            listFiles(assignedPath, fileNames);
        }
        catch (IOException i) {
            i.printStackTrace();
            System.exit(-1);
        }

        for (String name: fileNames) {

            //TODO: trim serial
            Pattern morphologyFilePattern = Pattern.compile(Long.toString(timestamp)+"[:][0-9]+.morphology");
            Matcher fileMatcher = morphologyFilePattern.matcher(name);

            if (fileMatcher.find()) {
                morphologies.put(new MorphologyConfig(name), name);
            }
        }

        return morphologies;
    }

    //get IP of this host on Nightmare
    public static String getIP() {

        String nightmareIP = null;
        Enumeration e = null;

        try {
            e = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e1) {
            e1.printStackTrace();
        }
        while(e.hasMoreElements())
        {
            NetworkInterface n = (NetworkInterface) e.nextElement();
            Enumeration ee = n.getInetAddresses();
            while (ee.hasMoreElements())
            {
                InetAddress i = (InetAddress) ee.nextElement();
                String address = null;

                try {
                    address = i.getLocalHost().getHostAddress();
                } catch (UnknownHostException e1) {
                    e1.printStackTrace();
                }

                if (address.toString().startsWith("137.158.")) {
                    nightmareIP = address.toString();
                    return nightmareIP;
                }
            }
        }

        return "???";
    }
}
