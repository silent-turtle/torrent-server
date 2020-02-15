package bg.sofia.uni.fmi.mjt.torrent.client;

import bg.sofia.uni.fmi.mjt.torrent.client.TorrentClientStub;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public class TorrentClientUserNameExistsStub extends TorrentClientStub {
    TorrentClientUserNameExistsStub() {
        this(null, null);
    }

    TorrentClientUserNameExistsStub(SocketChannel socketChannel, String localFileName) {
        super(socketChannel, localFileName);
    }


    @Override
    public String sendNameCheck(String name) throws IOException {
        return "Nickname already exists." + System.lineSeparator();
    }

}
