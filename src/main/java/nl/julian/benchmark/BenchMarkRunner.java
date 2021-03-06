package nl.julian.benchmark;

import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

public class BenchMarkRunner {
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(DaprHttpCaller.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }
}
