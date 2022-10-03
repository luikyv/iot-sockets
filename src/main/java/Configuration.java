import java.net.InetAddress;
import java.net.UnknownHostException;

public class Configuration {
    public static String groupId = "228.5.6.7";
    public static int groupPort = 6789;
    public static InetAddress gatewayId;

    static {
        try {
            gatewayId = InetAddress.getByName("localhost");
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    public static int gatewayPort = 3456;
}
