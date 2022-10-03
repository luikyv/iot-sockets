import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.Scanner;

public class AirConditioner extends Device {
    private boolean isOn;
    private int temperature;

    public AirConditioner(String name, boolean isOn, int temperature, int port, InetAddress gatewayIp, int gatewayPort) {
        super(name, port, gatewayIp, gatewayPort);
        this.isOn = isOn;
        this.temperature = temperature;
    }

    public void listenCommands() throws IOException {
        Scanner in = new Scanner(System.in);
        while(true) {
            String command = in.nextLine();
            if(command.equals("turn on")) {
                isOn = true;
                pingGateway("the air conditioner is on. Current temperature: " + temperature);
            }
            if(command.equals("turn off")) {
                isOn = false;
                pingGateway("the air conditioner is off. Current temperature: " + temperature);
            }
            if(command.startsWith("set temperature: ")) {
                isOn = true;
                temperature = Integer.parseInt(command.substring("set temperature: ".length()));
                pingGateway("the air conditioner is on. Current temperature: " + temperature);
            }
        }
    }

    class ContinuousUpdaterThread extends Thread {
        public void updateGateway() throws IOException, InterruptedException {
            while(true) {
                Thread.sleep(5000);
                if(isOn) {
                    pingGateway("the air conditioner is on. Current temperature: " + temperature);
                }
            }

        }

        public void run() {
            try {
                this.updateGateway();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    class GatewayCommunicatorThread extends Thread {

        public void answerMessages() throws IOException {
            ServerSocket airConditionerSocket = new ServerSocket(port);
            while(true) {
                Socket serverSocket = airConditionerSocket.accept();
                DataInputStream in = new DataInputStream(serverSocket.getInputStream());
                DataOutputStream out = new DataOutputStream(serverSocket.getOutputStream());
                String message = in.readUTF();
                if(message.startsWith("turn_on")) {
                    isOn = true;
                    System.out.println("the air conditioner is on");
                }
                if(message.startsWith("turn_off")) {
                    isOn = false;
                    System.out.println("the air conditioner is off");
                }
                if(message.startsWith("is_on")) {
                    if(isOn) {
                        out.writeUTF("yes");
                    } else {
                        out.writeUTF("no");
                    }
                    System.out.println("answering if the air conditioner is on");
                }
                if(message.startsWith("set_temperature:")) {
                    temperature = Integer.parseInt(message.substring("set_temperature:".length()));
                    System.out.println("setting temperature to " + temperature);
                }
                if(message.startsWith("get_temperature")) {
                    out.writeUTF(Integer.toString(temperature));
                    System.out.println("answering the current temperature");
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
        int airConditionerPort = 7439;

        AirConditioner airConditioner = new AirConditioner("air_conditioner", false, 25, airConditionerPort, Configuration.gatewayId, Configuration.gatewayPort);
        ContinuousUpdaterThread continuousUpdater = airConditioner.new ContinuousUpdaterThread();
        AirConditioner.GroupCommunicatorThread groupCommunicator = airConditioner.new GroupCommunicatorThread(Configuration.groupId, Configuration.groupPort);
        AirConditioner.GatewayCommunicatorThread gatewayCommunicator = airConditioner.new GatewayCommunicatorThread();

        continuousUpdater.start();
        groupCommunicator.start();
        gatewayCommunicator.start();
        airConditioner.listenCommands();
    }

}
