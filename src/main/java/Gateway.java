import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.Arrays;

public class Gateway {

    private int gatewayPort;
    private InetAddress lampIp;
    private InetAddress airConditionerIp;
    private InetAddress audioSystemIp;
    private int lampPort;
    private int airConditionerPort;
    private int audioSystemPort;

    public Gateway(int gatewayPort) {
        this.gatewayPort = gatewayPort;
    }

    class DeviceListenerThread extends Thread {
        public void listen() throws IOException {
            DatagramSocket gatewaySocket = new DatagramSocket(gatewayPort);
            byte[] buffer = new byte[128];
            while(true) {
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                gatewaySocket.receive(request);
                String message = new String(request.getData(), 0, request.getLength());
                System.out.println(message);
            }
        }

        public void run() {
            try {
                this.listen();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    class GroupCommunicatorThread extends Thread {
        private GroupCommunicator groupHandler;

        public GroupCommunicatorThread(String groupIp, int groupPort) throws IOException {
            this.groupHandler = new GroupCommunicator(groupIp, groupPort);
            this.identifyDevices();
        }

        public void identifyDevices() throws IOException {
            this.groupHandler.send("identify");
            System.out.println("Identify devices");
        }

        public void listenForIdentifications() throws IOException {
            while(true) {
                DatagramPacket message = this.groupHandler.receive();
                String messageStr = new String(message.getData());
                if(!messageStr.startsWith("identify")) {
                    InetAddress ip = message.getAddress();

                    if(messageStr.startsWith("lamp:")) {
                        lampIp = ip;
                        lampPort = Integer.parseInt(new String(messageStr.toCharArray(), 5, 4));
                        System.out.println("Lamp was identified");
                    }
                    if(messageStr.startsWith("air_conditioner:")) {
                        airConditionerIp = ip;
                        airConditionerPort = Integer.parseInt(new String(messageStr.toCharArray(), 16, 4));
                        System.out.println("Air Conditioner was identified");
                    }
                    if(messageStr.startsWith("audio_system:")) {
                        audioSystemIp = ip;
                        audioSystemPort = Integer.parseInt(new String(messageStr.toCharArray(), 13, 4));
                        System.out.println("Audio system was identified");
                    }
                }

            }
        }

        public void run() {
            try {
                this.listenForIdentifications();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void send(InetAddress ip, int port, String message) throws IOException {
        Socket socket = new Socket(ip, port);
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        out.writeUTF(message);
        socket.close();
    }

    public String sendAndReceive(InetAddress ip, int port, String message) throws IOException {
        Socket socket = new Socket(ip, port);
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        DataInputStream in = new DataInputStream(socket.getInputStream());

        out.writeUTF(message);
        String response = in.readUTF();
        socket.close();
        return response;
    }

    public void turnOnLamp() throws IOException {
        System.out.println("Turning on the lamp");
        this.send(this.lampIp, this.lampPort, "turn_on");
    }

    public void turnOffLamp() throws IOException {
        System.out.println("Turning off the lamp");
        this.send(this.lampIp, this.lampPort, "turn_off");
    }

    public void setLamp(DeviceProto.Lamp lamp) throws IOException {
        if(lamp.getOn()) {
            this.turnOnLamp();
        } else {
            this.turnOffLamp();
        }
    }

    public void turnOnAirConditioner() throws IOException {
        System.out.println("Turning on the air conditioner");
        this.send(this.airConditionerIp, this.airConditionerPort, "turn_on");
    }

    public void turnOffAirConditioner() throws IOException {
        System.out.println("Turning off the air conditioner");
        this.send(this.airConditionerIp, this.airConditionerPort, "turn_off");
    }

    public void setAirConditionerTemperature(int temperature) throws IOException {
        this.send(this.airConditionerIp, this.airConditionerPort, "set_temperature:" + temperature);
        System.out.println("Setting temperature to " + temperature);
    }

    public void setAirConditioner(DeviceProto.AirConditioner airConditioner) throws IOException {
        if(airConditioner.getOn()) {
            this.turnOnAirConditioner();
        } else {
            this.turnOffAirConditioner();
        }
        this.setAirConditionerTemperature(airConditioner.getTemperature());
    }

    public void turnOnAudioSystem() throws IOException {
        System.out.println("Turning on the audio system");
        this.send(this.audioSystemIp, this.audioSystemPort, "turn_on");
    }

    public void turnOffAudioSystem() throws IOException {
        System.out.println("Turning off the audio system");
        this.send(this.audioSystemIp, this.audioSystemPort, "turn_off");
    }

    public void setAudioSystemSong(String song) throws IOException {
        this.send(this.audioSystemIp, this.audioSystemPort, "set_song:" + song);
        System.out.println("Setting song to " + song);
    }

    public void setAudioSystem(DeviceProto.AudioSystem audioSystem) throws IOException {
        if(audioSystem.getOn()) {
            this.turnOnAudioSystem();
        } else {
            this.turnOffAudioSystem();
        }
        this.setAudioSystemSong(audioSystem.getCurrentSong());
    }

    public void manageDevices() throws IOException {
        ServerSocket gatewaySocket = new ServerSocket(this.gatewayPort);
        while(true) {
            Socket clientSocket = gatewaySocket.accept();
            DataInputStream in = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());

            byte[] buffer = new byte[1024];
            int len = in.read(buffer);
            DeviceProto.Request request = DeviceProto.Request.parseFrom(Arrays.copyOfRange(buffer, 0, len));
            if(request.hasLampToSet()) {
                this.setLamp(request.getLampToSet());
            }
            if(request.hasGetLamp()) {
                boolean isLampOn = this.sendAndReceive(this.lampIp, this.lampPort, "is_on").equals("yes");
                DeviceProto.Lamp lamp = DeviceProto.Lamp.newBuilder().setOn(isLampOn).build();
                DeviceProto.Response response = DeviceProto.Response.newBuilder().setLamp(lamp).build();
                response.writeTo(out);
                out.flush();
            }
            if(request.hasAirConditionerToSet()) {
                this.setAirConditioner(request.getAirConditionerToSet());
            }
            if(request.hasGetAirConditioner()) {
                boolean isAirConditionerOn = this.sendAndReceive(this.airConditionerIp, this.airConditionerPort, "is_on").equals("yes");
                int airConditionerTemperature = Integer.parseInt(this.sendAndReceive(this.airConditionerIp, this.airConditionerPort, "get_temperature"));
                DeviceProto.AirConditioner airConditioner = DeviceProto.AirConditioner.newBuilder().setOn(isAirConditionerOn).setTemperature(airConditionerTemperature).build();
                DeviceProto.Response response = DeviceProto.Response.newBuilder().setAirConditioner(airConditioner).build();
                response.writeTo(out);
                out.flush();
            }
            if(request.hasAudioSystemToSet()) {
                this.setAudioSystem(request.getAudioSystemToSet());
            }
            if(request.hasGetAudioSystem()) {
                boolean isAudioSystemOn = this.sendAndReceive(this.audioSystemIp, this.audioSystemPort, "is_on").equals("yes");
                String audioSystemSong = this.sendAndReceive(this.audioSystemIp, this.audioSystemPort, "get_song");
                DeviceProto.AudioSystem audioSystem = DeviceProto.AudioSystem.newBuilder().setOn(isAudioSystemOn).setCurrentSong(audioSystemSong).build();
                DeviceProto.Response response = DeviceProto.Response.newBuilder().setAudioSystem(audioSystem).build();
                response.writeTo(out);
                out.flush();
            }
            clientSocket.close();
        }
    }

    public static void main(String[] args) throws IOException {
        Gateway gateway = new Gateway(Configuration.gatewayPort);
        DeviceListenerThread deviceListener = gateway.new DeviceListenerThread();
        GroupCommunicatorThread groupCommunicator = gateway.new GroupCommunicatorThread(Configuration.groupId, Configuration.groupPort);

        deviceListener.start();
        groupCommunicator.start();
        gateway.manageDevices();

    }
}
