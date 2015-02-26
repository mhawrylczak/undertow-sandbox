package mh.sandbox.undertow.rest;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Methods;
import org.xnio.ChainedChannelListener;
import org.xnio.ChannelListener;
import org.xnio.channels.StreamSourceChannel;

import java.nio.channels.Channel;

@Route(method = Methods.POST_STRING, template = "user")
public class PostUser implements HttpHandler{
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
//        StreamSourceChannel sourceChannel = exchange.getRequestChannel();
//        sourceChannel.getReadSetter().set(channel -> {
//            channel.isReadResumed()
//        });
    }
}
