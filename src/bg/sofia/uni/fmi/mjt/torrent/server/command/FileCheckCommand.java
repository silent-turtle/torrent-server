package bg.sofia.uni.fmi.mjt.torrent.server.command;

import bg.sofia.uni.fmi.mjt.torrent.server.TorrentServer;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

public class FileCheckCommand implements Command {
    private String name;
    private String fileName;
    private TorrentServer torrentServer;

    public FileCheckCommand(String name, String fileName, TorrentServer torrentServer) {
        this.name = name;
        this.fileName = fileName;
        this.torrentServer = torrentServer;
    }

    @Override
    public String executeCommand() {
        Map<String, Set<Path>> files = torrentServer.getFiles();

        if (!files.containsKey(name)) {
            return "User doesn't exist.";
        }

        Set<Path> userFiles = files.get(name);

        if (!userFiles.contains(Path.of(fileName))) {
            return "File is not registered.";
        }

        return "File is registered.";
    }
}
