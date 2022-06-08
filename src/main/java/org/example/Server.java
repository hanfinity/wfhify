package org.example;

/**
 * Hello world!
 *
 */

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.example.Err.ILLEGAL_MESS;
import static org.example.MakePacket.*;
import static org.example.OpCode.*;


public class Server {
    class MessageSchedule {
        public short startHour;
        public short startMinute;
        public short endHour;
        public short endMinute;
        ScheduledExecutorService schedule;
        MessageSchedule(short startH, short startM, short endH, short endM, ScheduledExecutorService s) {
            startHour = startH;
            startMinute = startM;
            endHour = endH;
            endMinute = endM;
            schedule = s;
        }
    }
    interface Scheduler {
        void schedule(byte[] m, MessageSchedule ms);
    }
    public static final int TIMEOUT = 15000;
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private JFrame frame;
    private JLabel label;
    private String currMessage;
    private String lastMessage;
    private String offWork = "Outside of<br>Work Hours";
    private InputStream in;
    private OutputStream os;
    private Map<byte[], MessageSchedule> schedule;
    private MessageSchedule workHours;
    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    public Server() {
        schedule = new TreeMap<>((o1, o2) -> {
            if(o2.length > o1.length) return -1;
            for(int i=0; i<o1.length; ++i) {
                if(o1[i] > o2[i]) return 1;
                if(o1[i] < o2[i]) return -1;
            }
            return 0;
        }

        );
        lastMessage = "default";
    }

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
                        setMessage("connected:<br>" + clientSocket.getLocalAddress());
                        break;
                    case SET_MESS:
                        System.out.println("new message received");
                        setMessage(payload);
                        break;
                    case LIST_MESS:
                        System.out.println("client requested current message: " + currMessage);
                        os.write(list_mess(currMessage));
                        break;
                    case MAKE_SCHED:
                        System.out.println("client scheduling a message");
                        os.write(decodeSchedulePacket(MAKE_SCHED, payload));
                        break;
                    case GET_SCHED:
                        System.out.println("client requests schedule");
                        for (Map.Entry<byte[], MessageSchedule> q:
                                schedule.entrySet()) {
                            os.write(generic_packet(LIST_RESP, new String(q.getKey(), StandardCharsets.UTF_8),
                                                    q.getValue().startHour, q.getValue().startMinute,
                                                    q.getValue().endHour, q.getValue().endMinute));
                        }
                        os.write(done());
                        break;
                    case DEL_SCHED:
                        System.out.println("client requests delete message");
                        byte[] key = new byte[40];
                        System.arraycopy(payload, 0, key, 0, 40);
                        MessageSchedule ms = schedule.get(key);
                        if(ms != null) {
                            ms.schedule.shutdown();
                            schedule.remove(key);
                            System.out.println("successfully deleted " + new String(payload, StandardCharsets.UTF_8));
                        } else {
                            System.out.println(new String(payload, StandardCharsets.UTF_8) + " not found");
                        }
                        break;
                    case SET_HRS:
                        System.out.println("client setting off-work message");
                        os.write(decodeSchedulePacket(SET_HRS, payload));
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

    protected byte[] decodeSchedulePacket(int code, byte[] payload) {
        byte[] message = new byte[40];
        System.arraycopy(payload,
                0,
                message,
                0, 40);
        byte[] startHA = new byte[2];
        System.arraycopy(payload,
                40,
                startHA,
                0, 2);
        byte[] startMA = new byte[2];
        System.arraycopy(payload,
                42,
                startMA,
                0, 2);
        byte[] endHA = new byte[2];
        System.arraycopy(payload,
                44,
                endHA,
                0, 2);
        byte[] endMA = new byte[2];
        System.arraycopy(payload,
                46,
                endMA,
                0, 2);
        try {
            if(code == MAKE_SCHED)
                scheduleMaker(
                    ByteBuffer.wrap(startHA).getShort(),
                    ByteBuffer.wrap(startMA).getShort(),
                    ByteBuffer.wrap(endHA).getShort(),
                    ByteBuffer.wrap(endMA).getShort(),
                    message, (m, ms) -> schedule.put(m, ms)
                );
            else if(code == SET_HRS) {
                scheduleMaker(
                        ByteBuffer.wrap(startHA).getShort(),
                        ByteBuffer.wrap(startMA).getShort(),
                        ByteBuffer.wrap(endHA).getShort(),
                        ByteBuffer.wrap(endMA).getShort(),
                        message,
                        (m, ms) -> {
                            offWork = new String(m, StandardCharsets.UTF_8);
                            workHours = ms;
                        }
                );
            }
            return list_mess("OK");
        } catch (Exception e) {
            System.out.println("invalid time: " + e.getMessage());
            e.printStackTrace();
            return error(ILLEGAL_MESS);
        }
    }

        /**
         * function to create a scheduled message
         * @param start_hour the hour (24h format) of the day to change the message
         * @param start_minute the minute of the day to change the message
         * @param message the message (in byte array form)
         * @param s what to do with the created schedule
         * @throws Exception if the start_hour or start_minute are invalid
         */
    protected void scheduleMaker(short start_hour, short start_minute,
                                 short end_hour, short end_minute,
                                 byte[] message, Scheduler s) throws Exception{
        ZonedDateTime now = ZonedDateTime.now();
        if(start_hour > 23 || start_hour < 0 || start_minute > 59 || start_minute < 0) {
            throw new Exception("Invalid start time: " + start_hour + ":" + start_minute);
        }
        if(end_hour > 23 || end_hour < 0 || end_minute > 59 || end_minute < 0) {
            throw new Exception("Invalid end time: " + end_hour + ":" + end_minute);
        }
        System.out.println("message schedule: " + start_hour + ":" + start_minute + " - " +
                end_hour + ":" + end_minute);
        ScheduledExecutorService sched = Executors.newScheduledThreadPool(1);
        makeSchedule(sched, start_hour, start_minute, message, now);
        makeSchedule(sched, end_hour, end_minute, lastMessage.getBytes(StandardCharsets.UTF_8), now);

        s.schedule(message, new MessageSchedule(start_hour, start_minute,
                                            end_hour, end_minute,
                                            sched));
    }

    private void makeSchedule(ScheduledExecutorService sched, int start_hour, int start_minute, byte[] message, ZonedDateTime now) {
        ZonedDateTime next = now.withHour(start_hour).withMinute(start_minute).withSecond(0);
        if(now.compareTo(next) > 0) next = next.plusDays(1);
        Duration dur = Duration.between(now, next);
        long initialDelay = dur.getSeconds();
        sched.scheduleAtFixedRate(() -> setMessage(message),
                initialDelay,
                TimeUnit.DAYS.toSeconds(1),
                TimeUnit.SECONDS);
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
