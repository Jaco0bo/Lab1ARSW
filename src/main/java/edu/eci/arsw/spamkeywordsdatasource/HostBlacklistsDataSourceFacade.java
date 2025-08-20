package edu.eci.arsw.spamkeywordsdatasource;

import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Thread-safe facade (simulada) de listas negras.
 */
public class HostBlacklistsDataSourceFacade {

    // Singleton thread-safe (double-checked locking)
    private static volatile HostBlacklistsDataSourceFacade instan;

    // Cache para resultados (clave: (server<<32) ^ host)
    private final ConcurrentHashMap<Long, Boolean> cache = new ConcurrentHashMap<>();

    private final Logger LOG = Logger.getLogger(HostBlacklistsDataSourceFacade.class.getName());

    // Número de servidores registrados (simulado). Puedes ajustar a 10000 si quieres "miles".
    private final int registeredServersCount = 10000;

    // Constructor privado para singleton
    private HostBlacklistsDataSourceFacade() {
    }

    /**
     * Determina si el host (representado como int) está en la blacklist del servidor blservernum.
     * Implementación determinística (para que las llamadas repetidas devuelvan el mismo resultado)
     * y con un pequeño sesgo para que los servidores iniciales tengan mayor probabilidad,
     * simulando el escenario del enunciado (host encontrado rápido en algunos casos).
     *
     * @param blservernum número de servidor (0..getRegisteredServersCount()-1)
     * @param host host codificado como int (ej. "200.24.34.55" -> "200243455")
     * @return true si está en la blacklist de ese servidor, false en otro caso
     */
    public boolean isInBlacklistServer(int blservernum, int host) {
        if (blservernum < 0 || blservernum >= registeredServersCount) {
            throw new IllegalArgumentException("Server number out of range: " + blservernum);
        }

        long key = (((long) blservernum) << 32) ^ (host & 0xffffffffL);
        Boolean cached = cache.get(key);
        if (cached != null) return cached;

        // Semilla determinística basada en server + host
        long seed = Objects.hash(host, blservernum);
        Random r = new Random(seed);

        // Simulación: probabilidad más alta en servidores con índices pequeños
        // chance será 0..99; devolvemos true si chance < threshold
        int baseThreshold = 7; // aproximadamente 7% por servidor en promedio
        int bias = Math.max(0, 5 - (blservernum / 1000)); // primeros 5000 servidores algo más probables
        int threshold = baseThreshold + bias; // primeros servidores tienen threshold mayor

        int chance = r.nextInt(100);
        boolean found = (chance < threshold);

        cache.put(key, found);
        return found;
    }

    /**
     * Reporta el host como confiable en la BD local (simulado con LOG).
     */
    public void reportAsTrustworthy(String host) {
        LOG.log(Level.INFO, "HOST {0} Reported as trustworthy", host);
    }

    /**
     * Reporta el host como NO confiable en la BD local (simulado con LOG).
     */
    public void reportAsNotTrustworthy(String host) {
        LOG.log(Level.INFO, "HOST {0} Reported as NOT trustworthy", host);
    }

    /**
     * Singleton accessor (thread-safe).
     */
    public static HostBlacklistsDataSourceFacade getInstance() {
        if (instan == null) {
            synchronized (HostBlacklistsDataSourceFacade.class) {
                if (instan == null) {
                    instan = new HostBlacklistsDataSourceFacade();
                }
            }
        }
        return instan;
    }

    /**
     * Número de servidores registrados.
     */
    public int getRegisteredServersCount() {
        return registeredServersCount;
    }
}
