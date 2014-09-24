package za.redbridge.simulator.experiment;

import za.redbridge.simulator.config.MorphologyConfig;

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

    private HashMap<String,Float> hosts;
    private Pattern ip = Pattern.compile("[0-9]{1,3}[.][0-9]{1,3}[.][0-9]{1,3}[.][0-9]{1,3}");
    private Pattern processor = Pattern.compile("[@][:blank:][1-9][.][0-9]{1,2}");

    private float totalProcessingPower;

    public void parseHosts (String filename) {

        hosts = new HashMap<>();

        try {

            BufferedReader input = new BufferedReader( new FileReader (new File(filename)));
            String line = input.readLine();

            totalProcessingPower = 0;

            while (line != null) {

                Matcher ipMatcher = ip.matcher(line);
                Matcher procMatcher = processor.matcher(line);

                if (ipMatcher.find() && procMatcher.find()) {

                    String strippedIP = ipMatcher.group(1);
                    String strippedProc = procMatcher.group(1);

                    totalProcessingPower += Float.parseFloat(strippedProc);

                    hosts.put(strippedIP, Float.parseFloat(strippedProc.substring(2)));
                }

                line = input.readLine();
            }
        }
        catch (IOException i) {
            i.printStackTrace();
        }
    }

    public Map<String,Float> getHosts () { return hosts; }

    //assigns some MorphologyConfigs to an IP or three, why the fuck not. also gives ticket to each assigned morpohlogy
    public HashMap<String,Map<MorphologyConfig,String>> assignIPs(Set<MorphologyConfig> morphologies) {

        HashMap<String,Map<MorphologyConfig,String>> assignment = new HashMap<>();
        int size = morphologies.size();
        Iterator<MorphologyConfig> morphologyIterator = morphologies.iterator();

        long time = System.currentTimeMillis();
        int id = 0;

        for (Map.Entry<String,Float> computr: hosts.entrySet()) {

            HashMap<MorphologyConfig,String> configList = new HashMap<>();
            float proc = computr.getValue();
            float ratio = proc/totalProcessingPower;
            int numMorphologies = (int) Math.ceil((size / 100) * ratio);

            for (int i = 0; i < numMorphologies && morphologyIterator.hasNext(); i++) {
                String serial = time + ":" + id;
                configList.put(morphologyIterator.next(), serial);
                id++;
            }

            assignment.put(computr.getKey(),configList);
        }

        return assignment;
    }

    //write morphologies to their respective IPs' directories in the /shared directory
    public void writeMorphologiesToAssignment(HashMap<String,HashMap<MorphologyConfig,String>> assignments) {

        for (Map.Entry<String,HashMap<MorphologyConfig,String>> assigned: assignments.entrySet()) {

            File morphologyFolder = new File("/shared/" + assigned.getKey() + "/morphologies");

            if (!morphologyFolder.exists()) {
                System.out.println("creating directory " + morphologyFolder.getAbsolutePath());
                boolean result = false;

                try{
                    morphologyFolder.mkdirs();
                    result = true;
                } catch(SecurityException se){
                    //handle it
                    System.out.println("DONT GO THERE IT'S UNSAFE");
                    System.exit(-1);
                }
                if(result) {
                    System.out.println("DIR created");
                }
            }

            for (Map.Entry<MorphologyConfig,String> morphology: assigned.getValue().entrySet()) {
                morphology.getKey().dumpMorphology("/shared/" + assigned.getKey() + "/morphologies/" + morphology.getValue() + ".morphology");
            }
        }
    }

    //read the morphologies assigned to this IP
    public HashMap<MorphologyConfig,String> readAssignedMorphology(long timestamp, String IP) {

        HashMap<MorphologyConfig,String> morphologies = new HashMap<>();
        File morphologyFolder = new File("/shared/" + IP + "/morphologies");
        File[] morphologyFiles = morphologyFolder.listFiles(new MorphologyFileFilter(timestamp));

        for (File file: morphologyFiles) {

            //TODO: trim serial
            morphologies.put(new MorphologyConfig(file.getPath()),file.getName());
        }

        return morphologies;
    }

    //get IP of this host on Nightmare
    public String getIP() {

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

                if (i.toString().startsWith("137.158.60.")) {
                    nightmareIP = i.toString();
                    return nightmareIP;
                }
            }
        }

        return "???";
    }

    //matches morphologyfile with a valid serial file name
    private class MorphologyFileFilter implements FileFilter {

        private long timeGenerated;

        public MorphologyFileFilter(long timeGenerated) {

            this.timeGenerated = timeGenerated;
        }

        public boolean accept(File file) {

            String t = Long.toString(timeGenerated);

            Pattern morphologyFilePattern = Pattern.compile(t +"[:][0,9]*.morphology");
            Matcher fileMatcher = morphologyFilePattern.matcher(file.getName());

            return fileMatcher.find();
        }
    }

}
