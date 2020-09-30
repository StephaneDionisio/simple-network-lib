package snetwork.receiver;

import snetwork.AbstractP2PLink;
import snetwork.SuccessCallback;

import java.io.IOException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.util.Arrays;

/**
 * Abstract class for an unilateral data flux with a Peer-to-Peer communication.
 * This class is for the data receiver side.
 */
@SuppressWarnings({"DanglingJavadoc", "WeakerAccess", "unused"})
public abstract class AbstractP2PReceiver extends AbstractP2PLink {

    /**
     * Runnable called after the connection has been established.
     */
    private SuccessCallback connectionCallback;

    /*******************************************/
    /*              Constructor                */
    /*******************************************/

    /**
     * <i><b>AbstractP2PReceiver</b></i>
     *
     * <pre> protected AbstractP2PReceiver(int port, int timeout) </pre>
     *
     * Constructor of {@link AbstractP2PReceiver}.
     * @param port the used port.
     * @param timeout the socket timeout in milliseconds. timeout <= 0 for no timeout.
     */
    protected AbstractP2PReceiver(int port, int timeout) {
        super(port, timeout);
    }

    /*******************************************/
    /*             Initialization              */
    /*******************************************/

    @Override
    public void startProtocol(SuccessCallback connectionCallback) throws BindException {
        stopPeerConnection();
        init();

        this.connectionCallback = connectionCallback;
        getBackgroundThread().start();
    }

    @Override
    protected ListenerThread createBackgroundThread() {
        return new ListenerThread();
    }

    protected class ListenerThread extends AbstractP2PLink.ListenerThread {
        @Override
        protected final boolean beforeAll() {
            if (isInterrupted() || !searchPeer()) {
                finish();
                connectionCallback.onResult(false);
                return false;
            }

            connectionCallback.onResult(true);

            if (isInterrupted()) {
                finish();
                return false;
            }

            return true;
        }
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

            Thread backgroundThread = getBackgroundThread();

            /* Wait for a valid packet */
            while (!backgroundThread.isInterrupted()) {
                buffer = new byte[1000];
                packet = new DatagramPacket(buffer, buffer.length);

                getSocket().receive(packet);

                buffer = Arrays.copyOf(buffer, packet.getLength());

                if (isAcceptableConnection(buffer))
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
     * <pre> </pre>protected byte[] getAcceptConnectionMessage() </pre>
     *
     * @return the message which will be send as an ack on a successful connection.
     */
    protected abstract byte[] getAcceptConnectionMessage();

}
