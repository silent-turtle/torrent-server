package bg.sofia.uni.fmi.mjt.torrent.client;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;

import static org.junit.Assert.*;

public class TorrentClientTest {

    private SocketChannel mockSocketChannel = Mockito.mock(SocketChannel.class);
    private Socket mockSocket = Mockito.mock(Socket.class);
    private TorrentClient torrentClient;
    private final int BYTE_ARRAY_SIZE = 256;
    private ByteArrayOutputStream out = new ByteArrayOutputStream(BYTE_ARRAY_SIZE);
    private final String localFileName = "pesho555";
    private final String userName = "pesho";
    private final String fileName = Path.of("resources" + File.separator + "file.txt")
            .toAbsolutePath().toString();
    private final String command = "register " + userName + " " + fileName;

    @Before
    public void init() throws IOException {
        torrentClient = new TorrentClient(mockSocketChannel, localFileName);

        Mockito.when(mockSocketChannel.socket()).thenReturn(mockSocket);
        Mockito.when(mockSocket.getOutputStream()).thenReturn(out);
    }

    @Test(expected = IOException.class)
    public void testSendMessageIfNoConnection() throws IOException {
        torrentClient = new TorrentClient();
        torrentClient.sendMessage(null);
    }

    @Test(expected = IOException.class)
    public void testReadMessageIfNoConnection() throws IOException {
        torrentClient = new TorrentClient();
        torrentClient.readMessage();
    }

    @Test
    public void testSendMessage() throws IOException {
        String expected = "hi";

        torrentClient.sendMessage(expected);

        String reply = out.toString();

        assertEquals(expected, reply);
    }

    @Test
    public void testRegisterWithValidFile() throws IOException, NicknameAlreadyExistsException {
        TorrentClientStub torrentClientStub = new TorrentClientStub();

        String expected = "Files registered.";


        String reply = torrentClientStub.handleCommand(command);

        assertEquals(expected, reply);
    }

    @Test(expected = IOException.class)
    public void testRegisterWithNonExistentFile() throws NicknameAlreadyExistsException, IOException {
        TorrentClientStub torrentClient = new TorrentClientStub();

        String fileName = Path.of("resources" + File.separator + "file10.txt")
                .toAbsolutePath().toString();
        String command = "register " + userName + " " + fileName;

        torrentClient.handleCommand(command);
    }

    @Test(expected = NicknameAlreadyExistsException.class)
    public void testRegisterWithRegisteredNickname() throws IOException, NicknameAlreadyExistsException {
        TorrentClientUserNameExistsStub torrentClient = new TorrentClientUserNameExistsStub();

        torrentClient.handleCommand(command);
    }

    @Test
    public void testRegisterWhenNicknameIsLocalFileName() throws IOException, NicknameAlreadyExistsException {
        TorrentClientStub torrentClient = new TorrentClientStub(mockSocketChannel, localFileName);

        String command = "register " + localFileName + " " + fileName;
        String command2 = "register " + userName + " " + fileName;

        torrentClient.handleCommand(command);
        String oldName = torrentClient.getNickname();

        torrentClient.handleCommand(command2);
        String newName = torrentClient.getNickname();

        assertTrue(newName.equals(userName) && !newName.equals(oldName));
    }


    @Test(expected = NicknameAlreadyExistsException.class)
    public void testRegisterWhenNicknameIsLocalFileAndUsernameAndNicknameMatch()
            throws IOException, NicknameAlreadyExistsException {
        TorrentClientStub torrentClient = new
                TorrentClientStub(mockSocketChannel, localFileName);

        String otherUserName = "gosho";
        String command2 = "register " + otherUserName + " " + fileName;

        torrentClient.handleCommand(command);
        torrentClient.handleCommand(command2);
    }
}
