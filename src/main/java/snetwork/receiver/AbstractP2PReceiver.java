package snetwork.receiver;

import snetwork.AbstractP2PLink;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;

/**
 * Abstract class for an unilateral data flux with a Peer-to-Peer communication.
 * This class is for the data receiver side.
 */
@SuppressWarnings({"DanglingJavadoc", "WeakerAccess", "unused"})
public abstract class AbstractP2PReceiver extends AbstractP2PLink {

    /**
     * Runnable called after the connection has been established.
     */
    private Runnable connectionCallback;

    /*******************************************/
    /*              Constructor                */
    /*******************************************/

    /**
     * <i><b>AbstractP2PReceiver</b></i>
     *
     * <pre> protected AbstractP2PReceiver() </pre>
     * <p>
     * Constructor of {@link AbstractP2PReceiver}.
     */
    protected AbstractP2PReceiver(int port) {
        super(port);
    }

    /*******************************************/
    /*             Initialization              */
    /*******************************************/

    @Override
    public void startProtocol() {
        startProtocol(() -> {/* empty */});
    }

    /**
     * <i><b>startProtocol</b></i>
     *
     * <pre> public void startProtocol({@link Runnable} connectionCallback) </pre>
     *
     * Method which start the protocol.
     * @param connectionCallback the function which will be called after the connection has been established.
     */
    public void startProtocol(Runnable connectionCallback) {
        stopPeerConnection();
        init();

        this.connectionCallback = connectionCallback;
        getBackgroundThread().start();
    }

    @Override
    protected ListenerThread createBackgroundThread() {
        return new ListenerThread(){
            @Override
            protected boolean beforeAll() {
                if (!searchPeer()) {
                    finish();
                    return false;
                }

                connectionCallback.run();

                if (isInterrupted()) {
                    finish();
                    return false;
                }

                return true;
            }
        };
    }

    /*******************************************/
    /*               Connection                */
    /*******************************************/

    /**
     * <i><b>searchPeer</b></i>
     *
     * <pre> private synchronized boolean searchPeer() </pre>
     * <p>
     * Search a peer to be the sender.
     *
     * @return false if an error has occurred and true otherwise.
     */
    @Override
    protected synchronized boolean searchPeer() {
        try {

            byte[] buffer;
            DatagramPacket packet = null;
            String message;

            Thread backgroundThread = getBackgroundThread();

            /* Wait for a valid packet */
            while (!backgroundThread.isInterrupted()) {
                buffer = new byte[1000];
                packet = new DatagramPacket(buffer, buffer.length);

                socket.receive(packet);

                message = new String(buffer).substring(0, packet.getLength());

                if (isAcceptableConnection(message))
                    break;
            }

            if (backgroundThread.isInterrupted()) {
                return false;
            }

            /* Set attributes */
            setConnectedAddress(packet.getAddress());

            /* Send an ack */
            send(getAcceptConnectionMessage());

            return true;

        } catch (IOException e) {
            return false;
        }
    }

    /*******************************************/
    /*                Abstract                 */
    /*******************************************/

    @Override
    protected abstract boolean isAcceptableConnection(String receivedMessage);

    @Override
    protected abstract String getAcceptConnectionMessage();

    @Override
    protected abstract String getEndConnectionMessage();

    @Override
    protected abstract void onListening(String receivedMessage);

}
