package org.example;

/**
 * Hello world!
 *
 */
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Vector;

import static org.example.MakePacket.MAX_PKT;
import static org.example.OpCode.HELLO;
import static org.example.OpCode.SET_MESS;


public class Server {
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter pw;
    private BufferedReader in;
    private JFrame frame;
    private JLabel label;
    private String currMessage;
    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private void createAndShowGUI(String message) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        //Create and set up the window.
        frame = new JFrame("HelloWorldSwing");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        gd.setFullScreenWindow(frame);

        //Add the ubiquitous "Hello World" label.
        label = new JLabel(message, SwingConstants.CENTER);
        label.setHorizontalAlignment(JLabel.CENTER);
        label.setVerticalAlignment(JLabel.CENTER);
        label.setFont(new Font("Courier", Font.PLAIN, 48));
        frame.add(label);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public void start(int port) {
        try {
            serverSocket = new ServerSocket(port);
            clientSocket = serverSocket.accept();
            System.out.println("client connected from " + clientSocket.getInetAddress());
            boolean live = true;
            pw = new PrintWriter(clientSocket.getOutputStream(), true);
            while (live) {
                byte[] payload = new byte[MAX_PKT];
                byte[] opcode = new byte[4];
                byte[] size = new byte[4];
                int code;
                int msg_length = MAX_PKT + 8;
                byte b;
                int i = 0;
                try {
                    do {
                        b = (byte) clientSocket.getInputStream().read();
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
                        System.out.print(b + ",");
                        ++i;
                    } while (b != -1 && i < msg_length + 8);
                } catch (SocketException s) {
                    System.out.println(s.getMessage());
                    live = false;
                }
                code = ByteBuffer.wrap(opcode).getInt();
                System.out.println("\npacket received: " + Arrays.toString(payload));
                System.out.println("opcode: " + code);
                System.arraycopy(payload, 0, opcode, 0, 4);
                switch (code) {
                    case HELLO:
                        System.out.println("hello received");
                        label.setText("Connected");
                        frame.revalidate();
                        break;
                    case SET_MESS:
                        System.out.println("new message received");
                        setMessage(payload);
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void stop() {
        try {
            in.close();
            pw.close();
            clientSocket.close();
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    protected void setMessage(byte[] message) {
        String m = new String(message, StandardCharsets.UTF_8);
        System.out.println("New Message: " + m);
        label.setText(m);
        label.setFont(new Font("Courier", Font.PLAIN, 32));
        frame.revalidate();
        currMessage = m;
    }

    public static void main(String[] args) {
        Server server=new Server();
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    server.createAndShowGUI(Inet4Address.getLocalHost().toString());
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
            }
        });
        while(true){
            server.start(6666);
            server.stop();
        }
    }
}
