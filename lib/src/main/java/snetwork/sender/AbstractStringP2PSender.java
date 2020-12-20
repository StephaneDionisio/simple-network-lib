package snetwork.sender;

public abstract class AbstractStringP2PSender extends AbstractGenericP2PSender<String> {

    /**
     * <i><b>AbstractStringP2PSender</b></i>
     *
     * <pre> protected AbstractStringP2PSender(int port, int timeout) </pre>
     * <p>
     * Constructor of {@link AbstractStringP2PSender}.
     *
     * @param port    the port used.
     * @param timeout the socket timeout in milliseconds for the search of peers.
     */
    protected AbstractStringP2PSender(int port, int timeout) {
        super(port, timeout);
    }

    /**
     * <i><b>translateFromBytes</b></i>
     *
     * <pre> protected String translateFromBytes(byte[] message) </pre>
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
     * <pre> protected byte[] translateToBytes(String message) </pre>
     *
     * @param message the message to translate.
     * @return the byte array obtained from the message.
     */
    @Override
    public byte[] translateToBytes(String message) {
        return message.getBytes();
    }

}
