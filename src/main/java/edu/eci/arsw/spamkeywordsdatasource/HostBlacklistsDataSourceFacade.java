/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.arsw.spamkeywordsdatasource;

import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author hcadavid
 */
//Thread-safe class
public class HostBlacklistsDataSourceFacade {

    public static HostBlacklistsDataSourceFacade instan;

    public HostBlacklistsDataSourceFacade() {
    }

    public boolean isInBlacklistServer(int blservernum, int host) {
        return true;
    }

    public void reportAsTrustworthy(String host) {
    }

    public void reportAsNotTrustworthy(String host) {
    }

    public static HostBlacklistsDataSourceFacade getInstance() {
        if ( instan == null ) {
            instan = new HostBlacklistsDataSourceFacade();
        }
        return instan;
    }

    public int getRegisteredServersCount() {
        return 5;
    }

    public boolean isInBlackListServer(int i, String ipaddress) {
        return true;
    }
}