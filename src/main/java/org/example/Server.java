package org.example;

/**
 * Hello world!
 *
 */
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Vector;

import static org.example.MakePacket.MAX_PKT;
import static org.example.MakePacket.readMessage;
import static org.example.OpCode.*;


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
            int code = ERR;
            while (live) {
                byte[] payload = new byte[MAX_PKT];
                try {
                    code = readMessage(payload, clientSocket.getInputStream());
                } catch (SocketException s) {
                    System.out.println(s.getMessage());
                    live = false;
                }
                System.out.println("\npacket received: " + Arrays.toString(payload));
                System.out.println("opcode: " + code);
                switch (code) {
                    case ERR:
                        System.err.println("invalid message");
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
