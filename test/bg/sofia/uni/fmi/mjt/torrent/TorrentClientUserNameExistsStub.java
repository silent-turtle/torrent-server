package bg.sofia.uni.fmi.mjt.torrent;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public class TorrentClientUserNameExistsStub extends  TorrentClientStub{
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
