package receiver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * Abstract class for a Peer-to-Peer communication. This class is for the active side.
 */
@SuppressWarnings({"DanglingJavadoc", "WeakerAccess", "unused"})
public abstract class AbstractP2PReceiver {

    /**
     * Listened port.
     */
    private final int used_port;

    /**
     * Socket of this program.
     */
    private DatagramSocket socket;

    /**
     * Current peer connection.
     */
    private PeerConnection currentConnection;

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
        super();
        this.used_port = port;
    }

    /*******************************************/
    /*             Initialization              */
    /*******************************************/

    /**
     * <i><b>startPeerConnection</b></i>
     *
     * <pre> public void startPeerConnection({@link Runnable} connectionCallback) </pre>
     *
     * @param connectionCallback the function which will be called after the connection has been established.
     */
    public void startPeerConnection(Runnable connectionCallback) {
        stopPeerConnection();

        if (socket == null || socket.isClosed()) {
            try {
                socket = new DatagramSocket(used_port);
            } catch (SocketException e) {
                e.printStackTrace();
            }
        }

        currentConnection = new PeerConnection(connectionCallback);
        currentConnection.start();
    }

    /**
     * <i><b>isConnected</b></i>
     *
     * <pre> public void isConnected() </pre>
     *
     * @return true if a connection with a peer has been established, false otherwise.
     */
    public boolean isConnected() {
        return currentConnection != null;
    }

    /**
     * A class used to create and use a connection with a peer
     * which will be use as a sender.
     */
    private class PeerConnection extends Thread {

        /**
         * Socket of the connected device.
         */
        private InetAddress connectedAddress;

        /**
         * Runnable called after the connection has been established.
         */
        private final Runnable connectionCallback;


        /*******************************************/
        /*               Constructor               */
        /*******************************************/

        /**
         * <i><b>PeerConnection</b></i>
         *
         * <pre> PeerConnection({@link Runnable} connectionCallback) </pre>
         *
         * Constructor of {@link PeerConnection}.
         * @param connectionCallback : function called after the connection has been established.
         */
        private PeerConnection(Runnable connectionCallback) {
            super();
            this.connectionCallback = connectionCallback;
        }

        @Override
        public void run() {

            if (!searchPeer()) {
                finish();
                return;
            }

            connectionCallback.run();

            if (isInterrupted()) {
                finish();
                return;
            }

            listen();
            finish();
        }

        /*******************************************/
        /*               Connection                */
        /*******************************************/

        /**
         * <i><b>searchPeer</b></i>
         *
         * <pre> private synchronized boolean searchPeer() </pre>
         *
         * Search a peer to be the sender.
         * @return false if an error has occurred and true otherwise.
         */
        private synchronized boolean searchPeer() {
            try {

                byte[] buffer;
                DatagramPacket packet = null;
                String message;

                /* Wait for a valid packet */
                while (!isInterrupted()) {
                    buffer = new byte[1000];
                    packet = new DatagramPacket(buffer, buffer.length);

                    socket.receive(packet);

                    message = new String(buffer).substring(0, packet.getLength());

                    if (isAcceptableConnection(message))
                        break;
                }

                if (isInterrupted()) {
                    return false;
                }

                /* Set attributes */
                this.connectedAddress = packet.getAddress();

                /* Send an ack */
                buffer = getAcceptConnectionMessage().getBytes();
                socket.send(new DatagramPacket(buffer, buffer.length, connectedAddress, used_port));

                return true;

            } catch (IOException e) {
                return false;
            }
        }

        /*******************************************/
        /*                Listening                */
        /*******************************************/

        /**
         * <i><b>receiveData</b></i>
         *
         * <pre> private void receiveData() </pre>
         *
         * Receive and read data from the sender.
         */
        private void listen() {
            byte[] buffer;
            DatagramPacket packet;
            String message;

            while (!isInterrupted()) {

                try {

                    buffer = new byte[1000];
                    packet = new DatagramPacket(buffer, buffer.length);

                    socket.receive(packet);
                    if (!packet.getAddress().equals(connectedAddress))
                        continue;

                    message = new String(buffer).substring(0, packet.getLength());

                    /* END */
                    if (message.equals(getEndConnectionMessage())) {
                        stopListening();

                    } else {
                        onListening(message);
                    }

                } catch (IOException e) {
                    System.out.println(e.getMessage());
                    return;
                }

            }
        }

        /**
         * <i><b>stopListening</b></i>
         *
         * <pre> private void stopListening() </pre>
         *
         * Stop listening by interrupting the thread.
         */
        private void stopListening() {
            if (isAlive() && !isInterrupted()) {
                socket.close();
                interrupt();
            }
        }
    }

    /*******************************************/
    /*                   Send                  */
    /*******************************************/

    /**
     * <i><b>send</b></i>
     *
     * <pre> private void send() </pre>
     *
     * Send a message to the peer if the connection is up.
     * @param message the message to send.
     */
    protected void send(String message) {
        if(!isConnected())
            return;

        byte[] buffer = message.getBytes();
        try {
            socket.send(new DatagramPacket(buffer, buffer.length, currentConnection.connectedAddress, used_port));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*******************************************/
    /*                   End                   */
    /*******************************************/

    /**
     * <i><b>stopPeerConnection</b></i>
     *
     * <pre> public void stopPeerConnection() </pre>
     *
     * Stop the connection with the peer.
     */
    public void stopPeerConnection() {
        if (currentConnection != null && !currentConnection.isInterrupted()) {
            currentConnection.interrupt();
            socket.close();
            try {
                currentConnection.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            currentConnection = null;
        }
    }

    /**
     * <i><b>closeConnection</b></i>
     *
     * <pre> private void closeConnection() </pre>
     * <p>
     * End the communication by sending an end message.
     */
    private void closeConnection() {
        if(currentConnection == null)
            return;

        try {
            socket = new DatagramSocket(used_port);
        } catch (SocketException e) {
            e.printStackTrace();
        }

        byte[] buffer = getEndConnectionMessage().getBytes();
        try {
            socket.send(new DatagramPacket(buffer, buffer.length, currentConnection.connectedAddress, used_port));
        } catch (IOException e) {
            e.printStackTrace();
        }

        socket.close();
    }

    /**
     * <i><b>finish</b></i>
     *
     * <pre> private void finish() </pre>
     *
     * Close all that need to be closed in {@link AbstractP2PReceiver}.
     */
    protected void finish() {
        if (currentConnection == null || currentConnection.connectedAddress == null)
            return;

        closeConnection();
        currentConnection.connectedAddress = null;
        currentConnection = null;
    }

    /*******************************************/
    /*                Abstract                 */
    /*******************************************/

    /**
     * <i><b>isAcceptableConnection</b></i>
     *
     * <pre> protected boolean isAcceptableConnection(String receivedMessage) </pre>
     *
     * @param receivedMessage the received message.
     * @return true if the received message is the waited message to start a connection, false otherwise.
     */
    protected abstract boolean isAcceptableConnection(String receivedMessage);

    /**
     * <i><b>getAcceptConnectionMessage</b></i>
     *
     * <pre> </pre>protected String getAcceptConnectionMessage() </pre>
     *
     * @return the message which will be send as an ack on a successful connection.
     */
    protected abstract String getAcceptConnectionMessage();

    /**
     * <i><b>getEndConnectionMessage</b></i>
     *
     * <pre> protected String getEndConnectionMessage() </pre>
     *
     * @return the message which will be send to end a connection.
     */
    protected abstract String getEndConnectionMessage();

    /**
     * <i><b>onListening</b></i>
     *
     * <pre> protected boolean onListening(String receivedMassage) </pre>
     *
     * The action to when a message is received.
     * @param receivedMessage the received message.
     */
    protected abstract void onListening(String receivedMessage);

}
