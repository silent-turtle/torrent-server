package bg.sofia.uni.fmi.mjt.torrent.server;

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
    public SocketChannel userSocketChannel;
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


                        if (reply.equals("Disconnected." + System.lineSeparator())) {
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
            throw new ConnectException("No connection." + System.lineSeparator());
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
            throw new ConnectException("No connection." + System.lineSeparator());
        }

        buffer.clear();
        userSocketChannel.read(buffer);
        buffer.flip();

        return new String(buffer.array(), 0,
                buffer.limit(), StandardCharsets.UTF_8);
    }

    public String handleCommand(String command) {
        String[] split = command.split("\\s+|" + System.lineSeparator(), 3);

        if (split[0].equals("register")) {
            return executeRegisterCommand(command);
        }
        if (split[0].equals("unregister")) {
            return executeUnregisterCommand(command);
        }
        if (split[0].equals("list-files")) {
            return executeListFilesCommand();
        }
        if (split[0].equals("list-users")) {
            return executeListUsersCommand();
        }
        if (split[0].equals("update")) {
            return executeUpdateCommand(split);
        }
        if (split[0].equals("name-check")) {
            return executeNameCheckCommand(split[1]);
        }
        if (split[0].equals("file-check")) {
            return executeFileCheckCommand(split);
        }
        if (split[0].equals("disconnect")) {
            return executeDisconnectCommand(split[1]);
        }

        return "Wrong command." + System.lineSeparator();
    }

    private String executeDisconnectCommand(String s) {
        users.remove(s);
        files.remove(s);
        return "Disconnected." + System.lineSeparator();
    }

    private String executeRegisterCommand(String command) {
        String[] split = command.split("\\s+|" + System.lineSeparator(), 3);
        String name = split[1];

        addFiles(split[2], name);
        addUser(name);

        return "Files registered." + System.lineSeparator();
    }

    private String executeUnregisterCommand(String command) {
        String[] split = command.split("\\s+|" + System.lineSeparator(), 3);
        String name = split[1];

        String[] fileNames = split[2].split("\\s*,\\s*|" + System.lineSeparator());

        Set<Path> userFiles = files.get(name);

        if (userFiles == null || userFiles.size() == 0) {
            return "No files." + System.lineSeparator();
        }

        for (int i = 0; i < fileNames.length; ++i) {
            if (!userFiles.contains(Path.of(fileNames[i]))) {
                return "No such file(s)." + System.lineSeparator();
            }
            userFiles.remove(Path.of(fileNames[i]));
        }

        return "Unregistered files." + System.lineSeparator();
    }

    private String executeListFilesCommand() {
        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, Set<Path>> elem : files.entrySet()) {
            for (Path path : elem.getValue()) {
                result.append(elem.getKey()).append(" : ").
                        append(path.toAbsolutePath().toString()).append(System.lineSeparator());
            }
        }

        return result.toString();
    }

    private String executeListUsersCommand() {
        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, InetSocketAddress> elem : users.entrySet()) {
            result.append(elem.getKey()).append(" ").append(elem.getValue().getAddress())
                    .append(" ").append(elem.getValue().getPort()).append(System.lineSeparator());
        }

        return result.toString();
    }

    private String executeUpdateCommand(String[] split) {
        String oldNickname = split[1];
        String newNickname = split[2];

        users.put(newNickname, users.get(oldNickname));
        users.remove(oldNickname);

        files.put(newNickname, files.get(oldNickname));
        files.remove(oldNickname);

        return "Nickname updated successfully." + System.lineSeparator();
    }

    private String executeNameCheckCommand(String s) {
        if (users.containsKey(s)) {
            return "Nickname already exists." + System.lineSeparator();
        }
        return "Nickname doesn't exist." + System.lineSeparator();
    }


    private String executeFileCheckCommand(String[] split) {
        if (!files.containsKey(split[1])) {
            return "User doesn't exist." + System.lineSeparator();
        }

        Set<Path> userFiles = files.get(split[1]);

        if (!userFiles.contains(Path.of(split[2]))) {
            return "File is not registered." + System.lineSeparator();
        }

        return "File is registered." + System.lineSeparator();
    }

    private void addUser(String name) {
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

    private void addFiles(String fileNames, String name) {
        String[] fileSplit = fileNames.split("\\s*,\\s*|" + System.lineSeparator());
        Set<Path> userFiles = files.get(name);
        Set<Path> temp;
        if (userFiles == null) {
            temp = new HashSet<>();
        } else {
            temp = userFiles;
        }

        for (int i = 0; i < fileSplit.length; ++i) {
            Path path = Path.of(fileSplit[i]);
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
