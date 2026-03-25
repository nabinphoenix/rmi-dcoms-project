package client.ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class ChatInputField extends JTextField {

    public ChatInputField() {
        setOpaque(false);
        setFont(UIConstants.FONT_BODY);
        setForeground(UIConstants.COLOR_TEXT_DARK);
        setCaretColor(UIConstants.COLOR_PRIMARY);
        setBorder(new EmptyBorder(0, 16, 0, 16));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        // Background
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());

        // Border
        g2.setColor(new Color(0xD1D5DB));
        g2.setStroke(new BasicStroke(1));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());

        // Placeholder
        if (getText().isEmpty() && !hasFocus()) {
            g2.setColor(new Color(0x9CA3AF));
            FontMetrics fm = g2.getFontMetrics();
            int ty = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString("Type a message\u2026", 16, ty);
        }

        g2.dispose();
        super.paintComponent(g);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(super.getPreferredSize().width, 38);
    }
}
