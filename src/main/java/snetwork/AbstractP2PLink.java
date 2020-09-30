package snetwork;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;

/**
 * Abstract class for a Peer-to-Peer communication protocol.
 */
@SuppressWarnings({"DanglingJavadoc", "WeakerAccess", "unused"})
public abstract class AbstractP2PLink {

    /**
     * Listened port.
     */
    private final int usedPort;

    /**
     * Socket of this program.
     */
    private DatagramSocket socket;

    /**
     * Address of the connected peer.
     */
    private InetAddress connectedAddress;

    /**
     * Thread where all background stuff is done.
     */
    private ListenerThread backgroundThread;

    /**
     * The socket timeout.
     */
    private final int timeout;

    /*******************************************/
    /*              Constructor                */
    /*******************************************/

    /**
     * <i><b>AbstractP2PReceiver</b></i>
     *
     * <pre> protected AbstractP2PReceiver(int port, int timeout) </pre>
     *
     * Constructor of {@link snetwork.AbstractP2PLink}.
     * @param port the port used.
     * @param timeout the socket timeout in milliseconds. timeout <= 0 for no timeout.
     */
    protected AbstractP2PLink(int port, int timeout) {
        this.usedPort = port;
        this.timeout = timeout;
    }

    /**
     * <i><b>initSocket</b></i>
     *
     * <pre> protected void initSocket() </pre>
     *
     * The method used to initialize the socket.
     */
    private void initSocket() throws BindException {
        // Loop in case the socket is closing and so not already open.
        long time = System.currentTimeMillis();
        while(true) {
            try {
                this.socket = new DatagramSocket(this.usedPort);
                if (this.timeout > 0)
                    socket.setSoTimeout(this.timeout);
                break;
            } catch (BindException e) {
                /* Retry during 3s in case the socket is closing and so not already open. */
                if(System.currentTimeMillis() - time >= 3000) {
                    System.err.println("Socket already in use (port: " + usedPort + ").");
                    throw e;
                }
            } catch (SocketException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    /**
     * <i><b>init</b></i>
     *
     * <pre> protected void init() </pre>
     *
     * The method used to initialize the socket and the background thread.
     */
    protected final void init() throws BindException {
        if (this.socket == null || this.socket.isClosed()) {
            initSocket();
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
     * <i><b>getSocket</b></i>
     *
     * <pre> protected {@link DatagramSocket} getSocket() </pre>
     *
     * @return the used udp socket.
     */
    protected DatagramSocket getSocket() {
        return socket;
    }

    /**
     * <i><b>getUsedPort</b></i>
     *
     * <pre> protected int getUsedPort() </pre>
     *
     * @return the used port.
     */
    protected int getUsedPort() {
        return usedPort;
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
     * @param connectionCallback the function which will be called after the connection has been established.
     *                           If the connection success the argument is true, false otherwise.
     */
    public abstract void startProtocol(SuccessCallback connectionCallback) throws BindException;

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
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
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

        @SuppressWarnings("EmptyMethod")
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

        while (!backgroundThread.isInterrupted()) {

            try {

                buffer = new byte[1000];
                packet = new DatagramPacket(buffer, buffer.length);

                socket.receive(packet);
                if (!packet.getAddress().equals(connectedAddress))
                    continue;

                buffer = Arrays.copyOf(buffer, packet.getLength());

                /* END */
                if (isEndConnection(buffer)) {
                    stopListening();

                } else {
                    onListening(buffer);
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
            backgroundThread.interrupt();
            socket.close();
        }
    }

    /*******************************************/
    /*                   Send                  */
    /*******************************************/

    /**
     * <i><b>send</b></i>
     *
     * <pre> protected void send(byte[] message) </pre>
     *
     * Send a message to the peer if the connection is up.
     * @param message the message to send.
     */
    protected void send(byte[] message) {
        if(!isConnected())
            return;

        send(message, connectedAddress);
    }

    /**
     * <i><b>send</b></i>
     *
     * <pre> protected void send(byte[] message, {@link InetAddress} address) </pre>
     *
     * Send a message to the given address.
     * @param message the message to send.
     * @param address the destination.
     */
    protected void send(byte[] message, InetAddress address) {
        try {
            socket.send(new DatagramPacket(message, message.length, address, usedPort));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * <i><b>send</b></i>
     *
     * <pre> protected void send({@link String} message) </pre>
     *
     * Send a message to the peer if the connection is up.
     * @param message the message to send.
     */
    protected void send(String message) {
        send(message.getBytes());
    }

    /**
     * <i><b>send</b></i>
     *
     * <pre> protected void send({@link String} message, {@link InetAddress} address) </pre>
     *
     * Send a message to the given address.
     * @param message the message to send.
     */
    protected void send(String message, InetAddress address) {
        send(message.getBytes(), address);
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
        if (backgroundThread != null && !backgroundThread.isInterrupted()) {
            backgroundThread.interrupt();
            socket.close();
            try {
                backgroundThread.join();
                backgroundThread = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
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

        if(socket.isClosed()) {
            try {
                initSocket();
            } catch (BindException e) {
                /* Should not happen */
            }
        }

        byte[] buffer = getEndConnectionMessage();
        try {
            socket.send(new DatagramPacket(buffer, buffer.length, connectedAddress, usedPort));
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
     * <pre> protected boolean isAcceptableConnection(byte[] receivedMessage) </pre>
     *
     * @param receivedMessage the received message.
     * @return true if the received message is the waited message to start a connection, false otherwise.
     */
    protected abstract boolean isAcceptableConnection(byte[] receivedMessage);

    private boolean isEndConnection(byte[] receivedMessage) {
        byte[] endConnectionMessage = getEndConnectionMessage();
        return Arrays.equals(receivedMessage, endConnectionMessage);
    }

    /**
     * <i><b>getEndConnectionMessage</b></i>
     *
     * <pre> protected byte[] getEndConnectionMessage() </pre>
     *
     * @return the message which will be send to end a connection.
     */
    protected abstract byte[] getEndConnectionMessage();

    /**
     * <i><b>onListening</b></i>
     *
     * <pre> protected boolean onListening(byte[] receivedMassage) </pre>
     *
     * The action to when a message is received.
     * @param receivedMessage the received message.
     */
    protected abstract void onListening(byte[] receivedMessage);

}

