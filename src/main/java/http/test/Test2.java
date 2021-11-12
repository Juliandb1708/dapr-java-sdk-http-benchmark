package http.test;

import http.exceptions.DaprException;

import java.util.concurrent.CompletableFuture;

public class Test2 {

    public static void main(String[] args) {
        System.out.println(
                completeWithBiFunction().join()
        );
    }

    public static CompletableFuture<Integer> completeWithConsumer() {
        var future = new CompletableFuture<Integer>();

        CompletableFuture.supplyAsync(Test2::httpRequest)
                .thenAccept(res -> {
                    if(res / 100 != 2) {
                        future.completeExceptionally(new DaprException("HTTP Status code: " + res));
                    } else {
                        future.complete(res);
                    }
                });

        return future;
    }

    public static CompletableFuture<Integer> completeWithFunction() {
        return CompletableFuture.supplyAsync(Test2::httpRequest)
                .thenApply(res -> {
                    if(res / 100 != 2) {
                        throw new DaprException("HTTP Status code: " + res);
                    }
                    return res;
                });
    }

    public static CompletableFuture<Integer> completeWithBiConsumer() {
        var future = new CompletableFuture<Integer>();

        CompletableFuture.supplyAsync(Test2::httpRequest)
                .whenComplete((res, ex) -> {
                   if(ex != null) {
                       future.completeExceptionally(new DaprException("HTTP Status code: UNKNOWN"));
                   } else {
                       if(res / 100 != 2) {
                           future.completeExceptionally(new DaprException("HTTP Status code: " + res));
                       } else {
                           future.complete(res);
                       }
                   }
                });

        return future;
    }

    public static CompletableFuture<Integer> completeWithBiFunction() {
        return CompletableFuture.supplyAsync(Test2::httpRequest)
                .handle((res, ex) -> {
                    if(ex != null) {
                        throw new DaprException("HTTP Status code: UNKNOWN");
                    } else {
                        if(res / 100 != 2) {
                            throw new DaprException("HTTP Status code: " + res);
                        } else {
                            return res;
                        }
                    }
                });
    }

    private static int httpRequest() {
        if(false) {
            throw new RuntimeException("CONNECTION EXCEPTION");
        } else {
            return 200;
        }
    }
}
