import javacard.framework.*;
import javacard.security.*;
import javacardx.crypto.Cipher;

import java.util.Arrays;


/**
 * AES CBC applet that supports encryption and decryption.
 *
 * CLA | Meaning
 * -----+-----------------------------
 * 0x00 | ENCRYPT (AES CBC Encryption)
 * 0x10 | DECRYPT (AES CBC Decryption)
 * 0x20 | Set IV value (for AES CBC)
 * 0x30 | Set Key value (for AES CBC)
 *
 * INS | Meaning
 * -----+-----------------------------
 * 0x10 | P_START (Encrypt/Decrypt 128bit blocks of data with AES CBC)
 * 0x20 | INS_P_END (Encrypt/Decrypt the last block of data with AES CBC)
 */
public class AES_Applet extends Applet {

    /*-----Command Constants-----*/
    public static final byte INS_SET_ENCRYPT = 0x00;
    public static final byte INS_SET_DECRYPT = 0x10;
    public static final byte INS_SET_IV = 0x20;
    public static final byte INS_SET_KEY = 0x30;

    public static final byte INS_P_START = 0x10;
    public static final byte INS_P_END = 0x20;

    /*-----Fields kept in EEPROM-----*/
    protected static Cipher aesCipher;
    protected static AESKey aesKey;

    /*-----Algorithm Constants-----*/
    private static final byte[] key = {0x2d, 0x2a, 0x2d, 0x42, 0x55, 0x49, 0x4c, 0x44, 0x41, 0x43, 0x4f, 0x44, 0x45, 0x2d, 0x2a, 0x2d};
    private static byte[] iv = {0x2d, 0x2a, 0x2d, 0x42, 0x55, 0x49, 0x4c, 0x44, 0x41, 0x43, 0x4f, 0x44, 0x45, 0x2d, 0x2a, 0x2d};
    private static final int BYTE_LENGTH = 16;

    /** This is the Class Constructor used for initialising Fields kept in EEPROM.*/
    private AES_Applet() {
        /* create cipher object */
        aesCipher = Cipher.getInstance(Cipher.ALG_AES_BLOCK_128_CBC_NOPAD, false);

        /* create cipher key object */
        aesKey = (AESKey) KeyBuilder.buildKey(KeyBuilder.TYPE_AES, KeyBuilder.LENGTH_AES_128, false);
        aesKey.setKey(key, (short) 0);
        register();
    }

    /** This is the method used when first installing the Applet on the Java Card. */
    public static void install(byte[] bArray, short bOffset, byte BLength) {
        new AES_Applet();
    }

    @Override
    public void process(APDU apdu) throws ISOException {
        byte[] buffer = apdu.getBuffer();

        /* check if CLA is the one for signing; throws error if not*/
        /** This is an extra validation for future modification if need be.*/
        switch (buffer[ISO7816.OFFSET_CLA]) {
            case INS_SET_KEY:
                aesKey.setKey(buffer, ISO7816.OFFSET_CDATA);
                break;
            case INS_SET_IV:
                setIv(buffer);
                break;
            case INS_SET_ENCRYPT: {
                setEncrypt();
                applyAES(apdu);
                break;
            }
            case INS_SET_DECRYPT: {
                setDecrypt();
                applyAES(apdu);
                break;
            }
            default:
                ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
        }
    }

    private void setIv(byte[] buffer) {
        if ((short) buffer[ISO7816.OFFSET_CDATA - 1] != BYTE_LENGTH) {
            ISOException.throwIt(ISO7816.SW_DATA_INVALID);
        }
        iv = Arrays.copyOfRange(buffer, ISO7816.OFFSET_CDATA, ISO7816.OFFSET_CDATA + 16);
    }

    private void setEncrypt() {
        aesCipher.init(aesKey, Cipher.MODE_ENCRYPT, iv, (short) 0, (short) BYTE_LENGTH);
    }

    private void setDecrypt() {
        aesCipher.init(aesKey, Cipher.MODE_DECRYPT, iv, (short) 0, (short) BYTE_LENGTH);
    }

    public static void applyAES(APDU apdu) {

        byte[] buffer = apdu.getBuffer();
        short len = apdu.setIncomingAndReceive();

        switch (buffer[ISO7816.OFFSET_INS]) {

            /* Feeding the first N-1 blocks of data to the AES CBC Cipher Object*/
            case INS_P_START:
                aesCipher.update(buffer, ISO7816.OFFSET_CDATA, len, buffer, (short) 0x00);
                apdu.setOutgoingAndSend((short) 0, len);
                break;

            /* Feeding the last block of data to the AES CBC Cipher Object*/
            case INS_P_END:
                aesCipher.doFinal(buffer, ISO7816.OFFSET_CDATA, len, buffer, (short) 0x00);
                apdu.setOutgoingAndSend((short) 0, len);
                break;
            default:
                ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
        }
    }
}
