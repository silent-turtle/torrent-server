package bg.sofia.uni.fmi.mjt.torrent.server.command;

import bg.sofia.uni.fmi.mjt.torrent.server.TorrentServer;

import java.util.Set;

public class RegisterCommand implements Command {
    private TorrentServer torrentServer;
    private String name;
    private Set<String> filePaths;

    public RegisterCommand(String name, Set<String> filePaths,
                           TorrentServer torrentServer) {
        this.torrentServer = torrentServer;
        this.name = name;
        this.filePaths = filePaths;
    }

    @Override
    public String executeCommand() {
        torrentServer.addUser(name);
        torrentServer.addFiles(filePaths, name);

        return "Files registered.";
    }
}
