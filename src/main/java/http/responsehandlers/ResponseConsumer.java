package http.responsehandlers;

import http.DaprHttp;

import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class ResponseConsumer implements Consumer<HttpResponse<byte[]>> {

    CompletableFuture<DaprHttp.Response> future;

    public ResponseConsumer(CompletableFuture<DaprHttp.Response> future) {
        this.future = future;
    }

    @Override
    public void accept(HttpResponse<byte[]> response) {
        if(response.statusCode() / 100 != 2) {
            this.future.completeExceptionally(new Exception("STATUS CODE " + response.statusCode()));
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
