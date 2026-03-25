package client.ui;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;

import client.HRMClient;
import model.Employee;
import model.LeaveApplication;

public class HRDashboard extends JFrame {

    private Employee currentUser;
    private HRChatPanel chatPanel;
    private Map<Integer, String> employeeNameMap = new HashMap<>();

    private JLabel totalEmployeesLabel  = new JLabel("0");
    private JLabel pendingRequestsLabel = new JLabel("0");
    private JLabel approvedLeavesLabel  = new JLabel("0");

    private DefaultTableModel allEmployeesModel;
    private JComboBox<EmployeeWrapper> manageBalanceCombo;
    private JComboBox<EmployeeWrapper> reportCombo;

    public HRDashboard(Employee user) {
        this.currentUser = user;
        initializeChat();
        initializeUI();
    }

    private void initializeChat() {
        try {
            List<Employee> emps = HRMClient.getService().getAllEmployees();
            chatPanel = new HRChatPanel(currentUser.getUsername(), emps);
        } catch (Exception e) {
            chatPanel = new HRChatPanel(currentUser.getUsername(), new ArrayList<>());
        }
    }

    private void initializeUI() {
        setTitle("HRM System | HR Administration Panel");
        setSize(1200, 870);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIConstants.COLOR_BG_PAGE);

        root.add(buildTopBar(), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(UIConstants.COLOR_BG_PAGE);
        tabs.setFont(UIConstants.FONT_NAV);

        tabs.addTab("  REGISTER EMPLOYEE  ", createRegisterTab());
        tabs.addTab("  ALL EMPLOYEES  ",     createAllEmployeesTab());
        tabs.addTab("  LEAVE APPROVALS  ",   createLeaveApprovalTab());
        tabs.addTab("  MANAGE BALANCE  ",    createManageBalanceTab());
        tabs.addTab("  YEARLY REPORTS  ",    createReportTab());
        tabs.addTab("  CHAT  ",              chatPanel);

        root.add(tabs, BorderLayout.CENTER);
        for (int i = 0; i < tabs.getTabCount(); i++) {
            tabs.setTabComponentAt(i, new ModernTabComponent(tabs, tabs.getTitleAt(i).trim()));
        }
        root.add(buildStatsBar(), BorderLayout.SOUTH);
        add(root);
        root.revalidate();
        root.repaint();
        refreshAllDashboardData();
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(UIConstants.COLOR_BG_TOPBAR);
        bar.setPreferredSize(new Dimension(0, 52));
        bar.setBorder(new EmptyBorder(0, 24, 0, 20));

        JLabel title = new JLabel("HR ADMINISTRATION");
        title.setFont(UIConstants.FONT_TOPBAR);
        title.setForeground(Color.WHITE);

        RoundedButton logout = new RoundedButton("LOGOUT", UIConstants.COLOR_DANGER, 6);
        logout.setPreferredSize(new Dimension(110, 32));
        logout.addActionListener(e -> {
            if (chatPanel != null && chatPanel.getChatClient() != null)
                chatPanel.getChatClient().logout();
            dispose();
            new LoginScreen().setVisible(true);
        });

        bar.add(title, BorderLayout.WEST);
        bar.add(logout, BorderLayout.EAST);
        return bar;
    }

