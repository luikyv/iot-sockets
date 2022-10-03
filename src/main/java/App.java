import com.google.protobuf.CodedOutputStream;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;


public class App {
    private InetAddress gatewayIp;
    private int gatewayPort;

    public App(InetAddress gatewayIp, int gatewayPort) {
        this.gatewayIp = gatewayIp;
        this.gatewayPort = gatewayPort;
    }

    public void setLampIsOn(boolean on) throws IOException {
        DeviceProto.Lamp lamp = DeviceProto.Lamp.newBuilder().setOn(on).build();
        DeviceProto.Request request = DeviceProto.Request.newBuilder().setLampToSet(lamp).build();
        Socket socket = new Socket(this.gatewayIp, this.gatewayPort);
        CodedOutputStream out = CodedOutputStream.newInstance(socket.getOutputStream());
        request.writeTo(out);
        out.flush();
        socket.close();
    }

    public boolean isLampOn() throws IOException {
        DeviceProto.Request request = DeviceProto.Request.newBuilder().setGetLamp(true).build();
        Socket socket = new Socket(this.gatewayIp, this.gatewayPort);

        DataInputStream in = new DataInputStream(socket.getInputStream());
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());

        request.writeTo(out);
        out.flush();

        byte[] buffer = new byte[1024];
        int len = in.read(buffer);
        DeviceProto.Response response = DeviceProto.Response.parseFrom(Arrays.copyOfRange(buffer, 0, len));

        socket.close();
        return response.getLamp().getOn();
    }

    public void setAirConditioner(boolean on, int temperature) throws IOException {
        DeviceProto.AirConditioner airConditioner = DeviceProto.AirConditioner.newBuilder().setOn(on).setTemperature(temperature).build();
        DeviceProto.Request request = DeviceProto.Request.newBuilder().setAirConditionerToSet(airConditioner).build();
        Socket socket = new Socket(this.gatewayIp, this.gatewayPort);
        CodedOutputStream out = CodedOutputStream.newInstance(socket.getOutputStream());
        request.writeTo(out);
        out.flush();
        socket.close();
    }

    public int getTemperature() throws IOException {
        DeviceProto.Request request = DeviceProto.Request.newBuilder().setGetAirConditioner(true).build();
        Socket socket = new Socket(this.gatewayIp, this.gatewayPort);

        DataInputStream in = new DataInputStream(socket.getInputStream());
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());

        request.writeTo(out);
        out.flush();
        DeviceProto.Response response = DeviceProto.Response.parseFrom(in);

        socket.close();
        return response.getAirConditioner().getTemperature();
    }

    public void setAudioSystem(boolean on, String song) throws IOException {
        DeviceProto.AudioSystem audioSystem = DeviceProto.AudioSystem.newBuilder().setOn(on).setCurrentSong(song).build();
        DeviceProto.Request request = DeviceProto.Request.newBuilder().setAudioSystemToSet(audioSystem).build();
        Socket socket = new Socket(this.gatewayIp, this.gatewayPort);
        CodedOutputStream out = CodedOutputStream.newInstance(socket.getOutputStream());
        request.writeTo(out);
        out.flush();
        socket.close();
    }

    public String getCurrentSong() throws IOException {
        DeviceProto.Request request = DeviceProto.Request.newBuilder().setGetAudioSystem(true).build();
        Socket socket = new Socket(this.gatewayIp, this.gatewayPort);

        DataInputStream in = new DataInputStream(socket.getInputStream());
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());

        request.writeTo(out);
        out.flush();
        DeviceProto.Response response = DeviceProto.Response.parseFrom(in);

        socket.close();
        return response.getAudioSystem().getCurrentSong();
    }

    public void manageDevices() throws IOException {
        Scanner in = new Scanner(System.in);
        while(true) {
            String command = in.nextLine();
            if(command.equals("turn on the lamp")) {
                this.setLampIsOn(true);
            }
            if(command.equals("turn off the lamp")) {
                this.setLampIsOn(false);
            }
            if(command.equals("is the lamp on?")) {
                if(this.isLampOn()) {
                    System.out.println("The lamp is on");
                } else {
                    System.out.println("The lamp is off");
                }
            }
            if(command.equals("turn on the air conditioner")) {
                this.setAirConditioner(true, 20);
            }
            if(command.equals("turn off the air conditioner")) {
                this.setAirConditioner(false, 20);
            }
            if(command.startsWith("set temperature: ")) {
                this.setAirConditioner(false, Integer.parseInt(command.substring("set temperature: ".length())));
            }
            if(command.equals("get temperature")) {
                System.out.println("temperature is " + this.getTemperature());
            }
            if(command.equals("turn on the audio system")) {
                this.setAudioSystem(true, "none");
            }
            if(command.equals("turn off the audio system")) {
                this.setAudioSystem(false, "none");
            }
            if(command.startsWith("set song: ")) {
                this.setAudioSystem(false, command.substring("set song: ".length()));
            }
            if(command.equals("get song")) {
                System.out.println("the current song is " + this.getCurrentSong());
            }

        }
    }

    public static void main(String[] args) throws IOException {
        App app = new App(Configuration.gatewayId, Configuration.gatewayPort);
        app.manageDevices();
    }
}
