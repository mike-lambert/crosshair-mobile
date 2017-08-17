package cyberspacelabs.ru.crosshairmobile.services.factory;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import cyberspacelabs.ru.crosshairmobile.ApplicationState;
import cyberspacelabs.ru.crosshairmobile.contracts.rest.DPMQueryService;
import retrofit.converter.JacksonConverter;
import retrofit2.Retrofit;

/**
 * Created by mike on 17.08.17.
 */
public class DPMQueryServiceFactory {
    private static DPMQueryServiceFactory instance;

    private String baseURL;
    private DPMQueryService proxy;

    private DPMQueryServiceFactory(Context context) {
        SharedPreferences preferences = ApplicationState.getInstance(context).getDefaultPreferences();
        baseURL = preferences.getString("discovery_server_address", "");
        proxy = new Retrofit.Builder()
                .baseUrl(baseURL)
                .build()
                .create(DPMQueryService.class);
    }

    public DPMQueryService getProxy(){
        return proxy;
    }

    public static DPMQueryServiceFactory getInstance(Context context) {
        if (instance == null){
            synchronized (DPMQueryServiceFactory.class){
                if (instance == null) {
                    instance = new DPMQueryServiceFactory(context);
                }
            }
        }
        return instance;
    }
}
