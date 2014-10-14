package za.redbridge.simulator.phenotype;

/**
 * Created by racter on 2014/10/05.
 * Defines a Genotype that contains an underlying structure or enclosing layer which keeps its own performance scores.
 */
public interface ScoreKeepingController {

    public double getAverageTaskScore();

    public double getCurrentTaskScore();

    public void incrementTotalTaskScore(double score);

    public void setTotalTaskScore(double score);

    public void cacheTaskScore();
}
