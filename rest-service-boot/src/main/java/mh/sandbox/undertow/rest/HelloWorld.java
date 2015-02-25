package mh.sandbox.undertow.rest;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

@Route(template = "hello")
public class HelloWorld implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        exchange.getResponseSender().send("Hello World!");
    }
}
