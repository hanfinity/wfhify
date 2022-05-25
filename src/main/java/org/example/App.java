package org.example;

import static org.example.MakePacket.set_mess;

public class App {
    public static void main(String[] args) throws Exception{
        Client client = new Client();
        client.startConnection("192.168.68.167", 6666);
        client.send_pkt(set_mess("hello world"));
    }
}
