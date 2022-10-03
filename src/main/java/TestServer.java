import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

public class TestServer {

    public static void main2(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(4321);
        Socket clientSocket = serverSocket.accept();

//        CodedInputStream in = CodedInputStream.newInstance(clientSocket.getInputStream());
//        CodedOutputStream out = CodedOutputStream.newInstance(clientSocket.getOutputStream());

        InputStream in = clientSocket.getInputStream();
        OutputStream out = clientSocket.getOutputStream();

        System.out.println("Server1");
        DeviceProto.Lamp lamp = DeviceProto.Lamp.parseFrom(in);
        System.out.println("Server2");

        System.out.println("Is lamp on?" + lamp.getOn());
        DeviceProto.Lamp lamp2 = DeviceProto.Lamp.newBuilder().setOn(true).build();
        lamp2.writeTo(out);
        out.flush();

    }

    public static void main(String[] args) throws IOException {
        ServerSocket socket = new ServerSocket(4321);
        Socket clientSocket = socket.accept();
        DataInputStream in = new DataInputStream(clientSocket.getInputStream());
        DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());

        DeviceProto.Lamp lamp = DeviceProto.Lamp.newBuilder().setOn(true).build();
        byte[] request = new byte[1024];

        int len = in.read(request);
        lamp.writeTo(out);
        out.flush();
        DeviceProto.Lamp clientLamp = DeviceProto.Lamp.parseFrom(Arrays.copyOfRange(request, 0, len));
        System.out.println("Is the lamp on? " + clientLamp.getOn());
    }
}
