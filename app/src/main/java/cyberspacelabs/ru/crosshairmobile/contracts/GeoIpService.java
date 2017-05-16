package cyberspacelabs.ru.crosshairmobile.contracts;

/**
 * Created by mike on 11.05.17.
 */
public interface GeoIpService {
    String locate(String ip) throws Exception;
}
