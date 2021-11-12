package http.test;

import http.DaprHttp;
import http.NativeHttp;
import reactor.core.publisher.Mono;

public class Test {
    private static final String[] pathSegments = new String[]{"employees.json"};

    public static void main(String[] args) {
        try(var nativeHttp = new NativeHttp("localhst", 4005)) {
            Mono<DaprHttp.Response> response = nativeHttp.invokeApi("GET", pathSegments, null);
            System.out.println(response.flatMap(Test::getMono).block());
        }
    }

    private static Mono<String> getMono(DaprHttp.Response r) {
        String object = new String(r.getBody());
        return Mono.just(object);
    }
}
