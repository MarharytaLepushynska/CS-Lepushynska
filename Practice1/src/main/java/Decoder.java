import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

public class Decoder {
    private static final String secret = "1234567891234567";

    public Message decode(byte[] input) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        validateInput(input);
        ByteBuffer buffer = ByteBuffer.wrap(input);

        byte bMagic = buffer.get();
        validateBMagic(bMagic);
        byte bSrc = buffer.get();
        long bPktId = buffer.getLong();
        int wLen = buffer.getInt();
        validateWLen(wLen);

        short wCrc16 = buffer.getShort();
        short checkSum = Crc16.calculateCrc(input, 0, 14);
        validateCrc(checkSum, wCrc16);

        int cType = buffer.getInt();
        int bUserId = buffer.getInt();

        byte[] cipherText = new byte[wLen - 8];
        buffer.get(cipherText);
        Cipher cipher = Cipher.getInstance("AES");
        Key key = new SecretKeySpec(secret.getBytes(), "AES");
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] plainText = cipher.doFinal(cipherText);

        short wCrc162 = buffer.getShort();
        short checkSum2 = Crc16.calculateCrc(input, 16, wLen);
        validateCrc(checkSum2, wCrc162);

        return new Message(bSrc, bPktId, cType, bUserId, new String(plainText));
    }

    private void validateCrc(short expected, short actual) {
        if (expected != actual) {
            throw new IllegalArgumentException("Checksum does not match");
        }
    }

    private void validateBMagic(byte bMagic) {
        if(bMagic != (byte)0x13) {
            throw new IllegalArgumentException("BMAGIC does not match");
        }
    }

    private void validateInput(byte[] input) {
        if(input == null || input.length < 24) {
            throw new IllegalArgumentException("Ivalid message");
        }
    }

    private void validateWLen(int wLen) {
        if(wLen < 8) {
            throw new IllegalArgumentException("W length does not match");
        }
    }
}
