package nl.julian.http;

import nl.julian.http.exceptions.DaprException;
import nl.julian.http.responsehandlers.ResponseBiFunction;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.*;
import java.util.function.BiFunction;

public class NativeHttp implements DaprHttp {

    private final BiFunction<HttpResponse<byte[]>, Throwable, DaprHttp.Response> responseFunction;

    private final HttpClient httpClient;
    private final ExecutorService executorService;

    private final String hostname;
    private final int port;

    public NativeHttp(String hostname, int port) {
        this.executorService = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60, TimeUnit.SECONDS,
                new SynchronousQueue<>(), threadFactory());

        this.httpClient = HttpClient.newBuilder()
                .executor(executorService)
                .build();

        this.responseFunction = new ResponseBiFunction();

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
            throw new DaprException(e);
        }

        if(HttpMethods.GET.name().equals(method)) {
            requestBuilder.GET();
        } else {
            requestBuilder.method(method, null);
        }

        HttpRequest request = requestBuilder.build();

        return this.httpClient
                .sendAsync(request, HttpResponse.BodyHandlers.ofByteArray())
                .handle(this.responseFunction);
    }

    @Override
    public void close() {
        executorService.shutdown();
    }

    private ThreadFactory threadFactory() {
        return r -> {
            Thread t = new Thread(r, "$okHttpName Dispatcher");
            t.setDaemon(false);
            return t;
        };
    }
}
