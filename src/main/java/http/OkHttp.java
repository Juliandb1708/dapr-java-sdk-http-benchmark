package http;

import okhttp3.OkHttpClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class OkHttp implements DaprHttp {

    private final OkHttpClient okHttpClient;
    private final String hostname;
    private final int port;

    public OkHttp(String hostname, int port) {
        this.okHttpClient = new OkHttpClient();
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
