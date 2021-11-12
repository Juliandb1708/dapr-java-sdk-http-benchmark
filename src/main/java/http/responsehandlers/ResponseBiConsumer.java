package http.responsehandlers;

import http.DaprHttp;
import http.exceptions.DaprException;

import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class ResponseBiConsumer implements BiConsumer<HttpResponse<byte[]>, Throwable> {

    CompletableFuture<DaprHttp.Response> future;

    public ResponseBiConsumer(CompletableFuture<DaprHttp.Response> future) {
        this.future = future;
    }

    @Override
    public void accept(HttpResponse<byte[]> response, Throwable throwable) {
        if(throwable != null) {
            this.future.completeExceptionally(throwable);
        }

        if(response.statusCode() / 100 != 2) {
            this.future.completeExceptionally(new DaprException("HTTP Status code: " + response.statusCode()));
        } else {
            Map<String, String> mapHeaders = new HashMap<>();
            byte[] result = response.body();
            response.headers().map().forEach((key, value) -> {
                Optional.ofNullable(value).orElse(Collections.emptyList()).forEach(urlParameterValue -> {
                    mapHeaders.put(key, urlParameterValue);
                });
            });

            this.future.complete(new DaprHttp.Response(result, mapHeaders, response.statusCode()));
        }
    }
}
