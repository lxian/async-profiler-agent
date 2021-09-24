package io.kyligence;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.Configuration;

public class AsyncProfiler {

    private static final String libPath = "/lib/linux64/libasyncProfiler.so";
    public static AsyncProfiler profiler = new AsyncProfiler();
    public static boolean isLocal = false;

    AsyncProfiler() {
        try {
            final java.nio.file.Path tmpLib = java.io.File.createTempFile("libasyncProfiler", ".so").toPath();
            java.nio.file.Files.copy(
                    AsyncProfiler.class.getResourceAsStream(libPath),
                    tmpLib,
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            System.load(tmpLib.toAbsolutePath().toString());
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static void start() {
        try {
            profiler.execute0("start,event=cpu");
            System.out.println(">>> started");
        } catch (IOException e) {
            System.out.println(">>> error");
            e.printStackTrace();
        }
    }

    public static void dump() {
        try {
            String dumped = profiler.execute0("collapsed,event=cpu");
            if (isLocal) {
                String path = "/tmp/stacks" + System.currentTimeMillis();
                PrintWriter pw = new PrintWriter(path);
                pw.print(dumped);
                pw.close();
                System.out.println("dumped to " + path);
            } else {
                String path = String.format("/kylin/profilestacks/%s/%s/%s",
                        SparkUtils.appId, SparkUtils.executorId, "stack" + System.currentTimeMillis());
                writeToHdfsFile(dumped, path);
                System.out.println("dumped to " + path);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeToHdfsFile(String content, String filePath) {
        try (OutputStream outputStream = FileSystem.get(new Configuration()).create(new Path(filePath))) {
            BufferedWriter br = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            br.write(content);
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void stop() {
        AsyncProfiler profiler = new AsyncProfiler();
        profiler.stop0();
    }

    public static String execute(String command) throws IOException {
        AsyncProfiler profiler = new AsyncProfiler();
        return profiler.execute0(command);
    }

    public native void start0(String event, long interval, boolean reset) throws IllegalStateException;
    public native void stop0() throws IllegalStateException;
    public native String execute0(String command) throws IllegalArgumentException, IllegalStateException, IOException;
    public native void filterThread0(Thread thread, boolean enable);
}
