package cyberspacelabs.ru.crosshairmobile.darkplaces;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by mzakharov on 01.02.17.
 */
public class GameBrowser {
    public static final String DPMASTER = "dpmaster.deathmask.net:27950";
    private static final String CHALLENGE_CHARSET = "qwertyuiopasdfghjklzxcvbnm1234567890QWERTYUIOPASDFGHJKLZXCVBNM";
    private static final String MARKER_END_SERVERS = "454F54000000"; // EOT\\x00\\x00\\x00
    public static final String QUERY_OPENARENA_DEFAULT = "getservers 71 empty full demo";
    public static final String QUERY_XONOTIC_DEFAULT = "getservers Xonotic 3 empty full";
    public static final String QUERY_QUAKE_3_ARENA_DEFAULT = "getservers 68 empty full";
    private static final int PACKET_SIZE = 1400;

    private final AtomicLong threadCounter = new AtomicLong(0);
    private final ExecutorService threadPool = Executors.newCachedThreadPool(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setName(GameBrowser.class.getSimpleName() + "-Worker::" + threadCounter.incrementAndGet());
            t.setDaemon(true);
            return t;
        }
    });
    private final ExecutorCompletionService<GameServer> completionService = new ExecutorCompletionService<GameServer>(threadPool);
    private String masterAddress;
    private String masterQuery;
    private DatagramSocket client;
    private long completionTimeout;
    private String game;

    public GameBrowser(){
        this(DPMASTER, QUERY_OPENARENA_DEFAULT);
        game = "OpenArena";
    }

    public GameBrowser(String masterAddress, String masterQuery){
        setMasterAddress(masterAddress);
        setMasterQuery(masterQuery);
        completionTimeout = 1000;
    }

    public long getCompletionTimeout() {
        return completionTimeout;
    }

    public GameBrowser setCompletionTimeout(long completionTimeout) {
        this.completionTimeout = completionTimeout;
        return this;
    }

    public String getMasterAddress() {
        return masterAddress;
    }

    public GameBrowser setMasterAddress(String masterAddress) {
        this.masterAddress = masterAddress;
        return this;
    }

    public String getMasterQuery() {
        return masterQuery;
    }

    public GameBrowser setMasterQuery(String masterQuery) {
        this.masterQuery = masterQuery;
        return this;
    }

    public String getGame() {
        return game;
    }

    public GameBrowser setGame(String game) {
        this.game = game;
        return this;
    }

    public Set<GameServer> refresh() {
        Set<GameServer> result = new HashSet<>();
        try {
            //System.out.println(new Date() + " Contacting master");
            provideSocket();
            //System.out.println(new Date() + " Querying master");
            sendClientCommand(getMasterQuery());
            //System.out.println(new Date() + " Awaiting master response");
            List<String> servers = awaitServersList();
            System.out.println(new Date() + " Got " + servers.size() + " address(es) from master");
            for(String entry : servers){
                completionService.submit(getServerInfoTask(entry));
            }

            int tasks = servers.size();
            do{
                try {
                    Future<GameServer> completed = completionService.poll(completionTimeout, TimeUnit.MILLISECONDS);
                    try {
                        if (completed != null){
                            tasks--;
                            GameServer server = completed.get();
                            if (server != null) {
                                result.add(server);
                            }
                        } else {
                            break;
                        }
                    } catch (ExecutionException e) {
                        //e.printStackTrace();
                    }
                } catch (InterruptedException e){
                    e.printStackTrace();
                    break;
                }
            }while(true);
        } catch (SocketException e) {
            e.printStackTrace();
            client = null;
        } catch (UnknownHostException e) {
            e.printStackTrace();
            client = null;
        } catch (IOException e) {
            e.printStackTrace();
            client = null;
        }
        return result;
    }

    private void provideSocket() throws SocketException, UnknownHostException {
        if (client == null){
            client = new DatagramSocket();
        }
        if (!client.isConnected()){
            String[] ac = getMasterAddress().split(":");
            String saddress = ac[0];
            String sport = ac[1];
            int port = Integer.parseInt(sport);
            InetAddress address = InetAddress.getByName(saddress);
            client.connect(address, port);
        }
    }

    private void sendMasterCommand(String command, DatagramSocket socket) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(command.length() + 4);
        buffer.put((byte)0xFF);
        buffer.put((byte)0xFF);
        buffer.put((byte)0xFF);
        buffer.put((byte)0xFF);
        buffer.put(command.getBytes("ASCII"));
        byte[] array = buffer.array();
        DatagramPacket packet = new DatagramPacket(array, array.length);
        socket.send(packet);
    }

    private void sendClientCommand(String command) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(command.length() + 5);
        buffer.put((byte)0xFF);
        buffer.put((byte)0xFF);
        buffer.put((byte)0xFF);
        buffer.put((byte)0xFF);
        buffer.put(command.getBytes("ASCII"));
        buffer.put((byte)0x0A);
        byte[] array = buffer.array();
        DatagramPacket packet = new DatagramPacket(array, array.length);
        client.send(packet);
    }

    private List<String> awaitServersList() throws IOException {
        byte[] buffer = null;
        DatagramPacket rd = null;
        List<String> result = new ArrayList<>();
        boolean finalPacket = false;
        do {
            buffer = new byte[PACKET_SIZE];
            rd = new DatagramPacket(buffer, buffer.length);
            client.receive(rd);
            if (rd.getData() != null && rd.getData().length > 0){
                // getserversResponse + header = 22
                // each entry starts with \ then 6 bytes (IPv4 + port)
                int index = 22;
                byte[] entry = new byte[7];
                do {
                    System.arraycopy(rd.getData(), index, entry, 0, 7);
                    //System.out.println(new Date() + " RCV: " + hex(entry));
                    if (hex(entry).toUpperCase().contains(MARKER_END_SERVERS)){
                        finalPacket = true;
                        break;
                    }
                    int port = ((entry[5] & 0xff) << 8 | (entry[6] & 0xff));
                    StringBuilder address = new StringBuilder();
                    address.append(Integer.toString(entry[1] & 0xFF)).append('.')
                            .append(Integer.toString(entry[2] & 0xFF)).append('.')
                            .append(Integer.toString(entry[3] & 0xFF)).append('.')
                            .append(Integer.toString(entry[4] & 0xFF)).append(':')
                            .append(Integer.toString(port));
                    result.add(address.toString());
                    index += 7;
                } while(true && index + 6 < rd.getData().length);
            }
        }while (rd.getData() != null && rd.getData().length > 0 && !finalPacket);
        return result;
    }

    private String awaitInfoResponse(DatagramSocket socket) throws IOException {
        byte[] buffer = new byte[PACKET_SIZE];
        DatagramPacket rd = new DatagramPacket(buffer, buffer.length);
        socket.receive(rd);
        if (rd.getData() != null && rd.getData().length > 0){
            //System.out.println(new Date() + " RCV: " + hex(rd.getData()));
            // \xFF\xFF\xFF\xFFinfoResponse\x0A = 18;
            // each entry starts with \ then 6 bytes (IPv4 + port)
            int index = 18;
            int end = firstZero(rd.getData());
            int strlen = end - index;
            if (strlen > 0){
                byte[] strbuf = new byte[strlen];
                System.arraycopy(rd.getData(), index, strbuf, 0, strlen);
                String result = new String(strbuf, "ASCII");
                //System.out.println(result);
                return result;
            }
        }
        return "";
    }

    private Callable<GameServer> getServerInfoTask(final String entry) {
        return new Callable<GameServer>() {
            @Override
            public GameServer call() throws Exception {
                GameServer result = new GameServer();
                try {
                    long started = System.currentTimeMillis();
                    //System.out.println(new Date() + " Contacting zone " + entry);
                    String[] addrcomponents = entry.split(":");
                    int port = Integer.parseInt(addrcomponents[1]);
                    DatagramSocket socket = new DatagramSocket();
                    socket.connect(InetAddress.getByName(addrcomponents[0]), port);
                    String cmd = "getinfo " + createChallenge();
                    //System.out.println(new Date() + " Querying zone: " + entry + ": \"" + cmd + "\"");
                    sendMasterCommand(cmd, socket);
                    String resp = awaitInfoResponse(socket);
                    long completed = System.currentTimeMillis();
                    //System.out.println(new Date() + " Zone answer  : " + entry + ": \"" + resp + "\"");
                    String[] tokens = resp.split("\\\\");
                    Map<String, String> properties = new HashMap<String, String>();
                    int i = 0;
                    do{
                        properties.put(tokens[i], tokens[i+1]);
                        i += 2;
                    } while(i < tokens.length);
                    long ping = completed - started;
                    properties.put("ping", new Long(ping).toString());

                    result.setGame(game);
                    result.setAddress(entry);
                    result.setDisplayName(properties.get("hostname"));
                    String oagt = properties.get("game");
                    String xgt = properties.get("qcstatus");
                    if (xgt != null){
                        int colon = xgt.indexOf(":");
                        if (colon != -1 ){
                            xgt = xgt.substring(0, colon).trim();
                        }
                    }
                    String gt = xgt != null ? xgt : oagt;
                    result.setGameType((gt == null ? "ffa" : gt));
                    result.setMap(properties.get("mapname"));
                    try {
                        result.setPlayersPresent(Integer.parseInt(properties.get("g_humanplayers")));
                    } catch (NumberFormatException nfx1){
                        result.setPlayersPresent(Integer.parseInt(properties.get("clients")));
                    }
                    result.setRequestDuration(ping);
                    result.setServerProtocol(Integer.parseInt(properties.get("protocol")));
                    result.setSlotsAvailable(Integer.parseInt(properties.get("sv_maxclients")));
                } catch (Exception e){
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    e.printStackTrace(pw);
                    //System.out.println(new Date() + " <- " + Thread.currentThread().getName() +  ": Zone fault  : " + entry + "\r\n" + sw.toString());
                    result = null;
                }
                return result;
            }
        };
    }

    private static String createChallenge(){
        StringBuilder sb = new StringBuilder();
        Random r = new Random();
        for(int i = 0; i < 8; i++){
            int rnd = r.nextInt();
            if (rnd < 0){ rnd = rnd * -1; }
            char c = CHALLENGE_CHARSET.charAt(rnd % CHALLENGE_CHARSET.length());
            sb.append(c);
        }
        return sb.toString();
    }

    private static String hex(byte[] arr){
        StringBuilder sb = new StringBuilder();
        for(byte b : arr){ sb.append(String.format("%02x", b));}
        return sb.toString();
    }

    private static int firstZero(byte[] data) {
        for(int i = 0; i < data.length; i++){
            if (data[i] == 0){
                return i;
            }
        }
        return -1;
    }
}