    private JPanel buildStatsBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.CENTER, 60, 8));
        bar.setBackground(UIConstants.COLOR_BG_TOPBAR);
        bar.setPreferredSize(new Dimension(0, 44));
        bar.setBorder(new MatteBorder(1, 0, 0, 0, new Color(0x2C3E50)));

        bar.add(statPill("Total Employees",  totalEmployeesLabel));
        bar.add(statPill("Pending Requests", pendingRequestsLabel));
        bar.add(statPill("Approved Leaves",  approvedLeavesLabel));
        return bar;
    }

    private JPanel statPill(String label, JLabel val) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        p.setOpaque(false);
        JLabel lbl = new JLabel(label + ": ");
        lbl.setFont(UIConstants.FONT_LABEL);
        lbl.setForeground(UIConstants.COLOR_TEXT_HINT);
        val.setFont(UIConstants.FONT_LABEL_M);
        val.setForeground(Color.WHITE);
        p.add(lbl);
        p.add(val);
        return p;
    }

    private JPanel createRegisterTab() {
        JPanel page = new JPanel(new GridBagLayout());
        page.setBackground(UIConstants.COLOR_BG_PAGE);
        page.setBorder(new EmptyBorder(22, 22, 22, 22));

        RoundedPanel card = new RoundedPanel(new GridBagLayout());
        card.setBorder(new EmptyBorder(32, 44, 32, 44));

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;

        JLabel heading = new JLabel("New Employee Registration");
        heading.setFont(UIConstants.FONT_SECTION);
        heading.setForeground(UIConstants.COLOR_PRIMARY);
        g.gridwidth = 2; g.gridy = 0; g.insets = new Insets(0, 0, 24, 0);
        card.add(heading, g);

        JTextField fn = styledField(20);
        JTextField ln = styledField(20);
        JTextField ic = styledField(20);
        g.gridwidth = 1; g.insets = new Insets(6, 8, 6, 8);
        addFormRow(card, "First Name:",     fn, 1, g);
        addFormRow(card, "Last Name:",      ln, 2, g);
        addFormRow(card, "IC/Passport No:", ic, 3, g);

        RoundedButton reg = new RoundedButton("REGISTER EMPLOYEE", UIConstants.COLOR_PRIMARY);
        reg.setPreferredSize(new Dimension(Integer.MAX_VALUE, 42));
        g.gridwidth = 2; g.gridy = 4; g.insets = new Insets(28, 0, 0, 0);
        card.add(reg, g);

        reg.addActionListener(e -> {
            String fv = fn.getText().trim();
            String lv = ln.getText().trim();
            String icVal = ic.getText().trim();

            if (fv.isEmpty() || lv.isEmpty() || icVal.isEmpty()) {
                popup("Error", "All fields are required.", true); return;
            }
            if (!fv.matches("^[A-Za-z]+$") || !lv.matches("^[A-Za-z]+$")) {
                popup("Validation Error", "Names must contain only letters.", true); return;
            }
            if (!icVal.matches("^NP-PA\\d{8}$")) {
                popup("Format Error", "IC/Passport must follow NP-PAXXXXXXXX (8 digits) format.", true); return;
            }
            new SwingWorker<Boolean, Void>() {
                private boolean duplicate = false;
                protected Boolean doInBackground() throws Exception {
                    if (HRMClient.getService().icPassportExists(icVal)) { duplicate = true; return false; }
                    Employee emp = new Employee();
                    emp.setFirstName(fn.getText());
                    emp.setLastName(ln.getText());
                    emp.setIcPassport(icVal);
                    return HRMClient.getService().registerEmployee(emp);
                }
                protected void done() {
                    try {
                        if (get()) {
                            String u = (fn.getText().toLowerCase() + "." + ln.getText().toLowerCase()).replaceAll("\\s+", "");
                            popup("Success", "Employee registered!\nUsername: " + u + "\nDefault Password: password123", false);
                            fn.setText(""); ln.setText(""); ic.setText("");
                            refreshAllDashboardData();
                        } else {
                            popup("Failed", duplicate ? "IC/Passport already exists!" : "Registration failed.", true);
                        }
                    } catch (Exception ex) { popup("Error", ex.getMessage(), true); }
                }
            }.execute();
        });

        page.add(card, new GridBagConstraints());
        return page;
    }

    private JPanel createAllEmployeesTab() {
        JPanel page = new JPanel(new BorderLayout(0, 0));
        page.setBackground(UIConstants.COLOR_BG_PAGE);
        page.setBorder(new EmptyBorder(22, 22, 22, 22));

        allEmployeesModel = new DefaultTableModel(
                new String[]{"", "ID", "First Name", "Last Name", "IC/Passport", "Username", "Role", "Status"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        SelectionTable table = new SelectionTable(allEmployeesModel);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);

        RoundedPanel tableCard = new RoundedPanel(new BorderLayout());
        tableCard.add(scroll, BorderLayout.CENTER);
        page.add(tableCard, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        actions.setBackground(UIConstants.COLOR_BG_PAGE);
        actions.setBorder(new EmptyBorder(16, 0, 0, 0));

        RoundedButton edit    = new RoundedButton("EDIT EMPLOYEE", UIConstants.COLOR_SUCCESS);
        RoundedButton delete  = new RoundedButton("DELETE",        UIConstants.COLOR_DANGER);
        RoundedButton refresh = new RoundedButton("REFRESH LIST",  UIConstants.COLOR_PRIMARY);

        edit.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { popup("Warning", "Select an employee to edit.", true); return; }
            showEditDialog((int) allEmployeesModel.getValueAt(row, 1), row);
        });

        delete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { popup("Warning", "Select an employee to delete.", true); return; }
            int id = (int) allEmployeesModel.getValueAt(row, 1);
            String name = allEmployeesModel.getValueAt(row, 2) + " " + allEmployeesModel.getValueAt(row, 3);
            if (ModernDialog.showConfirm(this, "Confirm Deletion", "Are you sure you want to delete " + name + "?")) {
                new SwingWorker<Boolean, Void>() {
                    protected Boolean doInBackground() throws Exception { return HRMClient.getService().deleteEmployee(id); }
                    protected void done() {
                        try { if (get()) { popup("Success", "Employee deleted.", false); loadAllEmployees(); }
                              else popup("Error", "Delete failed.", true);
                        } catch (Exception ex) {}
                    }
                }.execute();
            }
        });

        refresh.addActionListener(e -> loadAllEmployees());
        actions.add(edit); actions.add(delete); actions.add(refresh);
        page.add(actions, BorderLayout.SOUTH);
        loadAllEmployees();
        return page;
    }

    private JPanel createLeaveApprovalTab() {
        JPanel page = new JPanel(new BorderLayout(0, 16));
        page.setBackground(UIConstants.COLOR_BG_PAGE);
        page.setBorder(new EmptyBorder(22, 22, 22, 22));

        JLabel heading = new JLabel("Pending Leave Requests");
        heading.setFont(UIConstants.FONT_SECTION);
        heading.setForeground(UIConstants.COLOR_TEXT_DARK);
        heading.setBorder(new EmptyBorder(0, 0, 16, 0));
        page.add(heading, BorderLayout.NORTH);

        DefaultTableModel model = new DefaultTableModel(
                new String[]{"", "App ID", "Employee Name", "Type", "Start Date", "End Date", "Status"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        SelectionTable table = new SelectionTable(model);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);

        RoundedPanel tableCard = new RoundedPanel(new BorderLayout());
        tableCard.add(scroll, BorderLayout.CENTER);
        page.add(tableCard, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        actions.setBackground(UIConstants.COLOR_BG_PAGE);
        actions.setBorder(new EmptyBorder(16, 0, 0, 0));

        RoundedButton approve = new RoundedButton("APPROVE",      UIConstants.COLOR_SUCCESS);
        RoundedButton reject  = new RoundedButton("REJECT",       UIConstants.COLOR_DANGER);
        RoundedButton refresh = new RoundedButton("REFRESH",      new Color(0x34495E));
        RoundedButton history = new RoundedButton("VIEW HISTORY", UIConstants.COLOR_PRIMARY);

        approve.addActionListener(e -> updateStatusWithNotify(table, model, "APPROVED"));
        reject.addActionListener(e  -> updateStatusWithNotify(table, model, "REJECTED"));
        refresh.addActionListener(e -> loadPendingLeaves(model));
        history.addActionListener(e -> showLeaveHistory());

        actions.add(approve); actions.add(reject); actions.add(refresh); actions.add(history);
        page.add(actions, BorderLayout.SOUTH);
        loadPendingLeaves(model);
        return page;
    }

    private void showLeaveHistory() {
        JDialog dialog = new JDialog(this, "Leave Application History", false);
        dialog.setSize(900, 600);
        dialog.setLocationRelativeTo(this);

        JPanel p = new JPanel(new BorderLayout(0, 16));
        p.setBackground(UIConstants.COLOR_BG_PAGE);
        p.setBorder(new EmptyBorder(24, 24, 24, 24));

        JLabel heading = new JLabel("Complete Leave History");
        heading.setFont(UIConstants.FONT_SECTION);
        heading.setForeground(UIConstants.COLOR_TEXT_DARK);
        p.add(heading, BorderLayout.NORTH);

        DefaultTableModel m = new DefaultTableModel(
                new String[]{"ID", "Employee", "Type", "Start", "End", "Status", "Applied Date"}, 0);
        SelectionTable table = new SelectionTable(m);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);
        p.add(scroll, BorderLayout.CENTER);

        new SwingWorker<List<LeaveApplication>, Void>() {
            protected List<LeaveApplication> doInBackground() throws Exception {
                return HRMClient.getService().getAllLeaveApplications();
            }
            protected void done() {
                try {
                    for (LeaveApplication l : get()) {
                        String name = employeeNameMap.getOrDefault(l.getEmployeeId(), "ID: " + l.getEmployeeId());
                        m.addRow(new Object[]{l.getId(), name, l.getLeaveType(), l.getStartDate(),
                                l.getEndDate(), l.getStatus(), l.getAppliedDate()});
                    }
                } catch (Exception ex) {}
            }
        }.execute();
        dialog.add(p);
        dialog.setVisible(true);
    }

    private JPanel createManageBalanceTab() {
        JPanel page = new JPanel(new GridBagLayout());
        page.setBackground(UIConstants.COLOR_BG_PAGE);
        page.setBorder(new EmptyBorder(22, 22, 22, 22));

        RoundedPanel card = new RoundedPanel(new GridBagLayout());
        card.setBorder(new EmptyBorder(32, 44, 32, 44));
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(6, 8, 6, 8);

        JLabel heading = new JLabel("Manage Leave Balance");
        heading.setFont(UIConstants.FONT_SECTION);
        heading.setForeground(UIConstants.COLOR_PRIMARY);
        g.gridwidth = 2; g.gridy = 0; g.insets = new Insets(0, 0, 24, 0);
        card.add(heading, g);
        g.gridwidth = 1; g.insets = new Insets(6, 8, 6, 8);

        manageBalanceCombo = new JComboBox<>();
        manageBalanceCombo.setFont(UIConstants.FONT_BODY);
        JSpinner annualSpin = new JSpinner(new SpinnerNumberModel(14, 0, 30, 1));
        JSpinner sickSpin   = new JSpinner(new SpinnerNumberModel(14, 0, 30, 1));

        addFormRow(card, "Employee:", manageBalanceCombo, 1, g);
        addFormRow(card, "Annual:",   annualSpin, 2, g);
        addFormRow(card, "Sick:",     sickSpin,   3, g);

        RoundedButton loadBtn = new RoundedButton("LOAD BALANCE", new Color(0x34495E));
        RoundedButton updBtn  = new RoundedButton("UPDATE",       UIConstants.COLOR_SUCCESS);
        g.gridy = 4; g.gridx = 0; card.add(loadBtn, g);
        g.gridx = 1; card.add(updBtn, g);

        loadDropdownData(manageBalanceCombo);

        loadBtn.addActionListener(e -> {
            EmployeeWrapper w = (EmployeeWrapper) manageBalanceCombo.getSelectedItem();
            if (w == null) return;
            new SwingWorker<int[], Void>() {
                protected int[] doInBackground() throws Exception {
                    return new int[]{HRMClient.getService().checkLeaveBalance(w.emp.getId(), "Annual"),
                                     HRMClient.getService().checkLeaveBalance(w.emp.getId(), "Sick")};
                }
                protected void done() {
                    try { int[] r = get(); annualSpin.setValue(r[0]); sickSpin.setValue(r[1]); }
                    catch (Exception ex) {}
                }
            }.execute();
        });

        updBtn.addActionListener(e -> {
            EmployeeWrapper w = (EmployeeWrapper) manageBalanceCombo.getSelectedItem();
            if (w == null) return;
            new SwingWorker<Boolean, Void>() {
                protected Boolean doInBackground() throws Exception {
                    return HRMClient.getService().updateLeaveBalance(w.emp.getId(),
                            (int) annualSpin.getValue(), (int) sickSpin.getValue());
                }
                protected void done() {
                    try { if (get()) popup("Success", "Balance updated!", false); } catch (Exception ex) {}
                }
            }.execute();
        });

        page.add(card, new GridBagConstraints());
        return page;
    }

    private JPanel createReportTab() {
        JPanel page = new JPanel(new GridBagLayout());
        page.setBackground(UIConstants.COLOR_BG_PAGE);
        page.setBorder(new EmptyBorder(22, 22, 22, 22));

        RoundedPanel card = new RoundedPanel(new GridBagLayout());
        card.setBorder(new EmptyBorder(32, 44, 32, 44));
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;

        JLabel heading = new JLabel("Generate Yearly Report");
        heading.setFont(UIConstants.FONT_SECTION);
        heading.setForeground(UIConstants.COLOR_PRIMARY);
        g.gridwidth = 2; g.gridy = 0; g.insets = new Insets(0, 0, 24, 0);
        card.add(heading, g);
        g.gridwidth = 1; g.insets = new Insets(6, 8, 6, 8);

        reportCombo = new JComboBox<>();
        reportCombo.setFont(UIConstants.FONT_BODY);
        JSpinner yrSpin = new JSpinner(new SpinnerNumberModel(2026, 2025, 2026, 1));
        yrSpin.setEditor(new JSpinner.NumberEditor(yrSpin, "#"));

        addFormRow(card, "Employee:", reportCombo, 1, g);
        addFormRow(card, "Year:",     yrSpin,      2, g);

        RoundedButton gen = new RoundedButton("GENERATE REPORT", UIConstants.COLOR_SUCCESS);
        gen.setPreferredSize(new Dimension(Integer.MAX_VALUE, 42));
        g.gridwidth = 2; g.gridy = 3; g.insets = new Insets(28, 0, 0, 0);
        card.add(gen, g);

        loadDropdownData(reportCombo);

        gen.addActionListener(e -> {
            EmployeeWrapper w = (EmployeeWrapper) reportCombo.getSelectedItem();
            if (w == null) return;
            int yr = (int) yrSpin.getValue();
            new SwingWorker<String, Void>() {
                protected String doInBackground() throws Exception {
                    return HRMClient.getService().generateYearlyReport(w.emp.getId(), yr);
                }
                protected void done() {
                    try {
                        JTextArea a = new JTextArea(get());
                        a.setFont(new Font("Consolas", Font.PLAIN, 12));
                        a.setEditable(false);
                        a.setMargin(new Insets(10, 10, 10, 10));
                        JScrollPane s = new JScrollPane(a);
                        s.setPreferredSize(new Dimension(600, 500));
                        JOptionPane.showMessageDialog(HRDashboard.this, s, "Yearly Employee Report", JOptionPane.PLAIN_MESSAGE);
                    } catch (Exception ex) {}
                }
            }.execute();
        });

        page.add(card, new GridBagConstraints());
        return page;
    }

    private void loadAllEmployees() {
        if (allEmployeesModel == null) return;
        new SwingWorker<List<Employee>, Void>() {
            protected List<Employee> doInBackground() throws Exception {
                List<Employee> list = HRMClient.getService().getAllEmployees();
                employeeNameMap.clear();
                for (Employee e : list)
                    employeeNameMap.put(e.getId(), e.getFirstName() + " " + e.getLastName());
                return list;
            }
            protected void done() {
                try {
                    allEmployeesModel.setRowCount(0);
                    for (Employee e : get())
                        allEmployeesModel.addRow(new Object[]{"", e.getId(), e.getFirstName(), e.getLastName(),
                                e.getIcPassport(), e.getUsername(), e.getRole(), e.getStatusString()});
                } catch (Exception ex) {}
            }
        }.execute();
    }

    private void updateStatusWithNotify(JTable table, DefaultTableModel model, String status) {
        int row = table.getSelectedRow();
        if (row < 0) return;
        int leaveId  = (int)    model.getValueAt(row, 1);
        String empName = (String) model.getValueAt(row, 2);
        String type    = (String) model.getValueAt(row, 3);
        String start   = (String) model.getValueAt(row, 4);
        String end     = (String) model.getValueAt(row, 5);

        new SwingWorker<Boolean, Void>() {
            protected Boolean doInBackground() throws Exception {
                boolean ok = status.equals("APPROVED")
                        ? HRMClient.getService().approveLeave(leaveId)
                        : HRMClient.getService().rejectLeave(leaveId);
                if (ok && status.equals("APPROVED"))
                    HRMClient.getService().updatePayrollAfterLeaveApproval(0, 5);
                if (ok && chatPanel != null) {
                    List<Employee> emps = HRMClient.getService().getAllEmployees();
                    for (Employee e : emps) {
                        if ((e.getFirstName() + " " + e.getLastName()).equals(empName)) {
                            chatPanel.getChatClient().sendNotification(e.getUsername(),
                                    "Your " + type + " leave (" + start + " to " + end + ") has been " + status);
                            break;
                        }
                    }
                }
                return ok;
            }
            protected void done() {
                try { if (get()) { popup("Success", "Updated!", false); loadPendingLeaves(model); } }
                catch (Exception ex) {}
            }
        }.execute();
    }

    private void loadPendingLeaves(DefaultTableModel m) {
        new SwingWorker<List<LeaveApplication>, Void>() {
            protected List<LeaveApplication> doInBackground() throws Exception {
                return HRMClient.getService().getPendingLeaveApplications();
            }
            protected void done() {
                try {
                    m.setRowCount(0);
                    for (LeaveApplication l : get()) {
                        String name = employeeNameMap.getOrDefault(l.getEmployeeId(), "ID: " + l.getEmployeeId());
                        m.addRow(new Object[]{"", l.getId(), name, l.getLeaveType(), l.getStartDate(), l.getEndDate(), l.getStatus()});
                    }
                } catch (Exception ex) {}
            }
        }.execute();
    }

    private void loadDropdownData(JComboBox<EmployeeWrapper> combo) {
        if (combo == null) return;
        new SwingWorker<List<Employee>, Void>() {
            protected List<Employee> doInBackground() throws Exception {
                return HRMClient.getService().getAllEmployees();
            }
            protected void done() {
                try {
                    EmployeeWrapper selected = (EmployeeWrapper) combo.getSelectedItem();
                    combo.removeAllItems();
                    for (Employee e : get()) {
                        EmployeeWrapper w = new EmployeeWrapper(e);
                        combo.addItem(w);
                        if (selected != null && selected.emp.getId() == e.getId())
                            combo.setSelectedItem(w);
                    }
                } catch (Exception ex) {}
            }
        }.execute();
    }

    private void loadStatistics() {
        new SwingWorker<Map<String, Integer>, Void>() {
            protected Map<String, Integer> doInBackground() throws Exception {
                return HRMClient.getService().getHRStatistics();
            }
            protected void done() {
                try {
                    Map<String, Integer> s = get();
                    totalEmployeesLabel.setText(String.valueOf(s.getOrDefault("TotalEmployees", 0)));
                    pendingRequestsLabel.setText(String.valueOf(s.getOrDefault("PendingLeaveRequests", 0)));
                    approvedLeavesLabel.setText(String.valueOf(s.getOrDefault("ApprovedLeaves", 0)));
                } catch (Exception ex) {}
            }
        }.execute();
    }

    private void refreshAllDashboardData() {
        loadAllEmployees();
        loadDropdownData(manageBalanceCombo);
        loadDropdownData(reportCombo);
        loadStatistics();
        if (chatPanel != null) {
            new SwingWorker<List<Employee>, Void>() {
                protected List<Employee> doInBackground() throws Exception {
                    return HRMClient.getService().getAllEmployees();
                }
                protected void done() {
                    try { chatPanel.updateEmployeeList(get()); } catch (Exception e) {}
                }
            }.execute();
        }
    }

    private void showEditDialog(int id, int row) {
        String fnStr   = (String) allEmployeesModel.getValueAt(row, 2);
        String lnStr   = (String) allEmployeesModel.getValueAt(row, 3);
        String icStr   = (String) allEmployeesModel.getValueAt(row, 4);
        String userStr = (String) allEmployeesModel.getValueAt(row, 5);
        String roleStr = (String) allEmployeesModel.getValueAt(row, 6);

        JTextField fnField   = styledField(fnStr);
        JTextField lnField   = styledField(lnStr);
        JTextField icField   = styledField(icStr);
        JTextField userField = styledField(userStr);
        JPasswordField passField = new JPasswordField(16);
        passField.setFont(UIConstants.FONT_BODY);
        passField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UIConstants.COLOR_BORDER_INPUT, 1, true), new EmptyBorder(7, 12, 7, 12)));
        JComboBox<String> roleCombo = new JComboBox<>(new String[]{"EMPLOYEE", "HR"});
        roleCombo.setSelectedItem(roleStr);

        JPanel msg = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 8, 6, 8);
        g.fill = GridBagConstraints.HORIZONTAL;

        JLabel heading = new JLabel("Update Employee Profile");
        heading.setFont(UIConstants.FONT_SECTION);
        heading.setForeground(UIConstants.COLOR_PRIMARY);
        g.gridwidth = 2; g.gridy = 0; msg.add(heading, g);
        g.gridwidth = 1;

        addFormRow(msg, "First Name:",    fnField,   1, g);
        addFormRow(msg, "Last Name:",     lnField,   2, g);
        addFormRow(msg, "IC / Passport:", icField,   3, g);
        addFormRow(msg, "Username:",      userField, 4, g);
        addFormRow(msg, "New Password:",  passField, 5, g);

        JLabel note = new JLabel("(Leave blank to keep current password)");
        note.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        note.setForeground(UIConstants.COLOR_TEXT_HINT);
        g.gridy = 6; g.gridx = 1; msg.add(note, g);

        addFormRow(msg, "Role:", roleCombo, 7, g);

        if (JOptionPane.showConfirmDialog(this, msg, "HRM System | Full Profile Edit",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
            String newIc   = icField.getText().trim();
            String newUser = userField.getText().trim();
            String newPass = new String(passField.getPassword());

            if (!newIc.matches("^NP-PA\\d{8}$")) { popup("Format Error", "IC must follow NP-PAXXXXXXXX.", true); return; }
            if (newUser.isEmpty()) { popup("Error", "Username cannot be empty.", true); return; }

            new SwingWorker<Boolean, Void>() {
                private boolean duplicate = false;
                protected Boolean doInBackground() throws Exception {
                    if (!newIc.equals(icStr) && HRMClient.getService().icPassportExists(newIc)) {
                        duplicate = true; return false;
                    }
                    Employee e = new Employee();
                    e.setId(id);
                    e.setFirstName(fnField.getText().trim());
                    e.setLastName(lnField.getText().trim());
                    e.setIcPassport(newIc);
                    e.setRole((String) roleCombo.getSelectedItem());
                    e.setUsername(newUser);
                    if (!newPass.isEmpty()) e.setPassword(newPass);
                    return HRMClient.getService().updateEmployeeProfile(e);
                }
                protected void done() {
                    try {
                        if (get()) { popup("Success", "Employee updated.", false); loadAllEmployees(); }
                        else popup("Error", duplicate ? "IC/Passport already exists!" : "Update failed.", true);
                    } catch (Exception ex) {}
                }
            }.execute();
        }
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
    }

    private JTextField styledField(int cols) {
        JTextField f = new JTextField(cols);
        f.setFont(UIConstants.FONT_BODY);
        f.setForeground(UIConstants.COLOR_TEXT_DARK);
        f.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UIConstants.COLOR_BORDER_INPUT, 1, true), new EmptyBorder(7, 12, 7, 12)));
        return f;
    }

    private JTextField styledField(String text) {
        JTextField f = new JTextField(text, 16);
        f.setFont(UIConstants.FONT_BODY);
        f.setForeground(UIConstants.COLOR_TEXT_DARK);
        f.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UIConstants.COLOR_BORDER_INPUT, 1, true), new EmptyBorder(7, 12, 7, 12)));
        return f;
    }

    private void popup(String title, String msg, boolean error) {
        ModernDialog.showMessage(this, title, msg, error);
    }

    private class EmployeeWrapper {
        Employee emp;
        EmployeeWrapper(Employee e) { this.emp = e; }
        @Override public String toString() {
            return emp.getFirstName() + " " + emp.getLastName() + " (" + emp.getUsername() + ")";
        }
    }
}
