package client.ui;

import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

import client.HRMClient;
import model.Employee;
import model.FamilyDetail;
import model.LeaveApplication;
import socket.ChatClient;

public class EmployeeDashboard extends JFrame {

    private Employee currentUser;
    private ChatClient chatClient;

    public EmployeeDashboard(Employee user) {
        this.currentUser = user;
        this.chatClient = new ChatClient(currentUser.getUsername(), null);
        initializeUI();
    }

    // --- INIT ---
    private void initializeUI() {
        String hrUser = "admin.hr";
        try { hrUser = getHRUsername(); } catch (Exception ignored) {}

        setTitle("HRM System | " + currentUser.getFirstName() + " " + currentUser.getLastName());
        setSize(1150, 820);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setBackground(UIConstants.COLOR_BG_PAGE);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIConstants.COLOR_BG_PAGE);
        root.add(buildTopBar(), BorderLayout.NORTH);

        JTabbedPane tabs = buildTabs(hrUser);
        root.add(tabs, BorderLayout.CENTER);
        add(root);
    }

    private String getHRUsername() {
        try { return HRMClient.getService().getHRUsername(); } catch (Exception e) { return "admin.hr"; }
    }

    // --- TOP BAR ---
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(UIConstants.COLOR_BG_TOPBAR);
        bar.setPreferredSize(new Dimension(0, 52));
        bar.setBorder(new EmptyBorder(0, 24, 0, 20));

        JLabel title = new JLabel("HRM DASHBOARD");
        title.setFont(UIConstants.FONT_TOPBAR);
        title.setForeground(Color.WHITE);
        title.setBorder(new EmptyBorder(0, 4, 0, 0));

        RoundedButton logout = new RoundedButton("LOGOUT", UIConstants.COLOR_DANGER, 6);
        logout.setPreferredSize(new Dimension(110, 32));
        logout.addActionListener(e -> {
            if (chatClient != null) chatClient.logout();
            dispose();
            new LoginScreen().setVisible(true);
        });

        bar.add(title,  BorderLayout.WEST);
        bar.add(logout, BorderLayout.EAST);
        return bar;
    }

    // --- TABBED PANE ---
    private JTabbedPane buildTabs(String hrUser) {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(UIConstants.COLOR_BG_PAGE);
        tabs.setFont(UIConstants.FONT_NAV);

        tabs.addTab("  MY PROFILE  ",      createProfileTab());
        tabs.addTab("  FAMILY DETAILS  ",  createFamilyTab());
        tabs.addTab("  LEAVE BALANCE  ",   createBalanceTab());
        tabs.addTab("  APPLY LEAVE  ",     createApplyTab());
        tabs.addTab("  LEAVE STATUS  ",    createHistoryTab());
        tabs.addTab("  CHAT  ",            new EmployeeChatPanel(currentUser.getUsername(), chatClient, hrUser));
        for (int i = 0; i < tabs.getTabCount(); i++) {
            tabs.setTabComponentAt(i, new ModernTabComponent(tabs, tabs.getTitleAt(i).trim()));
        }
        return tabs;
    }

    // (1) MY PROFILE
    private JPanel createProfileTab() {
        JPanel page = new JPanel(new GridBagLayout());
        page.setBackground(UIConstants.COLOR_BG_PAGE);
        GridBagConstraints outer = new GridBagConstraints();
        outer.insets = new Insets(32, 24, 32, 24);
        outer.fill   = GridBagConstraints.BOTH;

        RoundedPanel infoCard = buildProfileInfoCard();
        outer.gridx = 0; outer.gridy = 0;
        page.add(infoCard, outer);

        RoundedPanel passCard = buildPasswordCard();
        outer.gridx = 1;
        page.add(passCard, outer);
        return page;
    }

    private RoundedPanel buildProfileInfoCard() {
        RoundedPanel card = new RoundedPanel(new GridBagLayout());
        card.setBorder(new EmptyBorder(32, 36, 32, 36));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 8, 8, 8);
        g.fill   = GridBagConstraints.HORIZONTAL;

        JLabel heading = new JLabel("Personal Information");
        heading.setFont(UIConstants.FONT_SECTION);
        heading.setForeground(UIConstants.COLOR_PRIMARY);
        g.gridwidth = 2; g.gridy = 0; g.insets = new Insets(0, 0, 22, 0);
        card.add(heading, g);

        g.gridwidth = 1; g.insets = new Insets(8, 8, 8, 8);
        addInfoRow(card, "Employee ID:",   String.valueOf(currentUser.getId()), 1, g);
        addInfoRow(card, "Username:",      currentUser.getUsername(),           2, g);
        addInfoRow(card, "IC/Passport:",   currentUser.getIcPassport(),         3, g);

        JTextField fn = styledField(currentUser.getFirstName());
        JTextField ln = styledField(currentUser.getLastName());
        addInputRow(card, "First Name:", fn, 4, g);
        addInputRow(card, "Last Name:",  ln, 5, g);

        RoundedButton save = new RoundedButton("UPDATE NAMES", UIConstants.COLOR_PRIMARY);
        save.setPreferredSize(new Dimension(320, 40));
        g.gridwidth = 2; g.gridy = 6; g.insets = new Insets(24, 0, 0, 0);
        card.add(save, g);

        save.addActionListener(e -> new SwingWorker<Boolean, Void>() {
            protected Boolean doInBackground() throws Exception {
                Employee u = new Employee();
                u.setId(currentUser.getId());
                u.setFirstName(fn.getText());
                u.setLastName(ln.getText());
                return HRMClient.getService().updateProfile(u);
            }
            protected void done() {
                popup("Profile", "Name updated successfully!", false);
                currentUser.setFirstName(fn.getText());
                currentUser.setLastName(ln.getText());
            }
        }.execute());
        return card;
    }

    private RoundedPanel buildPasswordCard() {
        RoundedPanel card = new RoundedPanel(new GridBagLayout());
        card.setBorder(new EmptyBorder(32, 36, 32, 36));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 8, 8, 8);
        g.fill   = GridBagConstraints.HORIZONTAL;

        JLabel heading = new JLabel("Security & Password");
        heading.setFont(UIConstants.FONT_SECTION);
        heading.setForeground(UIConstants.COLOR_DANGER);
        g.gridwidth = 2; g.gridy = 0; g.insets = new Insets(0, 0, 22, 0);
        card.add(heading, g);

        JPasswordField oldP = styledPwdField();
        JPasswordField newP = styledPwdField();
        g.gridwidth = 1; g.insets = new Insets(8, 8, 8, 8);
        addInputRow(card, "Current Password:", oldP, 1, g);
        addInputRow(card, "New Password:",     newP, 2, g);

        JLabel spacer = new JLabel(" ");
        spacer.setPreferredSize(new Dimension(0, 80));
        g.gridwidth = 2; g.gridy = 3; g.insets = new Insets(0, 0, 0, 0);
        card.add(spacer, g);

        RoundedButton changeBtn = new RoundedButton("CHANGE PASSWORD", UIConstants.COLOR_DANGER);
        changeBtn.setPreferredSize(new Dimension(320, 40));
        g.gridy = 4; g.insets = new Insets(24, 0, 0, 0);
        card.add(changeBtn, g);

        changeBtn.addActionListener(e -> {
            String oldStr = new String(oldP.getPassword());
            String newStr = new String(newP.getPassword());
            if (oldStr.isEmpty() || newStr.isEmpty()) { popup("Error", "Fill both fields.", true); return; }
            new SwingWorker<Boolean, Void>() {
                protected Boolean doInBackground() throws Exception {
                    return HRMClient.getService().changePassword(currentUser.getId(), oldStr, newStr);
                }
                protected void done() {
                    try {
                        if (get()) { popup("Success", "Password changed!", false); oldP.setText(""); newP.setText(""); }
                        else         popup("Error", "Incorrect current password.", true);
                    } catch (Exception ex) {}
                }
            }.execute();
        });
        return card;
    }

    // (2) FAMILY DETAILS
    private JPanel createFamilyTab() {
        JPanel page = new JPanel(new BorderLayout(0, 0));
        page.setBackground(UIConstants.COLOR_BG_PAGE);
        page.setBorder(new EmptyBorder(22, 22, 22, 22));

        JLabel heading = new JLabel("Family Record Management");
        heading.setFont(UIConstants.FONT_SECTION);
        heading.setForeground(UIConstants.COLOR_TEXT_DARK);
        heading.setBorder(new EmptyBorder(0, 0, 16, 0));
        page.add(heading, BorderLayout.NORTH);

        DefaultTableModel model = new DefaultTableModel(
                new String[]{"", "Full Name", "Relationship", "IC/Passport"}, 0);
        SelectionTable table = new SelectionTable(model);
        table.setFillsViewportHeight(true);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);

        RoundedPanel tableCard = new RoundedPanel(new BorderLayout());
        tableCard.setBorder(new EmptyBorder(0, 0, 0, 0));
        tableCard.add(scroll, BorderLayout.CENTER);
        page.add(tableCard, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actions.setBackground(UIConstants.COLOR_BG_PAGE);
        actions.setBorder(new EmptyBorder(16, 0, 0, 0));

        RoundedButton add  = new RoundedButton("+ ADD MEMBER",    new Color(0x27AE60));
        add.setHoverColor(new Color(0x219A52));
        RoundedButton del  = new RoundedButton("REMOVE SELECTED", new Color(0xE05C4A));
        del.setHoverColor(new Color(0xC94F3E));
        RoundedButton save = new RoundedButton("SUBMIT ALL",      new Color(0x2980B9));
        save.setHoverColor(new Color(0x2471A3));

        RoundedButton refresh = new RoundedButton("REFRESH",         UIConstants.COLOR_PRIMARY);
        refresh.setHoverColor(new Color(0x2471A3));

        add.addActionListener(e -> model.addRow(new Object[]{"", "", ""}));
        del.addActionListener(e -> { 
            int row = table.getSelectedRow();
            if (row < 0) return;
            String name = (String) model.getValueAt(row, 1);
            String prompt = (name != null && !name.isEmpty()) ? "Delete " + name + "?" : "Remove this row?";
            if (ModernDialog.showConfirm(this, "Confirm Removal", "Are you sure you want to " + prompt.toLowerCase())) {
                model.removeRow(row); 
            }
        });
        refresh.addActionListener(e -> {
            if (ModernDialog.showConfirm(this, "Confirm Refresh", "Discard unsaved changes and reload?")) {
                loadFamily(model);
            }
        });
        save.addActionListener(e -> {
            if (table.isEditing()) table.getCellEditor().stopCellEditing();
            List<FamilyDetail> list = new ArrayList<>();
            for (int i = 0; i < model.getRowCount(); i++) {
                String nameValue = (String) model.getValueAt(i, 1);
                String relValue  = (String) model.getValueAt(i, 2);
                String icValue   = (String) model.getValueAt(i, 3);

                if (nameValue == null || nameValue.trim().isEmpty()) continue;

                // 1. Name Validation: Only letters and spaces
                if (!nameValue.trim().matches("^[A-Za-z\\s]+$")) {
                    popup("Validation Error", "Row " + (i+1) + ": Name must contain only letters.", true);
                    return;
                }

                // 2. IC/Passport Validation: NP-PAXXXXXXXX (8 digits)
                if (icValue == null || !icValue.trim().matches("^NP-PA\\d{8}$")) {
                    popup("Validation Error", "Row " + (i+1) + ": IC/Passport must be like 'NP-PA12345678'.", true);
                    return;
                }

                list.add(new FamilyDetail(currentUser.getId(), nameValue.trim(),
                        relValue == null ? "" : relValue.trim(), icValue.trim()));
            }
            new SwingWorker<Boolean, Void>() {
                protected Boolean doInBackground() throws Exception {
                    return HRMClient.getService().updateFamilyDetails(currentUser.getId(), list);
                }
                protected void done() { popup("Success", "Family details saved.", false); }
            }.execute();
        });

        actions.add(add); actions.add(del); actions.add(refresh); actions.add(save);
        page.add(actions, BorderLayout.SOUTH);
        loadFamily(model);
        return page;
    }

    // (3) LEAVE BALANCE
    private JPanel createBalanceTab() {
        JPanel page = new JPanel(new BorderLayout());
        page.setBackground(UIConstants.COLOR_BG_PAGE);
        page.setBorder(new EmptyBorder(22, 22, 22, 22));

        JLabel heading = new JLabel("Leave Balance");
        heading.setFont(UIConstants.FONT_SECTION);
        heading.setForeground(UIConstants.COLOR_TEXT_DARK);
        heading.setBorder(new EmptyBorder(0, 0, 16, 0));
        page.add(heading, BorderLayout.NORTH);

        JLabel annualVal = new JLabel("--", JLabel.CENTER);
        JLabel sickVal   = new JLabel("--", JLabel.CENTER);

        JPanel cards = new JPanel(new FlowLayout(FlowLayout.CENTER, 32, 60));
        cards.setBackground(UIConstants.COLOR_BG_PAGE);
        cards.add(buildMetricCard("Annual Leave",  "Days Available", annualVal, UIConstants.COLOR_SUCCESS));
        cards.add(buildMetricCard("Sick Leave",    "Days Available", sickVal,   UIConstants.COLOR_DANGER));
        page.add(cards, BorderLayout.CENTER);

        RoundedButton refresh = new RoundedButton("REFRESH BALANCE", UIConstants.COLOR_PRIMARY);
        JPanel btmRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btmRow.setBackground(UIConstants.COLOR_BG_PAGE);
        btmRow.add(refresh);
        page.add(btmRow, BorderLayout.SOUTH);

        refresh.addActionListener(e -> loadBalance(annualVal, sickVal));
        loadBalance(annualVal, sickVal);
        return page;
    }

    private RoundedPanel buildMetricCard(String title, String sub, JLabel valLabel, Color accent) {
        RoundedPanel card = new RoundedPanel(new GridBagLayout());
        card.setPreferredSize(new Dimension(260, 200));
        card.setBorder(new EmptyBorder(24, 24, 24, 24));

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1;

        JLabel lbl = new JLabel(title.toUpperCase(), JLabel.CENTER);
        lbl.setFont(UIConstants.FONT_LABEL_M);
        lbl.setForeground(UIConstants.COLOR_TEXT_MUTED);
        g.gridy = 0;
        card.add(lbl, g);

        valLabel.setFont(UIConstants.FONT_METRIC);
        valLabel.setForeground(accent);
        g.gridy = 1; g.insets = new Insets(8, 0, 4, 0);
        card.add(valLabel, g);

        JLabel subLbl = new JLabel(sub, JLabel.CENTER);
        subLbl.setFont(UIConstants.FONT_LABEL);
        subLbl.setForeground(UIConstants.COLOR_TEXT_HINT);
        g.gridy = 2; g.insets = new Insets(0, 0, 0, 0);
        card.add(subLbl, g);
        return card;
    }

    // (4) APPLY LEAVE
    private JPanel createApplyTab() {
        JPanel page = new JPanel(new GridBagLayout());
        page.setBackground(UIConstants.COLOR_BG_PAGE);
        page.setBorder(new EmptyBorder(22, 22, 22, 22));

        RoundedPanel card = new RoundedPanel(new GridBagLayout(), 10);
        card.setBorder(new EmptyBorder(36, 44, 36, 44));

        GridBagConstraints g = new GridBagConstraints();
        g.fill   = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(0, 0, 14, 0);

        JLabel heading = new JLabel("Leave Application Form");
        heading.setFont(UIConstants.FONT_SECTION);
        heading.setForeground(UIConstants.COLOR_PRIMARY);
        g.gridwidth = 2; g.gridy = 0; g.insets = new Insets(0, 0, 24, 0);
        card.add(heading, g);
        g.gridwidth = 1; g.insets = new Insets(0, 0, 14, 0);

        JComboBox<String> type = new JComboBox<>(new String[]{"Annual", "Sick"});
        type.setFont(UIConstants.FONT_BODY);
        addFormRow(card, "Leave Type:", type, 1, g);

        JLabel balLabel = new JLabel("Available Balance: --");
        balLabel.setFont(UIConstants.FONT_LABEL_M);
        balLabel.setForeground(UIConstants.COLOR_PRIMARY);
        g.gridwidth = 2; g.gridy = 2; g.insets = new Insets(0, 0, 6, 0);
        card.add(balLabel, g);
        g.gridwidth = 1; g.insets = new Insets(0, 0, 14, 0);

        JTextField start = styledField(LocalDate.now().toString());
        JTextField end   = styledField("yyyy-MM-dd");
        addFormRow(card, "Start Date:", start, 3, g);
        addFormRow(card, "End Date:",   end,   4, g);

        JLabel daysLbl = new JLabel("Days requested: 0", JLabel.RIGHT);
        daysLbl.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        daysLbl.setForeground(UIConstants.COLOR_TEXT_MUTED);
        g.gridwidth = 2; g.gridy = 5; g.insets = new Insets(0, 0, 6, 0);
        card.add(daysLbl, g);

        KeyAdapter dayCalc = new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                try {
                    LocalDate s = LocalDate.parse(start.getText());
                    LocalDate en = LocalDate.parse(end.getText());
                    long d = ChronoUnit.DAYS.between(s, en) + 1;
                    if (d > 0) daysLbl.setText("Days requested: " + d);
                } catch (Exception ignored) {}
            }
        };
        start.addKeyListener(dayCalc);
        end.addKeyListener(dayCalc);

        RoundedButton submit = new RoundedButton("SUBMIT REQUEST", UIConstants.COLOR_PRIMARY);
        submit.setPreferredSize(new Dimension(Integer.MAX_VALUE, 42));
        g.gridy = 6; g.insets = new Insets(18, 0, 0, 0);
        card.add(submit, g);

        RoundedButton refresh = new RoundedButton("REFRESH FORM & BALANCE", new Color(0x34495E));
        refresh.setPreferredSize(new Dimension(Integer.MAX_VALUE, 38));
        g.gridy = 7; g.insets = new Insets(8, 0, 0, 0);
        card.add(refresh, g);

        Runnable loadBal = () -> new SwingWorker<Integer, Void>() {
            protected Integer doInBackground() throws Exception {
                return HRMClient.getService().checkLeaveBalance(currentUser.getId(), (String) type.getSelectedItem());
            }
            protected void done() {
                try { balLabel.setText("Available Balance: " + get() + " days"); } catch (Exception ignored) {}
            }
        }.execute();

        type.addActionListener(e -> loadBal.run());
        loadBal.run();

        refresh.addActionListener(e -> {
            loadBal.run();
            start.setText(LocalDate.now().toString());
            end.setText("yyyy-MM-dd");
            daysLbl.setText("Days requested: 0");
        });

        submit.addActionListener(e -> {
            try {
                LocalDate sDate = LocalDate.parse(start.getText());
                LocalDate eDate = LocalDate.parse(end.getText());
                if (sDate.isBefore(LocalDate.now())) { popup("Error", "Start date cannot be in the past.", true); return; }
                if (eDate.isBefore(sDate))           { popup("Error", "End date must be after start date.", true); return; }
                long days = ChronoUnit.DAYS.between(sDate, eDate) + 1;
                daysLbl.setText("Days requested: " + days);
                new SwingWorker<Integer, Void>() {
                    protected Integer doInBackground() throws Exception {
                        return HRMClient.getService().checkLeaveBalance(currentUser.getId(), (String) type.getSelectedItem());
                    }
                    protected void done() {
                        try {
                            if (get() < days) { popup("Error", "Insufficient leave balance.", true); return; }
                            new SwingWorker<Boolean, Void>() {
                                protected Boolean doInBackground() throws Exception {
                                    LeaveApplication app = new LeaveApplication(currentUser.getId(), 
                                        (String) type.getSelectedItem(), sDate.toString(), eDate.toString());
                                    return HRMClient.getService().applyLeave(app);
                                }
                                protected void done() {
                                    try {
                                        if (get()) popup("Success", "Leave request submitted!", false);
                                        else        popup("Error", "Failed to submit.", true);
                                    } catch (Exception ex) {}
                                }
                            }.execute();
                        } catch (Exception ex) {}
                    }
                }.execute();
            } catch (Exception ex) {
                popup("Error", "Invalid date format (use yyyy-MM-dd).", true);
            }
        });

        page.add(card, new GridBagConstraints());
        return page;
    }

    // (5) LEAVE STATUS
    private JPanel createHistoryTab() {
        JPanel page = new JPanel(new BorderLayout(0, 16));
        page.setBackground(UIConstants.COLOR_BG_PAGE);
        page.setBorder(new EmptyBorder(22, 22, 22, 22));

        JLabel heading = new JLabel("My Leave Status");
        heading.setFont(UIConstants.FONT_SECTION);
        heading.setForeground(UIConstants.COLOR_TEXT_DARK);
        page.add(heading, BorderLayout.NORTH);

        DefaultTableModel model = new DefaultTableModel(
                new String[]{"ID", "Type", "Start Date", "End Date", "Status"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        SelectionTable table = new SelectionTable(model);
        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int row, int col) {
                JLabel lbl = new JLabel(v == null ? "" : v.toString(), JLabel.CENTER) {
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(getBackground());
                        g2.fillRoundRect(2, 2, getWidth()-4, getHeight()-4, 10, 10);
                        g2.dispose();
                        super.paintComponent(g);
                    }
                };
                lbl.setOpaque(false);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
                String status = v == null ? "" : v.toString().toUpperCase();
                switch (status) {
                    case "APPROVED": lbl.setForeground(UIConstants.BADGE_APPROVED_FG); lbl.setBackground(UIConstants.BADGE_APPROVED_BG); break;
                    case "REJECTED": lbl.setForeground(UIConstants.BADGE_REJECTED_FG); lbl.setBackground(UIConstants.BADGE_REJECTED_BG); break;
                    default:         lbl.setForeground(UIConstants.BADGE_PENDING_FG);  lbl.setBackground(UIConstants.BADGE_PENDING_BG);
                }
                if (sel) lbl.setBackground(lbl.getBackground().darker());
                return lbl;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);

        RoundedPanel tableCard = new RoundedPanel(new BorderLayout());
        tableCard.add(scroll, BorderLayout.CENTER);
        page.add(tableCard, BorderLayout.CENTER);

        RoundedButton refresh = new RoundedButton("REFRESH HISTORY", UIConstants.COLOR_PRIMARY);
        refresh.addActionListener(e -> loadHistory(model));
        JPanel btm = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        btm.setBackground(UIConstants.COLOR_BG_PAGE);
        btm.setBorder(new EmptyBorder(12, 0, 0, 0));
        btm.add(refresh);
        page.add(btm, BorderLayout.SOUTH);

        loadHistory(model);
        return page;
    }

    private void loadFamily(DefaultTableModel m) {
        new SwingWorker<List<FamilyDetail>, Void>() {
            protected List<FamilyDetail> doInBackground() throws Exception {
                return HRMClient.getService().getFamilyDetails(currentUser.getId());
            }
            protected void done() {
                try {
                    m.setRowCount(0);
                    for (FamilyDetail f : get())
                        m.addRow(new Object[]{"", f.getName(), f.getRelationship(), f.getIcPassport()});
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    private void loadBalance(JLabel a, JLabel s) {
        new SwingWorker<int[], Void>() {
            protected int[] doInBackground() throws Exception {
                return new int[]{
                        HRMClient.getService().checkLeaveBalance(currentUser.getId(), "Annual"),
                        HRMClient.getService().checkLeaveBalance(currentUser.getId(), "Sick")};
            }
            protected void done() {
                try { int[] r = get(); a.setText(String.valueOf(r[0])); s.setText(String.valueOf(r[1])); }
                catch (Exception ignored) {}
            }
        }.execute();
    }

    private void loadHistory(DefaultTableModel m) {
        new SwingWorker<List<LeaveApplication>, Void>() {
            protected List<LeaveApplication> doInBackground() throws Exception {
                return HRMClient.getService().checkLeaveStatus(currentUser.getId());
            }
            protected void done() {
                try {
                    m.setRowCount(0);
                    for (LeaveApplication l : get())
                        m.addRow(new Object[]{l.getId(), l.getLeaveType(), l.getStartDate(), l.getEndDate(), l.getStatus()});
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    private void addInfoRow(JPanel p, String label, String value, int row, GridBagConstraints g) {
        g.gridy = row; g.gridx = 0;
        JLabel lbl = new JLabel(label);
        lbl.setFont(UIConstants.FONT_LABEL);
        lbl.setForeground(UIConstants.COLOR_TEXT_MUTED);
        p.add(lbl, g);

        g.gridx = 1;
        JLabel val = new JLabel(value);
        val.setFont(UIConstants.FONT_VALUE);
        val.setForeground(UIConstants.COLOR_TEXT_DARK);
        p.add(val, g);
    }

    private void addInputRow(JPanel p, String label, JComponent field, int row, GridBagConstraints g) {
        g.gridy = row; g.gridx = 0;
        JLabel lbl = new JLabel(label);
        lbl.setFont(UIConstants.FONT_LABEL);
        lbl.setForeground(UIConstants.COLOR_TEXT_MUTED);
        p.add(lbl, g);
        g.gridx = 1;
        p.add(field, g);
    }

    private void addFormRow(JPanel p, String label, JComponent field, int row, GridBagConstraints g) {
        g.gridwidth = 1;
        g.gridy = row; g.gridx = 0;
        JLabel lbl = new JLabel(label);
        lbl.setFont(UIConstants.FONT_LABEL);
        lbl.setForeground(UIConstants.COLOR_TEXT_MUTED);
        p.add(lbl, g);
        g.gridx = 1;
        p.add(field, g);
        g.gridwidth = 2;
    }

    private JTextField styledField(String text) {
        JTextField f = new JTextField(text, 16);
        f.setFont(UIConstants.FONT_BODY);
        f.setForeground(UIConstants.COLOR_TEXT_DARK);
        f.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UIConstants.COLOR_BORDER_INPUT, 1, true),
                new EmptyBorder(7, 12, 7, 12)));
        return f;
    }

    private JPasswordField styledPwdField() {
        JPasswordField f = new JPasswordField(16);
        f.setFont(UIConstants.FONT_BODY);
        f.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UIConstants.COLOR_BORDER_INPUT, 1, true),
                new EmptyBorder(7, 12, 7, 12)));
        return f;
    }

    private void popup(String title, String msg, boolean error) {
        ModernDialog.showMessage(this, title, msg, error);
    }
}
