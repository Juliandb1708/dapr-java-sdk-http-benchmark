package benchmark;

public class MainClass {

    public static void main(String[] args) {
        DaprClientHttp client = new DaprClientHttp();

        String res = client.invokeMethod().block();
        System.out.println(res);
    }
}
