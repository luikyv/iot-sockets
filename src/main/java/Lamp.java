import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.Scanner;

public class Lamp extends Device {
    private boolean isOn;

    public Lamp(String name, boolean isOn, int port, InetAddress gatewayIp, int gatewayPort) {
        super(name, port, gatewayIp, gatewayPort);
        this.isOn = isOn;
    }

    public void listenCommands() throws IOException {
        Scanner in = new Scanner(System.in);
        while(true) {
            String command = in.nextLine();
            if(command.equals("turn on")) {
                isOn = true;
                pingGateway("the lamp is on");
            }
            if(command.equals("turn off")) {
                isOn = false;
                pingGateway("the lamp is off");
            }
        }
    }

    class GatewayCommunicatorThread extends Thread {

        public void answerMessages() throws IOException {
            ServerSocket lampSocket = new ServerSocket(port);
            while(true) {
                Socket serverSocket = lampSocket.accept();
                DataInputStream in = new DataInputStream(serverSocket.getInputStream());
                DataOutputStream out = new DataOutputStream(serverSocket.getOutputStream());
                String message = in.readUTF();
                if(message.startsWith("turn_on")) {
                    isOn = true;
                    System.out.println("the lamp is on");
                }
                if(message.startsWith("turn_off")) {
                    isOn = false;
                    System.out.println("the lamp is off");
                }
                if(message.startsWith("is_on")) {
                    if(isOn) {
                        out.writeUTF("yes");
                    } else {
                        out.writeUTF("no");
                    }
                    System.out.println("answering if the lamp is on");
                }
            }
        }

        public void run() {
            try {
                this.answerMessages();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void main(String[] args) throws IOException {
        int lampPort = 9876;

        Lamp lamp = new Lamp("lamp", false, lampPort, Configuration.gatewayId, Configuration.gatewayPort);
        GroupCommunicatorThread groupCommunicator = lamp.new GroupCommunicatorThread(Configuration.groupId, Configuration.groupPort);
        GatewayCommunicatorThread gatewayCommunicator = lamp.new GatewayCommunicatorThread();

        groupCommunicator.start();
        gatewayCommunicator.start();
        lamp.listenCommands();
    }
}
