package bg.sofia.uni.fmi.mjt.torrent.server.command;

import bg.sofia.uni.fmi.mjt.torrent.server.TorrentServer;

import java.net.InetSocketAddress;
import java.util.Map;

public class NameCheckCommand implements Command {
    private String name;
    private TorrentServer torrentServer;

    public NameCheckCommand(String name, TorrentServer torrentServer) {
        this.name = name;
        this.torrentServer = torrentServer;
    }

    @Override
    public String executeCommand() {
        Map<String, InetSocketAddress> users = torrentServer.getUsers();

        if (users.containsKey(name)) {
            return "Nickname already exists.";
        }
        return "Nickname doesn't exist.";
    }
}
