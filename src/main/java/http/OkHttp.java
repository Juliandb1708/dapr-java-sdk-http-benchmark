package http;

import http.responsehandlers.ResponseFutureCallBack;
import okhttp3.HttpUrl.Builder;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
        Builder urlBuilder = new Builder();
        urlBuilder.scheme("http").host(this.hostname).port(this.port);
        for(String pathSegment : pathSegments) {
            urlBuilder.addPathSegment(pathSegment);
        }

        Optional.ofNullable(urlParameters).orElse(Collections.emptyMap()).forEach((key, value) -> {
            Optional.ofNullable(value).orElse(Collections.emptyList()).forEach(urlParameterValue -> {
                urlBuilder.addQueryParameter(key, urlParameterValue);
            });
        });

        Request.Builder requestBuilder = new Request.Builder()
                .url(urlBuilder.build());

        if(HttpMethods.GET.name().equals(method)) {
            requestBuilder.get();
        } else {
            requestBuilder.method(method, null);
        }

        if(headers != null) {
            Optional.of(headers.entrySet()).orElse(Collections.emptySet()).forEach(header -> {
                requestBuilder.addHeader(header.getKey(), header.getValue());
            });
        }

        try {
            Request request = requestBuilder.build();
            CompletableFuture<Response> future = new CompletableFuture<>();
            this.okHttpClient.newCall(request).enqueue(new ResponseFutureCallBack(future));

            return future;
        } finally {
            this.okHttpClient.dispatcher().executorService().shutdown();
        }
    }

    @Override
    public void close() {}
}
