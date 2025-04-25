import javacard.framework.*;
import javacard.security.*;
import javacardx.crypto.Cipher;


public class AES_ENCRYPTOR extends Applet{
    private static final byte[] key= {0x2d, 0x2a, 0x2d, 0x42, 0x55, 0x49, 0x4c, 0x44, 0x41, 0x43, 0x4f, 0x44, 0x45, 0x2d, 0x2a, 0x2d};
    private static final byte[] iv= {0x2d, 0x2a, 0x2d, 0x42, 0x55, 0x49, 0x4c, 0x44, 0x41, 0x43, 0x4f, 0x44, 0x45, 0x2d, 0x2a, 0x2d};
    protected static Cipher aesCipher;
    protected  static AESKey aesKey;
    public static final byte INS_SET_KEY = 0x00;
    public static final byte INS_ENCRYPT_START = 0x10;
    public static final byte INS_ENCRYPT_END = 0x20;

    private AES_ENCRYPTOR(){
        aesCipher=Cipher.getInstance(Cipher.ALG_AES_BLOCK_128_CBC_NOPAD, false);
        aesKey= (AESKey) KeyBuilder.buildKey(KeyBuilder.TYPE_AES, KeyBuilder.LENGTH_AES_128, false);
        aesKey.setKey(key,(short)0);
        aesCipher.init(aesKey, Cipher.MODE_ENCRYPT, iv,(short)0 ,(short)16);
        register();
    }

    public static void install(byte[] bArray, short bOffset, byte BLength){
        new AES_ENCRYPTOR();
    }

    @Override
    public void process(APDU apdu) throws ISOException {
        encrypt(apdu);
    }

    public static void encrypt(APDU apdu)
    {
        byte[] buffer = apdu.getBuffer();
        apdu.setIncomingAndReceive();
        short len = (short) (buffer[ISO7816.OFFSET_LC] & 0x00FF);

        switch (buffer[ISO7816.OFFSET_INS]) {
            case INS_SET_KEY:
                aesKey.setKey(buffer, ISO7816.OFFSET_CDATA);
                break;
            case INS_ENCRYPT_START:
                System.out.println("Intrat start");
                aesCipher.update(buffer, ISO7816.OFFSET_CDATA, len, buffer, (short) 0x00);
                apdu.setOutgoingAndSend((short) 0, len);
                break;
            case INS_ENCRYPT_END:
                System.out.println("Intrat end");
                aesCipher.doFinal(buffer, ISO7816.OFFSET_CDATA, len, buffer, (short) 0x00);
                apdu.setOutgoingAndSend((short) 0, len);
                break;
            default:
                ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
        }
    }
}
