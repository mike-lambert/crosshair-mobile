package cyberspacelabs.ru.crosshairmobile;

import org.junit.Test;

import cyberspacelabs.ru.crosshairmobile.services.GeoIpLocationService;

import static org.junit.Assert.assertEquals;

/**
 * Created by mike on 12.05.17.
 */
public class GeoIp {
    @Test
    public void GeoIPLocationSucceeded() throws Exception {
        String ip = "212.164.234.91";
        String location = new GeoIpLocationService().locate(ip);
        System.out.println(ip + " <- " + location);
        assertEquals("Russia/Novosibirskaya Oblast'/Novosibirsk", location);
    }
}
