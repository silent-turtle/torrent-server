package bg.sofia.uni.fmi.mjt.torrent;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public class TorrentClientStub extends TorrentClient {

    TorrentClientStub() {
        this(null, null);
    }

    TorrentClientStub(SocketChannel socketChannel, String localFileName) {
        super(socketChannel, localFileName);
    }


    @Override
    public String sendRegister(String command) throws IOException {
        return "Files registered." + System.lineSeparator();
    }

    @Override
    public String sendUpdateName(String name, String old) throws IOException {
        return "Nickname updated successfully." + System.lineSeparator();
    }

    @Override
    public String sendNameCheck(String name) throws IOException {
        return "Nickname doesn't exist." + System.lineSeparator();
    }
}
