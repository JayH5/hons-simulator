package za.redbridge.simulator.gp;

import org.epochx.stats.AbstractStat;
import org.epochx.stats.Stat;
import org.epochx.stats.Stats;

/**
 * Created by xenos on 10/16/14.
 */
public class CustomStatFields {
    public static Stat GEN_TEAM_FITNESS_MIN = new AbstractStat(Stats.ExpiryEvent.GENERATION){};
    public static Stat GEN_FITTEST_TEAM = new AbstractStat(Stats.ExpiryEvent.GENERATION){};
}
