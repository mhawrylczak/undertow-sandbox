package mh.sandbox.undertow.rest;

import io.undertow.io.DefaultIoCallback;
import io.undertow.io.IoCallback;
import io.undertow.io.Sender;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.xnio.ChannelListener;
import org.xnio.Pooled;
import org.xnio.channels.StreamSinkChannel;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystems;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.LinkedTransferQueue;

import static io.undertow.util.FileUtils.close;
import static io.undertow.util.HttpString.tryFromString;

@Route(template = "downloadAsync")
public class DownloadAsync implements HttpHandler {

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        AsynchronousFileChannel channel = AsynchronousFileChannel.open(FileSystems.getDefault().getPath("/tmp/upload75220729.zip"), StandardOpenOption.READ);
        new WriteAsyncTask(channel, exchange).run();
    }

    static class WriteAsyncTask
            implements
            CompletionHandler<Integer, WriteAsyncTask>,
            IoCallback,
            Runnable{
        private final AsynchronousFileChannel channel;
        private final HttpServerExchange exchange;
        private final Pooled<ByteBuffer> bufferPooled;
        private final ByteBuffer buffer;
        private long readPos = 0;

        WriteAsyncTask(AsynchronousFileChannel channel, HttpServerExchange exchange) {
            this.channel = channel;
            this.exchange = exchange;
            this.bufferPooled = exchange.getConnection().getBufferPool().allocate();
            this.buffer = bufferPooled.getResource();
        }

        @Override
        public void completed(Integer result, WriteAsyncTask attachment) {
            if (result >= 0 ){
                readPos+=result;
                buffer.flip();
                exchange.getResponseSender().send(buffer, this);
            } else {
                endExchange();
            }
        }

        private void endExchange(){
            close(channel);
            bufferPooled.close();
            exchange.endExchange();
        }

        @Override
        public void failed(Throwable exc, WriteAsyncTask attachment) {
            System.out.println("CompletionHandler.onException, exception = [" + exc + "]");
            endExchange();
        }

        @Override
        public void run() {
            exchange.getResponseHeaders().add(tryFromString("Content-Type"), "application/octet-stream");
            channel.read(buffer, readPos, this, this);
            exchange.dispatch();
        }

        @Override
        public void onComplete(HttpServerExchange exchange, Sender sender) {
            buffer.clear();
            channel.read(buffer, readPos, this, this);
        }

        @Override
        public void onException(HttpServerExchange exchange, Sender sender, IOException exception) {
            System.out.println("IoCallback.onException, exception = [" + exception + "]");
            endExchange();
        }
    }
}
