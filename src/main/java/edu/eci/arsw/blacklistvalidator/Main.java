package edu.eci.arsw.blacklistvalidator;

import java.util.List;

/**
 *
 * @author hcadavid
 */
public class Main {
    
    public static void main(String a[]){
        HostBlackListsValidator hblv=new HostBlackListsValidator();
        List<Integer> blackListOcurrences=hblv.checkHost("200.24.34.55");
        List<Integer> blackListOcurrences2=hblv.checkHost("212.24.24.55");
        System.out.println("The host was found in the following blacklists:"+blackListOcurrences);
        System.out.println("The host was found in the following blacklists:"+blackListOcurrences2);
    }
}
