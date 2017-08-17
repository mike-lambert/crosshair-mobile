package cyberspacelabs.ru.crosshairmobile.services.factory;

import android.content.Context;

import cyberspacelabs.ru.crosshairmobile.ApplicationState;
import cyberspacelabs.ru.crosshairmobile.contracts.DiscoveryService;
import cyberspacelabs.ru.crosshairmobile.services.discovery.DPMQueryDiscoveryService;
import cyberspacelabs.ru.crosshairmobile.services.discovery.NativeDiscoveryService;

/**
 * Created by mike on 17.08.17.
 */
public class DiscoveryServiceFactory {
    private static  DiscoveryService service;
    public static DiscoveryService getDiscoveryService(Context context) {
        if (service == null){
            boolean useServer = ApplicationState.getInstance(context).getDefaultPreferences().getBoolean("discovery_use_server", false);
            if (useServer){
                service = new DPMQueryDiscoveryService(context);
            } else {
                service = new NativeDiscoveryService(context);
            }
        }
        return service;
    }
}
