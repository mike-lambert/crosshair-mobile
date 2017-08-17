package cyberspacelabs.ru.crosshairmobile.services.discovery;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import cyberspacelabs.ru.crosshairmobile.contracts.DiscoveryService;
import cyberspacelabs.ru.crosshairmobile.contracts.DiscoveryStatusListener;
import cyberspacelabs.ru.crosshairmobile.darkplaces.GameBrowser;
import cyberspacelabs.ru.crosshairmobile.darkplaces.GameServer;
import cyberspacelabs.ru.crosshairmobile.dto.Server;
import cyberspacelabs.ru.crosshairmobile.services.factory.GameBrowserFactory;

/**
 * Created by mzakharov on 10.05.17.
 */
public class NativeDiscoveryService extends AbstractDiscoveryService {
    private List<GameBrowser> browsers;
    public NativeDiscoveryService(Context context){
        super(context);
        browsers = GameBrowserFactory.getGameBrowsers();
    }

    @Override
    protected int submitTasks() {
        int i = 0;
        for(final GameBrowser browser: browsers){
            i++;
            completionService.submit(new Callable<List<Server>>() {
                @Override
                public List<Server> call() throws Exception {
                    return transformGameServerToServer(browser.refresh());
                }
            });
            updateStatus(i, browsers.size());
        }
        return i;
    }

    @Override
    protected int getTotalTasksCount() {
        return browsers.size();
    }

}
