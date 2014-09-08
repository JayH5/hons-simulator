package za.redbridge.simulator.config;

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
}
