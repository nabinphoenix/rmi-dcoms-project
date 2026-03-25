package client.ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.font.TextAttribute;
import java.util.Collections;

public class SelectionTable extends JTable {
    private int hoveredRow = -1;

    public SelectionTable(TableModel model) {
        super(model);
        styleTable();
        setupInteractions();
    }

    private void styleTable() {
        setRowHeight(44);
        setShowGrid(false);
        setIntercellSpacing(new Dimension(0, 0));
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setBackground(Color.WHITE);
        
        // Header Styling
        JTableHeader header = getTableHeader();
        header.setPreferredSize(new Dimension(0, 42));
        header.setBackground(new Color(0xF8FAFC));
        header.setFont(UIConstants.FONT_TABLE_HEADER);
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                lbl.setBackground(new Color(0xF8FAFC));
                lbl.setForeground(new Color(0x6B7280));
                lbl.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xE2E5EA)));
                lbl.setHorizontalAlignment(JLabel.LEFT);
                lbl.setText(value.toString().toUpperCase());
                // Handle Tracking
                lbl.setFont(UIConstants.FONT_TABLE_HEADER.deriveFont(
                    Collections.singletonMap(TextAttribute.TRACKING, 0.07)));
                lbl.setBorder(BorderFactory.createCompoundBorder(lbl.getBorder(), new EmptyBorder(0, 16, 0, 16)));
                return lbl;
            }
        });

        // Cell Renderer
        setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                if (column == 0) {
                    return new CheckboxRenderer(isSelected);
                }

                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                lbl.setFont(UIConstants.FONT_BODY);
                lbl.setForeground(new Color(0x374151));
                lbl.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xF3F4F6)),
                    new EmptyBorder(0, 16, 0, 16)));

                if (isSelected) {
                    lbl.setBackground(UIConstants.COLOR_TABLE_SELECTION_BG);
                } else if (row == hoveredRow) {
                    lbl.setBackground(UIConstants.COLOR_TABLE_HOVER_BG);
                } else {
                    lbl.setBackground(Color.WHITE);
                }
                return lbl;
            }
        });
    }

    private static class CheckboxRenderer extends JPanel implements TableCellRenderer {
        private final boolean selected;
        CheckboxRenderer(boolean selected) { this.selected = selected; setOpaque(false); }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            this.setBackground(isSelected ? UIConstants.COLOR_TABLE_SELECTION_BG : Color.WHITE);
            // Also need to draw bottom border to match row style
            return this;
        }
        @Override
        protected void paintComponent(Graphics g) {
            // Draw background fill for the cell first to match selection/hover
            if (getBackground() != null) {
                g.setColor(getBackground());
                g.fillRect(0, 0, getWidth(), getHeight());
            }
            // Bottom row border
            g.setColor(new Color(0xF3F4F6));
            g.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
            
            int boxSize = 16;
            int x = (getWidth() - boxSize) / 2;
            int y = (getHeight() - boxSize) / 2;
            
            if (selected) {
                g2.setColor(new Color(0x2980B9));
                g2.fillRoundRect(x, y, boxSize, boxSize, 4, 4);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                // Draw checkmark lines
                g2.drawLine(x + 4, y + 8, x + 7, y + 11);
                g2.drawLine(x + 7, y + 11, x + 12, y + 4);
            } else {
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(x, y, boxSize, boxSize, 4, 4);
                g2.setColor(new Color(0xD1D5DB));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(x, y, boxSize, boxSize, 4, 4);
            }
            g2.dispose();
        }
    }

    private void setupInteractions() {
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int row = rowAtPoint(e.getPoint());
                if (row != hoveredRow) {
                    hoveredRow = row;
                    repaint();
                }
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                hoveredRow = -1;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int selectedRow = getSelectedRow();
        if (selectedRow != -1) {
            Rectangle rect = getCellRect(selectedRow, 0, true);
            g2.setColor(UIConstants.COLOR_TABLE_ACCENT);
            g2.fillRect(0, rect.y, 3, rect.height);
        }
        g2.dispose();
    }
}
