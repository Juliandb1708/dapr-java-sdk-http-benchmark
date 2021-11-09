package http;

import http.responsehandlers.ResponseConsumer;
import org.apache.http.client.utils.URIBuilder;
import reactor.core.publisher.Mono;

import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
        URIBuilder urlBuilder = new URIBuilder();
        urlBuilder.setScheme("http").setHost(this.hostname).setPort(this.port)
                .setPathSegments(pathSegments);

        Optional.ofNullable(urlParameters).orElse(Collections.emptyMap()).forEach((key, value) -> {
            Optional.ofNullable(value).orElse(Collections.emptyList()).forEach(urlParameterValue -> {
                urlBuilder.addParameter(key, urlParameterValue);
            });
        });

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

        if(headers != null) {
            Optional.of(headers.entrySet()).orElse(Collections.emptySet()).forEach(header -> {
                requestBuilder.setHeader(header.getKey(), header.getValue());
            });
        }

        ExecutorService service = null;
        try {
            service = Executors.newSingleThreadExecutor();

            HttpRequest request = requestBuilder.build();
            CompletableFuture<Response> future = new CompletableFuture<>();

            HttpClient.newBuilder()
                    .executor(service)
                    .build()
                    .sendAsync(request, HttpResponse.BodyHandlers.ofByteArray())
                    .thenAccept(new ResponseConsumer(future))
                    .join();

            return future;
        } finally {
            if(service != null) service.shutdown();
        }
    }

    @Override
    public void close() {}
}
