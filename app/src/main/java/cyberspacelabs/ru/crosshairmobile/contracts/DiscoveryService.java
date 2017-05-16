package cyberspacelabs.ru.crosshairmobile.contracts;

import java.util.List;

import cyberspacelabs.ru.crosshairmobile.dto.Server;

/**
 * Created by mzakharov on 10.05.17.
 */
public interface DiscoveryService {
    List<Server> refresh();
    void setStatusListener(DiscoveryStatusListener listener);
    DiscoveryStatusListener getStatusListener();
}
