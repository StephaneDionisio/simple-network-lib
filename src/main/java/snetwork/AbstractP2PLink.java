package snetwork;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * Abstract class for a Peer-to-Peer communication protocol.
 */
@SuppressWarnings({"DanglingJavadoc", "WeakerAccess", "unused"})
public abstract class AbstractP2PLink {

    /**
     * Listened port.
     */
    private final int used_port;

    /**
     * Socket of this program.
     */
    protected DatagramSocket socket;

    /**
     * Address of the connected peer.
     */
    private InetAddress connectedAddress;

    /**
     * Thread where all background stuff is done.
     */
    private ListenerThread backgroundThread;

    /*******************************************/
    /*              Constructor                */
    /*******************************************/

    /**
     * <i><b>AbstractP2PReceiver</b></i>
     *
     * <pre> protected AbstractP2PReceiver(int port) </pre>
     *
     * Constructor of {@link snetwork.AbstractP2PLink}.
     * @param port the port used.
     */
    protected AbstractP2PLink(int port) {
        super();
        this.used_port = port;
    }

    /**
     * <i><b>init</b></i>
     *
     * <pre> protected void init() </pre>
     *
     * The method used to initialize the socket and the background thread.
     */
    protected final void init() {
        if (this.socket == null || this.socket.isClosed()) {
            try {
                this.socket = new DatagramSocket(this.used_port);
            } catch (SocketException e) {
                e.printStackTrace();
            }
        }

        backgroundThread = createBackgroundThread();
    }

    /**
     * <i><b>createBackgroundThread</b></i>
     *
     * <pre> protected {@link ListenerThread} createBackgroundThread() </pre>
     *
     * Create the background thread.
     * @return the thread created.
     */
    protected ListenerThread createBackgroundThread() {
        return new ListenerThread();
    }

    /*******************************************/
    /*             Getters/Setters             */
    /*******************************************/

    /**
     * <i><b>getBackgroundThread</b></i>
     *
     * <pre> protected {@link ListenerThread} getBackgroundThread() </pre>
     *
     * @return the background thread.
     */
    protected ListenerThread getBackgroundThread() {
        return backgroundThread;
    }

    /**
     * <i><b>setConnectedAddress</b></i>
     *
     * <pre> protected void setConnectedAddress({@link InetAddress} connectedAddress) </pre>
     *
     * Set the connected address if no connexion is up.
     * @throws IllegalStateException if there is already a connection up.
     */
    public void setConnectedAddress(InetAddress connectedAddress) {
        if(this.connectedAddress != null)
            throw new IllegalStateException("A connection is already up. Close it to assign a new connected address");
        this.connectedAddress = connectedAddress;
    }

    /*******************************************/
    /*               Connection                */
    /*******************************************/

    /**
     * <i><b>startProtocol</b></i>
     *
     * <pre> public void startProtocol() </pre>
     *
     * Method to start the protocol.
     */
    public abstract void startProtocol();

    /**
     * <i><b>isConnected</b></i>
     *
     * <pre> public void isConnected() </pre>
     *
     * @return true if a connection with a peer has been established, false otherwise.
     */
    public boolean isConnected() {
        return connectedAddress != null;
    }

    /**
     * <i><b>searchPeer</b></i>
     *
     * <pre> protected boolean searchPeer() </pre>
     *
     * Search and set a peer to be the sender.
     * @return true if a peer is found, otherwise false.
     */
    protected abstract boolean searchPeer();

    /*******************************************/
    /*                Listening                */
    /*******************************************/

    /**
     * Thread used to listen datagrams and clean-up when the the communication is done.
     */
    protected class ListenerThread extends Thread {

        @Override
        public final void run() {
            if(!beforeAll())
                return;

            listen();
            finish();

            afterAll();
        }

        /**
         * <i><b>beforeAll</b></i>
         *
         * <pre> private boolean beforeAll() </pre>
         *
         * Method call before the listen one.
         * @return false if an error as occurred so we can't listen, otherwise true.
         */
        protected boolean beforeAll() {
            return true;
        }

        protected void afterAll() {
            /* do nothing */
        }

    }


    /**
     * <i><b>listen</b></i>
     *
     * <pre> private void listen() </pre>
     *
     * Receive and read data from the peer.
     */
    private void listen() {
        if(connectedAddress == null)
            throw new IllegalStateException("No peer connected.");

        byte[] buffer;
        DatagramPacket packet;
        String message;

        while (!backgroundThread.isInterrupted()) {

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
     * Stop listening by interrupting the background thread.
     */
    private void stopListening() {
        if (backgroundThread.isAlive() && !backgroundThread.isInterrupted()) {
            socket.close();
            backgroundThread.interrupt();
        }
    }

    /*******************************************/
    /*                   Send                  */
    /*******************************************/

    /**
     * <i><b>send</b></i>
     *
     * <pre> protected void send({@link String} message) </pre>
     *
     * Send a message to the peer if the connection is up.
     * @param message the message to send.
     */
    protected void send(String message) {
        if(!isConnected())
            return;

        byte[] buffer = message.getBytes();
        try {
            socket.send(new DatagramPacket(buffer, buffer.length, connectedAddress, used_port));
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
        if (connectedAddress != null && !backgroundThread.isInterrupted()) {
            backgroundThread.interrupt();
            socket.close();
            try {
                backgroundThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            connectedAddress = null;
        }
    }

    /**
     * <i><b>finish</b></i>
     *
     * <pre> protected void finish() </pre>
     *
     * Close and reset all that need to.
     */
    protected void finish() {
        if (connectedAddress == null)
            return;

        closeConnection();
        connectedAddress = null;
    }

    /**
     * <i><b>closeConnection</b></i>
     *
     * <pre> private void closeConnection() </pre>
     *
     * End the communication by sending an end message.
     */
    private void closeConnection() {
        if(connectedAddress == null)
            return;

        try {
            socket = new DatagramSocket(used_port);
        } catch (SocketException e) {
            e.printStackTrace();
        }

        byte[] buffer = getEndConnectionMessage().getBytes();
        try {
            socket.send(new DatagramPacket(buffer, buffer.length, connectedAddress, used_port));
        } catch (IOException e) {
            e.printStackTrace();
        }

        socket.close();
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

