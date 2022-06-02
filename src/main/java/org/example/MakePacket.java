package org.example;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static org.example.OpCode.*;

public class MakePacket {
    public static final int MAX_PKT = 100;
    public static final int NO_CONNECTION = -16777216;

    /**
     * attempt to read a packet from a given input stream
     * @param payload byte array to return message contents
     * @param is InputStream to read the message from
     * @return op code of the read in message
     * @throws IOException if there is a problem reading from is
     */
    static int readMessage(byte[] payload, InputStream is) throws IOException {
        int code = ERR;
        int i = 0;
        byte[] opcode = new byte[4];
        byte[] size = new byte[4];
        int msg_length = MAX_PKT + 8;
        byte b;
        do {
            b = (byte) is.read();
            // if (b == -1) throw (new SocketException("no data from remote side"));
            if (i < 4) {
                opcode[i] = b;
            } else if (i < 8) {
                size[i - 4] = b;
                if (i == 7) {
                    msg_length = ByteBuffer.wrap(size).getInt();
                }
            } else {
                payload[i - 8] = b;
            }
            //System.out.print(b + ",");
            ++i;
        } while (b != -1 && i < msg_length + 8);
        code = ByteBuffer.wrap(opcode).getInt();
        return code;
    }

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
        return text_packet(SET_MESS, message);
    }

    static byte[] list_mess(String message) throws Exception {
        return text_packet(LIST_RESP, message);
    }

    static byte[] set_sched(String message, int start, int end) throws Exception {
        return generic_packet(MAKE_SCHED, message, start, end);
    }

    /**
     * create a packet instructing the server to return the current message
     * @return byte array representing the packet
     */
    static byte[] get_mess(){
        return header(LIST_MESS, 0);
    }

    static byte[] text_packet(int code, String message) throws Exception {
        return generic_packet(code, message, -1, -1);
    }

    private static byte[] generic_packet(int code, String message, int start, int end) throws Exception {

        byte[] toReturn;
        if(start == -1) toReturn = new byte[48];
        else toReturn = new byte[56];
        message = message.trim();
        if(message.length() > 40 || message.length() < 1) {
            throw(new Exception("message size invalid"));
        } else {
            System.arraycopy(header(code, 40), 0, toReturn, 0, 8);
            System.arraycopy(message.getBytes(StandardCharsets.UTF_8), 0, toReturn, 8, message.length());
            for(int i = message.length(); i<40; ++i) {
                toReturn[i + 8] = 0x00;
            }
        }
        if(code != -1) {
            System.arraycopy(ByteBuffer.allocate(4).putInt(start).array(),
                    0,
                    toReturn,
                    48, 4);
            System.arraycopy(ByteBuffer.allocate(4).putInt(start).array(),
                    0,
                    toReturn,
                    52, 4);
        }
        return toReturn;
    }

    public static byte[] error(int code) {
        byte[] toReturn = new byte[12];
        System.arraycopy(header(ERR, 4), 0, toReturn, 0, 8);
        System.arraycopy(ByteBuffer.allocate(4).putInt(code).array(),
                0,
                toReturn,
                8, 4);
        return toReturn;
    }
}
