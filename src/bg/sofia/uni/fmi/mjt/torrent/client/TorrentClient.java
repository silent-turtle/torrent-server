package bg.sofia.uni.fmi.mjt.torrent.client;

import java.io.*;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TorrentClient {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 7777;
    private static final int BUFFER_SIZE = 256;
    private static ByteBuffer serverBuffer = ByteBuffer.allocate(BUFFER_SIZE);
    private static ByteBuffer miniServerBuffer = ByteBuffer.allocate(BUFFER_SIZE);

    private MiniServer miniServer;
    private LocalMapping localMapping;
    private Path localMappingFile;
    private String localFileName;
    private SocketChannel client;
    private SocketChannel peer;
    private String nickname;
    private Scanner scanner;

    public TorrentClient() {
        this(null, null);
    }

    public TorrentClient(SocketChannel client, String localFileName) {
        this.client = client;
        this.localFileName = localFileName;
    }

    public static void main(String[] args) {
        try {
            Path path = createTempFile();

            TorrentClient torrentClient = new TorrentClient();
            torrentClient.init(path);

            while (true) {
                String message = torrentClient.scanner.nextLine();
                try {
                    String reply = torrentClient.handleCommand(message);
                    System.out.println(reply);

                    if (reply.equals("Disconnected.")) {
                        Files.deleteIfExists(path);
                        torrentClient.stop();
                        break;
                    }
                } catch (NicknameAlreadyExistsException | FileAlreadyExistsException |
                        IllegalArgumentException | FileNotFoundException e) {
                    System.out.println(e.getMessage());
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Path createTempFile() throws IOException {
        return Files.createTempFile(Path.of("resources"), "user", ".txt");
    }

    private void init(Path path) throws IOException {
        client = SocketChannel.open();
        scanner = new Scanner(System.in);

        client.connect(new InetSocketAddress(SERVER_HOST, SERVER_PORT));

        nickname = null;
        localMappingFile = path;
        localFileName = localMappingFile.getFileName().toString().split("\\.")[0];
        miniServer = new MiniServer(client.socket().getLocalPort() + 1);
        localMapping = new LocalMapping(localMappingFile);

        miniServer.start();
        localMapping.start();

        System.out.println("Connected to the server.");
    }

    public void sendMessage(String message) throws IOException {
        if (client == null) {
            throw new ConnectException("No connection.");
        }

        serverBuffer.clear();
        serverBuffer.put(message.getBytes());
        serverBuffer.flip();

        OutputStream out = client.socket().getOutputStream();
        out.write(message.getBytes());
    }

    public String readMessage() throws IOException {
        if (client == null) {
            throw new ConnectException("No connection.");
        }

        serverBuffer.clear();
        InputStream in = client.socket().getInputStream();
        int a = in.read(serverBuffer.array());

        return new String(serverBuffer.array(), 0,
                a, StandardCharsets.UTF_8);
    }

    public String handleCommand(String command) throws IOException, NicknameAlreadyExistsException, IllegalArgumentException {
        String[] split = command.split("\\s+|" + System.lineSeparator());

        if (split[0].equals("register")) {
            return handleRegister(command);
        }

        if (split[0].equals("download")) {
            return handleDownload(command);
        }

        sendMessage(command);
        return readMessage();
    }

    private String handleRegister(String command) throws IOException, NicknameAlreadyExistsException {
        String[] split = command.split("\\s+|" + System.lineSeparator(), 3);
        updateNickname(split[1]);

        String[] fileSplit = split[2].split("\\s*,\\s*|" + System.lineSeparator());

        for (String file : fileSplit) {
            if (!Files.exists(Path.of(file))) {
                throw new FileNotFoundException("File " + file +
                        " doesn't exist.");
            }
        }

        return sendRegister(split[0] + " " + nickname + " " + split[2]);
    }

    public String sendRegister(String command) throws IOException {
        String[] split = command.split("\\s+|" + System.lineSeparator(), 3);
        if (split[0].equals("register")) {
            sendMessage(split[0] + " " + nickname + " " + split[2]);

            return readMessage();
        }

        return "Wrong command.";
    }

    private void updateNickname(String name) throws IOException, NicknameAlreadyExistsException {
        if (nickname == null) {
            String reply = sendNameCheck(name);

            if (reply.equals("Nickname already exists.")) {
                throw new NicknameAlreadyExistsException("Cannot use " +
                        "this nickname.");
            }

            nickname = name;
            return;
        }

        if (nickname.equals(localFileName)) {
            String reply = sendNameCheck(name);

            if (reply.equals("Nickname already exists.")) {
                throw new NicknameAlreadyExistsException("Cannot use " +
                        "this nickname.");
            }

            String old = nickname;
            reply = sendUpdateName(name, old);

            if (reply.equals("Nickname updated successfully.")) {
                nickname = name;
                return;
            }
        }

        if (name.equals(localFileName)) {
            return;
        }

        if (!nickname.equals(name)) {
            throw new NicknameAlreadyExistsException("Cannot use " +
                    "this nickname.");
        }
    }

    public String sendUpdateName(String name, String old) throws IOException {
        String reply;
        sendMessage("update " + old + " " + name);
        reply = readMessage();
        return reply;
    }

    public String sendNameCheck(String name) throws IOException {
        sendMessage("name-check " + name);
        return readMessage();
    }

    private String handleDownload(String command) throws IOException,
            NicknameAlreadyExistsException, IllegalArgumentException {
        String[] split = command.split("\\s+|" + System.lineSeparator());

        sendMessage("file-check " + split[1] + " " + split[2]);
        String reply = readMessage();
        if (reply.equals("File is not registered.")
                || reply.equals("User doesn't exist.")) {
            throw new IllegalArgumentException(reply);
        }

        peer = SocketChannel.open();
        InetSocketAddress inetSocketAddress = getInetSocketAddress(split[1]);
        peer.connect(inetSocketAddress);

        miniServerBuffer.clear();
        miniServerBuffer.put(command.getBytes());
        miniServerBuffer.flip();
        peer.write(miniServerBuffer);

        try {
            getFile(split[3]);

            String[] getUserFileName = localMappingFile.getFileName().toString().split("\\.");
            String name = getUserFileName[0];

            handleRegister("register " + name + " " + split[3]);

        } catch (IOException | NicknameAlreadyExistsException e) {
            peer.close();
            throw e;
        }
        peer.close();

        return "File downloaded successfully.";
    }

    private InetSocketAddress getInetSocketAddress(String string) throws
            IOException, IllegalArgumentException {

        Stream<String> stream = Files.lines(localMappingFile);
        String user = stream.filter(s -> s.substring(0, string.length())
                .equals(string)).collect(Collectors.joining());

        if (user.equals("")) {
            throw new IllegalArgumentException("User " + string +
                    " doesn't exist.");
        }
        String[] split = user.split("\\s+|" + System.lineSeparator());

        return new InetSocketAddress(SERVER_HOST, Integer.parseInt(split[2]) + 1);
    }

    private void getFile(String pathname) throws IOException {
        if (Files.exists(Path.of(pathname))) {
            throw new FileAlreadyExistsException("File " + pathname +
                    " already exists.");
        }
        File file = new File(pathname);
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));

        ByteBuffer sizeBuffer = ByteBuffer.allocate(4);

        sizeBuffer.clear();
        peer.read(sizeBuffer);
        sizeBuffer.flip();

        int size = sizeBuffer.getInt();

        if (size != 0) {
            miniServerBuffer.clear();
            int a;
            do {
                a = peer.read(miniServerBuffer);
                miniServerBuffer.flip();
                out.write(miniServerBuffer.array(), 0, a);
                miniServerBuffer.clear();
            } while (a == BUFFER_SIZE);

            out.flush();
        }
        out.close();
    }

    public String getNickname() {
        return nickname;
    }

    private void stop() {
        miniServer.terminate();
        localMapping.terminate();
    }
}
