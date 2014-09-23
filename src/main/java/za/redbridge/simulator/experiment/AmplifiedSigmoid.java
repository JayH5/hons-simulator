package za.redbridge.simulator.experiment;

import org.encog.engine.network.activation.ActivationFunction;
import org.encog.engine.network.activation.ActivationSigmoid;
import org.encog.mathutil.BoundMath;

/**
 * Created by shsu on 2014/09/23.
 */
public class AmplifiedSigmoid implements ActivationFunction {

    /**
     * The parameters.
     */
    private final double[] params;

    /**
     * Construct a basic sigmoid function, with a slope of 1.
     */
    public AmplifiedSigmoid() {
        this.params = new double[0];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void activationFunction(final double[] x, final int start,
                                         final int size) {
        for (int i = start; i < start + size; i++) {
            x[i] = ((1.0 / (1.0 + BoundMath.exp(-1 * x[i])))*2) - 1;
        }
    }

    /**
     * @return The object cloned;
     */
    @Override
    public final ActivationFunction clone() {
        return new ActivationSigmoid();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final double derivativeFunction(final double b, final double a) {
        return a * (1.0 - a);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String[] getParamNames() {
        final String[] results = {};
        return results;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final double[] getParams() {
        return this.params;
    }

    /**
     * @return True, sigmoid has a derivative.
     */
    @Override
    public final boolean hasDerivative() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setParam(final int index, final double value) {
        this.params[index] = value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFactoryCode() {
        return null;
    }

}
