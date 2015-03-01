package mh.sandbox.undertow.rest;

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.RoutingHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;


@SpringBootApplication
public class Application implements CommandLineRunner {

    @Autowired
    private Undertow undertow;

    @Bean
    @Autowired
    public Undertow buildUndertow(@Qualifier("routingHandler") HttpHandler rootHandler) {
        Undertow.Builder builder = Undertow.builder()
                .addHttpListener(8080, "localhost")
                .setHandler(rootHandler);

        return builder.build();
    }

    @Bean
    @Autowired
    public HttpHandler routingHandler(ApplicationContext context) {
        final RoutingHandler routes = new RoutingHandler();
        context.getBeansWithAnnotation(Route.class)
                .values()
                .forEach((bean) -> {
                    Route route = bean.getClass().getAnnotation(Route.class);
                    routes.add(route.method(), route.template(), (HttpHandler) bean);
                });
        return routes;
    }

    @Override
    public void run(String... args) throws Exception {
        undertow.start();
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }


}
