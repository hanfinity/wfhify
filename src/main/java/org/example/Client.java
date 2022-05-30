package org.example;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.Buffer;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static org.example.MakePacket.*;
import static org.example.OpCode.*;

public class Client {
    private Socket clientSocket;
    private PrintWriter pw;
    private InputStream in;
    private OutputStream os;

    private static final String persist = "hist.txt";
    private static final String menu = "Menu:\n" +
            "Please Make a selection:\n" +
            "1 - Set Message\n" +
            "2 - Get Current Message\n" +
            "3 - Enter Scheduled Message\n" +
            "4 - Get Scheduled Messages\n" +
            "5 - Set Working Hours\n" +
            "6 - Get Working Hours\n" +
            "7 - Set After Hours Message\n" +
            "8 - Quit\n" +
            "Please enter your selection:";

    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String domainName = load_url();
        if(domainName.equals("")) {
            System.out.println("No saved url, please enter an address: ");
            domainName = reader.readLine();
        }
        System.out.println("Connecting to " + domainName);
        Client client = new Client();
        client.startConnection(domainName, 6666);
        boolean quit = false;
        while(!quit) {
            System.out.println(menu);
            int choice = Integer.parseInt(reader.readLine());
            switch (choice) {
                case 1: // set the current message
                    System.out.println("Please enter message:");
                    try {
                        client.send_pkt(set_mess(reader.readLine()));
                    } catch (Exception e) {
                        System.err.println("Failed to set message: " + e.getMessage());
                    }
                    break;
                case 2: // get the current message
                    client.send_pkt(get_mess());
                    System.out.println("Current message: " + client.getPacket());
                    break;
                case 8: // exit client application
                    System.out.println("Goodbye!");
                    quit = true;
                    break;
                default:
                    System.out.println("Invalid option");
            }
        }
        client.stopConnection();
        save_url(domainName);
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

    protected String getPacket() throws IOException {
        byte[] message = new byte[MAX_PKT];
        int code = readMessage(message, in);
        String message_text = new String(message, StandardCharsets.UTF_8);
        return message_text.trim();
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

    protected static String load_url() {
        try {
            File inFile = new File (persist);
            Scanner scan = new Scanner(inFile);
            return scan.nextLine();
        } catch (FileNotFoundException e) {
            return "";
        }
    }

    protected static void save_url(String url) {
        try {
            BufferedWriter write = new BufferedWriter(new FileWriter(persist));
            write.write(url);
            write.close();
        } catch (Exception e) {
            System.err.println("Failed to write to " + persist);
        }
    }
}
