package snetwork.sender;

import snetwork.BinaryObjectTranslator;

/**
 * Same class as {@link AbstractP2PSender} but it works with generic object instead of bytes arrays.
 */
public abstract class AbstractGenericP2PSender<T> extends AbstractP2PSender implements BinaryObjectTranslator<T> {

    /**
     * <i><b>AbstractGenericP2PSender</b></i>
     *
     * <pre> protected AbstractGenericP2PSender(int port, int timeout) </pre>
     * <p>
     * Constructor of {@link AbstractGenericP2PSender}.
     *
     * @param port    the port used.
     * @param timeout the socket timeout in milliseconds for the search of peers.
     */
    protected AbstractGenericP2PSender(int port, int timeout) {
        super(port, timeout);
    }

    /**
     * <i><b>getConnectionMessage</b></i>
     *
     * <pre> protected {@link T} getConnectionMessage() </pre>
     *
     * @return the message which will be send as a start connection message.
     */
    protected abstract  T getConnectionTranslatedMessage();

    @Override
    protected final byte[] getConnectionMessage() {
        return translateToBytes( getConnectionTranslatedMessage() );
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
        return translateToBytes( getEndConnectionTranslatedMessage() );
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

}
