package za.redbridge.simulator.experiment;

import za.redbridge.simulator.config.MorphologyConfig;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by shsu on 2014/09/25.
 */
public class ComplementDistributor {

    private static final String EXPERIMENT_SET_LIST = "txt/experimentSets.txt";

    private HashMap<String,Float> hosts;
    private HashMap<String,Map<MorphologyConfig,String>> assignments;

    private long thisTimestamp;
    private float totalProcessingPower;
    private final String hostsFile;

    private Pattern ip = Pattern.compile("[0-9]{1,3}[.][0-9]{1,3}[.][0-9]{1,3}[.][0-9]{1,3}");
    private Pattern processor = Pattern.compile("[@][' '][1-9][.][0-9]{1,2}");

    private final Set<MorphologyConfig> morphologyList;

    public ComplementDistributor(String hostsFile, Set<MorphologyConfig> morphologyList) {

        this.hostsFile = hostsFile;
        this.morphologyList = morphologyList;
        assignments = new HashMap<>();
    }

    public void assignHosts() {

        //parse hosts and computing power, assign morphologies, write inputs to machines under respective directories
        parseHosts();
        assignIPs();
    }

    //write morphologies to their respective IPs' directories in the /shared directory
    public void writeMorphologiesToAssignment() {

        //log this timestamp in list of timestamps
        try
        {
            String filename = EXPERIMENT_SET_LIST;
            FileWriter fw = new FileWriter(filename,true); //the true will append the new data
            fw.write(thisTimestamp + "\n");//appends the string to the file
            fw.close();
        }
        catch(IOException ioe)
        {
            System.err.println("IOException: " + ioe.getMessage());
        }

        for (Map.Entry<String,Map<MorphologyConfig,String>> assigned: assignments.entrySet()) {

            File morphologyFolder = new File("shared/" + assigned.getKey() + "/morphologies");

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
                morphology.getKey().dumpMorphology("shared/" + assigned.getKey() + "/morphologies/", morphology.getValue() + ".morphology");
            }
        }

        System.out.println("Wrote experiment set " + thisTimestamp);
    }

    private void parseHosts () {

        hosts = new HashMap<>();

        try {

            BufferedReader input = new BufferedReader( new FileReader(new File(hostsFile)));
            String line = input.readLine();
            totalProcessingPower = 0;

            while (line != null) {

                Matcher ipMatcher = ip.matcher(line);
                Matcher procMatcher = processor.matcher(line);

                if (ipMatcher.find() && procMatcher.find()) {

                    String strippedIP = ipMatcher.group(0);
                    String strippedProc = procMatcher.group(0).substring(2);

                    totalProcessingPower += Float.parseFloat(strippedProc);
                    hosts.put(strippedIP, Float.parseFloat(strippedProc));
                }

                line = input.readLine();
            }
        }
        catch (IOException i) {
            i.printStackTrace();
        }
    }

    //assigns some MorphologyConfigs to an IP or three, why the fuck not. also gives ticket to each assigned morpohlogy
    private void assignIPs() {

        assignments.clear();

        int size = morphologyList.size();
        Iterator<MorphologyConfig> morphologyIterator = morphologyList.iterator();
        thisTimestamp = System.currentTimeMillis();
        int id = 0;

        for (Map.Entry<String,Float> computr: hosts.entrySet()) {

            HashMap<MorphologyConfig,String> configList = new HashMap<>();
            float proc = computr.getValue();
            float ratio = proc/totalProcessingPower;
            double numMorphologies = Math.ceil(ratio*size);

            for (int i = 0; i < numMorphologies && morphologyIterator.hasNext(); i++) {
                String serial = thisTimestamp + ":" + id;
                configList.put(morphologyIterator.next(), serial);
                id++;
            }

            assignments.put(computr.getKey(),configList);
        }

        System.out.println("Assigned " + morphologyList.size() + " morphologies to " + assignments.size() + " hosts.");
    }

    public Map<String,Float> getHosts () { return hosts; }
}
