package za.redbridge.simulator.experiment;

import za.redbridge.simulator.config.ExperimentConfig;
import za.redbridge.simulator.config.MorphologyConfig;
import za.redbridge.simulator.config.SimConfig;

import java.io.*;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
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
        File morphologyFolder = new File("shared/" + IP + "/morphologies");

        System.out.println(morphologyFolder.getAbsolutePath());

        File[] morphologyFiles = morphologyFolder.listFiles();

        for (File file: morphologyFiles) {

            //TODO: trim serial
            Pattern morphologyFilePattern = Pattern.compile(Long.toString(timestamp)+"[:][0-9]+.morphology");
            Matcher fileMatcher = morphologyFilePattern.matcher(file.getName());

            if (fileMatcher.find()) {
                morphologies.put(new MorphologyConfig(file.getPath()), file.getName());
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

                if (address.toString().startsWith("137.158.60.")) {
                    nightmareIP = address.toString();
                    return nightmareIP;
                }
            }
        }

        return "???";
    }

    //matches morphologyfile with a valid serial file name
    private static class MorphologyFileFilter implements FileFilter {

        private long timestamp; 

        public MorphologyFileFilter(long timestamp) { System.out.println("PENIS"); this.timestamp = timestamp; }

        public boolean accept(File file) {

            String t = Long.toString(timestamp);
            Pattern morphologyFilePattern = Pattern.compile(t +"[0-9]+[:][0-9]+.morphology");
            System.out.println(file.getAbsoluteFile());
            Matcher fileMatcher = morphologyFilePattern.matcher(file.getName());

            return fileMatcher.find();
        }
    }
}
