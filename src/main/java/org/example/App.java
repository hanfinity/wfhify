package org.example;

public class App {
    public static void main(String[] args) {
        Client client = new Client();
        client.startConnection("192.168.68.167", 6666);
        String response = client.sendMessage("hello server");
        System.out.println(response);
    }
}
