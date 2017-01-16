package io.devcon5.timeseries;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 */
public class MainVerticleTest {

    @Test
    @Ignore
    public void start() throws Exception {

        String configFile = "src/main/resources/collector.json";

        final Vertx vertx = Vertx.vertx();
        final JsonObject config = readFileToJson(vertx, configFile);
        final DeploymentOptions opts = new DeploymentOptions().setConfig(config);

        vertx.deployVerticle(new MainVerticle(), opts);
    }

    private static JsonObject readFileToJson(Vertx vertx, String configFile)
            throws ExecutionException, InterruptedException {

        CompletableFuture<JsonObject> future = new CompletableFuture<>();
        vertx.fileSystem().readFile(configFile, result -> {
            if (result.succeeded()) {
                future.complete(result.result().toJsonObject());
            } else {
                throw new RuntimeException("Reading Config File failed", result.cause());
            }
        });
        return future.get();
    }

}
