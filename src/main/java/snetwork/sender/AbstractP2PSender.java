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
    public void startProtocol(Runnable connectionCallback) {
        stopPeerConnection();
        init();

        searchPeer();
        connectionCallback.run();
        getBackgroundThread().start();
    }

    /**
     * <i><b>searchPeer</b></i>
     *
     * <pre> protected boolean searchPeer({@link List}<{@link InetAddress}> addresses) </pre>
     *
     * Search and set a peer to be the sender (equivalent to {@link #searchPeer(List)} with a null argument)
     * @return true if a peer is found, otherwise false.
     */
    @Override
    protected boolean searchPeer() {
        return searchPeer(null);
    }

    /**
     * <i><b>searchPeer</b></i>
     *
     * <pre> protected boolean searchPeer({@link List}<{@link InetAddress}> addresses) </pre>
     *
     * Search and set a peer to be the sender.
     * @param addresses the of of potential peers addresses.
     *                  If null, use all the addresses that are in the local network.
     * @return true if a peer is found, otherwise false.
     */
    protected boolean searchPeer(List<InetAddress> addresses) {

        // Search; udp socket.
        DatagramPacket packet;
        String connectionMessage = getConnectionMessage();

        if(addresses == null) {
            try {
                addresses = listAllBroadcastAddresses();
            } catch (SocketException e) {
                e.printStackTrace();
                return false;
            }
        }

        for (InetAddress address : addresses) {
            send(connectionMessage, address);
            System.out.println(getClass().getName() + "Broadcast packet sent to: " + address.getHostAddress());
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
