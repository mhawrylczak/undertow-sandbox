package mh.sandbox.undertow.rest;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Methods;
import javafx.collections.ObservableListBase;
import org.xnio.Pool;
import org.xnio.Pooled;
import org.xnio.channels.StreamSourceChannel;
import rx.Observable;
import rx.Subscriber;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalTime;
import java.util.Random;

@Route(method = Methods.POST_STRING, template = "upload")
public class Upload implements HttpHandler{

    Observable<ByteBuffer> observeRequest(final HttpServerExchange exchange){
        return observeStreamSourceChannel(exchange.getRequestChannel(), exchange.getConnection().getBufferPool());
    }

    Observable<ByteBuffer> observeStreamSourceChannel(final StreamSourceChannel streamSourceChannel, final Pool<ByteBuffer> pool){
        return Observable.create(new Observable.OnSubscribe<ByteBuffer>() {
            @Override
            public void call(Subscriber<? super ByteBuffer> subscriber) {
                try(Pooled<ByteBuffer> bufferPooled = pool.allocate()){
                    ByteBuffer buffer = bufferPooled.getResource();
                    long readBytes;
                    try {
                        while(0<= (readBytes = streamSourceChannel.read(bufferPooled.getResource()))){
                            buffer.flip();
                            subscriber.onNext(buffer);
                            buffer.flip();
                        }
                        subscriber.onCompleted();
                    } catch (IOException e) {
                        subscriber.onError(e);
                    }
                }
            }
        });
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        System.out.println("size request begin "+ LocalTime.now());



        Path path = FileSystems.getDefault().getPath("C:\\tmp", "upload" + new Random().nextInt());
        System.out.println("upload path  "+ path);
        AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(path, StandardOpenOption.CREATE, StandardOpenOption.WRITE);//TODO check append
        Pooled<ByteBuffer> pooled = exchange.getConnection().getBufferPool().allocate();
        ByteBuffer buffer = pooled.getResource();
        StreamSourceChannel requestChannel = exchange.getRequestChannel();

        final long bytes[] = {0};
        long readBytes;
        readBytes = requestChannel.read(buffer);
        if (readBytes >=0 ){
            buffer.flip();

            fileChannel.write(buffer, bytes[0], exchange, new CompletionHandler<Integer, HttpServerExchange>() {
                @Override
                public void completed(Integer result, HttpServerExchange attachment) {
                    bytes[0] += result;
                    try {
                        buffer.flip();
                        long read = requestChannel.read(buffer);
                        if (read >=0 ){
                            buffer.flip();
                            fileChannel.write(buffer, bytes[0], exchange, this);
                        }else{
                            pooled.free();
                            fileChannel.close();
                            System.out.println("size request end "+ LocalTime.now());
                            exchange.getResponseSender().send("" + bytes[0]);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void failed(Throwable exc, HttpServerExchange attachment) {

                }
            });
            exchange.dispatch();
        } else {
            exchange.getResponseSender().send("empty");
            System.out.println("size request end "+ LocalTime.now());
        }

        System.out.println("hablder leave "+ LocalTime.now());
    }
}
