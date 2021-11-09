package http;

import http.responsehandlers.ResponseConsumer;
import org.apache.http.client.utils.URIBuilder;
import reactor.core.publisher.Mono;

import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NativeHttp implements DaprHttp {

    private final ExecutorService executorService;
    private final String hostname;
    private final int port;

    public NativeHttp(String hostname, int port) {
        this.executorService = Executors.newFixedThreadPool(5);

        this.hostname = hostname;
        this.port = port;
    }

    @Override
    public Mono<Response> invokeApi(String method, String[] pathSegments) {
        return this.invokeApi(method, pathSegments, null);
    }

    @Override
    public Mono<Response> invokeApi(String method, String[] pathSegments, byte[] content) {
        return Mono.fromCallable(() -> this.doInvokeApi(method, pathSegments, content))
                .flatMap(Mono::fromFuture);
    }

    @Override
    public CompletableFuture<Response> doInvokeApi(String method, String[] pathSegments, byte[] content) {
        URIBuilder urlBuilder = new URIBuilder();
        urlBuilder.setScheme("http").setHost(this.hostname).setPort(this.port)
                .setPathSegments(pathSegments);

        HttpRequest.Builder requestBuilder;
        try {
            requestBuilder = HttpRequest.newBuilder()
                    .uri(urlBuilder.build());
        } catch(URISyntaxException e) {
            return null;
        }

        if(HttpMethods.GET.name().equals(method)) {
            requestBuilder.GET();
        } else {
            requestBuilder.method(method, null);
        }

        HttpRequest request = requestBuilder.build();
        CompletableFuture<Response> future = new CompletableFuture<>();

        HttpClient.newBuilder()
                .executor(executorService)
                .build()
                .sendAsync(request, HttpResponse.BodyHandlers.ofByteArray())
                .thenAccept(new ResponseConsumer(future))
                .join();

        return future;
    }

    @Override
    public void close() {
        executorService.shutdown();
    }
}
