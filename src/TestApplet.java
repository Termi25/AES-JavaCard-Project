import javacard.framework.*;
import javacard.security.*;
import javacardx.crypto.*;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class TestApplet extends Applet{
    private RandomData rnd;

    private TestApplet(){
        rnd=RandomData.getInstance(RandomData.ALG_TRNG);
    }

    public static void install(byte[] bArray, short bOffset, byte BLength){
        (new TestApplet()).register();
    }

    public void process(APDU apdu){
        byte[] buffer=apdu.getBuffer();
        switch(buffer[ISO7816.OFFSET_INS]){
            case (byte)0x00:
                for(byte set:buffer){
                    System.out.printf("%02X ",set);
                }
                System.out.println();
                short bytesLeft = (short) (buffer[ISO7816.OFFSET_LC] & 0x00FF);
                short readCount=apdu.setIncomingAndReceive();
                for(short i=1;i<=bytesLeft;i++){
                    System.out.printf("%02X \n",buffer[ISO7816.OFFSET_LC+i]);
                    //readCount=apdu.receiveBytes(ISO7816.OFFSET_CDATA);
                }
                ByteBuffer buffer2=ByteBuffer.allocate(Long.BYTES);
                buffer2.putLong(System.nanoTime());
                short length=(short)(buffer[ISO7816.OFFSET_CDATA]&(short)0xff);
                rnd.setSeed(buffer2.array(),(short)0, (short) Long.BYTES);
                rnd.nextBytes(buffer,(short)0,length);
                apdu.setOutgoingAndSend((short)0,length);
                return;
        }
        ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
    }
}