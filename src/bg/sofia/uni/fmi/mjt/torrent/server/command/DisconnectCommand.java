package bg.sofia.uni.fmi.mjt.torrent.server.command;

import bg.sofia.uni.fmi.mjt.torrent.server.TorrentServer;

import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

public class DisconnectCommand implements Command {
    private String name;
    private TorrentServer torrentServer;

    public DisconnectCommand(String name, TorrentServer torrentServer) {
        this.name = name;
        this.torrentServer = torrentServer;
    }

    @Override
    public String executeCommand() {
        Map<String, InetSocketAddress> users = torrentServer.getUsers();
        Map<String, Set<Path>> files = torrentServer.getFiles();

        users.remove(name);
        files.remove(name);
        return "Disconnected.";
    }
}
