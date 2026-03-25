package client.ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import client.HRMClient;
import model.Employee;
import remote.HRMService;

public class LoginScreen extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private RoundedButton loginButton;
    private JLabel statusLabel;

    public LoginScreen() {
        initializeUI();
    }

    private void initializeUI() {
        setTitle("HRM System - Login");
        setSize(460, 440);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(UIConstants.COLOR_BG_PAGE);

        // Card panel
        RoundedPanel card = new RoundedPanel(new GridBagLayout());
        card.setBorder(new EmptyBorder(32, 44, 32, 44));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 1.0;

        // Title
        JLabel titleLabel = new JLabel("HRM System", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(UIConstants.COLOR_PRIMARY);
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 2, 0);
        card.add(titleLabel, gbc);

        // Subtitle
        JLabel subtitleLabel = new JLabel("Human Resource Management", SwingConstants.CENTER);
        subtitleLabel.setFont(UIConstants.FONT_LABEL);
        subtitleLabel.setForeground(UIConstants.COLOR_TEXT_HINT);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 24, 0);
        card.add(subtitleLabel, gbc);

        // Username label
        JLabel usernameLabel = new JLabel("Username");
        usernameLabel.setFont(UIConstants.FONT_BODY);
        usernameLabel.setForeground(UIConstants.COLOR_TEXT_DARK);
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 5, 0);
        card.add(usernameLabel, gbc);

        // Username field
        usernameField = new JTextField(20);
        usernameField.setFont(UIConstants.FONT_BODY);
        usernameField.setPreferredSize(new Dimension(300, 38));
        usernameField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UIConstants.COLOR_BORDER_INPUT, 1, true),
                new EmptyBorder(6, 12, 6, 12)));
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 14, 0);
        card.add(usernameField, gbc);

        // Password label
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(UIConstants.FONT_BODY);
        passwordLabel.setForeground(UIConstants.COLOR_TEXT_DARK);
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 0, 5, 0);
        card.add(passwordLabel, gbc);

        // Password field
        passwordField = new JPasswordField(20);
        passwordField.setFont(UIConstants.FONT_BODY);
        passwordField.setPreferredSize(new Dimension(0, 38));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UIConstants.COLOR_BORDER_INPUT, 1, true),
                new EmptyBorder(6, 12, 6, 12)));

        // Eye toggle
        JButton eyeButton = new JButton("Show");
        eyeButton.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        eyeButton.setPreferredSize(new Dimension(60, 38));
        eyeButton.setBackground(UIConstants.COLOR_BG_PAGE);
        eyeButton.setBorder(new LineBorder(UIConstants.COLOR_BORDER_INPUT, 1, true));
        eyeButton.setFocusPainted(false);
        eyeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        eyeButton.addActionListener(e -> {
            if (passwordField.getEchoChar() == 0) {
                passwordField.setEchoChar('\u2022');
                eyeButton.setText("Show");
            } else {
                passwordField.setEchoChar((char) 0);
                eyeButton.setText("Hide");
            }
        });

        JPanel passwordPanel = new JPanel(new BorderLayout(5, 0));
        passwordPanel.setOpaque(false);
        passwordPanel.setPreferredSize(new Dimension(300, 38));
        passwordPanel.add(passwordField, BorderLayout.CENTER);
        passwordPanel.add(eyeButton, BorderLayout.EAST);

        gbc.gridy = 5;
        gbc.insets = new Insets(0, 0, 20, 0);
        card.add(passwordPanel, gbc);

        // Login button
        loginButton = new RoundedButton("LOGIN", UIConstants.COLOR_PRIMARY, 8);
        loginButton.setPreferredSize(new Dimension(300, 42));

        gbc.gridy = 6;
        gbc.insets = new Insets(0, 0, 10, 0);
        card.add(loginButton, gbc);

        // Status label
        statusLabel = new JLabel(" ", SwingConstants.CENTER);
        statusLabel.setFont(UIConstants.FONT_LABEL);
        statusLabel.setForeground(UIConstants.COLOR_DANGER);
        gbc.gridy = 7;
        gbc.insets = new Insets(0, 0, 0, 0);
        card.add(statusLabel, gbc);

        mainPanel.add(card);
        add(mainPanel);

        // Action listeners
        loginButton.addActionListener(e -> performLogin());

        passwordField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) performLogin();
            }
        });
        usernameField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) passwordField.requestFocusInWindow();
            }
        });
    }

    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please enter both username and password.");
            return;
        }
        statusLabel.setText("Logging in...");
        loginButton.setEnabled(false);

        new SwingWorker<Employee, Void>() {
            protected Employee doInBackground() throws Exception {
                HRMService service = HRMClient.getService();
                return service == null ? null : service.login(username, password);
            }
            protected void done() {
                try {
                    Employee emp = get();
                    if (emp != null) {
                        statusLabel.setForeground(UIConstants.COLOR_SUCCESS);
                        statusLabel.setText("Login successful! Welcome, " + emp.getFirstName());
                        if ("HR".equals(emp.getRole())) new HRDashboard(emp).setVisible(true);
                        else new EmployeeDashboard(emp).setVisible(true);
                        dispose();
                    } else {
                        statusLabel.setForeground(UIConstants.COLOR_DANGER);
                        statusLabel.setText("Invalid username or password.");
                        passwordField.setText("");
                    }
                } catch (Exception ex) {
                    statusLabel.setForeground(UIConstants.COLOR_DANGER);
                    String msg = ex.getMessage();
                    if (msg != null && msg.contains("Connection refused"))
                        statusLabel.setText("Connect Error: Is the Server running?");
                    else
                        statusLabel.setText("Error: " + (msg != null ? msg : "Unknown"));
                }
                loginButton.setEnabled(true);
            }
        }.execute();
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new LoginScreen().setVisible(true));
    }
}
