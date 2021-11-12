package http;

import http.exceptions.DaprException;
import http.responsehandlers.ResponseBiConsumer;
import http.responsehandlers.ResponseBiFunction;
import http.responsehandlers.ResponseConsumer;
import http.responsehandlers.ResponseFunction;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.*;

public class NativeHttp implements DaprHttp {

    private final HttpClient httpClient;
    private final ExecutorService executorService;
    private final String hostname;
    private final int port;

    public NativeHttp(String hostname, int port) {
        this.executorService = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60, TimeUnit.SECONDS,
                new SynchronousQueue<>(), threadFactory("$okHttpName Dispatcher", false));

        this.httpClient = HttpClient.newBuilder()
                .executor(executorService)
                .build();

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
        HttpRequest.Builder requestBuilder;
        try {
            String segments = String.join("/", pathSegments);

            requestBuilder = HttpRequest.newBuilder()
                    .uri(new URI("http://" + this.hostname + ":" + this.port + "/" + segments));
        } catch(URISyntaxException e) {
            throw new DaprException(e.getReason());
        }

        if(HttpMethods.GET.name().equals(method)) {
            requestBuilder.GET();
        } else {
            requestBuilder.method(method, null);
        }

        HttpRequest request = requestBuilder
                .build();

//        var future = new CompletableFuture<Response>();
//        this.httpClient
//                .sendAsync(request, HttpResponse.BodyHandlers.ofByteArray())
//                .thenAccept(new ResponseConsumer(future));
//        return future;

//        return this.httpClient
//                .sendAsync(request, HttpResponse.BodyHandlers.ofByteArray())
//                .thenApply(new ResponseFunction());

        var future = new CompletableFuture<Response>();
        this.httpClient
                .sendAsync(request, HttpResponse.BodyHandlers.ofByteArray())
                .whenComplete(new ResponseBiConsumer(future));
        return future;

//        return this.httpClient
//                .sendAsync(request, HttpResponse.BodyHandlers.ofByteArray())
//                .handle(new ResponseBiFunction());
    }

    @Override
    public void close() {
        executorService.shutdown();
    }

    private ThreadFactory threadFactory(String name, boolean daemon) {
        return r -> {
            Thread t = new Thread(r, name);
            t.setDaemon(daemon);
            return t;
        };
    }
}
