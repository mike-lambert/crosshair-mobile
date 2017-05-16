package cyberspacelabs.ru.crosshairmobile.dto;

/**
 * Created by mzakharov on 10.05.17.
 */
public class Server {
    private String name;
    private String location;
    private long ping;
    private int slots;
    private int players;
    private String game;
    private String mode;
    private String address;
    private String map;

    public String getName() {
        return name;
    }

    public Server setName(String name) {
        this.name = name;
        return this;
    }

    public String getLocation() {
        return location;
    }

    public Server setLocation(String location) {
        this.location = location;
        return this;
    }

    public long getPing() {
        return ping;
    }

    public Server setPing(long ping) {
        this.ping = ping;
        return this;
    }

    public int getSlots() {
        return slots;
    }

    public Server setSlots(int slots) {
        this.slots = slots;
        return this;
    }

    public int getPlayers() {
        return players;
    }

    public Server setPlayers(int players) {
        this.players = players;
        return this;
    }

    public String getGame() {
        return game;
    }

    public Server setGame(String game) {
        this.game = game;
        return this;
    }

    public String getMode() {
        return mode;
    }

    public Server setMode(String mode) {
        this.mode = mode;
        return this;
    }

    public String getAddress() {
        return address;
    }

    public Server setAddress(String address) {
        this.address = address;
        return this;
    }

    public String getMap() {
        return map;
    }

    public Server setMap(String map) {
        this.map = map;
        return this;
    }
}
