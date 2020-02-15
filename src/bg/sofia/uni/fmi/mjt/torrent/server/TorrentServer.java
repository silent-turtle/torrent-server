package bg.sofia.uni.fmi.mjt.torrent.server;

import bg.sofia.uni.fmi.mjt.torrent.server.command.CommandFactory;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;

public class TorrentServer {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 7777;
    private static final int BUFFER_SIZE = 256;

    private Map<String, Set<Path>> files = new HashMap<>();
    private Map<String, InetSocketAddress> users = new HashMap<>();
    private ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
    private SocketChannel userSocketChannel;
    private Selector selector;
    private ServerSocketChannel serverSocketChannel;

    public TorrentServer() {
        this(null);
    }

    public TorrentServer(SocketChannel userSocketChannel) {
        this.userSocketChannel = userSocketChannel;
    }

    public static void main(String[] args) {
        TorrentServer torrentServer = new TorrentServer();
        try {
            torrentServer.init();

            while (true) {
                if (torrentServer.selector.select() == 0) {
                    continue;
                }

                Set<SelectionKey> selectionKeys = torrentServer.selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();

                while (iterator.hasNext()) {

                    SelectionKey key = iterator.next();

                    if (key.isAcceptable()) {
                        SocketChannel client = torrentServer.serverSocketChannel.accept();
                        client.configureBlocking(false);
                        client.register(torrentServer.selector, SelectionKey.OP_READ);
                    } else if (key.isReadable()) {
                        torrentServer.userSocketChannel = (SocketChannel) key.channel();

                        String command = torrentServer.readMessage();
                        String reply = torrentServer.handleCommand(command);
                        torrentServer.sendMessage(reply);


                        if (reply.equals("Disconnected.")) {
                            key.cancel();
                            torrentServer.userSocketChannel.close();
                        }
                    }
                }

                iterator.remove();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void init() throws IOException {
        selector = Selector.open();
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind((new InetSocketAddress(SERVER_HOST, SERVER_PORT)));
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    public void sendMessage(String message) throws IOException {
        if (userSocketChannel == null) {
            throw new ConnectException("No connection.");
        }

        if (message.equals("")) {
            userSocketChannel.close();
            return;
        }

        buffer.clear();
        buffer.put(message.getBytes());
        buffer.flip();
        userSocketChannel.write(buffer);
    }

    public String readMessage() throws IOException {
        if (userSocketChannel == null) {
            throw new ConnectException("No connection.");
        }

        buffer.clear();
        userSocketChannel.read(buffer);
        buffer.flip();

        return new String(buffer.array(), 0,
                buffer.limit(), StandardCharsets.UTF_8);
    }

    public String handleCommand(String command) {
       return CommandFactory.getCommand(this, command).executeCommand();
    }

    public void addUser(String name) {
        boolean isRegistered = false;

        for (String user : users.keySet()) {
            if (user.equals(name)) {
                isRegistered = true;
                break;
            }
        }

        if (!isRegistered) {
            users.put(name, new InetSocketAddress(userSocketChannel.socket()
                    .getInetAddress(), userSocketChannel.socket().getPort()));
        }
    }

    public void addFiles(Set<String> fileNames, String name) {
        Set<Path> userFiles = files.get(name);
        Set<Path> temp;
        if (userFiles == null) {
            temp = new HashSet<>();
        } else {
            temp = userFiles;
        }

        for (String file : fileNames) {
            Path path = Path.of(file);
            temp.add(path);
        }
        if (userFiles == null) {
            files.put(name, temp);
        }
    }

    public Map<String, InetSocketAddress> getUsers() {
        return users;
    }

    public Map<String, Set<Path>> getFiles() {
        return files;
    }
}
