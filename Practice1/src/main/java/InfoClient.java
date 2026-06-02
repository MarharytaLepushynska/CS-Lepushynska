import java.net.InetAddress;

public class InfoClient {
    private final byte bSrc;
    private final InetAddress address;
    private final int port;

    public InfoClient(byte bSrc, InetAddress address, int port) {
        this.bSrc = bSrc;
        this.address = address;
        this.port = port;
    }

    public byte getbSrc() {
        return bSrc;
    }

    public InetAddress getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }
}
