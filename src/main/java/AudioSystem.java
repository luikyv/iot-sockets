import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.Scanner;

public class AudioSystem extends Device{
    private boolean isOn;
    private String currentSong;

    public AudioSystem(String name, boolean isOn, int port, InetAddress gatewayIp, int gatewayPort) {
        super(name, port, gatewayIp, gatewayPort);
        this.isOn = isOn;
        this.currentSong = "none";
    }

    public void listenCommands() throws IOException {
        Scanner in = new Scanner(System.in);
        while(true) {
            String command = in.nextLine();
            if(command.equals("turn on")) {
                isOn = true;
                pingGateway("the audio system is on. Current song: " + currentSong);
            }
            if(command.equals("turn off")) {
                isOn = false;
                currentSong = "none";
                pingGateway("the audio system is off. Current song: " + currentSong);
            }
            if(command.startsWith("set song: ")) {
                isOn = true;
                currentSong = command.substring("set song: ".length());
                pingGateway("the audio system is on. Current song: " + currentSong);
            }
        }
    }

    class GatewayCommunicatorThread extends Thread {

        public void answerMessages() throws IOException {
            ServerSocket audioSystemSocket = new ServerSocket(port);
            while(true) {
                Socket serverSocket = audioSystemSocket.accept();
                DataInputStream in = new DataInputStream(serverSocket.getInputStream());
                DataOutputStream out = new DataOutputStream(serverSocket.getOutputStream());
                String message = in.readUTF();
                if(message.startsWith("turn_on")) {
                    isOn = true;
                    System.out.println("the audio system is on");
                }
                if(message.startsWith("turn_off")) {
                    isOn = false;
                    System.out.println("The audio system is off");
                }
                if(message.startsWith("is_on")) {
                    if(isOn) {
                        out.writeUTF("yes");
                    } else {
                        out.writeUTF("no");
                    }
                    System.out.println("answering if the audio system is on");
                }
                if(message.startsWith("set_song:")) {
                    currentSong = message.substring("set_song:".length());
                    System.out.println("Setting song " + currentSong);
                }
                if(message.startsWith("get_song")) {
                    out.writeUTF(currentSong);
                    System.out.println("answering the current song");
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
        int audioSystemPort = 8423;

        AudioSystem audioSystem = new AudioSystem("audio_system" ,false, audioSystemPort, Configuration.gatewayId, Configuration.gatewayPort);
        AudioSystem.GroupCommunicatorThread groupCommunicator = audioSystem.new GroupCommunicatorThread(Configuration.groupId, Configuration.groupPort);
        AudioSystem.GatewayCommunicatorThread gatewayCommunicator = audioSystem.new GatewayCommunicatorThread();

        groupCommunicator.start();
        gatewayCommunicator.start();
        audioSystem.listenCommands();
    }
}
