import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;

public class TestClient {

    public static void main2(String[] args) throws IOException {
//        Socket socket = new Socket(InetAddress.getByName("localhost"), 4321);

//        CodedInputStream in = CodedInputStream.newInstance(socket.getInputStream());
//        CodedOutputStream out = CodedOutputStream.newInstance(socket.getOutputStream());
//        InputStream in = socket.getInputStream();
//        OutputStream out = socket.getOutputStream();

        DeviceProto.Lamp lamp = DeviceProto.Lamp.newBuilder().setOn(true).build();

//        lamp.writeTo(out);
//        out.flush();
//        System.out.println("Client1");
//        DeviceProto.Lamp lamp2 = DeviceProto.Lamp.parseFrom(in);
//        System.out.println("Client1");
//        System.out.println(lamp2.getOn());
//        lamp2.toString();
    }

    public static void main(String[] args) throws IOException {

        Socket socket = new Socket(InetAddress.getByName("localhost"), 4321);
        DataInputStream in = new DataInputStream(socket.getInputStream());
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());

        DeviceProto.Lamp lamp = DeviceProto.Lamp.newBuilder().setOn(true).build();
        byte[] response = new byte[1024];

        lamp.writeTo(out);
        out.flush();
        int len = in.read(response);
        DeviceProto.Lamp serverLamp = DeviceProto.Lamp.parseFrom(Arrays.copyOfRange(response, 0, len));
        System.out.println("Is the lamp on? " + serverLamp.getOn());

    }
}
