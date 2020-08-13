package snetwork.sender;

import snetwork.AbstractP2PLink;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public abstract class AbstractP2PSender extends AbstractP2PLink {

    /**
     * <i><b>AbstractP2PSender</b></i>
     *
     * <pre> protected AbstractP2PSender(int port) </pre>
     *
     * Constructor of {@link AbstractP2PSender}.
     * @param port the port used.
     */
    protected AbstractP2PSender(int port) {
        super(port);
    }

    @Override
    public void startProtocol() {
        stopPeerConnection();
        init();

        searchPeer();
        getBackgroundThread().start();
    }

    @Override
    protected boolean searchPeer() {

        // Search; udp socket.
        DatagramPacket packet;
        String connectionMessage = getConnectionMessage();

        try {
            for (InetAddress address : listAllBroadcastAddresses()) {
                send(connectionMessage, address);
                System.out.println(getClass().getName() + "Broadcast packet sent to: " + address.getHostAddress());
            }
        } catch (SocketException e) {
            e.printStackTrace();
            return false;
        }

        byte[] buf;
        while (true) {
            try {
                buf = new byte[1000];
                packet = new DatagramPacket(buf, buf.length);

                getSocket().receive(packet);

                String message = new String(buf).substring(0, packet.getLength());

                if (isAcceptableConnection(message)) {
                    return true;
                }

            } catch (SocketTimeoutException e) {
                return false;

            }catch (IOException e) {
                System.out.println(e.getMessage());
                return false;
            }
        }
    }

    /**
     * <i><b>AbstractP2PSender</b></i>
     *
     * <pre> private  {@link List}<{@link InetAddress}> listAllBroadcastAddresses() </pre>
     *
     * Get a list of broadcast addresses for each network interface.
     *
     * @return The list of addresses.
     * @throws SocketException if an error occurs while getting network interfaces.
     */
    private List<InetAddress> listAllBroadcastAddresses() throws SocketException {
        List<InetAddress> broadcastList = new ArrayList<>();
        Enumeration<NetworkInterface> interfaces
                = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();

            if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                continue;
            }

            for(InterfaceAddress address : networkInterface.getInterfaceAddresses()) {
                if (address != null) {
                    broadcastList.add(address.getBroadcast());
                }
            }

        }

        return broadcastList;
    }

    /**
     * <i><b>getConnectionMessage</b></i>
     *
     * <pre> </pre>protected {@link String} getConnectionMessage() </pre>
     *
     * @return the message which will be send as a start connection message.
     */
    protected abstract String getConnectionMessage();

}
