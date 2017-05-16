package cyberspacelabs.ru.crosshairmobile.services;

import android.text.TextUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

import cyberspacelabs.ru.crosshairmobile.contracts.GeoIpService;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by mike on 11.05.17.
 */
public class GeoIpLocationService implements GeoIpService {
    private static class FreeGeoIpResponse {
        public String ip;
        public String country_code;
        public String country_name;
        public String region_code;
        public String region_name;
        public String city;
        public String zip_code;
        public String time_zone;
        public double latitude;
        public double longitude;
        public int metro_code;
    }

    private String cachePath;
    private Map<String, String> cache;
    private ExecutorService cacheSaver;
    private AtomicLong threadCounter;
    private OkHttpClient client;

    public GeoIpLocationService(){
        client = new OkHttpClient();
        cache = new ConcurrentHashMap<>();
        threadCounter = new AtomicLong(0);
        cacheSaver = Executors.newSingleThreadExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread runner = new Thread(r);
                runner.setDaemon(true);
                runner.setName(GeoIpLocationService.class.getSimpleName()
                        + "-Saver::"
                        + new SimpleDateFormat("yyyyMMddHHmmssXXX")
                        + "." + threadCounter.incrementAndGet());
                return runner;
            }
        });

    }

    @Override
    public String locate(String ip) throws Exception {
        String location = cache.get(ip);
        if (location == null){
            InputStream in = client.newCall(new Request.Builder().url("https://freegeoip.net/json/" + ip).build())
                    .execute()
                    .body().byteStream();
            FreeGeoIpResponse response = new ObjectMapper().readValue(in, FreeGeoIpResponse.class);
            location = responseToString(response);
            cache.put(ip, location);
            System.out.println("Cache missing: " + ip + " <- " + location);
            //saveCache();
        } else {
            System.out.println("Cache hit: " + ip + " <- " + location);
        }
        return location;
    }

    private String responseToString(FreeGeoIpResponse response) {
        return response.country_name
                + (response.region_name != null && !response.region_name.trim().isEmpty() ? "/" + response.region_name : "")
                + (response.city != null && !response.city.trim().isEmpty() ? "/" + response.city : "");
    }

    private void saveCache() {
        cacheSaver.submit(new Runnable() {
            @Override
            public void run() {
                File root = new File(cachePath);
                root.mkdirs();
                for(Map.Entry<String, String> entry : cache.entrySet()){
                    String path = entry.getKey().replace(".", "/");
                    File folder = new File(root, path);
                    folder.mkdirs();
                    ObjectMapper json = new ObjectMapper();
                    json.enable(SerializationFeature.INDENT_OUTPUT);
                    File target = new File(folder, entry.getValue());
                    try {
                        json.writeValue(target, entry.getValue());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public void loadCache(){
        ObjectMapper json = new ObjectMapper();
        File root = new File(cachePath);
        root.mkdirs();
        for(File level1 : root.listFiles()){
            if (level1.isDirectory()){
                for(File level2 : level1.listFiles()){
                    if (level2.isDirectory()){
                        for(File level3 : level2.listFiles()){
                            if (level3.isDirectory()){
                                for (File level4 : level3.listFiles()){
                                    if (level4.isDirectory()){
                                        for(File entry : level4.listFiles()){
                                            if (entry.isFile()){
                                                String location = null;
                                                try {
                                                    System.out.println(this.getClass().getSimpleName() + ".loadCache: reading " + entry.getAbsolutePath());
                                                    location = json.readValue(entry, String.class);
                                                    String ip = entry.getParentFile().getCanonicalPath().replace(cachePath, "").replace("/", ".");
                                                    System.out.println(this.getClass().getSimpleName() + ".loadCache: " + ip + " <- " + location);
                                                    cache.put(ip, location);
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        System.out.println("GeoIP cache: " + cache.size());
    }

}
