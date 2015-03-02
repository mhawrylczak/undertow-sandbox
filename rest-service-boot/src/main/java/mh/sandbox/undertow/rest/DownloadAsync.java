package mh.sandbox.undertow.rest;

import io.undertow.io.DefaultIoCallback;
import io.undertow.io.Sender;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystems;
import java.nio.file.StandardOpenOption;

import static io.undertow.util.FileUtils.close;

@Route(template = "downloadAsync")
public class DownloadAsync implements HttpHandler {

    public static class FileChannelPublisher implements Publisher<ByteBuffer>{

        @Override
        public void subscribe(Subscriber<? super ByteBuffer> s) {

        }
    }
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        FileChannel channel = FileChannel.open(FileSystems.getDefault().getPath("/tmp/upload75220729.zip"), StandardOpenOption.READ);
        exchange.getResponseSender().transferFrom(channel, new DefaultIoCallback(){
            @Override
            public void onComplete(HttpServerExchange exchange, Sender sender) {
                super.onComplete(exchange, sender);
                close(channel);
            }

            @Override
            public void onException(HttpServerExchange exchange, Sender sender, IOException exception) {
                super.onException(exchange, sender, exception);
                close(channel);
            }
        });

    }
}
