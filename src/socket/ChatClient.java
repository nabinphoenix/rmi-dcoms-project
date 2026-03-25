package socket;

import javax.swing.*;
import java.io.*;
import java.net.*;
import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ChatClient {
    public interface ChatDisplay {
        void appendMessage(String from, String message, boolean isMe);
        void appendStatus(String text);
        void clear();
    }

    private Socket socket;
    private PrintWriter out;
    private String username;
    private String targetFilter; 
    private ChatDisplay display;
    private StringBuilder messageBuffer = new StringBuilder(); 

    public ChatClient(String username, ChatDisplay display) {
        this.username = username.trim().toLowerCase();
        this.display = display;
        new Thread(() -> {
            try {
                this.socket = new Socket("localhost", 12345);
                this.out = new PrintWriter(socket.getOutputStream(), true);
                out.println("LOGIN:" + this.username);
                System.out.println("[CHAT CLIENT] Socket connected for user: " + this.username);
                startListener();
            } catch (IOException e) {
                System.err.println("[CHAT CLIENT ERROR] Could not connect to socket server: " + e.getMessage());
            }
        }).start();
    }

    private void startListener() {
        new Thread(() -> {
            try (Scanner in = new Scanner(socket.getInputStream())) {
                while (in.hasNextLine()) {
                    String line = in.nextLine();
                    if (line.startsWith("MSG:")) {
                        String[] parts = line.split(":", 4);
                        if (parts.length == 4) {
                            String from = parts[2], msg = parts[3];
                            if (targetFilter != null && !from.equalsIgnoreCase(targetFilter)) {
                                // Potentially buffer or ignore messages from others
                                return;
                            }
                            SwingUtilities.invokeLater(() -> {
                                if (display != null) {
                                    display.appendMessage(from, msg, false);
                                } else {
                                    messageBuffer.append("[MSG]").append(from).append(":").append(msg).append("\n");
                                }
                            });
                        }
                    } else if (line.startsWith("NOTIFY:")) {
                        String[] parts = line.split(":", 3);
                        if (parts.length == 3) {
                            String msg = parts[2];
                            SwingUtilities.invokeLater(() -> {
                                JOptionPane.showMessageDialog(null, msg, "HRM Notification",
                                        JOptionPane.INFORMATION_MESSAGE);
                            });
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("[CHAT CLIENT LISTENER ERROR]: " + e.getMessage());
            }
        }).start();
    }

    public void setChatDisplay(ChatDisplay newDisplay) {
        this.display = newDisplay;
        // Simple buffer handling for new display could be added here
    }

    public void sendMessage(String toUser, String message) {
        if (out != null) {
            String to = toUser.trim().toLowerCase();
            out.println("MSG:" + to + ":" + username + ":" + message);
        } else {
            System.err.println("[CHAT CLIENT] Not connected to server!");
        }
    }

    public void setTargetFilter(String target) {
        this.targetFilter = (target != null) ? target.trim().toLowerCase() : null;
    }

    public void sendNotification(String toUser, String message) {
        if (out != null) {
            String to = toUser.trim().toLowerCase();
            out.println("NOTIFY:" + to + ":" + message);
        }
    }

    public String getCurrentUser() {
        return username;
    }

    public void logout() {
        try {
            if (out != null)
                out.println("LOGOUT:" + username);
            if (socket != null)
                socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
