package snetwork;

public interface BinaryObjectTranslator<T> {

    /**
     * <i><b>translateFromBytes</b></i>
     *
     * <pre> public T translateFromBytes(byte[] message) </pre>
     *
     * @param message the message to translate.
     * @return the T object obtained from the message.
     */
    T translateFromBytes(byte[] message);

    /**
     * <i><b>translateToBytes</b></i>
     *
     * <pre> public byte[] translateToBytes(T message) </pre>
     *
     * @param message the message to translate.
     * @return the byte array obtained from the message.
     */
    byte[] translateToBytes(T message);

}
