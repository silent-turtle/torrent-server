package bg.sofia.uni.fmi.mjt.torrent.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Set;

public class MiniServer extends Thread {
    private static final String SERVER_HOST = "localhost";
    private static final int BUFFER_SIZE = 256;

    private final InetSocketAddress inetSocketAddress;
    private ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
    private boolean running;

    public MiniServer(int SERVER_PORT) {
        this.inetSocketAddress = new InetSocketAddress(SERVER_HOST, SERVER_PORT);
    }

    public void terminate() {
        running = false;
    }

    @Override
    public void run() {
        running = true;
        try (Selector selector = Selector.open();
             ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {

            serverSocketChannel.bind(this.inetSocketAddress);
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            while (running) {
                if (selector.select() == 0) {
                    continue;
                }

                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();

                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();

                    if (key.isAcceptable()) {
                        SocketChannel client = serverSocketChannel.accept();
                        client.configureBlocking(false);
                        client.register(selector, SelectionKey.OP_READ);

                    } else if (key.isReadable()) {
                        SocketChannel sc = (SocketChannel) key.channel();

                        buffer.clear();
                        sc.read(buffer);
                        buffer.flip();

                        String command = new String(buffer.array(), 0,
                                buffer.limit(), StandardCharsets.UTF_8);

                        String[] split = command.split("\\s+|" + System.lineSeparator());


                        if (split[0].equals("download")) {
                                byte[] content = Files.readAllBytes(Path.of(split[2]));

                                ByteBuffer sizeBuffer = ByteBuffer.allocate(4);

                                sizeBuffer.putInt(content.length);
                                sizeBuffer.flip();
                                sc.write(sizeBuffer);

                                sc.write(ByteBuffer.wrap(content));
                        }

                        key.cancel();
                    }
                }

                iterator.remove();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
