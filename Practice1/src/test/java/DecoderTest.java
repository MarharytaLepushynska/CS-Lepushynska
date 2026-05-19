import org.apache.commons.codec.binary.Hex;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DecoderTest {

    private static final Decoder SUT = new Decoder();
    private static final Encoder encoder = new Encoder();

    @Test
    void shouldDecodeTheSameWayEveryTime() throws Exception {
        Message actual = SUT.decode(Hex.decodeHex("1312000000000000000100000018cb1600000002000000033892efab6427ab1568092cd2262387d5799a"));

        org.assertj.core.api.Assertions.assertThat(actual)
                .returns((byte) 0x12, Message::getbSrc)
                .returns(1L, Message::getbPktId)
                .returns(2, Message::getcType);
    }

    @Test
    void shouldThrowExceptionWhenIncorrectMagicNumber() throws Exception {
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                SUT.decode(Hex.decodeHex("1112000000000000000100000018cb1600000002000000033892efab6427ab1568092cd2262387d5799a")));
    }

    @Test
    void shouldThrowExceptionWhenIncorrectHeaderCrc() throws Exception{
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                SUT.decode(Hex.decodeHex("1312000000000000000100000018cc1600000002000000033892efab6427ab1568092cd2262387d5799a")));
    }

    @Test
    void shouldThrowExceptionWhenIncorrectPayloadCrc() throws Exception{
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                SUT.decode(Hex.decodeHex("1312000000000000000100000018cb1600000002000000033892efab6427ab1568092cd2262387d5799b")));
    }

    @Test
    void shouldThrowExceptionWhenIncorrectMessage() throws Exception{
        byte[] input = new byte[]{};
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                SUT.decode(input));
    }

    @Test
    void shouldTrowExceptionWhenIncorrectWLen() throws Exception{
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                SUT.decode(Hex.decodeHex("1312000000000000000100000007cc1600000002000000033892efab6427ab1568092cd2262387d5799a")));
    }

    @Test
    void shouldDecodeMessageCorrectly() throws Exception {
        Message actual = SUT.decode(Hex.decodeHex("1312000000000000000100000018cb1600000002000000033892efab6427ab1568092cd2262387d5799a"));
        Assertions.assertEquals("Hello", actual.getMessage());
    }

    @Test
    void shouldDecodeSameTwoMessagesTheSameWay() throws Exception {
        Message first = SUT.decode(Hex.decodeHex("1312000000000000000100000018cb1600000002000000033892efab6427ab1568092cd2262387d5799a"));
        Message second = SUT.decode(Hex.decodeHex("1312000000000000000100000018cb1600000002000000033892efab6427ab1568092cd2262387d5799a"));
        Assertions.assertEquals(first.getMessage(), second.getMessage());
        Assertions.assertEquals(first.getbSrc(), second.getbSrc());
        Assertions.assertEquals(first.getbPktId(), second.getbPktId());
        Assertions.assertEquals(first.getcType(), second.getcType());
        Assertions.assertEquals(first.getbUserId(), second.getbUserId());
    }

    @Test
    void shouldDecodeDifferentTwoMessagesDifferently() throws Exception {
        Message first = SUT.decode(Hex.decodeHex("1312000000000000000100000018cb1600000002000000033892efab6427ab1568092cd2262387d5799a"));
        Message second = SUT.decode(Hex.decodeHex("1314000000000000000b000000180c87000000070000002488f945a537ec00b13d6d4b519a06ea2d6e92"));
        Assertions.assertNotEquals(first.getMessage(), second.getMessage());
        Assertions.assertNotEquals(first.getbSrc(), second.getbSrc());
        Assertions.assertNotEquals(first.getbPktId(), second.getbPktId());
        Assertions.assertNotEquals(first.getcType(), second.getcType());
        Assertions.assertNotEquals(first.getbUserId(), second.getbUserId());
    }

    @Test
    void shouldEncodeAndDecodeMessageCorrectly() throws Exception {
        Message actual = SUT.decode(Hex.decodeHex("1312000000000000000100000018cb1600000002000000033892efab6427ab1568092cd2262387d5799a"));

        byte[] encoded = encoder.encode(actual);
        Message decoded = SUT.decode(encoded);

        Assertions.assertEquals(actual.getMessage(), decoded.getMessage());
        Assertions.assertEquals(actual.getbSrc(), decoded.getbSrc());
        Assertions.assertEquals(actual.getbPktId(), decoded.getbPktId());
        Assertions.assertEquals(actual.getcType(), decoded.getcType());
        Assertions.assertEquals(actual.getbUserId(), decoded.getbUserId());
    }
}
