package nl.julian.http.responsehandlers;

import nl.julian.http.DaprHttp;
import nl.julian.http.exceptions.DaprException;

import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class ResponseBiFunction implements BiFunction<HttpResponse<byte[]>, Throwable, DaprHttp.Response> {

    @Override
    public DaprHttp.Response apply(HttpResponse<byte[]> response, Throwable throwable) {
        if(throwable != null) {
            throw new DaprException(throwable);
        }

        if(response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new DaprException(response.statusCode(), "HTTP Status code: " + response.statusCode());
        } else {
            Map<String, String> mapHeaders = new HashMap<>();
            byte[] result = response.body();
            response.headers().map().forEach((key, value) -> {
                if (value != null) {
                    mapHeaders.put(key, String.join(",", value));
                }
            });

            return new DaprHttp.Response(result, mapHeaders, response.statusCode());
        }
    }
}
