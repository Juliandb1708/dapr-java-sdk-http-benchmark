package benchmark;

import http.DaprHttp;
import http.NativeHttp;
import http.OkHttp;
import reactor.core.publisher.Mono;

public class DaprClientHttp {

    private static final DaprHttp daprHttp = new NativeHttp("localhost", 4005);

    public Mono<String> invokeMethod() {
        String[] pathSegments = new String[]{"employees.json"};

        Mono<DaprHttp.Response> response = daprHttp.invokeApi("GET", pathSegments, null, null);

        return response.flatMap(DaprClientHttp::getMono);
    }

    public static Mono<String> getMono(DaprHttp.Response r) {
        String object = new String(r.getBody());
        return Mono.just(object);
    }
}
