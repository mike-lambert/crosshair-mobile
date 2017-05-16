package cyberspacelabs.ru.crosshairmobile;

import android.app.Application;

import cyberspacelabs.ru.crosshairmobile.controllers.ServerListController;

/**
 * Created by mike on 16.05.17.
 */
public class ApplicationState extends Application {
    private ServerListController serverListController;

    @Override
    public void onCreate() {
        super.onCreate();
        serverListController = new ServerListController();
    }

    public ServerListController getServerListController() {
        return serverListController;
    }
}
