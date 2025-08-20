package edu.eci.arsw.blacklistvalidator;
import edu.eci.arsw.blacklistvalidator.HostBlackListsValidator.CheckResult;

import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        String host = "202.24.34.55"; // dirección que muestra el ejercicio
        HostBlackListsValidator validator = new HostBlackListsValidator();

        int cores = Runtime.getRuntime().availableProcessors();
        int[] Ns = new int[] { 1 };

        System.out.println("Warming up...");
        validator.checkHost(host, cores);
        Thread.sleep(500);

        int repetitions = 20; // repeticiones por configuración
        for (int N : Ns) {
            System.out.println("======================================");
            System.out.printf("Running experiment with N = %d threads (repetitions=%d)%n", N, repetitions);
            long totalTimeNs = 0;
            int lastChecked = 0;
            for (int r = 1; r <= repetitions; r++) {
                long t0 = System.nanoTime();
                CheckResult result = validator.checkHost(host, N);
                long elapsed = System.nanoTime() - t0;
                totalTimeNs += elapsed;
                lastChecked = result.getCheckedListsCount();
                System.out.printf("  run %d: time = %d ms, checkedLists = %d, occurrencesFound = %d%n",
                        r, elapsed / 1_000_000, lastChecked, result.getOccurrences().size());
                Thread.sleep(250);
            }
            long avgMs = (totalTimeNs / repetitions) / 1_000_000;
            System.out.printf("=> N=%d AVG time = %d ms (last checkedLists = %d)%n", N, avgMs, lastChecked);
        }

        System.out.println("All experiments finished.");
    }
}
