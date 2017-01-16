package io.devcon5.timeseries;

import java.net.ServerSocket;
import java.util.List;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 */
@RunWith(VertxUnitRunner.class)
public class TimeSeriesClientTest {

    @Rule
    public RunTestOnContext rule = new RunTestOnContext();

    private int defaultPort = 18080;

    private TestRecorderVerticle testRecorder;

    @Before
    public void setUp(TestContext context) throws Exception {

        try(ServerSocket s = new ServerSocket(0)){
            defaultPort = s.getLocalPort();
        }

        final JsonObject config = new JsonObject().put("http", new JsonObject().put("port", defaultPort));

        final Vertx vertx = rule.vertx();
        vertx.deployVerticle(MainVerticle.class.getName(), new DeploymentOptions().setConfig(config),
                context.asyncAssertSuccess());
    }

    @Test
    public void store(TestContext context) throws Exception {

        final Async async = context.async(3);
        this.testRecorder = new TestRecorderVerticle("/store/test", async);
        rule.vertx().deployVerticle(this.testRecorder, context.asyncAssertSuccess());

        TimeSeriesClient client = TimeSeriesClient.forTarget("localhost", defaultPort, "/store/test", Runnable::run);
        client.store(new Datapoint().addValue("test", 123));
        client.store(new Datapoint().addValue("test", 147));
        client.store(new Datapoint().addValue("test", 165));

        async.awaitSuccess(2000);

        List<JsonObject> dps = this.testRecorder.getMessages();
        context.assertFalse(dps.isEmpty());
        context.assertEquals(3, dps.size());

        assertMeasure(context, dps.get(0), 123L);
        assertMeasure(context, dps.get(1), 147L);
        assertMeasure(context, dps.get(2), 165L);

    }

    private void assertMeasure(TestContext context, JsonObject measure, Long value) {
        context.assertEquals("measure", measure.getString("name"));
        context.assertNotNull(measure.getLong("timestamp"));
        context.assertTrue(measure.getJsonObject("tags").isEmpty());
        context.assertFalse(measure.getJsonObject("values").isEmpty());
        context.assertEquals(value, measure.getJsonObject("values").getLong("test"));
    }

}
