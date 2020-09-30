package snetwork.android.sender;

import android.app.Application;

public abstract class AndroidAbstractStringP2PSender extends AndroidAbstractGenericP2PSender<String> {

    /**
     * <i><b>AndroidAbstractStringP2PSender</b></i>
     *
     * <pre> protected AndroidAbstractStringP2PSender(int port) </pre>
     * <p>
     * Constructor of {@link AndroidAbstractStringP2PSender}.
     *
     * @param port    the port used.
     * @param timeout the socket timeout in milliseconds for the search of peers.
     *                timeout <= 0 for no timeout.
     * @param owner   the application.
     */
    protected AndroidAbstractStringP2PSender(int port, int timeout, Application owner) {
        super(port, timeout, owner);
    }

    /**
     * <i><b>translateFromBytes</b></i>
     *
     * <pre> </pre>protected String translateFromBytes(byte[] message) </pre>
     *
     * @param message the message to translate.
     * @return the String obtained from the message.
     */
    @Override
    public String translateFromBytes(byte[] message) {
        return new String(message);
    }

    /**
     * <i><b>translateToBytes</b></i>
     *
     * <pre> </pre>protected byte[] translateToBytes(String message) </pre>
     *
     * @param message the message to translate.
     * @return the byte array obtained from the message.
     */
    @Override
    public byte[] translateToBytes(String message) {
        return message.getBytes();
    }

}
