import org.apache.commons.codec.binary.Hex;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

class EncoderTest {
    private static final Encoder encoder = new Encoder();

    @Test
    void encodeTheSameWayEveryTime() throws Exception {
        Message message = new Message((byte) 0x12, 1, 2, 3, "Hello");
        byte[] encoded = encoder.encode(message);
        Assertions.assertEquals("1312000000000000000100000018cb1600000002000000033892efab6427ab1568092cd2262387d5799a", Hex.encodeHexString(encoded));
    }

    @Test
    void shouldAddMagicNumber() throws Exception {
        Message message = new Message((byte) 0x12, 1, 2, 3, "Hello");
        byte[] encoded = encoder.encode(message);
        Assertions.assertEquals(0x13, encoded[0]);
    }

    @Test
    void shouldAddBSrc() throws Exception{
        Message message = new Message((byte) 0x12, 1, 2, 3, "Hello");
        byte[] encoded = encoder.encode(message);
        Assertions.assertEquals(0x12, encoded[1]);
    }

    @Test
    void shouldEncodeBPktIdCorrectly() throws Exception{
        Message message = new Message((byte) 0x12, 1, 2, 3, "Hello");
        byte[] encoded = encoder.encode(message);

        ByteBuffer buffer = ByteBuffer.wrap(encoded);
        long bPktId = buffer.getLong(2);
        Assertions.assertEquals(1L, bPktId);
    }

    @Test
    void shouldCountWLenCorrectly() throws Exception{
        Message message = new Message((byte) 0x12, 1, 2, 3, "Hello");
        byte[] encoded = encoder.encode(message);

        ByteBuffer buffer = ByteBuffer.wrap(encoded);
        int wLen = buffer.getInt(10);
        Assertions.assertEquals(24, wLen);
    }

    @Test
    void shouldCountHeaderCrcCorrectly() throws Exception{
        Message message = new Message((byte) 0x12, 1, 2, 3, "Hello");
        byte[] encoded = encoder.encode(message);

        ByteBuffer buffer = ByteBuffer.wrap(encoded);
        short actual = buffer.getShort(14);
        short expected = Crc16.calculateCrc(encoded, 0, 14);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void shouldCountPayloadCrcCorrectly() throws Exception{
        Message message = new Message((byte) 0x12, 1, 2, 3, "Hello");
        byte[] encoded = encoder.encode(message);

        ByteBuffer buffer = ByteBuffer.wrap(encoded);
        int wLen = buffer.getInt(10);

        short actual = buffer.getShort(16+wLen);
        short expected = Crc16.calculateCrc(encoded, 16, wLen);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void shouldCryptMessage() throws Exception{
        Message message = new Message((byte) 0x12, 1, 2, 3, "Hello");
        byte[] encoded = encoder.encode(message);

        ByteBuffer buffer = ByteBuffer.wrap(encoded);
        int wLen = buffer.getInt(10);

        byte[] text = new byte[wLen-8];
        buffer.get(24, text);
        String crypted = Hex.encodeHexString(text);
        Assertions.assertNotEquals(crypted, "Hello");
    }

    @Test
    void shouldEncodeDifferentMessagesDifferently() throws Exception {
        Message message1 = new Message((byte) 0x12, 1, 2, 3, "Hello");
        Message message2 = new Message((byte) 0x14, 11, 7, 36, "Bye");
        byte[] encoded1 = encoder.encode(message1);
        byte[] encoded2 = encoder.encode(message2);
        Assertions.assertNotEquals(Hex.encodeHexString(encoded1), Hex.encodeHexString(encoded2));
    }
}
