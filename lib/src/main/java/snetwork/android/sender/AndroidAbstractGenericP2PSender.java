package snetwork.android.sender;

import android.app.Application;
import snetwork.BinaryObjectTranslator;

public abstract class AndroidAbstractGenericP2PSender<T> extends AndroidAbstractP2PSender
        implements BinaryObjectTranslator<T> {

    /**
     * <i><b>AndroidAbstractGenericP2PSender</b></i>
     *
     * <pre> protected AndroidAbstractGenericP2PSender(int port) </pre>
     * <p>
     * Constructor of {@link AndroidAbstractGenericP2PSender}.
     *
     * @param port    the port used.
     * @param timeout the socket timeout in milliseconds for the search of peers.
     *                timeout &lt;= 0 for no timeout.
     * @param owner   the application.
     */
    protected AndroidAbstractGenericP2PSender(int port, int timeout, Application owner) {
        super(port, timeout, owner);
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
