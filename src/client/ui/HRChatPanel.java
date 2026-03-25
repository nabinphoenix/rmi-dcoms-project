package client.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;

import client.HRMClient;
import socket.ChatClient;
import model.ChatMessage;
import model.Employee;

public class HRChatPanel extends JPanel implements ChatClient.ChatDisplay {

    private ChatClient chatClient;
    private List<Employee> employees;
    private String currentChatTarget;

    private JList<String> employeeList;
    private DefaultListModel<String> listModel;

    private JPanel      bubbleBox;
    private JScrollPane chatScroll;
    private ChatInputField  messageField;

    public HRChatPanel(String hrUsername, List<Employee> employees) {
        this.employees = employees;
        setLayout(new BorderLayout(16, 0));
        setBackground(UIConstants.COLOR_BG_PAGE);
        setBorder(new EmptyBorder(24, 24, 24, 24));

        this.chatClient = new ChatClient(hrUsername, this);

        JPanel leftPanel = buildContactPanel();
        add(leftPanel, BorderLayout.WEST);

        JPanel rightPanel = buildChatArea();
        add(rightPanel, BorderLayout.CENTER);

        appendStatus("Select an employee to start chatting.");
    }

    private JPanel buildContactPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(240, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(UIConstants.COLOR_BORDER, 1));

        JLabel header = new JLabel("  EMPLOYEES", JLabel.LEFT);
        header.setFont(UIConstants.FONT_NAV);
        header.setOpaque(true);
        header.setBackground(UIConstants.COLOR_BG_TOPBAR);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 40));
        panel.add(header, BorderLayout.NORTH);

        listModel = new DefaultListModel<>();
        for (Employee emp : employees)
            listModel.addElement(emp.getFirstName() + " " + emp.getLastName());

        employeeList = new JList<>(listModel);
        employeeList.setFont(UIConstants.FONT_BODY);
        employeeList.setFixedCellHeight(48);
        employeeList.setSelectionBackground(UIConstants.COLOR_SELECTION_BG);
        employeeList.setSelectionForeground(UIConstants.COLOR_TEXT_DARK);
        employeeList.setBorder(new EmptyBorder(4, 8, 4, 8));

        employeeList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int idx = employeeList.getSelectedIndex();
                if (idx >= 0) {
                    currentChatTarget = employees.get(idx).getUsername();
                    chatClient.setTargetFilter(currentChatTarget);
                    clear();
                    appendStatus("Chatting with " + employees.get(idx).getFirstName());
                    loadChatHistory(currentChatTarget);
                }
            }
        });

        panel.add(new JScrollPane(employeeList), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildChatArea() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setBackground(UIConstants.COLOR_BG_PAGE);

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
        panel.add(chatCard, BorderLayout.CENTER);

        JPanel inputRow = buildInputRow();
        panel.add(inputRow, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildInputRow() {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setBackground(UIConstants.COLOR_BG_PAGE);

        messageField = new ChatInputField();
        messageField.setToolTipText("Type a message...");

        RoundedButton sendBtn = new RoundedButton("SEND", UIConstants.COLOR_PRIMARY, 19);
        sendBtn.setPreferredSize(new Dimension(85, 38));

        ActionListener send = e -> sendMessage();
        messageField.addActionListener(send);
        sendBtn.addActionListener(send);

        row.add(messageField, BorderLayout.CENTER);
        row.add(sendBtn, BorderLayout.EAST);
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
        SwingUtilities.invokeLater(() -> {
            bubbleBox.removeAll();
            bubbleBox.revalidate();
            bubbleBox.repaint();
        });
    }

    private void sendMessage() {
        String msg = messageField.getText().trim();
        if (msg.isEmpty() || currentChatTarget == null) return;
        chatClient.sendMessage(currentChatTarget, msg);
        appendMessage(chatClient.getCurrentUser(), msg, true);
        messageField.setText("");
    }

    private void scrollToBottom() {
        SwingUtilities.invokeLater(() -> {
            JScrollBar vsb = chatScroll.getVerticalScrollBar();
            vsb.setValue(vsb.getMaximum());
        });
    }

    private void loadChatHistory(String targetUser) {
        new SwingWorker<List<ChatMessage>, Void>() {
            protected List<ChatMessage> doInBackground() throws Exception {
                return HRMClient.getService().getChatHistory(chatClient.getCurrentUser(), targetUser);
            }
            protected void done() {
                try {
                    for (ChatMessage cm : get())
                        appendMessage(cm.getSender(), cm.getMessage(),
                                cm.getSender().equalsIgnoreCase(chatClient.getCurrentUser()));
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    public void updateEmployeeList(List<Employee> newEmployees) {
        this.employees = newEmployees;
        listModel.clear();
        for (Employee emp : employees)
            listModel.addElement(emp.getFirstName() + " " + emp.getLastName());
    }

    public ChatClient getChatClient() { return chatClient; }
}
