package mh.sandbox.undertow.rest;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import static org.springframework.util.StreamUtils.copy;

@Route(template = "downloadBlocking")
public class DownloadBlocking implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if(!exchange.isBlocking()){
            exchange.startBlocking();
        }

        if( exchange.isInIoThread() ){
            exchange.dispatch(this);
        } else {
            doHandleRequest(exchange);
        }
    }

    private void doHandleRequest(HttpServerExchange exchange) throws Exception {
        try(InputStream is = new BufferedInputStream(new FileInputStream("/tmp/upload75220729.zip"))){
            copy(is, exchange.getOutputStream());
        }
        exchange.endExchange();
    }
}
