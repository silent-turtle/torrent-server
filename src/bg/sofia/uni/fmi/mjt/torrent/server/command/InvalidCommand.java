package bg.sofia.uni.fmi.mjt.torrent.server.command;

public class InvalidCommand implements Command {
    private String message;

    public InvalidCommand(String message) {
        this.message = message;
    }

    @Override
    public String executeCommand() {
        return message;
    }
}
