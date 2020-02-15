package bg.sofia.uni.fmi.mjt.torrent.server.command;

import bg.sofia.uni.fmi.mjt.torrent.server.TorrentServer;

import java.net.InetSocketAddress;
import java.util.Map;

public class ListUsersCommand implements Command {
    private TorrentServer torrentServer;

    public ListUsersCommand(TorrentServer torrentServer) {
        this.torrentServer = torrentServer;
    }

    @Override
    public String executeCommand() {
        Map<String, InetSocketAddress> users = torrentServer.getUsers();

        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, InetSocketAddress> elem : users.entrySet()) {
            result.append(elem.getKey()).append(" ").append(elem.getValue().getAddress())
                    .append(" ").append(elem.getValue().getPort()).append(System.lineSeparator());
        }

        return result.toString();
    }
}
