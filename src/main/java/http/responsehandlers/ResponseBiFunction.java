package http.responsehandlers;

import http.DaprHttp;
import http.exceptions.DaprException;

import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

public class ResponseBiFunction implements BiFunction<HttpResponse<byte[]>, Throwable, DaprHttp.Response> {

    @Override
    public DaprHttp.Response apply(HttpResponse<byte[]> response, Throwable throwable) {
        if(throwable != null) {
            throw new DaprException("HTTP Status code: UNKNOWN");
        }

        if(response.statusCode() / 100 != 2) {
            throw new DaprException("HTTP Status code: " + response.statusCode());
        } else {
            Map<String, String> mapHeaders = new HashMap<>();
            byte[] result = response.body();
            response.headers().map().forEach((key, value) -> {
                Optional.ofNullable(value).orElse(Collections.emptyList()).forEach(urlParameterValue -> {
                    mapHeaders.put(key, urlParameterValue);
                });
            });

            return new DaprHttp.Response(result, mapHeaders, response.statusCode());
        }
    }
}
