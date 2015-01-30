@Grab(group = 'io.undertow', module = 'undertow-core', version = '1.1.2.Final')
import io.undertow.Undertow
import io.undertow.server.RoutingHandler
import io.undertow.util.Headers


def rootHandler = new RoutingHandler()
rootHandler.get('hello',
        {exchange ->
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, 'text/plain')
            exchange.getResponseSender().send('Hello World!')
        })



Undertow.builder()
        .addHttpListener(8080, 'localhost')
        .setHandler(rootHandler)
        .build()
        .start()