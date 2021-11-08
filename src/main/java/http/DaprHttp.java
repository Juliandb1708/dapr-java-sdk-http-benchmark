package http;

import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface DaprHttp extends AutoCloseable {

    Mono<DaprHttp.Response> invokeApi(String method, String[] pathSegments, Map<String, List<String>> urlParameters, Map<String, String> headers);

    Mono<DaprHttp.Response> invokeApi(String method, String[] pathSegments, Map<String, List<String>> urlParameters, byte[] content, Map<String, String> headers);

    CompletableFuture<Response> doInvokeApi(String method, String[] pathSegments, Map<String, List<String>> urlParameters, byte[] content, Map<String, String> headers);

    class Response {
        private final byte[] body;
        private final Map<String, String> headers;
        private final int statusCode;

        public Response(byte[] body, Map<String, String> headers, int statusCode) {
            this.body = body;
            this.headers = headers;
            this.statusCode = statusCode;
        }

        public byte[] getBody() {
            return Arrays.copyOf(this.body, this.body.length);
        }

        public Map<String, String> getHeaders() {
            return this.headers;
        }

        public int getStatusCode() {
            return this.statusCode;
        }
    }
}
