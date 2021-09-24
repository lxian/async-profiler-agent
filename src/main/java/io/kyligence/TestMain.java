package io.kyligence;

public class TestMain {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("running");
        int j = 0;
        for (int i = 0; i < 10000; i++) { // do some simple math
            j += i / 39;
        }
        Thread.sleep(15000);
        System.out.println("done");
    }
}
