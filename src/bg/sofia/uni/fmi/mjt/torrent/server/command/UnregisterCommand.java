package bg.sofia.uni.fmi.mjt.torrent.server.command;

import bg.sofia.uni.fmi.mjt.torrent.server.TorrentServer;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

public class UnregisterCommand implements Command {
    private String name;
    private Set<String> filePaths;
    private TorrentServer torrentServer;

    public UnregisterCommand(String name, Set<String> filePaths, TorrentServer torrentServer) {
        this.name = name;
        this.filePaths = filePaths;
        this.torrentServer = torrentServer;
    }

    @Override
    public String executeCommand() {
        Map<String, Set<Path>> files = torrentServer.getFiles();

        Set<Path> userFiles = files.get(name);

        if (userFiles == null || userFiles.size() == 0) {
            return "No files.";
        }

        for (String file : filePaths) {
            if (!userFiles.contains(Path.of(file))) {
                return "No such file(s).";
            }
            userFiles.remove(Path.of(file));
        }
        return "Unregistered files.";
    }
}
