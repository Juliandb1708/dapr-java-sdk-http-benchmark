package benchmark;

import http.DaprHttp;
import http.NativeHttp;
import http.OkHttp;
import org.openjdk.jmh.annotations.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

@Warmup(iterations = 20, time = 1, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 2000, time = 1, timeUnit = TimeUnit.MILLISECONDS)
@Fork(value = 1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class DaprHttpCaller {

    private DaprHttp nativeHttp;
    private DaprHttp okHttp;

    private String[] pathSegments;

    @Setup
    public void setup() {
        Properties properties = loadProperties();

        String url = properties.getProperty("url-host");
        int port = Integer.parseInt(properties.getProperty("url-port"));

        this.nativeHttp = new NativeHttp(url, port);
        this.okHttp = new OkHttp(url, port);

        this.pathSegments = properties.getProperty("url-segments").split(",");
    }

    @Benchmark
    public void callOkHttpClient() {
        okHttp.invokeApi("GET", pathSegments).block();
    }

    @Benchmark
    public void callNativeHttpClient() {
        nativeHttp.invokeApi("GET", pathSegments).block();
    }

    @TearDown
    public void tearDown() throws Exception {
        nativeHttp.close();
        okHttp.close();
    }

    private Properties loadProperties() {
        Properties properties = new Properties();
        try(var input = new FileInputStream("src/main/resources/application.properties")) {
            properties.load(input);
        } catch(IOException e) {
            e.printStackTrace();
        }

        return properties;
    }
}
