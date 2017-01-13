package io.devcon5.timeseries;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 *
 */
public class MainVerticle extends AbstractVerticle {

    public static void main(String... args) throws Exception {
        String configFile;
        if(args.length == 0) {
            configFile = "src/main/resources/collector.json";
        } else {
            configFile = args[0];
        }

        final Vertx vertx = Vertx.vertx();
        final JsonObject config = readFileToJson(vertx, configFile);
        final DeploymentOptions opts = new DeploymentOptions().setConfig(config);
        vertx.deployVerticle(new MainVerticle(), opts);
    }

    @Override
    public void start() throws Exception {

        vertx.deployVerticle(HttpServerVerticle.class.getName(), new DeploymentOptions(config()));
        vertx.deployVerticle(InfluxVerticle.class.getName(), new DeploymentOptions(config()));
    }


    private static JsonObject readFileToJson(Vertx vertx, String configFile) throws ExecutionException, InterruptedException {
        CompletableFuture<JsonObject> future = new CompletableFuture<>();
        vertx.fileSystem().readFile(configFile, result->{
            if(result.succeeded()){
                future.complete(result.result().toJsonObject());
            } else {
                throw new RuntimeException("Reading Config File failed", result.cause());
            }
        });
        return future.get();
    }
}
