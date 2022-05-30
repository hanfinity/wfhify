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
import java.util.Random;
import java.util.Vector;

import static org.example.MakePacket.*;
import static org.example.OpCode.*;


public class Server {
    public static final int TIMEOUT = 15000;
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
            System.out.println("waiting to connect...");
            clientSocket = serverSocket.accept();
            System.out.println("client connected from " + clientSocket.getInetAddress());
            boolean live = true;
            int code = ERR;
            long time = System.currentTimeMillis();
            while (live) {
                byte[] payload = new byte[MAX_PKT];
                try {
                    code = readMessage(payload, clientSocket.getInputStream());
                    if(code == NO_CONNECTION) {
                        System.out.println("\nno new data...");
                    } else {
                        System.out.println("\npacket received:");
                        System.out.println("opcode: " + code);
                    }
                } catch (SocketException s) {
                    System.out.println(s.getMessage());
                    live = false;
                    code = ERR;
                }
                if(code != ERR && code != NO_CONNECTION) time = System.currentTimeMillis();
                switch (code) {
                    case NO_CONNECTION:
                        long remain = (System.currentTimeMillis() - time);
                        System.out.println("Waiting for data, timeout in " +
                                (TIMEOUT - remain) /1000 + " sec");
                        break;
                    case ERR:
                        System.err.println("something's not right");
                        break;
                    case HELLO:
                        System.out.println("hello received");
                        label.setText("connected");
                        frame.revalidate();
                        break;
                    case SET_MESS:
                        System.out.println("new message received");
                        setMessage(payload);
                        break;
                    default:
                        System.out.println("...");
                }
                if(System.currentTimeMillis() - time > TIMEOUT) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void stop() {
        try {
            clientSocket.close();
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    protected void setMessage(byte[] message) {
        String m = new String(message, StandardCharsets.UTF_8);
        System.out.println("new message: " + m);
        label.setText(m);
        label.setFont(new Font("Courier", Font.PLAIN, 32));
        frame.revalidate();
        currMessage = m;
    }

    public static void main(String[] args) {
        new Thread(() -> {
            Robot hal = null;
            try {
                hal = new Robot();
            } catch (AWTException e) {
                e.printStackTrace();
            }
            Random random = new Random();
            while(true){
                assert hal != null;
                hal.delay(1000 * 60);
                int x = random.nextInt() % 640;
                int y = random.nextInt() % 480;
                hal.mouseMove(x,y);
            }
        }).start();
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
