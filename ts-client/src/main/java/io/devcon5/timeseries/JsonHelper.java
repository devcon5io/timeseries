package io.devcon5.timeseries;

import java.util.Collection;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collector;

/**
 * Helper class to hanlde Json String without the use of external libraries.
 */
final class JsonHelper {


    private JsonHelper(){}

    /**
     * Creates a string joiner for joining properties of an object into a json object.
     * @return
     */
    public static StringJoiner jsonObjectJoiner() {
        return new StringJoiner(",", "{", "}");
    }

    /**
     * Creates a string joiner for joining values of an array into a json array.
     * @return
     */
    public static StringJoiner jsonArrayJoiner() {
        return new StringJoiner(",", "[", "]");
    }

    /**
     * Creates a name:value string representing a Json Property to be used in JsonObjects. Strings will be embedded
     * into quotation marks.
     * @param name
     *  the name of the property
     * @param value
     *  the value of the property
     * @return
     *  a string representation of the name-value pair
     */
    public static String property(String name, Object value){

        return "\"" + name + "\":" + toValueString(value);

    }

    /**
     * Creates a name:value string representing a Json Property to be used in JsonObjects. Different to the object-valued
     * property method, the value is not embedded into quotation marks.
     * @param name
     *  the name of the property
     * @param value
     *  the value of the property
     * @return
     *  a string representation of the name-value pair
     */
    public static String property(String name, StringJoiner value){

        return "\"" + name + "\":" + value;

    }

    /**
     * Returns a string representation of the value
     * @param value
     *  the value to transform to json string
     * @return
     *  the string to be used as json value
     */
    public static String toValueString(Object value) {
        final String val;
        if(value == null) {
            val = "null";
        } else if(value instanceof Number || value instanceof Boolean){
            val = value.toString();
        } else {
            val = "\"" + value + "\"";
        }
        return val;
    }

    public static StringJoiner toJsonObject(Map<String, ?> map){
        return map.entrySet()
            .stream()
            .map(e -> property(e.getKey(), e.getValue()))
            .collect(Collector.of(JsonHelper::jsonObjectJoiner, StringJoiner::add, StringJoiner::merge));
    }

    public static StringJoiner toJsonArray(Collection<?> collection){
        return collection.stream()
                  .map(JsonHelper::toValueString)
                  .collect(Collector.of(JsonHelper::jsonArrayJoiner, StringJoiner::add, StringJoiner::merge));
    }


}
