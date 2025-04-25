import com.licel.jcardsim.smartcardio.CardSimulator;
import com.licel.jcardsim.utils.AIDUtil;
import javacard.framework.AID;

import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import java.io.*;


public class Main {
    public static void main(String[] args) throws IOException {
//        CardSimulator simulator = new CardSimulator();
//
//// 2. install applet
//        AID appletAID = AIDUtil.create("F000000001");
//        simulator.installApplet(appletAID, TestApplet.class);
//
//// 3. select applet
//        simulator.selectApplet(appletAID);
//
//// 4. send APDU
//        CommandAPDU commandAPDU = new CommandAPDU(0x00, 0x00, 0x00, 0x00,new byte[]{(byte) 0xFF,0x03});
//        ResponseAPDU response = simulator.transmitCommand(commandAPDU);
//
//// 5. check response
//        byte[] array= response.getData();
//        for(byte set:array){
//            System.out.printf("%02X ",set);
//        }
        CardSimulator simulator = new CardSimulator();

        AID appletAID = AIDUtil.create("F000000001");
        simulator.installApplet(appletAID, AES_ENCRYPTOR.class);

        simulator.selectApplet(appletAID);
//        byte[] buffer=new byte[]{0x2d, 0x2a, 0x2d, 0x42, 0x55, 0x49, 0x4c, 0x44, 0x41, 0x43, 0x4f, 0x44, 0x45, 0x2d, 0x2a, 0x2d};
//        for(byte b: buffer){
//            System.out.printf("%02X",b);
//        }
//        System.out.println();
//        CommandAPDU commandAPDU = new CommandAPDU(0x00, 0x00, 0x00, 0x00, buffer);
//        ResponseAPDU response = simulator.transmitCommand(commandAPDU);
//        for(byte b: response.getData()){
//            System.out.printf("%02X ",b);
//        }


        File file=new File("test.txt");
        File outputFile=new File("EncryptedFile.enc");

        if(file.exists()){
            FileInputStream fis=new FileInputStream(file);
            BufferedInputStream bis=new BufferedInputStream(fis);

            FileOutputStream fos=new FileOutputStream(outputFile);
            BufferedOutputStream bos=new BufferedOutputStream(fos);

            byte[] buffer=new byte[32];
            bis.read(buffer);
            CommandAPDU commandAPDU = new CommandAPDU(0x00, 0x10, 0x00, 0x00, buffer);
            ResponseAPDU response = simulator.transmitCommand(commandAPDU);
            bos.write(response.getData());

            buffer=new byte[16];

            while(true){
                int noBytes=bis.read(buffer);
                if(noBytes==-1){
                    break;
                }
                commandAPDU = new CommandAPDU(0x00, 0x10, 0x00, 0x00, buffer);
                response = simulator.transmitCommand(commandAPDU);
                bos.write(response.getData());
            }
            commandAPDU = new CommandAPDU(0x00, 0x20, 0x00, 0x00, buffer);
            response = simulator.transmitCommand(commandAPDU);
            bos.write(response.getData());

            bis.close();
            bos.close();
        }

        if(outputFile.exists()){
            FileInputStream fis=new FileInputStream(file);
            BufferedInputStream bis=new BufferedInputStream(fis);

            byte[] buffer=new byte[16];
            int noBytes=bis.read(buffer);
            for(byte b:buffer){
                System.out.printf("%02X",b);
            }
        }
    }
}
