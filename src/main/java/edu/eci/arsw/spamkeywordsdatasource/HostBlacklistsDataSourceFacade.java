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

    // Número de servidores registrados (simulado).
    private final int registeredServersCount = 10000;

    // Constructor privado para singleton
    private HostBlacklistsDataSourceFacade() {
    }

    /**
     * Determina si el host (representado como int) está en la blacklist del servidor blservernum.
     * Implementación determinística para pruebas reproducibles.
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

        // Simulación simple: probabilidad baja, determinista por seed
        int chance = r.nextInt(100);
        boolean found = (chance < 7); // ~7% de probabilidad por servidor

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
