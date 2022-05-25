package org.example;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

import static org.example.MakePacket.MAX_PKT;
import static org.example.MakePacket.hello;

public class Client {
    private Socket clientSocket;
    private PrintWriter pw;
    private BufferedReader in;
    private OutputStream os;

    public static void main(String[] args) {
        if(args.length < 1) {
            System.out.println("Invalid URL");
            System.exit(1);
        }
        String domainName = args[0];
        String hostname = "whois.internic.net";
        int port = 43;

        try (Socket socket = new Socket(hostname, port)) {
            OutputStream out = socket.getOutputStream();
            PrintWriter pw = new PrintWriter(out, true);
            pw.println(domainName);

            InputStream input = socket.getInputStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader((input)));

            String line;

            while((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startConnection(String ip, int port) {
        try {
            clientSocket = new Socket(ip, port);
            os = clientSocket.getOutputStream();
            pw = new PrintWriter(os, true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
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

    public String sendMessage(String msg){
        pw.println(msg);
        try {
            String resp = in.readLine();
            return resp;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public void stopConnection() {
        try {
            in.close();
            pw.close();
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String get(String url) {
        return "";
    }
}
