package io.devcon5.timeseries;

import java.util.List;
import java.util.StringJoiner;
import java.util.regex.Pattern;

import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.http.HttpClient;
import org.slf4j.LoggerFactory;

/**
 * The influx Verticle accepts a JSON datapoint of the format:
 * <pre>
 *     {
 *      name : "measureName",
 *      tags: {
 *       "tag1": "tagValue1",
 *       ...
 *      },
 *      values: {
 *          "valueName1": value1,
 *          "valueName2": value2,
 *          ...
 *      },
 *      timestamp: unixTimeStampInNanos
 *     }
 * </pre>
 */
public class InfluxVerticle extends AbstractVerticle {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(InfluxVerticle.class);

    private final Pattern SPECIAL_CHARS = Pattern.compile("([\\s,=\"])");

    private HttpClient http;

    @Override
    public void start() throws Exception {
        this.http = vertx.createHttpClient(new HttpClientOptions(config()));
        config().getJsonArray("dbnames").forEach(dbname -> registerConsumer((String) dbname));

    }

    private void registerConsumer(final String dbname) {

        vertx.eventBus().<JsonObject>consumer("/store/" + dbname).bodyStream()
                                                                 .toObservable()
                                                                 .buffer(config().getInteger("maxRowLimit", 10000))
                                                                 .map(this::joinDataPoints)
                                                                 .subscribe(result -> sendDatapoint(dbname, result));
    }

    void sendDatapoint(String dbname, String msg) {

        LOG.trace("Sending measures length = {} Bytes ", msg.length());
        this.http.post("/write?db=" + dbname, response -> {
            if (response.statusCode() >= 400) {
                LOG.warn("{} {}", response.statusCode(), response.statusMessage());
                response.bodyHandler(data -> LOG.warn(data.toString()));
            }
        }).end(msg);
    }

    /**
     * Joins multiple datapoints into a single string in the Infux line protocol
     * @param dps
     *  list of single datapoints
     * @return
     *  all datapoints in line protocol, with each line containing a datapoint
     */
    private String joinDataPoints(final List<JsonObject> dps) {

        return dps.stream()
                  .map(this::toLineProtocol)
                  .collect(() -> new StringJoiner("\n"), StringJoiner::add, StringJoiner::merge)
                  .toString();
    }

    /**
     * Converts a json datapoint with the properties: name, tags, values, timestamp into the line protocol used
     * by influx.
     *
     * @param datapoint
     *         datapoint to send
     *
     * @return the datapoint in line protocol representation
     */
    String toLineProtocol(JsonObject datapoint) {

        return toLineProtocol(datapoint.getString("name"),
                              datapoint.getJsonObject("tags"),
                              datapoint.getJsonObject("values"),
                              datapoint.getLong("timestamp"));
    }

    /**
     * Creates a Influx LineProtocol measure of the format
     * <pre>
     *     measure_name[,tag_name=tag_value]* field_name=field_value[,field_name=field_value]* timestamp
     * </pre>
     *
     * @param measureName
     *         the name of the measure, used for measure_name
     * @param tags
     *         an object with tags. Each property name is used as tag_name and the according value as tag_value
     * @param timestamp
     *         the timestamp in nanoseconds
     * @param values
     *         on object withe fields. Each property name is used as field_name and the according value as field_value
     *
     * @returns {string} a string representing a measure for influx
     */
    String toLineProtocol(String measureName, JsonObject tags, JsonObject values, Long timestamp) {

        return measureName + (tags.size() > 0 ? "," + flatten(tags) : "") + " " + flatten(values) + " " + timestamp;
    }

    /**
     * Flattens an object into a key=value pair representation, with each pair separated by a comma
     *
     * @param obj
     *         an object, i.e. { "aKey" : "aValue", "bKey":"bValue"}
     *
     * @returns {string} a comma separated string of the key-value pairs , i.e. aKey=aValue,bKey=bValue
     */
    String flatten(JsonObject obj) {

        return obj.getMap()
                  .entrySet()
                  .stream()
                  .map(e -> escape(e.getKey()) + "=" + escape(e.getValue()))
                  .collect(() -> new StringJoiner(","), StringJoiner::add, StringJoiner::merge)
                  .toString();
    }

    /**
     * Escapes characters with a backslash that are not allowed to be send unescaped over the line protocol
     *
     * @param obj
     *         the object to be escaped. Only strings are escaped. If the object is not a string, nothing is done.
     *
     * @return the escaped string or the original object if the input parameter was no string
     */
    Object escape(Object obj) {

        if (obj instanceof String) {
            return SPECIAL_CHARS.matcher((String) obj).replaceAll("\\\\$1");
        }
        return obj;
    }
}
