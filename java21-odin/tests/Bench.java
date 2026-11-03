import java.util.*;

public class Bench {
    static int fib(int n) {
        return n < 2 ? n : fib(n - 1) + fib(n - 2);
    }

    public static void main(String[] a) {
        System.out.println("vm: " + System.getProperty("java.vm.name") + " | " + System.getProperty("java.vm.info"));
        for (int round = 1; round <= 3; round++) {
            long t0 = System.nanoTime();
            int f = fib(32);
            long t1 = System.nanoTime();
            double[] arr = new double[400_000];
            Random r = new Random(1);
            for (int i = 0; i < arr.length; i++) arr[i] = r.nextDouble();
            Arrays.sort(arr);
            long t2 = System.nanoTime();
            long sum = 0;
            for (int i = 0; i < 30_000_000; i++) sum += (i ^ (i >>> 3)) % 7;
            long t3 = System.nanoTime();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 200_000; i++) sb.append(i % 10);
            Map<String, Integer> m = new HashMap<>();
            for (int i = 0; i < 200_000; i++) m.merge("k" + (i % 5000), 1, Integer::sum);
            long t4 = System.nanoTime();
            System.out.println("round " + round + ": fib(32)=" + f + " " + (t1 - t0) / 1000000 + " ms | sort 400k doubles "
                    + (t2 - t1) / 1000000 + " ms | loop 30M " + (t3 - t2) / 1000000 + " ms | strings/map " + (t4 - t3) / 1000000
                    + " ms (" + sum + "," + sb.length() + "," + m.size() + ")");
        }
        System.exit(0);
    }
}
