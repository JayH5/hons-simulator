package za.redbridge.simulator.factories;

import za.redbridge.simulator.ea.hetero.Team;

import java.util.List;

/**
 * Created by shsu on 2014/10/06.
 * Factory that divides genomes into teams.
 */
public interface TeamFactory {

   List<Team> placeInTeams();
}
