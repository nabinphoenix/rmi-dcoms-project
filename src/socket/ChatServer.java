package socket;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import database.DBManager;

public class ChatServer {
    private static final int PORT = 12345;
    // Normalized usernames (lowercase, trimmed)
    private static Map<String, PrintWriter> connectedUsers = new ConcurrentHashMap<>();

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("[SOCKET SERVER] Listening on port " + PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("[SOCKET ERROR] " + e.getMessage());
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket socket;
        private String username;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (Scanner in = new Scanner(socket.getInputStream());
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                while (in.hasNextLine()) {
                    String line = in.nextLine();
                    if (line.startsWith("LOGIN:")) {
                        username = line.substring(6).trim().toLowerCase();
                        connectedUsers.put(username, out);
                        System.out.println("[SOCKET] User linked: " + username);
                    } else if (line.startsWith("MSG:")) {
                        String[] parts = line.split(":", 4);
                        if (parts.length == 4) {
                            String to = parts[1].trim().toLowerCase();
                            String from = parts[2].trim().toLowerCase();
                            String msg = parts[3];
                            System.out.println("[SOCKET] Forwarding: " + from + " -> " + to);
                            // Save to database
                            DBManager.saveChatMessage(from, to, msg);
                            forwardMessage("MSG:" + to + ":" + from + ":" + msg, to);
                        }
                    } else if (line.startsWith("NOTIFY:")) {
                        String[] parts = line.split(":", 3);
                        if (parts.length == 3) {
                            String to = parts[1].trim().toLowerCase();
                            String msg = parts[2];
                            forwardMessage("NOTIFY:" + to + ":" + msg, to);
                        }
                    } else if (line.startsWith("LOGOUT:")) {
                        if (username != null)
                            connectedUsers.remove(username);
                        break;
                    }
                }
            } catch (IOException e) {
                System.err.println("[SOCKET HANDLER ERROR] " + username + ": " + e.getMessage());
            } finally {
                if (username != null) {
                    connectedUsers.remove(username);
                    System.out.println("[SOCKET] User unlinked: " + username);
                }
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }

        private void forwardMessage(String fullMessage, String toUser) {
            PrintWriter recipientOut = connectedUsers.get(toUser);
            if (recipientOut != null) {
                recipientOut.println(fullMessage);
            } else {
                System.out.println("[SOCKET] Delivery failed: " + toUser + " is offline.");
            }
        }
    }
}
