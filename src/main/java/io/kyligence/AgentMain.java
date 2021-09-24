package io.kyligence;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AgentMain {

    private static ScheduledExecutorService es = Executors.newScheduledThreadPool(1, r -> {
        Thread t = new Thread(null, r, "profiler-0", 0);
        t.setDaemon(true);
        return t;
    });

    public static List<String> getJvmInputArguments() {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        List<String> jvmArgs = runtimeMXBean.getInputArguments();
        return jvmArgs == null ? new ArrayList<>() : jvmArgs;
    }

    public static void profile() {
        try {
            System.out.println(">>>>> args");
            getJvmInputArguments().forEach(System.out::println);
            System.out.println(">>>>> profiling");
            System.out.println(AsyncProfiler.profiler);
            AsyncProfiler.start();
            System.out.println(">>>>> started");
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(">>>>> dumping");
            AsyncProfiler.dump();
            System.out.println(">>>>> dumped");
        } catch (Exception e) {
            System.out.println(">>>>> error");
            e.printStackTrace();
        }
    }

//    public static void agentmain(String agentArgs, Instrumentation inst) {
//        System.out.println(">>>>> agentmain");
//        System.out.println(">>>>> starting");
//    }

    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println(">>>>> premain");
        try {
            System.out.println(">>>>> status: " + AsyncProfiler.execute("status"));
        } catch (IOException e) {
            e.printStackTrace();
        }
//        Thread t = new Thread(AgentMain::profile);
//        t.setDaemon(false);
//        t.start();
        es.scheduleWithFixedDelay(AgentMain::profile , 2, 60, TimeUnit.SECONDS);
    }

    // for testing
    public static void main(String[] args) throws InterruptedException, IOException {
        AsyncProfiler.isLocal = true;
        System.out.println(AsyncProfiler.execute("status"));
        es.scheduleWithFixedDelay(AgentMain::profile , 0, 60, TimeUnit.SECONDS);
        System.out.println("running");
        int j = 0;
        for (int i = 0; i < 10000; i++) { // do some simple math
            j += i / 39;
        }
        Thread.sleep(30000);
        System.out.println("done");
    }
}
