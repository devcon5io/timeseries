package io.devcon5.timeseries;

import static org.slf4j.LoggerFactory.getLogger;

import org.slf4j.Logger;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

/**
 * Verticle to accept measurement data via http. Incoming data is forwarded directly onto the event bus to the
 * consumers listenting at the same path address as specified in the URL.
 */
public class HttpServerVerticle extends AbstractVerticle {

    private static final Logger LOG = getLogger(HttpServerVerticle.class);

    @Override
    public void start(Future<Void> startFuture) throws Exception {

        final Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        router.route("/").handler(this::ping);

        //route all other messages to the event bus
        //TODO support other dataformats than just JSON
        router.post("/*")
              .handler(ctx -> {
                  LOG.debug("> POST {}\n{}", ctx.normalisedPath(), ctx.getBodyAsJson());
                  vertx.eventBus()
                          .<Buffer>publish(ctx.normalisedPath(), ctx.getBodyAsJson());
                  ctx.response().setStatusCode(204).end();
              });

        vertx.createHttpServer()
             .requestHandler(router::accept)
             .listen(config().getInteger("port", 4040), result -> {
                 if (result.succeeded()) {
                     LOG.info("Server listening on port {}", config().getInteger("port", 4040));
                     startFuture.complete();
                 } else {
                     startFuture.fail("FAIL" + result.cause());
                 }
             });
    }

    protected void sendResponse(RoutingContext ctx, Message<Buffer> response) {

        if (response != null) {
            ctx.response().putHeader("content-type", "application/obj; charset=utf-8").end(response.body());
        }
    }

    /////////////// Helper methods

    private void ping(RoutingContext routingContext) {

        routingContext.response().putHeader("content-type", "text/html").end("TimeSeries Collector");
    }

}
