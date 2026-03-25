package model;

import java.io.Serializable;

public class ChatMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private String sender;
    private String receiver;
    private String message;
    private String sentAt;

    public ChatMessage(int id, String sender, String receiver, String message, String sentAt) {
        this.id = id;
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.sentAt = sentAt;
    }

    public int getId() {
        return id;
    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getMessage() {
        return message;
    }

    public String getSentAt() {
        return sentAt;
    }

    @Override
    public String toString() {
        return "[" + sentAt + "] " + sender + ": " + message;
    }
}
