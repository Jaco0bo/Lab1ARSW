package edu.eci.arsw.blacklistvalidator;

import edu.eci.arsw.spamkeywordsdatasource.HostBlacklistsDataSourceFacade;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Validator paralelo: checkHost(String host, int N)
 * - divide el espacio de servidores en N particiones (reparte el resto a las primeras)
 * - lanza N HostSearchThread
 * - espera con join()
 * - agrega los resultados y decide reportar
 */
public class HostBlackListsValidator {

    private static final int BLACK_LIST_ALARM_COUNT = 5;
    private static final Logger LOG = Logger.getLogger(HostBlackListsValidator.class.getName());

    /**
     * Backwards-compatible: usa el número de procesadores disponibles como N por defecto.
     */
    public CheckResult checkHost(String host) {
        int defaultThreads = Runtime.getRuntime().availableProcessors();
        return checkHost(host, defaultThreads);
    }

    /**
     * Versión paralela: divide el trabajo en N hilos.
     * @param host ip en formato "x.x.x.x"
     * @param N número de hilos
     * @return lista de índices de blacklists donde se encontró el host
     */
    public CheckResult checkHost(String host, int N) {
        LinkedList<Integer> blackListOcurrences = new LinkedList<>();

        AtomicInteger ocurrencesCountShared = new AtomicInteger(0);
        AtomicInteger checkedListsCountShared = new AtomicInteger(0);

        HostBlacklistsDataSourceFacade skds = HostBlacklistsDataSourceFacade.getInstance();
        int registeredServers = skds.getRegisteredServersCount();

        // sanitize N
        if (N <= 0) N = 1;
        if (N > registeredServers) N = registeredServers;

        // Convierte de string a int
        String ip_int_str = host.replace(".", "");
        final int ipInt;
        try {
            ipInt = Integer.parseInt(ip_int_str);
        } catch (NumberFormatException ex) {
            LOG.log(Level.SEVERE, "Invalid host format: {0}", host);
            return new CheckResult(blackListOcurrences, checkedListsCountShared.get());
        }

        // Particionado: Distribuye el resto a las primeras particiones 'rem'
        int base = registeredServers / N;
        int rem = registeredServers % N;

        HostSearchThread[] threads = new HostSearchThread[N];
        int start = 0;
        for (int i = 0; i < N; i++) {
            int extra = (i < rem) ? 1 : 0;
            int end = start + base + extra;
            threads[i] = new HostSearchThread(i, start, end, ipInt, skds, ocurrencesCountShared, checkedListsCountShared);
            start = end;
        }

        // Inicia el thread
        for (HostSearchThread t : threads) t.start();

        // Espera por thread para finalizar
        for (HostSearchThread t : threads) {
            try {
                t.join();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                LOG.log(Level.WARNING, "Interrupted while waiting for search threads", ex);
            }
        }

        // Agrega los resultados de los hilos
        for (HostSearchThread t : threads) {
            blackListOcurrences.addAll(t.getFoundLists());
        }

        if (ocurrencesCountShared.get() >= BLACK_LIST_ALARM_COUNT) {
            skds.reportAsNotTrustworthy(host);
        } else {
            skds.reportAsTrustworthy(host);
        }

        // LOG verídico: número de listas realmente revisadas
        LOG.log(Level.INFO, "Checked Black Lists:{0} of {1}", new Object[]{checkedListsCountShared.get(), registeredServers});

        return new CheckResult(blackListOcurrences, checkedListsCountShared.get());

    }

    public static class CheckResult {
        private final List<Integer> occurrences;
        private final int checkedListsCount;

        public CheckResult(List<Integer> occurrences, int checkedListsCount) {
            this.occurrences = occurrences;
            this.checkedListsCount = checkedListsCount;
        }

        public List<Integer> getOccurrences() { return occurrences; }
        public int getCheckedListsCount() { return checkedListsCount; }
    }

    /**
     * Hilo que verifica el sub-rango [start, end)
     */
    private static class HostSearchThread extends Thread {

        private final int id;
        private final int start;   // inclusivo
        private final int end;     // exclusivo
        private final int hostInt;
        private final HostBlacklistsDataSourceFacade skds;
        private final AtomicInteger ocurrencesCountShared;
        private final AtomicInteger checkedListsCountShared;
        private final LinkedList<Integer> foundLists = new LinkedList<>();

        HostSearchThread(int id, int start, int end, int hostInt,
                         HostBlacklistsDataSourceFacade skds,
                         AtomicInteger ocurrencesCountShared,
                         AtomicInteger checkedListsCountShared) {
            this.id = id;
            this.start = start;
            this.end = end;
            this.hostInt = hostInt;
            this.skds = skds;
            this.ocurrencesCountShared = ocurrencesCountShared;
            this.checkedListsCountShared = checkedListsCountShared;
            setName("HostSearchThread-" + id);
        }

        @Override
        public void run() {
            // Recorre el sub-rango y comprueba la condición global para terminar temprano
            for (int i = start; i < end && ocurrencesCountShared.get() < BLACK_LIST_ALARM_COUNT; i++) {
                boolean found = skds.isInBlacklistServer(i, hostInt);
                // Incrementa el contador de verificados solo cuando realmente verificamos
                checkedListsCountShared.incrementAndGet();

                if (found) {
                    foundLists.add(i);
                    ocurrencesCountShared.incrementAndGet();
                }
            }
        }

        public int getOcurrencesCount() {
            return foundLists.size();
        }

        /**
         * Lista de índices donde este hilo encontró el host
         */
        public List<Integer> getFoundLists() {
            return foundLists;
        }
    }
}

