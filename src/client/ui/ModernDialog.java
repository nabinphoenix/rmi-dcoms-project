package client.ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * ModernDialog: A premium, modern alternative to JOptionPane.
 * Matches the application's design language: Rounded corners, #2980B9 palette, Segoe UI fonts.
 */
public class ModernDialog extends JDialog {

    private boolean confirmed = false;

    public static void showMessage(Frame parent, String title, String message, boolean isError) {
        ModernDialog dialog = new ModernDialog(parent, title, message, false, isError);
        dialog.setVisible(true);
    }

    public static boolean showConfirm(Frame parent, String title, String message) {
        ModernDialog dialog = new ModernDialog(parent, title, message, true, false);
        dialog.setVisible(true);
        return dialog.confirmed;
    }

    private ModernDialog(Frame parent, String title, String message, boolean isConfirm, boolean isError) {
        super(parent, title, true);
        setUndecorated(true);
        
        // Background - Transparent frame
        setBackground(new Color(0, 0, 0, 0));

        RoundedPanel root = new RoundedPanel(new BorderLayout(), 12);
        root.setBackground(Color.WHITE);
        root.setBorder(BorderFactory.createLineBorder(UIConstants.COLOR_BORDER, 1));
        
        // --- Content Section ---
        JPanel content = new JPanel(new GridBagLayout());
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(32, 32, 16, 32));
        
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1;

        // Title Styling
        JLabel titleLbl = new JLabel(title.toUpperCase());
        titleLbl.setFont(UIConstants.FONT_LABEL_M.deriveFont(Font.BOLD, 16f));
        titleLbl.setForeground(isError ? UIConstants.COLOR_DANGER : UIConstants.COLOR_PRIMARY);
        g.gridy = 0; g.insets = new Insets(0, 0, 16, 0);
        content.add(titleLbl, g);

        // Body Message
        JTextArea msgBody = new JTextArea(message);
        msgBody.setFont(UIConstants.FONT_BODY);
        msgBody.setForeground(new Color(0x4B5563));
        msgBody.setLineWrap(true);
        msgBody.setWrapStyleWord(true);
        msgBody.setEditable(false);
        msgBody.setFocusable(false);
        msgBody.setOpaque(false);
        msgBody.setBackground(new Color(0, 0, 0, 0));
        g.gridy = 1; g.insets = new Insets(0, 0, 0, 0);
        content.add(msgBody, g);

        root.add(content, BorderLayout.CENTER);

        // --- Buttons Section ---
        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        buttonRow.setOpaque(false);
        buttonRow.setBorder(new EmptyBorder(16, 32, 32, 32));

        if (isConfirm) {
            RoundedButton noBtn = new RoundedButton("NO", new Color(0x9CA3AF), 6);
            noBtn.setPreferredSize(new Dimension(80, 36));
            noBtn.addActionListener(e -> {
                confirmed = false;
                dispose();
            });
            
            RoundedButton yesBtn = new RoundedButton("YES", UIConstants.COLOR_PRIMARY, 6);
            yesBtn.setPreferredSize(new Dimension(80, 36));
            yesBtn.addActionListener(e -> {
                confirmed = true;
                dispose();
            });
            
            buttonRow.add(noBtn);
            buttonRow.add(yesBtn);
        } else {
            RoundedButton okBtn = new RoundedButton("OK", UIConstants.COLOR_PRIMARY, 6);
            okBtn.setPreferredSize(new Dimension(100, 36));
            okBtn.addActionListener(e -> dispose());
            buttonRow.add(okBtn);
        }

        root.add(buttonRow, BorderLayout.SOUTH);
        
        setContentPane(root);
        pack();
        
        // Ensure minimum reasonable width
        if (getWidth() < 380) {
            setSize(380, getHeight());
        }
        
        setLocationRelativeTo(parent);
    }
}
