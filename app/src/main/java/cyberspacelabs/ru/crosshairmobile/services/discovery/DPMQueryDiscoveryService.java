package cyberspacelabs.ru.crosshairmobile.services.discovery;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import cyberspacelabs.ru.crosshairmobile.contracts.rest.DPMQueryService;
import cyberspacelabs.ru.crosshairmobile.dto.Server;
import cyberspacelabs.ru.crosshairmobile.services.factory.DPMQueryServiceFactory;
import cyberspacelabs.ru.crosshairmobile.services.factory.GameBrowserFactory;

/**
 * Created by mike on 17.08.17.
 */
public class DPMQueryDiscoveryService extends AbstractDiscoveryService {
    private DPMQueryService api;

    public DPMQueryDiscoveryService(Context context){
        super(context);
        api = DPMQueryServiceFactory.getInstance(context).getProxy();
    }

    @Override
    protected int submitTasks() {
        int i = 0;
        for(final String master : GameBrowserFactory.getMasters()){
            for(final String query : GameBrowserFactory.getQueries()){
                i++;
                completionService.submit(new Callable<List<Server>>() {
                    @Override
                    public List<Server> call() throws Exception {
                        return transformGameServerToServer(api.queryMasterServer(master, query).execute().body());
                    }
                });
                updateStatus(i, getTotalTasksCount());
            }
        }
        return i;
    }

    @Override
    protected int getTotalTasksCount() {
        return GameBrowserFactory.getMasters().size() * GameBrowserFactory.getQueries().size();
    }

}
