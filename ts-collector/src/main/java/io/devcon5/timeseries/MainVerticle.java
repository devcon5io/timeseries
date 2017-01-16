package io.devcon5.timeseries;

import static org.slf4j.LoggerFactory.getLogger;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;

/**
 * Main Verticle to launch the collector and deploys functional verticles.
 */
public class MainVerticle extends AbstractVerticle {

    private static final Logger LOG = getLogger(MainVerticle.class);

    @Override
    public void start() throws Exception {

        LOG.info("Deploying Modules");
        final JsonObject config = config();
        if (config.containsKey("modules")) {
            config.getJsonArray("modules")
                  .stream()
                  .map(m -> (JsonObject) m)
                  .forEach(m -> vertx.deployVerticle(m.getString("verticle"),
                                                     new DeploymentOptions().setConfig(m.getJsonObject("config")),
                                                     result -> LOG.info("Deployed module {}",
                                                                        m.getString("verticle"))));
        }

        vertx.deployVerticle(HttpServerVerticle.class.getName(),
                             new DeploymentOptions().setConfig(config.getJsonObject("http")));

    }
}
