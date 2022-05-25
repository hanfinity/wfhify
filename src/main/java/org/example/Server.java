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
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Vector;

import static org.example.MakePacket.MAX_PKT;
import static org.example.OpCode.HELLO;


public class Server {
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter pw;
    private BufferedReader in;
    private JFrame frame;
    private JLabel label;
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
            pw = new PrintWriter(clientSocket.getOutputStream(), true);
            while(true) {
                byte[] input = new byte[MAX_PKT];
                byte b;
                int i = 0;
                do {
                    b = (byte) clientSocket.getInputStream().read();
                    input[i] = b;
                    System.out.print(b + ",");
                    ++i;
                } while (b != -1 && i < MAX_PKT);
                System.out.println("\npacket received: " + Arrays.toString(input));
                byte[] opcode = new byte[4];
                System.arraycopy(input, 0, opcode, 0, 4);
                int code = ByteBuffer.wrap(opcode).getInt();
                switch(code) {
                    case HELLO:
                        System.out.println("hello received");
                        label.setText("Client Connected!");
                        frame.revalidate();
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
        server.start(6666);
    }
}
