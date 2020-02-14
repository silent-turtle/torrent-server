package bg.sofia.uni.fmi.mjt.torrent;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class TorrentServerTest {
    private static SocketChannel mockSocketChannel = Mockito.mock(SocketChannel.class);
    private static Socket mockSocket = Mockito.mock(Socket.class);
    private static TorrentServer torrentServer;
    private static String userName = "pesho";
    private static String filename = Path.of("resources" + File.separator + "file.txt")
            .toAbsolutePath().toString();
    private static int port = 5555;
    private static InetAddress address = InetAddress.getLoopbackAddress();

    @Before
    public void init() {
        torrentServer = new TorrentServer(mockSocketChannel);

        String command = "register " + userName + " " + filename;

        Mockito.when(mockSocketChannel.socket()).thenReturn(mockSocket);

        Mockito.when(mockSocket.getPort()).thenReturn(port);
        Mockito.when(mockSocket.getInetAddress()).thenReturn(address);

        torrentServer.handleCommand(command);
    }

    @Test
    public void testRegisterAFileSuccessfully() {
        Map<String, InetSocketAddress> users = torrentServer.getUsers();
        Map<String, Set<Path>> files = torrentServer.getFiles();

        assertTrue(users.containsKey(userName) && users.keySet().size() == 1 &&
                files.containsKey(userName) && files.get(userName).size() == 1);
    }

    @Test
    public void testRegisterSeveralFiles() {
        String file1 = Path.of("resources" + File.separator + "file2.txt").toAbsolutePath().toString();
        String file2 = Path.of("resources" + File.separator + "file3.txt").toAbsolutePath().toString();
        String command = "register " + userName + " " + file1 + ", " + file2;

        torrentServer.handleCommand(command);
        Map<String, Set<Path>> files = torrentServer.getFiles();

        assertEquals(3, files.get(userName).size());
    }

    @Test
    public void testDisconnectUserSuccessfully() {
        String command = "disconnect " + userName;

        torrentServer.handleCommand(command);

        Map<String, InetSocketAddress> users = torrentServer.getUsers();

        assertFalse(users.containsKey(userName));
    }

    @Test
    public void testUnregisterAFileSuccessfully() {
        String command = "unregister " + userName + " " + filename;

        torrentServer.handleCommand(command);

        Map<String, Set<Path>> files = torrentServer.getFiles();

        assertTrue(files.containsKey(userName));
        assertFalse(files.get(userName).contains(Path.of(filename)));
    }

    @Test
    public void testUnregisterWhenThereIsNoFiles() {
        String command = "unregister " + userName + " " + filename;

        torrentServer.handleCommand(command);
        String reply = torrentServer.handleCommand(command);
        String expected = "No files." + System.lineSeparator();

        assertEquals(expected, reply);
    }

    @Test
    public void testUnregisterIfFileIsNotRegistered() {
        String otherFilename = "file2.txt";
        String command = "unregister " + userName + " " + otherFilename;

        String reply = torrentServer.handleCommand(command);
        String expected = "No such file(s)." + System.lineSeparator();

        assertEquals(expected, reply);
    }

    @Test
    public void testListUsers() {
        String command = "list-users";

        String reply = torrentServer.handleCommand(command);
        String expected = userName + " " + address + " " + port + System.lineSeparator();

        assertEquals(expected, reply);
    }

    @Test
    public void testListFiles() {
        String command = "list-files";

        String reply = torrentServer.handleCommand(command);
        String expected = userName + " : " + filename + System.lineSeparator();

        assertEquals(expected, reply);
    }

    @Test
    public void testUpdateName() {
        String newUserName = "gosho";
        String command = "update " + userName + " " + newUserName;

        torrentServer.handleCommand(command);

        Map<String, InetSocketAddress> users = torrentServer.getUsers();
        Map<String, Set<Path>> files = torrentServer.getFiles();

        assertFalse(users.containsKey(userName) && files.containsKey(userName));
        assertTrue(users.containsKey(newUserName) && files.containsKey(newUserName)
                && files.get(newUserName).contains(Path.of(filename)));
    }

    @Test
    public void testNameCheck() {
        String command = "name-check " + userName;

        String reply = torrentServer.handleCommand(command);
        String expected = "Nickname already exists." + System.lineSeparator();

        assertEquals(expected, reply);
    }

    @Test
    public void testNameCheckIfNameDoesNotExist() {
        String name = "gosho";

        String command = "name-check " + name;

        String reply = torrentServer.handleCommand(command);
        String expected = "Nickname doesn't exist." + System.lineSeparator();

        assertEquals(expected, reply);
    }

    @Test
    public void testFileCheck() {
        String command = "file-check " + userName + " " + filename;

        String reply = torrentServer.handleCommand(command);
        String expected = "File is registered." + System.lineSeparator();

        assertEquals(expected, reply);
    }

    @Test
    public void testFileCheckIfFileDoesNotExist() {
        String otherFilename = Path.of("resources" + File.separator + "file2.txt").toAbsolutePath().toString();
        String command = "file-check " + userName + " " + otherFilename;

        String reply = torrentServer.handleCommand(command);
        String expected = "File is not registered." + System.lineSeparator();

        assertEquals(expected, reply);
    }

    @Test
    public void testFileCheckIfUserDoesNotExist() {
        String otherUserName = "gosho";
        String command = "file-check " + otherUserName + " " + filename;

        String reply = torrentServer.handleCommand(command);
        String expected = "User doesn't exist." + System.lineSeparator();

        assertEquals(expected, reply);
    }

    @Test
    public void testInvalidCommand() {
        String command = "random";

        String reply = torrentServer.handleCommand(command);
        String expected = "Wrong command." + System.lineSeparator();

        assertEquals(expected, reply);
    }

    @Test(expected = IOException.class)
    public void testSendMessageIfNoConnection() throws IOException {
        torrentServer = new TorrentServer();
        torrentServer.sendMessage(null);
    }

    @Test(expected = IOException.class)
    public void testReadMessageIfNoConnection() throws IOException {
        torrentServer = new TorrentServer();
        torrentServer.readMessage();
    }
}
