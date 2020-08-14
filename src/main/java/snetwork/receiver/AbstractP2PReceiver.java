package snetwork.receiver;

import snetwork.AbstractP2PLink;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.function.Consumer;

/**
 * Abstract class for an unilateral data flux with a Peer-to-Peer communication.
 * This class is for the data receiver side.
 */
@SuppressWarnings({"DanglingJavadoc", "WeakerAccess", "unused"})
public abstract class AbstractP2PReceiver extends AbstractP2PLink {

    /**
     * Runnable called after the connection has been established.
     */
    private Consumer<Boolean> connectionCallback;

    /*******************************************/
    /*              Constructor                */
    /*******************************************/

    /**
     * <i><b>AbstractP2PReceiver</b></i>
     *
     * <pre> protected AbstractP2PReceiver() </pre>
     *
     * Constructor of {@link AbstractP2PReceiver}.
     */
    protected AbstractP2PReceiver(int port) {
        super(port);
    }

    /*******************************************/
    /*             Initialization              */
    /*******************************************/

    @Override
    public void startProtocol(Consumer<Boolean> connectionCallback) {
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
                    connectionCallback.accept(false);
                    return false;
                }

                connectionCallback.accept(true);

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

                getSocket().receive(packet);

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

    /**
     * <i><b>getAcceptConnectionMessage</b></i>
     *
     * <pre> </pre>protected String getAcceptConnectionMessage() </pre>
     *
     * @return the message which will be send as an ack on a successful connection.
     */
    protected abstract String getAcceptConnectionMessage();

}
