package za.redbridge.simulator.factories;

import org.encog.EncogError;
import org.encog.ml.ea.genome.Genome;
import org.encog.neural.neat.NEATNetwork;
import org.encog.neural.neat.training.NEATGenome;
import za.redbridge.simulator.config.ExperimentConfig;
import za.redbridge.simulator.ea.hetero.CCHIndividual;
import za.redbridge.simulator.ea.hetero.NEATTeam;
import za.redbridge.simulator.ea.neat.CCHNEATCODEC;
import za.redbridge.simulator.ea.neat.CCHNEATPopulation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by shsu on 2014/10/06.
 * Factory that makes teams out of a population of NEAT genomes
 */
public class NEATTeamFactory {

    private final List<Genome> genomePopulation;
    private final CCHNEATPopulation population;
    private final ExperimentConfig experimentConfig;

    private final Set<CCHIndividual> all_individuals;

    public NEATTeamFactory(ExperimentConfig experimentConfig, CCHNEATPopulation population) {

        this.population = population;
        this.experimentConfig = experimentConfig;

        all_individuals = new HashSet();

        genomePopulation = new ArrayList<>();

        for (Genome g: population.flatten()) {
            genomePopulation.add(g);
        }
    }

    public List<NEATTeam> placeInTeams() {

        all_individuals.clear();

        //if population cannot be divided wholly into teams
        if (genomePopulation.size() % experimentConfig.getHeteroTeamSize() != 0) {
            throw new EncogError("Population cannot be evenly divided into teams.");
        }
        else {

            ArrayList<NEATTeam> teams = new ArrayList<>();
            CCHNEATCODEC decoder = new CCHNEATCODEC();

            for (int i = 0; i < genomePopulation.size(); i+=experimentConfig.getHeteroTeamSize()) {

                final Set<CCHIndividual> network_team = new HashSet<>();
                final NEATTeam neat_team = new NEATTeam(network_team);

                for (int j = 0; j < experimentConfig.getHeteroTeamSize(); j++) {

                    final CCHIndividual individual = new CCHIndividual( ((NEATNetwork) decoder.decodeToNetwork(genomePopulation.get(i+j))),
                            (NEATGenome) genomePopulation.get(i+j), neat_team);

                    network_team.add(individual);
                    all_individuals.add(individual);
                }
                teams.add(neat_team);
            }
            return teams;
        }
    }

    public Set<CCHIndividual> getAllIndividuals () { return all_individuals; }

    public List<Genome> getGenomePopulation() { return genomePopulation; }
}
