package io.devcon5.timeseries;

import static org.slf4j.LoggerFactory.getLogger;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import org.slf4j.Logger;

/**
 * Main Verticle to launch the collector and deploys functional verticles.
 */
public class MainVerticle extends AbstractVerticle {

    private static final Logger LOG = getLogger(MainVerticle.class);

    @Override
    public void start() throws Exception {

        LOG.info("CONFIG {}", config().encodePrettily());

        vertx.deployVerticle(HttpServerVerticle.class.getName(),
                             new DeploymentOptions().setConfig(config().getJsonObject("http")));
        vertx.deployVerticle(InfluxVerticle.class.getName(),
                             new DeploymentOptions().setConfig(config().getJsonObject("influx")));
    }
}
