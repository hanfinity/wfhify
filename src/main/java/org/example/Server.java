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
    private JFrame frame;
    private JLabel label;
    private String currMessage;
    private InputStream in;
    private OutputStream os;
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
        frame.setLayout(new GridBagLayout());
        gd.setFullScreenWindow(frame);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;

        // add title
        JLabel title = new JLabel();
        title.setHorizontalAlignment(JLabel.LEFT);
        title.setVerticalAlignment(JLabel.TOP);
        title.setFont(new Font("Courier", Font.PLAIN, 36));
        title.setOpaque(true);
        title.setBackground(Color.CYAN);
        title.setText("wfhify");
        title.setMinimumSize(new Dimension(frame.getWidth(), 0));
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0.5;
        frame.add(title, c);

        c.gridy = 1;
        frame.add(new JSeparator(SwingConstants.HORIZONTAL), c);

        // message label
        label = new JLabel(message, SwingConstants.CENTER);
        label.setHorizontalAlignment(JLabel.CENTER);
        label.setVerticalAlignment(JLabel.BOTTOM);
        label.setFont(new Font("Courier", Font.PLAIN, 48));
        c.gridy = 2;
        frame.add(label, c);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public void start(int port) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("waiting to connect...");
            clientSocket = serverSocket.accept();
            os = clientSocket.getOutputStream();
            in = clientSocket.getInputStream();
            System.out.println("client connected from " + clientSocket.getInetAddress());
            boolean live = true;
            int code = ERR;
            long time = System.currentTimeMillis();
            while (live) {
                byte[] payload = new byte[MAX_PKT];
                try {
                    code = readMessage(payload, in);
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
                        Thread.sleep(1000);
                        break;
                    case ERR:
                        System.err.println("something's not right");
                        break;
                    case HELLO:
                        System.out.println("hello received");
                        setMessage("connected:\n" + clientSocket.getLocalAddress());
                        break;
                    case SET_MESS:
                        System.out.println("new message received");
                        setMessage(payload);
                        break;
                    case LIST_MESS:
                        System.out.println("client requested current message: " + currMessage);
                        os.write(list_mess(currMessage));
                        break;
                    default:
                        System.out.println("...");
                }
                if(System.currentTimeMillis() - time > TIMEOUT) {
                    break;
                }
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Something went wrong: " + e.getMessage());
        } catch (Exception e) {
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
        setMessage(m);
    }

    protected void setMessage(String m) {
        System.out.println("new message: " + m);
        label.setText("<html>" + m + "</html>");
        label.setFont(new Font("Courier", Font.PLAIN, 42));
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
