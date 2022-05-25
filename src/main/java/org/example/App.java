package org.example;

public class App {
    public static void main(String[] args) {
        Server server=new Server();
        server.start(6666);
        Client client = new Client();
        client.startConnection("127.0.0.1", 6666);
        String response = client.sendMessage("hello server");
        System.out.println(response);
    }
}
