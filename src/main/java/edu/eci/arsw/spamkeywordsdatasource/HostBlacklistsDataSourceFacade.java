package edu.eci.arsw.spamkeywordsdatasource;

import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Thread-safe facade (simulada) de listas negras.
 * Incluye comportamiento determinista para pruebas:
 * - 202.24.34.55 (202243455) -> disperso en muchas listas
 * - 212.24.24.55 (212242455) -> no aparece en ninguna lista
 */
public class HostBlacklistsDataSourceFacade {

    private static volatile HostBlacklistsDataSourceFacade instan;
    private final ConcurrentHashMap<Long, Boolean> cache = new ConcurrentHashMap<>();
    private final Logger LOG = Logger.getLogger(HostBlacklistsDataSourceFacade.class.getName());
    private static final int registeredServersCount = 10000;

    private HostBlacklistsDataSourceFacade() {
    }

    public boolean isInBlacklistServer(int blservernum, int host) {
        if (blservernum < 0 || blservernum >= registeredServersCount) {
            throw new IllegalArgumentException("Server number out of range: " + blservernum);
        }

        long key = (((long) blservernum) << 32) ^ (host & 0xffffffffL);
        Boolean cached = cache.get(key);
        if (cached != null) return cached;

        if (host == 202243455) { // 202.24.34.55 -> Debe estar reportado de forma dispersa
            boolean found = (blservernum % 2003 == 0) ||
                    (blservernum % 1999 == 0) ||
                    (blservernum % 2011 == 0) ||
                    (blservernum % 2017 == 0);
            cache.put(key, found);
            return found;
        }

        if (host == 212242455) { // 212.24.24.55 -> No debe está en ninguna lista
            cache.put(key, false);
            return false;
        }

        // Comportamiento pseudoaleatorio determinista predeterminado para otras IP
        long seed = Objects.hash(host, blservernum);
        Random r = new Random(seed);

        int baseThreshold = 7; // ~7% por servidor
        int bias = Math.max(0, 5 - (blservernum / 1000)); // probabilidad ligeramente mayor en servidores de índice pequeño
        int threshold = baseThreshold + bias;

        int chance = r.nextInt(100);
        boolean found = (chance < threshold);

        cache.put(key, found);
        return found;
    }

    public void reportAsTrustworthy(String host) {
        LOG.log(Level.INFO, "HOST {0} Reported as trustworthy", host);
    }

    public void reportAsNotTrustworthy(String host) {
        LOG.log(Level.INFO, "HOST {0} Reported as NOT trustworthy", host);
    }

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

    public int getRegisteredServersCount() {
        return registeredServersCount;
    }
}

