package cyberspacelabs.ru.crosshairmobile.contracts;

import java.util.List;

import cyberspacelabs.ru.crosshairmobile.dto.Server;

/**
 * Created by mike on 11.05.17.
 */
public interface DiscoveryStatusListener {
    void updateStatus(int overallTasks, int pendingTasks);
    void updateData(List<Server> arrived);
    void refreshDone();
}
