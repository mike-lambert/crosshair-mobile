package cyberspacelabs.ru.crosshairmobile;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

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

    public static ApplicationState getInstance(Context context) {
        ApplicationState app =null;
        if (context instanceof ApplicationState){
            app = (ApplicationState) context;
        } else {
            app = (ApplicationState) context.getApplicationContext();
        }
        return app;
    }

    public SharedPreferences getDefaultPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(this);
    }
}
