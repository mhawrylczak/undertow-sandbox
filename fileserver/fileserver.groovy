@Grab(group = 'io.undertow', module = 'undertow-core', version = '1.1.2.Final')
import io.undertow.Undertow
import io.undertow.server.handlers.resource.FileResourceManager

import static io.undertow.Handlers.resource

Undertow.builder()
        .addHttpListener(8080, "localhost")
        .setHandler(resource(new FileResourceManager(new File(System.getProperty("user.dir")), 100)).setDirectoryListingEnabled(true))
        .build()
        .start()