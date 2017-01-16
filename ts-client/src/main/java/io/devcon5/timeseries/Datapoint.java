package io.devcon5.timeseries;

import static io.devcon5.timeseries.JsonHelper.jsonObjectJoiner;
import static io.devcon5.timeseries.JsonHelper.property;
import static io.devcon5.timeseries.JsonHelper.toJsonObject;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a single datapoint at a specific point in time. The datapoint may bear multiple values and may be
 * tagged with multiple tags. If a name is omitted during construction, 'measure' is used as default. If timestamp
 * is omitted during construction, system's current timestamp is used.
 * The datapoint is threadsafe.
 */
public class Datapoint {

    private final long timestamp;

    private final String name;

    private final Map<String, String> tags = new ConcurrentHashMap<>();

    private final Map<String, Number> values = new ConcurrentHashMap<>();

    /**
     * Creates a new datapoint for the specified timeseries and the specified timestamp
     * @param name
     *  the name of the timeseries
     * @param timestamp
     *  the unix timestamp in milliseconds
     */
    public Datapoint(String name, long timestamp) {
        this.timestamp = timestamp;
        this.name = name;
    }

    /**
     * Creates a datapoint for the default timeseries ('measure') using the specified timestamp.
     * @param timestamp
     *  the timestamp of this datapoint
     */
    public Datapoint(long timestamp) {
        this("measure", timestamp);
    }

    /**
     * Creates a datapoint for the specified timeseries using the current system time as timestamp
     * @param name
     *  the name of the timeseries
     */
    public Datapoint(String name) {
        this(name, System.currentTimeMillis());
    }

    /**
     * Creates a new datapoint for the default timeseries ('measure') and the current system time.
     */
    public Datapoint() {
        this(System.currentTimeMillis());
    }

    /**
     * The unix timestamp in milliseconds when the measures have been taken
     * @return
     *  the unix timestamp in milliseconds
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * The name of the timeseries the datapoint is associated with.
     * @return
     *  the name as a string
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the tags of this datapoint.
     * @return
     *  the tags as unmodifiable key-value map
     */
    public Map<String, String> getTags() {
        return Collections.unmodifiableMap(tags);
    }

    /**
     * Returns the values of this datapoint.
     * @return
     *  the values as unmodifiable key-value map
     */
    public Map<String, Number> getValues() {
        return Collections.unmodifiableMap(values);
    }

    /**
     * Adds an informative tag to this datapoint. Tags can be used to categorize datapoints of the same timeseries,
     * i.e. by host name, sub-series, stage etc.
     * @param tagName
     *  the name of the tag, i.e. host
     * @param tagValue
     *  the value of the tag, i.e. localhost
     * @return
     *  this datapoint for fluent api
     */
    public Datapoint addTag(String tagName, String tagValue) {
        this.tags.put(tagName, tagValue);
        return this;
    }

    /**
     * Adds a value to this datapoint. Each datapoint can have multiple values, i.e. temperature and pressure. At
     * least one value must be specified, otherwise the datapoint is incomplete.
     * @param valueName
     *  the name of the value, i.e. temperature
     * @param value
     *  the actual value, i.e. 37
     * @return
     *  this datapoint for fluent API
     */
    public Datapoint addValue(String valueName, Number value) {
        this.values.put(valueName, value);
        return this;
    }

    /**
     * Transforms this datapoint into a JSON string.
     * @return
     *  this datapoint as a string in dataformat.
     */
    public String toJsonString() {
        if(values.isEmpty()){
            throw new IllegalStateException("At least one value must be set");
        }
        return jsonObjectJoiner().add(property("name", this.name))
                                 .add(property("timestamp", this.timestamp))
                                 .add(property("tags", toJsonObject(this.tags)))
                                 .add(property("values", toJsonObject(this.values)))
                                 .toString();
    }

}
