package bg.sofia.uni.fmi.mjt.torrent.client;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;

public class LocalMapping extends Thread {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 7777;
    private static final int BUFFER_SIZE = 256;
    private static ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

    private Path localMappingFile;
    private boolean running;

    public LocalMapping(Path localMappingFile) {
        this.localMappingFile = localMappingFile;
    }

    public void terminate() {
        running = false;
    }

    @Override
    public void run() {
        running = true;
        try (SocketChannel socketChannel = SocketChannel.open()) {
            socketChannel.connect(new InetSocketAddress(SERVER_HOST, SERVER_PORT));

            while (running) {

                Thread.sleep(30000);

                buffer.clear();
                buffer.put("list-users".getBytes());
                buffer.flip();
                socketChannel.write(buffer);

                buffer.clear();
                if (socketChannel.read(buffer) > 0) {
                    buffer.flip();

                    try (FileOutputStream fileOutputStream = new FileOutputStream
                            (localMappingFile.toFile(), false)) {
                        fileOutputStream.write(buffer.array(), 0, buffer.limit());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        } catch (IOException |
                InterruptedException e) {
            e.printStackTrace();
        }
    }
}


