package io.devcon5.timeseries;

import static org.slf4j.LoggerFactory.getLogger;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

/**
 *
 */
@RunWith(VertxUnitRunner.class)
public class HttpServerVerticleTest {

    private static final Logger LOG = getLogger(HttpServerVerticleTest.class);

    @Rule
    public RunTestOnContext rule = new RunTestOnContext();

    private int defaultPort = 18080;

    @Before
    public void setUp(TestContext context) throws Exception {

        final JsonObject config = new JsonObject().put("port", defaultPort);
        rule.vertx().deployVerticle(HttpServerVerticle.class.getName(), new DeploymentOptions().setConfig(config),
                context.asyncAssertSuccess());

    }

    @Test
    public void httpConnectivity(TestContext context) {

        final HttpClient client = rule.vertx().createHttpClient(new HttpClientOptions().setDefaultPort(defaultPort));

        final Async async = context.async();
        client.getNow("/", response -> {
            LOG.info("Received response, code={}", response.statusCode());
            context.assertEquals(200, response.statusCode());
            response.bodyHandler(body -> context.assertEquals("TimeSeries Collector", body.toString()));
            client.close();
            async.complete();
        });
    }

}
