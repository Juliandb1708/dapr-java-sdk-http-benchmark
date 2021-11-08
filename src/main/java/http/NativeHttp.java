package http;

import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class NativeHttp implements DaprHttp {

    private final String hostname;
    private final int port;

    public NativeHttp(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    @Override
    public Mono<Response> invokeApi(String method, String[] pathSegments, Map<String, List<String>> urlParameters, Map<String, String> headers) {
        return this.invokeApi(method, pathSegments, urlParameters, null, headers);
    }

    @Override
    public Mono<Response> invokeApi(String method, String[] pathSegments, Map<String, List<String>> urlParameters, byte[] content, Map<String, String> headers) {
        return Mono.fromCallable(() -> this.doInvokeApi(method, pathSegments, urlParameters, content, headers))
                .flatMap(Mono::fromFuture);
    }

    @Override
    public CompletableFuture<Response> doInvokeApi(String method, String[] pathSegments, Map<String, List<String>> urlParameters, byte[] content, Map<String, String> headers) {
        return null;
    }

    @Override
    public void close() {}
}
