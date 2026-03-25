package client.ui;

import java.awt.*;
import java.awt.font.TextAttribute;
import java.util.Collections;
import javax.swing.*;

public class ModernTabComponent extends JPanel {
    private final JTabbedPane tabs;
    private final JLabel lbl;

    public ModernTabComponent(JTabbedPane tabs, String title) {
        this.tabs = tabs;
        setOpaque(false);
        setLayout(new BorderLayout());

        Font tabFont = UIConstants.FONT_NAV.deriveFont(
            Collections.singletonMap(TextAttribute.TRACKING, 0.1));

        lbl = new JLabel(title.toUpperCase());
        lbl.setFont(tabFont);
        lbl.setForeground(UIConstants.COLOR_NAV_INACTIVE);
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 24, 0, 24)); // Increased side padding
        add(lbl, BorderLayout.CENTER);

        // Calculate preferred size AFTER setting font
        FontMetrics fm = lbl.getFontMetrics(tabFont);
        // Add 15% buffer to accommodate tracking precisely
        int textW = (int)(fm.stringWidth(title.toUpperCase()) * 1.15) + 48; 
        setPreferredSize(new Dimension(textW, 44));

        tabs.addChangeListener(e -> updateAppearance());

        // Set initial appearance after a short delay so indexOfTabComponent works
        SwingUtilities.invokeLater(this::updateAppearance);
    }

    private void updateAppearance() {
        int idx = tabs.indexOfTabComponent(this);
        boolean active = (idx >= 0 && idx == tabs.getSelectedIndex());
        lbl.setForeground(active ? UIConstants.COLOR_NAV_ACTIVE : UIConstants.COLOR_NAV_INACTIVE);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int idx = tabs.indexOfTabComponent(this);
        if (idx >= 0 && idx == tabs.getSelectedIndex()) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(UIConstants.COLOR_NAV_ACTIVE);
            g2.fillRect(0, getHeight() - 2, getWidth(), 2);
            g2.dispose();
        }
    }
}
