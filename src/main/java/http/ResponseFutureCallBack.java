package http;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ResponseFutureCallBack implements Callback {

    public static final byte[] EMPTY_BYTES;

    private final CompletableFuture<DaprHttp.Response> future;

    static {
        EMPTY_BYTES = new byte[0];
    }

    public ResponseFutureCallBack(CompletableFuture<DaprHttp.Response> future) {
        this.future = future;
    }

    @Override
    public void onFailure(@NotNull Call call, @NotNull IOException e) {
        this.future.completeExceptionally(e);
    }

    @Override
    public void onResponse(@NotNull Call call, @NotNull Response response) {
        if(!response.isSuccessful()) {
            this.future.completeExceptionally(new Exception("STATUS CODE " + response.code()));
        } else {
            Map<String, String> mapHeaders = new HashMap<>();
            byte[] result = getBodyBytesOrEmptyArray(response);
            response.headers().forEach(pair -> {
                mapHeaders.put(pair.getFirst(), pair.getSecond());
            });

            this.future.complete(new DaprHttp.Response(result, mapHeaders, response.code()));
        }
    }

    private byte[] getBodyBytesOrEmptyArray(Response response) {
        try {
            ResponseBody body = response.body();
            return body != null ? body.bytes() : EMPTY_BYTES;
        } catch (IOException e) {
            return EMPTY_BYTES;
        }
    }
}
