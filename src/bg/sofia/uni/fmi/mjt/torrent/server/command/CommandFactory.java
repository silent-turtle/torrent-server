package bg.sofia.uni.fmi.mjt.torrent.server.command;

import bg.sofia.uni.fmi.mjt.torrent.server.TorrentServer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class CommandFactory {

    public static Command getCommand(TorrentServer torrentServer, String command) {
        if (command == null) {
            return null;
        }

        String[] split = command.split("\\s+|" + System.lineSeparator(), 3);
        String commandType = split[0];

        if (commandType.equalsIgnoreCase("register")) {
            if (split.length < 3) {
                return new InvalidCommand("Missing arguments");
            }

            String name = split[1];

            String[] files = split[2].split("\\s*,\\s*|" + System.lineSeparator());
            Set<String> fileNames = new HashSet<>(Arrays.asList(files));

            return new RegisterCommand(name, fileNames, torrentServer);
        }

        if (commandType.equalsIgnoreCase("unregister")) {
            if (split.length < 3) {
                return new InvalidCommand("Missing arguments");
            }

            String name = split[1];

            String[] files = split[2].split("\\*,\\*|" + System.lineSeparator());
            Set<String> fileNames = new HashSet<>(Arrays.asList(files));

            return new UnregisterCommand(name, fileNames, torrentServer);
        }

        if (commandType.equalsIgnoreCase("list-files")) {
            return new ListFilesCommand(torrentServer);
        }

        if (commandType.equalsIgnoreCase("list-users")) {
            return new ListUsersCommand(torrentServer);
        }

        if (commandType.equalsIgnoreCase("update")) {
            if (split.length < 3) {
                return new InvalidCommand("Missing arguments.");
            }

            String oldNickname = split[1];
            String newNickname = split[2];

            return new UpdateNameCommand(oldNickname, newNickname, torrentServer);
        }

        if (commandType.equalsIgnoreCase("name-check")) {
            if (split.length < 2) {
                return new InvalidCommand("Missing argument.");
            }
            String name = split[1];

            return new NameCheckCommand(name, torrentServer);
        }

        if (commandType.equalsIgnoreCase("file-check")) {
            if (split.length < 3) {
                return new InvalidCommand("Missing argument.");
            }

            String name = split[1];
            String fileName = split[2];

            return new FileCheckCommand(name, fileName, torrentServer);
        }

        if (commandType.equalsIgnoreCase("disconnect")) {
            if (split.length < 2) {
                return new InvalidCommand("Missing argument.");
            }

            String name = split[1];
            return new DisconnectCommand(name, torrentServer);
        }

        return new InvalidCommand("Wrong command.");
    }
}
