package mh.sandbox.undertow.rest;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Methods;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;
import org.xnio.Pool;
import org.xnio.Pooled;
import org.xnio.channels.StreamSourceChannel;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.*;
import java.time.LocalTime;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

@Route(method = Methods.POST_STRING, template = "upload")
public class Upload implements HttpHandler{



    public Publisher<ByteBuffer> publishRequestBody(final HttpServerExchange exchange){
        return publishRequestBody(exchange.getRequestChannel(), exchange.getConnection().getBufferPool());
    }

    private Publisher<ByteBuffer> publishRequestBody(StreamSourceChannel requestChannel, Pool<ByteBuffer> bufferPool) {
        return new StreamSourceChannelPublisher(requestChannel, bufferPool);
    }

    static class StreamSourceChannelPublisher implements Publisher<ByteBuffer>{

        private final StreamSourceChannel requestChannel;
        private final Pool<ByteBuffer> bufferPool;
        private AtomicReference<org.reactivestreams.Subscriber<? super ByteBuffer>> subscriber = new AtomicReference<>();

        private class ThisSubscription implements Subscription, Closeable{
            private Pooled<ByteBuffer> bufferPooled;

            @Override
            public void request(long n) {
                org.reactivestreams.Subscriber<? super ByteBuffer> s = subscriber.get();
                if (s == null){
                    throw new RuntimeException("empty subscriber");
                }

                if (n <= 0){
                    s.onError(new IllegalArgumentException("requested illegal number of items "+n));
                }

                if (bufferPooled == null){
                    bufferPooled = bufferPool.allocate();
                }

                ByteBuffer buffer = bufferPooled.getResource();

                for(long i = 0 ; i < n; i++ ){
                    try {
                        buffer.clear();
                        long readBytes = requestChannel.read(buffer);
                        if (readBytes >= 0 ){
                            buffer.flip();
                            //TODO problem the consumer must synchronously consume it (or it should use backpressure), thus maybe we should rely on subscriber to free buffer, and always allocate new one
                            //an option is to act differently depending on n
                            s.onNext(buffer);
                        } else {
                            s.onComplete();
                            cancel();
                            break;
                        }
                    } catch (IOException e) {
                       s.onError(e);
                       cancel();
                    }
                }
            }

            @Override
            public void cancel() {
                subscriber.set(null);
                try {
                    close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void close() throws IOException {
                if(bufferPooled != null){
                    bufferPooled.close();
                }
            }
        }

        public StreamSourceChannelPublisher(StreamSourceChannel requestChannel, Pool<ByteBuffer> bufferPool) {
            this.requestChannel = requestChannel;
            this.bufferPool = bufferPool;
        }

        @Override
        public void subscribe(org.reactivestreams.Subscriber<? super ByteBuffer> s) {
            if( !this.subscriber.compareAndSet(null, s) ){
                s.onError(new IllegalStateException("This publisher accepts only one subscriber"));
                return;
            }

            subscriber.get().onSubscribe( new ThisSubscription());
        }
    }

    static class AsynchronousFileChannelAppenderSubscriber implements org.reactivestreams.Subscriber<ByteBuffer>, Closeable{
        private Subscription subscription;

        private final AsynchronousFileChannel fileChannel;
        private final HttpServerExchange exchange;
        private long pos = 0;

        public AsynchronousFileChannelAppenderSubscriber(HttpServerExchange exchange, AsynchronousFileChannel fileChannel) {
            this.exchange = exchange;
            this.fileChannel = fileChannel;
        }

        @Override
        public void onSubscribe(Subscription s) {
            subscription = s;
            subscription.request(1);
        }

        @Override
        public void onNext(ByteBuffer byteBuffer) {
            fileChannel.write(byteBuffer, pos, null, new CompletionHandler<Integer, Object>() {
                @Override
                public void completed(Integer result, Object attachment) {
                    pos += result;
                    subscription.request(1);
                }

                @Override
                public void failed(Throwable exc, Object attachment) {
                    exchange.getResponseSender().send("Cancell");
                    subscription.cancel();
                    try {
                        close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        @Override
        public void onError(Throwable t) {
            exchange.getResponseSender().send("Error");
            try {
                close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onComplete() {
            exchange.getResponseSender().send("Success "+pos);
            try {
                close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void close() throws IOException {
            fileChannel.close();

            System.out.println("subscription closed "+ LocalTime.now());
        }
    }



    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        System.out.println("size request begin "+ LocalTime.now());

        Path path = FileSystems.getDefault().getPath("C:\\tmp", "upload" + new Random().nextInt());
        System.out.println("upload path  "+ path);
        AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(path, StandardOpenOption.CREATE, StandardOpenOption.WRITE);//TODO check append

        publishRequestBody(exchange).subscribe(new AsynchronousFileChannelAppenderSubscriber(exchange, fileChannel));

        exchange.dispatch();


        System.out.println("handler leave "+ LocalTime.now());
    }
}
