package za.redbridge.simulator.object;

/**
 * Created by jamie on 2014/08/03.
 */
public interface Taggable {
    /**
     * Get an arbitrary String tag for this object.
     * @return The tag set by {@link #setTag(String)}
     */
    String getTag();

    /**
     * Set an arbitrary String tag for this object.
     * @param tag
     */
    void setTag(String tag);
}
