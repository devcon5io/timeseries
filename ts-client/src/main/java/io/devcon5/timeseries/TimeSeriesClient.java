package io.devcon5.timeseries;

import static java.util.logging.Logger.getLogger;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Client for sending TimeSeries datapoints to the timeseries collector. The client sends datapoints as JSON String
 * to the endpoint using http post. The client can be created using an Executor which performs the acutal send.
 * The number of threads of this executor service can be configured using the {{timeseries.connection.pool}} system
 * property. The default is set to '1', creating a single-thread thread pool.
 * If the property is set to '0', the same thread as the caller is used for sending the timeseries datapoint, resulting
 * in blocking behavior.
 */
public class TimeSeriesClient {

    private static final Logger LOG = getLogger(TimeSeriesClient.class.getName());

    private final URL target;

    private final Executor executor;

    private TimeSeriesClient(URL target, Executor executor) {
        this.target = target;
        this.executor = executor;
    }

    /**
     * Creates a client for sending timeseries to the collector listening at context-path '/ts'. The created client
     * will use the default executor.
     * @param host
     *  the hostname running the timeseries collector
     * @param port
     *  the tcp port running the timeseries collector
     * @return
     */
    public static TimeSeriesClient forTarget(String host, int port) {
        return forTarget(host, port, "/ts");
    }

    /**
     * Creates a client for sending timeseries to the collector listening at context-path '/ts'
     * @param host
     *  the hostname running the timeseries collector
     * @param port
     *  the tcp port running the timeseries collector
     * @param executor
     *   the executor to be used
     * @return
     */
    public static TimeSeriesClient forTarget(String host, int port, Executor executor) {
        return forTarget(host, port, "/", executor);
    }

    /**
     * Creates a client for sending timeseries to the collector listening at specified context-path
     * @param host
     *  the hostname running the timeseries collector
     * @param port
     *  the tcp port running the timeseries collector
     * @return
     */
    public static TimeSeriesClient forTarget(String host, int port, String path) {
        try {
            return forTarget(new URL("http", host, port, path));
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Could not create endpoint", e);
        }
    }

    /**
     * Creates a client for sending timeseries to the collector listening at specified context-path
     * @param host
     *  the hostname running the timeseries collector
     * @param port
     *  the tcp port running the timeseries collector
     * @param executor
     *   the executor to be used
     * @return
     */
    public static TimeSeriesClient forTarget(String host, int port, String path, Executor executor) {
        try {
            return forTarget(new URL("http", host, port, path), executor);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Could not create endpoint", e);
        }
    }

    /**
     * Creates a timeseries client for the specified endpoint using the default executor.
     * @param target
     *  the target endpoint of the time series collector. Timeseries datapoints are sent using a http post request
     * @return
     */
    public static TimeSeriesClient forTarget(URL target) {
        return forTarget(target, defaultExecutor());
    }

    /**
     * Creates a timeseries client for the specified endpoint using the specified executor.
     * @param target
     *  the target endpoint of the time series collector. Timeseries datapoints are sent using a http post request
     * @param executor
     *  the executor that runs the send commands
     * @return
     */
    public static TimeSeriesClient forTarget(URL target, Executor executor) {
        return new TimeSeriesClient(target, executor);
    }

    /**
     * Creates the default executor depending on the number of threads configured in the system property
     * {{timeseries.connection.pool}}. If the system property is not set, the default '1' is used. The system
     * property may be changed at runtime affecting newly created timeseries clients.
     * @return
     *  an executor for running  the actual send command
     */
    private static Executor defaultExecutor() {
        final int numThreads = Integer.getInteger("timeseries.connection.pool", 1);
        if (numThreads == 0) {
            return Runnable::run;
        } else {
            return Executors.newFixedThreadPool(numThreads);
        }
    }

    /**
     * Sends a datapoint to the time series collector for further processing.
     * @param dp
     *  the datapoint to store
     */
    public void store(Datapoint dp) {
        store(dp.toJsonString(),"application/json");
    }

    /**
     * Sends a datapoint to the time series collector for further processing.
     * @param datapoint
     *  the datapoint as a string
     * @param contentType
     *  the content type of the string-datapoint. If sending json data, use 'application/json'
     */
    private void store(String datapoint, String contentType) {
        this.executor.execute(() -> {
            try {
                final HttpURLConnection conn = (HttpURLConnection) this.target.openConnection();
                final byte[] data = datapoint.getBytes(Charset.defaultCharset());
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");
                conn.setChunkedStreamingMode(512 * 1024);
                conn.setRequestProperty("Content-Type", contentType);
                conn.setRequestProperty("Content-Length", String.valueOf(data.length));
                conn.connect();
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(data);
                    os.flush();
                } finally {
                    int responseCode = conn.getResponseCode();
                    if( responseCode >= 400) {
                        LOG.warning("Sending timeseries data failed with code " + responseCode);
                    }
                    conn.disconnect();
                }
            } catch (IOException e) {
                LOG.log(Level.WARNING, "Could not send timeseries datapoint ", e);
            }
        });
    }

}
