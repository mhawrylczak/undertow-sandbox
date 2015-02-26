package mh.sandbox.undertow.rest;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Methods;
import org.xnio.Pooled;
import org.xnio.channels.StreamSourceChannel;

import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.time.LocalTime;

@Route(method = Methods.POST_STRING, template = "size")
public class Size implements HttpHandler{
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        System.out.println("size request begin "+ LocalTime.now());

        try(Pooled<ByteBuffer> pooled = exchange.getConnection().getBufferPool().allocate()) {
            ByteBuffer buffer = pooled.getResource();

            StreamSourceChannel requestChannel = exchange.getRequestChannel();
            long bytes = 0;
            long readBytes;
            while (0 <= (readBytes = requestChannel.read(buffer))){
                bytes+=readBytes;
                buffer.clear();
            }

            exchange.getResponseSender().send(""+bytes);
        }

        System.out.println("size request end "+ LocalTime.now());
    }
}
