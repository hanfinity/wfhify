package org.example;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

import static org.example.MakePacket.*;

public class Client {
    private Socket clientSocket;
    private PrintWriter pw;
    private InputStream in;
    private OutputStream os;

    private static final String menu = "Menu:\n" +
            "Please Make a selection:\n" +
            "1 - ";

    public static void main(String[] args) {
        if(args.length < 1) {
            System.out.println("Invalid URL");
            System.exit(1);
        }
        String domainName = args[0];
        Client client = new Client();
        client.startConnection("192.168.68.167", 6666);
        boolean quit = false;
        while(!quit) {
            try {
                client.send_pkt(set_mess("hello world"));
            } catch (Exception e) {
                e.printStackTrace();
            }
            quit = true;
        }
        client.stopConnection();
    }

    public void startConnection(String ip, int port) {
        try {
            clientSocket = new Socket(ip, port);
            os = clientSocket.getOutputStream();
            in = clientSocket.getInputStream();
            send_pkt(hello("asdf"));
        } catch (IOException e) {
            System.err.println("Couldn't open connection!");
        } catch (Exception e) {
            System.err.println("Failed to send hello packet");
        }

    }

    protected boolean send_pkt(byte[] pkt) {
        if(pkt.length > MAX_PKT) return false;
        try {
            os.write(pkt);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void stopConnection() {
        try {
            in.close();
            os.close();
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String get(String url) {
        return "";
    }
}
