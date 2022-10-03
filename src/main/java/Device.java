import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

abstract class Device {
    protected String name;
    protected int port;
    protected InetAddress gatewayIp;
    protected int gatewayPort;

    public Device(String name, int port, InetAddress gatewayIp, int gatewayPort) {
        this.name = name;
        this.port = port;
        this.gatewayIp = gatewayIp;
        this.gatewayPort = gatewayPort;
    }

    public void pingGateway(String message) throws IOException {
        DatagramSocket airConditionerSocket = new DatagramSocket();
        byte[] buffer = message.getBytes();
        DatagramPacket messagePacket = new DatagramPacket(buffer, buffer.length, gatewayIp, gatewayPort);
        airConditionerSocket.send(messagePacket);
    }

    class GroupCommunicatorThread extends Thread{
        private GroupCommunicator groupHandler;

        public GroupCommunicatorThread(String groupIp, int groupPort) throws IOException {
            this.groupHandler = new GroupCommunicator(groupIp, groupPort);
            this.identifyToGateway();
        }

        public void identifyToGateway() throws IOException {
            this.groupHandler.send(name + ":" + port);
            System.out.println("Identify to gateway");
        }

        public void replyIdentificationRequests() throws IOException {
            while(true) {
                DatagramPacket message = this.groupHandler.receive();
                String messageStr = new String(message.getData());
                if(messageStr.startsWith("identify")) {
                    this.identifyToGateway();
                }
            }
        }

        public void run() {
            try {
                this.replyIdentificationRequests();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    abstract void listenCommands() throws IOException;
}
