package snetwork.receiver;

public abstract class AbstractStringP2PReceiver extends AbstractGenericP2PReceiver<String> {

    /**
     * <i><b>AbstractStringP2PReceiver</b></i>
     *
     * <pre> protected AbstractStringP2PReceiver(int port, int timeout) </pre>
     * <p>
     * Constructor of {@link AbstractStringP2PReceiver}.
     *
     * @param port    the used port.
     * @param timeout the socket timeout in milliseconds. timeout <= 0 for no timeout.
     */
    protected AbstractStringP2PReceiver(int port, int timeout) {
        super(port, timeout);
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
