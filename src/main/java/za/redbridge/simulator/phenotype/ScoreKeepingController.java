package za.redbridge.simulator.phenotype;

/**
 * Created by racter on 2014/10/05.
 * Defines a Genotype that contains an underlying structure or enclosing layer which keeps its own performance scores.
 */
public interface ScoreKeepingController {

    public double getAverageTaskScore();
    public double getAverageCooperativeScore();

    public double getTotalTaskScore();
    public double getTotalCooperativeScore();

    public void incrementTotalTaskScore(double score);
    public void incrementTotalCooperativeScore(double score);

    //get the underlying controller for this ScoreKeepingController wrapper
    public Object getController();
}
