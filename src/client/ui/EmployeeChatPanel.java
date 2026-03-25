package client.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;

import client.HRMClient;
import socket.ChatClient;
import model.ChatMessage;

public class EmployeeChatPanel extends JPanel implements ChatClient.ChatDisplay {

    private final String employeeUsername;
    private final String hrUsername;
    private ChatClient   chatClient;

    private JPanel    bubbleBox;
    private JScrollPane chatScroll;
    private ChatInputField messageField;

    public EmployeeChatPanel(String employeeUsername, ChatClient chatClient, String hrUsername) {
        this.employeeUsername = employeeUsername;
        this.chatClient       = chatClient;
        this.hrUsername       = hrUsername;

        setLayout(new BorderLayout(0, 0));
        setBackground(UIConstants.COLOR_BG_PAGE);
        setBorder(new EmptyBorder(22, 22, 22, 22));

        JLabel heading = new JLabel("Secure Chat with HR", JLabel.CENTER);
        heading.setFont(UIConstants.FONT_LABEL_M);
        heading.setForeground(UIConstants.COLOR_TEXT_DARK);
        heading.setBorder(new EmptyBorder(0, 0, 14, 0));
        add(heading, BorderLayout.NORTH);

        bubbleBox = new JPanel();
        bubbleBox.setLayout(new BoxLayout(bubbleBox, BoxLayout.Y_AXIS));
        bubbleBox.setBackground(Color.WHITE);
        bubbleBox.setOpaque(true);

        chatScroll = new JScrollPane(bubbleBox);
        chatScroll.setBorder(null);
        chatScroll.setOpaque(false);
        chatScroll.getViewport().setOpaque(false);
        chatScroll.getViewport().setBackground(Color.WHITE);
        chatScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        RoundedPanel chatCard = new RoundedPanel(new BorderLayout());
        chatCard.setBackground(Color.WHITE);
        chatCard.setBorder(new EmptyBorder(16, 14, 16, 14));
        chatCard.add(chatScroll, BorderLayout.CENTER);
        add(chatCard, BorderLayout.CENTER);

        JPanel inputRow = buildInputRow();
        add(inputRow, BorderLayout.SOUTH);

        if (this.chatClient != null) {
            this.chatClient.setChatDisplay(this);
            this.chatClient.setTargetFilter(hrUsername);
        }

        loadHistory();
    }

    private JPanel buildInputRow() {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setBackground(UIConstants.COLOR_BG_PAGE);
        row.setBorder(new EmptyBorder(12, 0, 0, 0));

        messageField = new ChatInputField();
        messageField.setToolTipText("Type a message...");

        RoundedButton sendBtn = new RoundedButton("SEND", UIConstants.COLOR_PRIMARY, 19);
        sendBtn.setPreferredSize(new Dimension(85, 38));

        ActionListener send = e -> sendMessage();
        messageField.addActionListener(send);
        sendBtn.addActionListener(send);

        row.add(messageField, BorderLayout.CENTER);
        row.add(sendBtn,      BorderLayout.EAST);
        return row;
    }

    private String lastSender = null;
    @Override
    public void appendMessage(String from, String message, boolean isMe) {
        SwingUtilities.invokeLater(() -> {
            boolean isFirstInGroup = !from.equalsIgnoreCase(lastSender);
            BubblePanel bubble = new BubblePanel(from, message, isMe);
            
            JPanel wrap = new JPanel(new FlowLayout(isMe ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 0));
            wrap.setOpaque(false);
            wrap.add(bubble);
            wrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, bubble.getPreferredSize().height));
            
            if (lastSender != null) {
                bubbleBox.add(Box.createVerticalStrut(isFirstInGroup ? 14 : 4));
            }
            
            bubbleBox.add(wrap);
            lastSender = from;
            
            bubbleBox.revalidate();
            bubbleBox.repaint();
            scrollToBottom();
        });
    }

    @Override
    public void appendStatus(String text) {
        SwingUtilities.invokeLater(() -> {
            JLabel lbl = new JLabel(text, JLabel.CENTER);
            lbl.setFont(new Font("Segoe UI", Font.ITALIC, 10));
            lbl.setForeground(UIConstants.COLOR_TEXT_HINT);
            lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
            lbl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
            bubbleBox.add(lbl);
            bubbleBox.add(Box.createVerticalStrut(8));
            bubbleBox.revalidate();
            bubbleBox.repaint();
            scrollToBottom();
        });
    }

    @Override
    public void clear() {
        SwingUtilities.invokeLater(() -> { bubbleBox.removeAll(); bubbleBox.revalidate(); bubbleBox.repaint(); });
    }

    private void sendMessage() {
        String msg = messageField.getText().trim();
        if (msg.isEmpty()) return;
        if (chatClient != null) chatClient.sendMessage(hrUsername, msg);
        appendMessage(employeeUsername, msg, true);
        messageField.setText("");
    }

    private void scrollToBottom() {
        SwingUtilities.invokeLater(() -> {
            JScrollBar vsb = chatScroll.getVerticalScrollBar();
            vsb.setValue(vsb.getMaximum());
        });
    }

    private void loadHistory() {
        new SwingWorker<List<ChatMessage>, Void>() {
            protected List<ChatMessage> doInBackground() throws Exception {
                return HRMClient.getService().getChatHistory(employeeUsername, hrUsername);
            }
            protected void done() {
                try {
                    appendStatus("--- Chat history ---");
                    for (ChatMessage cm : get())
                        appendMessage(cm.getSender(), cm.getMessage(),
                                cm.getSender().equalsIgnoreCase(employeeUsername));
                } catch (Exception ignored) {}
            }
        }.execute();
    }
}
