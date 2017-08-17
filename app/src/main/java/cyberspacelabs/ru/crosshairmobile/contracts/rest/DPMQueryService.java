package cyberspacelabs.ru.crosshairmobile.contracts.rest;

import java.util.List;

import cyberspacelabs.ru.crosshairmobile.darkplaces.GameServer;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by mike on 17.08.17.
 * A base Retrofit contract for DPMQuery-based API servers
 */
public interface DPMQueryService {
    /***
     * Queries DPMQuery API server
     * @param endpoint master server UDP endpoint, e.g. "dpmaster.deathmask.net:27950" - port is obligatory
     * @param query master server query string, e.g. "getservers 68 full empty"
     * @return list of servers, running queried game
     */
    @GET("/api/v1/master/query/{address}/{query}")
    Call<List<GameServer>> queryMasterServer(
            @Path("address")
            String endpoint,

            @Path("query")
            String query
    );
}
