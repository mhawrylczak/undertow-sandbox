package mh.sandbox.undertow.rest;

import io.undertow.io.DefaultIoCallback;
import io.undertow.io.Sender;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;

import static io.undertow.util.FileUtils.close;
import static org.springframework.util.StreamUtils.copy;

@Route(template = "download1")
public class Download1 implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        FileChannel channel = FileChannel.open(FileSystems.getDefault().getPath("/tmp/upload75220729.zip"), StandardOpenOption.READ);
        //channel is not anyc thus this uses additional thread to send
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
