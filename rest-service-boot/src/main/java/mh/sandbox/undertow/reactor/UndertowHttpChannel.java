package mh.sandbox.undertow.reactor;

import io.undertow.server.HttpServerExchange;
import org.reactivestreams.Subscriber;
import reactor.Environment;
import reactor.core.Dispatcher;
import reactor.io.buffer.Buffer;
import reactor.io.codec.Codec;
import reactor.io.net.Channel;
import reactor.io.net.ChannelStream;
import reactor.io.net.PeerStream;
import reactor.io.net.http.HttpChannel;
import reactor.io.net.http.model.HttpHeaders;
import reactor.io.net.http.model.Method;
import reactor.io.net.http.model.Protocol;
import reactor.io.net.http.model.ResponseHeaders;
import reactor.io.net.http.model.Status;
import reactor.io.net.http.model.Transfer;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;


public class UndertowHttpChannel<IN,OUT> extends HttpChannel<IN,OUT> {

    private final HttpServerExchange exchange;

    public UndertowHttpChannel(
            HttpServerExchange exchange,
            Environment env,
            Codec<Buffer, IN, OUT> codec,
            long prefetch, PeerStream<IN, OUT, ChannelStream<IN, OUT>> peer,
            Dispatcher ioDispatcher,
            Dispatcher eventsDispatcher) {
        super(env, codec, prefetch, peer, ioDispatcher, eventsDispatcher);
        this.exchange = exchange;
    }

    @Override
    public HttpHeaders headers() {
        return null;
    }

    @Override
    protected void doHeader(String name, String value) {

    }

    @Override
    protected void doAddHeader(String name, String value) {

    }

    @Override
    public Protocol protocol() {
        return null;
    }

    @Override
    public String uri() {
        return null;
    }

    @Override
    public Method method() {
        return null;
    }

    @Override
    public Status responseStatus() {
        return null;
    }

    @Override
    protected void doResponseStatus(Status status) {

    }

    @Override
    public ResponseHeaders responseHeaders() {
        return null;
    }

    @Override
    protected void doResponseHeader(String name, String value) {

    }

    @Override
    protected void doAddResponseHeader(String name, String value) {

    }

    @Override
    public Transfer transfer() {
        return null;
    }

    @Override
    public HttpChannel<IN, OUT> transfer(Transfer transfer) {
        return null;
    }

    @Override
    public Object delegate() {
        return null;
    }

    @Override
    protected void write(ByteBuffer data, Subscriber<?> onComplete, boolean flush) {

    }

    @Override
    protected void write(Object data, Subscriber<?> onComplete, boolean flush) {

    }

    @Override
    protected void flush() {

    }

    @Override
    public InetSocketAddress remoteAddress() {
        return null;
    }

    @Override
    public ConsumerSpec on() {
        return null;
    }
}
