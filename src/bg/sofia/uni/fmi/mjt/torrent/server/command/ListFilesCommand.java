package bg.sofia.uni.fmi.mjt.torrent.server.command;

import bg.sofia.uni.fmi.mjt.torrent.server.TorrentServer;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

public class ListFilesCommand implements Command {
    private TorrentServer torrentServer;

    public ListFilesCommand(TorrentServer torrentServer) {
        this.torrentServer = torrentServer;
    }

    @Override
    public String executeCommand() {
        StringBuilder result = new StringBuilder();
        Map<String, Set<Path>> files = torrentServer.getFiles();

        for (Map.Entry<String, Set<Path>> elem : files.entrySet()) {
            for (Path path : elem.getValue()) {
                result.append(elem.getKey()).append(" : ").
                        append(path.toAbsolutePath().toString()).append(System.lineSeparator());
            }
        }

        return result.toString();
    }
}
