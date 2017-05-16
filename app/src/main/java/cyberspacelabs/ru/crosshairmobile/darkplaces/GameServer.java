package cyberspacelabs.ru.crosshairmobile.darkplaces;

/**
 * Created by mzakharov on 31.01.17.
 */
public class GameServer {
    private String address;
    private int serverProtocol;
    private String map;
    private int playersPresent;
    private int slotsAvailable;
    private String displayName;
    private String gameType;
    private long requestDuration;
    private String game;

    public GameServer(){
        setRequestDuration(-1);
    }

    public long getRequestDuration() {
        return requestDuration;
    }

    public void setRequestDuration(long requestDuration) {
        this.requestDuration = requestDuration;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getServerProtocol() {
        return serverProtocol;
    }

    public void setServerProtocol(int serverProtocol) {
        this.serverProtocol = serverProtocol;
    }

    public String getMap() {
        return map;
    }

    public void setMap(String map) {
        this.map = map;
    }

    public int getPlayersPresent() {
        return playersPresent;
    }

    public void setPlayersPresent(int playersPresent) {
        this.playersPresent = playersPresent;
    }

    public int getSlotsAvailable() {
        return slotsAvailable;
    }

    public void setSlotsAvailable(int slotsAvailable) {
        this.slotsAvailable = slotsAvailable;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getGameType() {
        return gameType;
    }

    public void setGameType(String gameType) {
        this.gameType = gameType;
    }

    public boolean isValid(){
        return getRequestDuration() >= 0;
    }

    public String getGame() {
        return game;
    }

    public void setGame(String game) {
        this.game = game;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GameServer that = (GameServer) o;

        if (getServerProtocol() != that.getServerProtocol()) return false;
        return getAddress().equals(that.getAddress());

    }

    @Override
    public int hashCode() {
        int result = getAddress().hashCode();
        result = 31 * result + getServerProtocol();
        return result;
    }
}
