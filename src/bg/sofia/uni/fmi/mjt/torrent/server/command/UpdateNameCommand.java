package bg.sofia.uni.fmi.mjt.torrent.server.command;

import bg.sofia.uni.fmi.mjt.torrent.server.TorrentServer;

import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

public class UpdateNameCommand implements Command {
    private String oldNickname;
    private String newNickname;
    private TorrentServer torrentServer;

    public UpdateNameCommand(String oldNickname, String newNickname, TorrentServer torrentServer) {
        this.oldNickname = oldNickname;
        this.newNickname = newNickname;
        this.torrentServer = torrentServer;
    }

    @Override
    public String executeCommand() {
        Map<String, InetSocketAddress> users = torrentServer.getUsers();
        Map<String, Set<Path>> files = torrentServer.getFiles();

        users.put(newNickname, users.get(oldNickname));
        users.remove(oldNickname);

        files.put(newNickname, files.get(oldNickname));
        files.remove(oldNickname);

        return "Nickname updated successfully." + System.lineSeparator();
    }
}
