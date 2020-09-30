package snetwork.receiver;

import snetwork.BinaryObjectTranslator;

public abstract class AbstractGenericP2PReceiver<T> extends AbstractP2PReceiver implements BinaryObjectTranslator<T> {

    /**
     * <i><b>AbstractGenericP2PReceiver</b></i>
     *
     * <pre> protected AbstractGenericP2PReceiver(int port, int timeout) </pre>
     * <p>
     * Constructor of {@link AbstractGenericP2PReceiver}.
     *
     * @param port    the used port.
     * @param timeout the socket timeout in milliseconds. timeout <= 0 for no timeout.
     */
    protected AbstractGenericP2PReceiver(int port, int timeout) {
        super(port, timeout);
    }

    /**
     * <i><b>isAcceptableConnection</b></i>
     *
     * <pre> protected boolean isAcceptableConnection(T receivedMessage) </pre>
     *
     * @param receivedMessage the received message.
     * @return true if the received message is the waited message to start a connection, false otherwise.
     */
    protected abstract boolean isAcceptableConnection(T receivedMessage);

    @Override
    protected final boolean isAcceptableConnection(byte[] receivedMessage) {
        return isAcceptableConnection( translateFromBytes(receivedMessage) );
    }

    /**
     * <i><b>getEndConnectionMessage</b></i>
     *
     * <pre> protected T getEndConnectionMessage() </pre>
     *
     * @return the message which will be send to end a connection.
     */
    protected abstract T getEndConnectionTranslatedMessage();

    @Override
    protected final byte[] getEndConnectionMessage() {
        return translateToBytes(getEndConnectionTranslatedMessage());
    }

    /**
     * <i><b>onListening</b></i>
     *
     * <pre> protected boolean onListening(T receivedMassage) </pre>
     *
     * The action to when a message is received.
     * @param receivedMessage the received message.
     */
    protected abstract void onListening(T receivedMessage);

    @Override
    protected final void onListening(byte[] receivedMessage) {
        onListening( translateFromBytes(receivedMessage) );
    }

    /**
     * <i><b>getAcceptConnectionMessage</b></i>
     *
     * <pre> </pre>protected T getAcceptConnectionMessage() </pre>
     *
     * @return the message which will be send as an ack on a successful connection.
     */
    protected abstract T getAcceptConnectionTranslatedMessage();

    @Override
    protected final byte[] getAcceptConnectionMessage() {
        return translateToBytes(getAcceptConnectionTranslatedMessage());
    }

}
