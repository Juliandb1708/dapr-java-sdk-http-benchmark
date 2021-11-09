package benchmark;

import http.DaprHttp;
import http.NativeHttp;
import http.OkHttp;
import org.openjdk.jmh.annotations.*;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;

@Warmup(iterations = 20, time = 1, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 2000, time = 1, timeUnit = TimeUnit.MILLISECONDS)
@Fork(value = 1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class DaprHttpCaller {

    DaprHttp nativeHttp;
    DaprHttp okHttp;

    String[] pathSegments;

    @Setup
    public void setup() {
        nativeHttp = new NativeHttp("127.0.0.1", 4005);
        okHttp = new OkHttp("127.0.0.1", 4005);

        pathSegments = new String[]{"employees.json"};
    }

    @Benchmark
    public void callOkHttpClient() {
        Mono<DaprHttp.Response> response = okHttp.invokeApi("GET", pathSegments);
        response.flatMap(DaprHttpCaller::getMono).block();
    }

    @Benchmark
    public void callNativeHttpClient() {
        Mono<DaprHttp.Response> response = nativeHttp.invokeApi("GET", pathSegments);
        response.flatMap(DaprHttpCaller::getMono).block();
    }

    @TearDown
    public void tearDown() throws Exception {
        nativeHttp.close();
        okHttp.close();
    }

    private static Mono<String> getMono(DaprHttp.Response r) {
        String object = new String(r.getBody());
        return Mono.just(object);
    }
}
