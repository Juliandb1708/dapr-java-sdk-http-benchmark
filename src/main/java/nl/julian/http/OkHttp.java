package nl.julian.http;

import nl.julian.http.responsehandlers.ResponseFutureCallBack;
import okhttp3.HttpUrl.Builder;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import reactor.core.publisher.Mono;

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
        Builder urlBuilder = new Builder();
        urlBuilder.scheme("http").host(this.hostname).port(this.port);
        for(String pathSegment : pathSegments) {
            urlBuilder.addPathSegment(pathSegment);
        }

        Request.Builder requestBuilder = new Request.Builder()
                .url(urlBuilder.build());

        if(HttpMethods.GET.name().equals(method)) {
            requestBuilder.get();
        } else {
            requestBuilder.method(method, null);
        }

        Request request = requestBuilder.build();
        CompletableFuture<Response> future = new CompletableFuture<>();
        this.okHttpClient.newCall(request).enqueue(new ResponseFutureCallBack(future));

        return future;
    }

    @Override
    public void close() {
        this.okHttpClient.dispatcher().executorService().shutdown();
    }
}
