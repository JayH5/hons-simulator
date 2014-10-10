package za.redbridge.simulator.factories;

import org.encog.EncogError;
import org.encog.ml.ea.genome.Genome;
import org.encog.neural.neat.NEATNetwork;
import org.encog.neural.neat.training.NEATGenome;
import za.redbridge.simulator.config.ExperimentConfig;
import za.redbridge.simulator.ea.hetero.CCHIndividual;
import za.redbridge.simulator.ea.hetero.NEATTeam;
import za.redbridge.simulator.ea.neat.CCHNEATCODEC;

import java.util.*;

/**
 * Created by shsu on 2014/10/06.
 */
public class NEATTeamFactory {

    private final List<Genome> genomePopulation;
    private final ExperimentConfig experimentConfig;

    private final Set<CCHIndividual> all_individuals;

    public NEATTeamFactory(ExperimentConfig experimentConfig, List<Genome> genomePopulation) {

        this.experimentConfig = experimentConfig;
        this.genomePopulation = genomePopulation;
        all_individuals = new HashSet();
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

            ArrayList<Genome> candidates = new ArrayList<>(genomePopulation);
            Random rand = new Random();

            while(!candidates.isEmpty()) {

                final Set<CCHIndividual> network_team = new HashSet<>();
                final NEATTeam neat_team = new NEATTeam(network_team);

                for (int j = 0; j < experimentConfig.getHeteroTeamSize(); j++) {

                    int index = rand.nextInt(candidates.size());

                    Genome member = candidates.remove(index);

                    final CCHIndividual individual = new CCHIndividual( ((NEATNetwork) decoder.decodeToNetwork(member)),
                            (NEATGenome) member, neat_team);

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
