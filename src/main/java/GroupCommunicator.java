import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class GroupCommunicator {
    private InetAddress groupIp;
    private int groupPort;
    private MulticastSocket groupSocket;

    public GroupCommunicator(String groupIp, int groupPort) throws IOException {
        this.groupIp = InetAddress.getByName(groupIp);
        this.groupPort = groupPort;
        this.joinGroup();
    }

    public void send(String message) throws IOException {
        byte[] identifyMessageBytes = message.getBytes();
        DatagramPacket identifyMessage = new DatagramPacket(
                identifyMessageBytes,
                identifyMessageBytes.length,
                this.groupIp,
                this.groupPort
        );
        this.groupSocket.send(identifyMessage);
    }

    public DatagramPacket receive() throws IOException {
        byte[] buffer = new byte[256];
        DatagramPacket message = new DatagramPacket(buffer, buffer.length);
        this.groupSocket.receive(message);
        return message;
    }

    public void joinGroup() throws IOException {
        this.groupSocket = new MulticastSocket(this.groupPort);
        this.groupSocket.joinGroup(this.groupIp);
    }
}
