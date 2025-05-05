import com.licel.jcardsim.smartcardio.CardSimulator;
import com.licel.jcardsim.utils.AIDUtil;
import javacard.framework.AID;

import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import java.io.*;

/** An example host for testing the AES_Applet using jCardSim. */
public class Main {

    /*-----APDU Contract Mode Modifiers (see PQC_Applet)*/
    public static final byte ENCRYPT = 0x00;
    public static final byte DECRYPT = 0x10;

    public static void main(String[] args) throws IOException {

        /*-----Card Simulator Creation-----*/

        CardSimulator simulator = new CardSimulator();

        /*-----Card Applet Creation, Registration & Selection for use-----*/

        AID appletAID = AIDUtil.create("F000000001");
        simulator.installApplet(appletAID, AESApplet.class);

        simulator.selectApplet(appletAID);

        /*-----Initialising Test Folder Root-----*/

        File folder = new File("testFiles");
        if(!folder.isDirectory()){
            System.out.println("Test folder does not exists. Add a test folder with this name in project root: testFiles");
            return;
        }

        /*-----Running Applet for each of the test files-----*/
        /** For each test file it will create:
         *  1) AES CBC 128 bits with PKCS#7 Padding encrypted file (*.enc);
         *  2) AES CBC 128 bits with PKCS#7 Padding decrypted file (Decrypted_*). */

        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                continue;
            }
            if (file.getName().endsWith(".enc") || file.getName().startsWith("Decrypted_")) {
                continue;
            }

            /*-----Creating file AES encryption using the provided Applet-----*/

            File encFile = new File(folder, file.getName().split("\\.")[0] + ".enc");
            encryptFile(simulator, file, encFile, true);

            /*-----Creating file AES decryption using the provided Applet-----*/

            File decFile = new File(folder, "Decrypted_" + file.getName());
            encryptFile(simulator, encFile, decFile, false);
        }
    }

    public static void encryptFile(CardSimulator simulator, File inputFile, File outputFile, boolean isEncrypting) throws IOException {
        if (inputFile.exists()) {

            /*-----Checking Applet Mode-----*/

            byte claAESMode = ENCRYPT;
            if (isEncrypting) {
                claAESMode = DECRYPT;
            }

            /*-----Creating Input Stream-----*/

            FileInputStream fis = new FileInputStream(inputFile);

            /*-----Creating Output Stream-----*/

            FileOutputStream fos = new FileOutputStream(outputFile);
            BufferedOutputStream bos = new BufferedOutputStream(fos);

            /*-----Creating Input Stream Buffer-----*/
            /** The Offset variable is used to address blocks of bytes from the buffer to be sent to the Java Card. */

            byte[] buffer = fis.readAllBytes();
            int offset = 0;
            while (offset < buffer.length) {
                int remainingBytes = buffer.length - offset;
                boolean lastBlock = (remainingBytes <= 16);

                /*-----Send blocks of bytes to the Java Card for AES CBC processing-----*/
                /** When reaching the last block of bytes, we need to announce the Java Card
                 *  so that it may perform the final operations (finishing the AES CBC Encryption or Decryption).*/
                if (lastBlock) {
                    byte[] lastDataBlock = new byte[16];
                    System.arraycopy(buffer, offset, lastDataBlock, 0, remainingBytes);

                    /*-----Padding with PKCS#7 the last block-----*/

                    if (isEncrypting) {
                        int padding = 16 - remainingBytes;
                        if (padding > 0 && padding < 16) {
                            for (int i = remainingBytes; i < 16; i++) {
                                lastDataBlock[i] = (byte) padding;
                            }
                        }
                        ResponseAPDU response = simulator.transmitCommand(new CommandAPDU(claAESMode,0x20,0x00,0x00,lastDataBlock));
                        bos.write(response.getData());

                    } else {

                        /*-----Unpadding with PKCS#7 the last block-----*/

                        ResponseAPDU response = simulator.transmitCommand(new CommandAPDU(claAESMode,0x20,0x00,0x00,lastDataBlock));

                        lastDataBlock = response.getData();

                        int padding = lastDataBlock[15];
                        if(padding > 0 && padding < 16){
                            bos.write(lastDataBlock, 0, 16 - padding);
                        }else{
                            bos.write(lastDataBlock);
                        }
                    }
                } else {
                    byte[] dataBlock = new byte[16];
                    System.arraycopy(buffer, offset, dataBlock, 0, 16);
                    ResponseAPDU response = simulator.transmitCommand(new CommandAPDU(claAESMode,0x10,0x00,0x00,dataBlock));
                    bos.write(response.getData());
                }
                offset += 16;
            }

            fis.close();
            bos.close();
        }
    }
}
