package Tools;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

public class Encoder {
    private static final String secret = "1234567891234567";

    public byte[] encode(Message message) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        byte bMagic = 0x13;

        byte[] plainText = message.getMessage().getBytes();
        Cipher cipher = Cipher.getInstance("AES");
        Key key = new SecretKeySpec(secret.getBytes(), "AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] cipherText = cipher.doFinal(plainText);

        int wLen = cipherText.length + 8;
        ByteBuffer buffer = ByteBuffer.allocate(wLen + 18);

        buffer.put(bMagic);
        buffer.put(message.getbSrc());
        buffer.putLong(message.getbPktId());
        buffer.putInt(wLen);

        short wCrc16 = Crc16.calculateCrc(buffer.array(), 0, 14);
        buffer.putShort(wCrc16);

        buffer.putInt(message.getcType());
        buffer.putInt(message.getbUserId());

        buffer.put(cipherText);

        short wCrc162 = Crc16.calculateCrc(buffer.array(), 16, wLen);
        buffer.putShort(wCrc162);

        return buffer.array();
    }
}
