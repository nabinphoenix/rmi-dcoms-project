package client.ui;

import java.awt.*;
import java.awt.event.*;
import java.awt.font.TextAttribute;
import java.util.Collections;
import javax.swing.*;

public class RoundedButton extends JButton {
    private Color baseColor;
    private Color hoverColor;
    private int radius = 7;

    public RoundedButton(String text, Color bg) {
        this(text, bg, 7);
    }

    public RoundedButton(String text, Color bg, int radius) {
        super(text.toUpperCase());
        this.baseColor = bg;
        this.hoverColor = bg.darker();
        this.radius = radius;

        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        // Font with Tracking
        setFont(UIConstants.FONT_BTN.deriveFont(
            Collections.singletonMap(TextAttribute.TRACKING, 0.05)));
        setForeground(Color.WHITE);
        
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { setBackground(hoverColor); repaint(); }
            @Override
            public void mouseExited(MouseEvent e)  { setBackground(baseColor); repaint(); }
        });
        setBackground(baseColor);
    }

    public void setHoverColor(Color hover) {
        this.hoverColor = hover;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius * 2, radius * 2);

        FontMetrics fm = g2.getFontMetrics();
        int tx = (getWidth() - fm.stringWidth(getText())) / 2;
        int ty = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();

        g2.setColor(getForeground());
        g2.drawString(getText(), tx, ty);
        g2.dispose();
    }
    
    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(Math.max(d.width + 30, 80), 38);
    }
}
