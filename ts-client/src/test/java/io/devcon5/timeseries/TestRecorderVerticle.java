package io.devcon5.timeseries;

import static java.util.logging.Logger.getLogger;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;

/**
 * Verticle that accepts datapoints and keeps them in a local list, so that tests can retrieve them. The number of
 * expected messages should be defined in the Async CountDown latch, defined by the syncpoint method. Otherwise no
 * guarantees can be made about the received datapoints.
 */
public class TestRecorderVerticle extends AbstractVerticle {

    private static final Logger LOG = getLogger(TestRecorderVerticle.class.getName());

    private final String address;

    private final Async syncpoint;
    private final List<JsonObject> messages = new CopyOnWriteArrayList<>();

    public TestRecorderVerticle(String address, Async async) {
        this.address = address;
        this.syncpoint = async;
    }

    @Override
    public void start() throws Exception {

        vertx.eventBus().consumer(this.address, msg -> {
            LOG.info("RCV: " + msg.body());
            JsonObject json = (JsonObject) msg.body();
            this.messages.add(json);
            syncpoint.countDown();
        });
    }

    /**
     * Returns the received JsonObject messages
     * @return
     */
    public List<JsonObject> getMessages() {
        return messages;
    }
}
