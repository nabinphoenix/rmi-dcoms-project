package client.ui;

import java.awt.*;
import java.awt.geom.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class BubblePanel extends JPanel {
    private static final double MAX_WIDTH_PCT = 0.64;
    private static final int PAD_V = 9;
    private static final int PAD_H = 14;
    private static final int AVATAR_SIZE = 30;
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("h:mm a");

    private final String sender;
    private final String message;
    private final boolean isSent;
    private final String timeStr;

    public BubblePanel(String sender, String message, boolean isSent) {
        this.sender = sender;
        this.message = message;
        this.isSent = isSent;
        this.timeStr = LocalTime.now().format(TIME_FMT);

        setOpaque(false);
    }

    @Override
    public Dimension getPreferredSize() {
        int contentW = getParentSize();
        FontMetrics fm = getFontMetrics(UIConstants.FONT_BODY);
        int maxBW = (int) (contentW * MAX_WIDTH_PCT);
        
        List<String> lines = wrapText(message, fm, maxBW - (2 * PAD_H));
        int bubbleW = Math.max(fm.stringWidth(lines.size() == 1 ? message : getLongest(lines, fm)) + (2 * PAD_H), 40);
        int bubbleH = PAD_V + (lines.size() * fm.getHeight()) + PAD_V;
        
        return new Dimension(contentW, bubbleH + 28); // 28px for caption space
    }

    private int getParentSize() {
        if (getParent() != null && getParent().getWidth() > 0) return getParent().getWidth();
        return 400; // fallback
    }

    private List<String> wrapText(String txt, FontMetrics fm, int maxWidth) {
        List<String> lines = new ArrayList<>();
        String[] words = txt.split(" ");
        StringBuilder cur = new StringBuilder();
        for (String w : words) {
            if (fm.stringWidth(cur.toString() + w) > maxWidth && cur.length() > 0) { // Check cur.length() to avoid empty first line
                lines.add(cur.toString().trim());
                cur = new StringBuilder(w + " ");
            } else {
                cur.append(w).append(" ");
            }
        }
        lines.add(cur.toString().trim());
        return lines;
    }

    private String getLongest(List<String> lines, FontMetrics fm) {
        String longest = "";
        for (String s : lines) if (fm.stringWidth(s) > fm.stringWidth(longest)) longest = s;
        return longest;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        FontMetrics fm = g2.getFontMetrics(UIConstants.FONT_BODY);
        int contentW = getWidth();
        int maxBW = (int) (contentW * MAX_WIDTH_PCT);
        List<String> lines = wrapText(message, fm, maxBW - (2 * PAD_H));
        
        int bubbleW = Math.max(fm.stringWidth(lines.size() == 1 ? message : getLongest(lines, fm)) + (2 * PAD_H), 40);
        int bubbleH = PAD_V + (lines.size() * fm.getHeight()) + PAD_V;

        int x, y = 0;
        if (isSent) {
            x = contentW - bubbleW;
            drawBubble(g2, x, y, bubbleW, bubbleH, true);
            drawMetadata(g2, x, y + bubbleH + 10, bubbleW, true);
        } else {
            // Layout: avatar on left + bubble on right, bottom aligned
            int avatarX = 0;
            int textX = 38;
            
            drawAvatar(g2, avatarX, y + bubbleH - AVATAR_SIZE);
            drawBubble(g2, textX, y, bubbleW, bubbleH, false);
            drawMetadata(g2, textX, y + bubbleH + 10, bubbleW, false);
        }
        g2.dispose();
    }

    private void drawBubble(Graphics2D g2, int x, int y, int w, int h, boolean sent) {
        g2.setColor(sent ? UIConstants.COLOR_BUBBLE_SENT_BG : UIConstants.COLOR_BUBBLE_RECV_BG);
        float r = 16f, tr = 16f, br = 16f, bl = 16f; // top-left, top-right, bottom-right, bottom-left
        if (sent) br = 4f; else bl = 4f;

        Path2D.Float path = new Path2D.Float();
        path.moveTo(x + r, y);
        path.lineTo(x + w - tr, y);
        path.quadTo(x + w, y, x + w, y + tr);
        path.lineTo(x + w, y + h - br);
        path.quadTo(x + w, y + h, x + w - br, y + h);
        path.lineTo(x + bl, y + h);
        path.quadTo(x, y + h, x, y + h - bl);
        path.lineTo(x, y + r);
        path.quadTo(x, y, x + r, y);
        path.closePath();
        g2.fill(path);

        // Draw Multi-line Text
        g2.setColor(sent ? UIConstants.COLOR_BUBBLE_SENT_FG : UIConstants.COLOR_BUBBLE_RECV_FG);
        g2.setFont(UIConstants.FONT_BODY);
        FontMetrics fm = g2.getFontMetrics();
        List<String> lines = wrapText(message, fm, w - (2 * PAD_H));
        int ty = y + PAD_V + fm.getAscent();
        for (String line : lines) {
            g2.drawString(line, x + PAD_H, ty);
            ty += fm.getHeight();
        }
    }

    private void drawAvatar(Graphics2D g2, int x, int y) {
        g2.setColor(UIConstants.COLOR_AVATAR_BG);
        g2.fillOval(x, y, AVATAR_SIZE, AVATAR_SIZE);
        g2.setColor(Color.WHITE);
        g2.setFont(UIConstants.FONT_AVATAR);
        String initial = sender.substring(0, Math.min(2, sender.length())).toUpperCase();
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(initial, x + (AVATAR_SIZE - fm.stringWidth(initial)) / 2, y + (AVATAR_SIZE - fm.getHeight()) / 2 + fm.getAscent());
    }

    private void drawMetadata(Graphics2D g2, int x, int y, int bw, boolean sent) {
        String meta = sent ? timeStr : sender + " \u00B7 " + timeStr;
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        FontMetrics fm = g2.getFontMetrics();
        if (sent) {
            g2.setColor(new Color(100, 130, 160, 204)); // rgba(100,130,160,0.8) -> alpha 204
            g2.drawString(meta, x + bw - fm.stringWidth(meta), y + fm.getAscent());
        } else {
            g2.setColor(new Color(0x9CA3AF));
            g2.drawString(meta, x, y + fm.getAscent());
        }
    }
}
