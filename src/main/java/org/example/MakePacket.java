package org.example;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static org.example.OpCode.*;

public class MakePacket {
    public static final int MAX_PKT = 100;
    static byte[] header(int opcode, int length) {
        byte[] toReturn = new byte[8];
        System.arraycopy(ByteBuffer.allocate(4).putInt(opcode).array(),
                0,
                toReturn,
                0, 4);
        System.arraycopy(ByteBuffer.allocate(4).putInt(length).array(),
                0,
                toReturn,
                4, 4);
        return toReturn;
    }

    /**
     * Construct a byte array representing a wfh_pkt_hello
     * @param password user's password used to identify them
     * @return the byte array to be transmitted
     * @throws Exception if the password is the wrong size
     */
    static byte[] hello(String password) throws Exception {
        byte[] toReturn = new byte[8 + 24];
        if(password.length() < 20 && password.length() > 0) {
            System.arraycopy(header(HELLO, 24), 0, toReturn, 0, 8);
            System.arraycopy(ByteBuffer.allocate(4).putInt(0xFACE0FF1).array(), 0, toReturn, 8, 4);
            System.arraycopy(password.getBytes(StandardCharsets.UTF_8), 0, toReturn, 12, password.length());
            for(int i=password.length(); i<20; ++i) {
                toReturn[i + 12] = 0x00;
            }
        } else {
            throw(new Exception("invalid password"));
        }
        return toReturn;
    }

    /**
     * create a set message packet
     * @param message the message to be displayed
     * @return a configured byte array
     * @throws Exception if message is invalid
     */
    static byte[] set_mess(String message) throws Exception {
        byte[] toReturn = new byte[48];
        if(message.length() > 40 || message.length() < 1) {
            throw(new Exception("message size invalid"));
        } else {
            System.arraycopy(header(SET_MESS, 40), 0, toReturn, 0, 8);
            System.arraycopy(message.getBytes(StandardCharsets.UTF_8), 0, toReturn, 8, message.length());
            for(int i=message.length(); i<40; ++i) {
                toReturn[i + 8] = 0x00;
            }
        }
        return toReturn;
    }
}
