@Grab(group = 'io.undertow', module = 'undertow-core', version = '1.1.2.Final')
import io.undertow.Undertow

Undertow.builder()
        .addHttpListener(8080, 'localhost')
        .setHandler(
        { exchange ->
            exchange.getResponseSender().send('Hello World!')
        })
        .build()
        .start()