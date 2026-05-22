package ui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class MainFrame extends JFrame {

    // ── Nav entries: [display text, card key, icon] ───────────────────────────
    private static final String[][] NAV = {
        { "Dashboard",    "Dashboard",    "≡" },
        { "Entry / Exit", "Entry / Exit", "↑" },
        { "Slot Priority","Slot Priority","★" },
        { "Search",       "Search",       "⌕" },
        { "Routes",       "Routes",       "⇌" },
        { "Logs",         "Logs",         "⏱" },
    };

    private final CardLayout   cardLayout  = new CardLayout();
    private final JPanel       contentArea = new JPanel(cardLayout);
    private final JButton[]    navBtns     = new JButton[NAV.length];
    private final ActivityLog  log         = new ActivityLog();

    // Panels
    private DashboardPanel  dashboardPanel;
    private ManagementPanel managementPanel;
    private GateControlPanel gateControlPanel;
    private AssignmentPanel  assignmentPanel;
    private NavigationPanel  navigationPanel;
    private SearchPanel      searchPanel;
    private RetrievalPanel   retrievalPanel;
    private LogsPanel        logsPanel;

    // Sidebar stat refs
    private JLabel     utilLabel;
    private JProgressBar utilBar;

    public MainFrame() {
        super("SmartPark — Ops Console");
        UITheme.applyGlobalDefaults();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1320, 860);
        setMinimumSize(new Dimension(1060, 680));
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.BG_DARK);

        buildUI();
        showPanel(0);
        setVisible(true);
    }

    // ── Layout assembly ───────────────────────────────────────────────────────
    private void buildUI() {
        setLayout(new BorderLayout());
        add(buildSidebar(),  BorderLayout.WEST);
        add(buildContent(),  BorderLayout.CENTER);
    }

    private JPanel buildContent() {
        contentArea.setBackground(UITheme.BG_DARK);

        dashboardPanel   = new DashboardPanel(log);
        gateControlPanel = new GateControlPanel(log);
        assignmentPanel  = new AssignmentPanel(log);
        searchPanel      = new SearchPanel(log);
        navigationPanel  = new NavigationPanel(log);
        retrievalPanel   = new RetrievalPanel(log);
        logsPanel        = new LogsPanel(log);
        managementPanel  = new ManagementPanel(log, dashboardPanel);

        contentArea.add(dashboardPanel,   "Dashboard");
        contentArea.add(gateControlPanel, "Entry / Exit");
        contentArea.add(assignmentPanel,  "Slot Priority");
        contentArea.add(searchPanel,      "Search");
        contentArea.add(navigationPanel,  "Routes");
        contentArea.add(logsPanel,        "Logs");
        // Management is accessed via Dashboard as it's embedded in the concept,
        // but we also expose it — nav slot "Management" is not in the sidebar list
        // (matches screenshot). We surface it through the Slot Priority route instead.
        // Add it for direct card access even if unreachable via sidebar:
        contentArea.add(managementPanel, "Management");

        return contentArea;
    }

    // ── Sidebar ───────────────────────────────────────────────────────────────
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(UITheme.BG_SIDEBAR);
        sidebar.setPreferredSize(new Dimension(210, 0));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UITheme.BORDER));

        sidebar.add(buildBrand(),   BorderLayout.NORTH);
        sidebar.add(buildNav(),     BorderLayout.CENTER);
        sidebar.add(buildBottom(),  BorderLayout.SOUTH);
        return sidebar;
    }

    private JPanel buildBrand() {
        JPanel p = new JPanel(new BorderLayout(10, 0));
        p.setBackground(UITheme.BG_SIDEBAR);
        p.setBorder(new EmptyBorder(22, 18, 20, 18));

        // Blue "P" badge
        JLabel icon = new JLabel("P", SwingConstants.CENTER);
        icon.setFont(new Font("Segoe UI", Font.BOLD, 16));
        icon.setForeground(Color.WHITE);
        icon.setBackground(UITheme.ACCENT);
        icon.setOpaque(true);
        icon.setPreferredSize(new Dimension(34, 34));

        JPanel text = new JPanel(new GridLayout(2, 1, 0, 0));
        text.setOpaque(false);
        JLabel name = new JLabel("SmartPark");
        name.setFont(new Font("Segoe UI", Font.BOLD, 17));
        name.setForeground(UITheme.TEXT_PRIMARY);
        JLabel sub = new JLabel("Ops Console");
        sub.setFont(UITheme.FONT_SMALL);
        sub.setForeground(UITheme.TEXT_MUTED);
        text.add(name);
        text.add(sub);

        p.add(icon, BorderLayout.WEST);
        p.add(text, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildNav() {
        JPanel p = new JPanel();
        p.setBackground(UITheme.BG_SIDEBAR);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(4, 10, 4, 10));

        for (int i = 0; i < NAV.length; i++) {
            final int idx = i;
            JButton btn = new JButton(NAV[i][2] + "   " + NAV[i][0]);
            btn.setFont(UITheme.FONT_BODY);
            btn.setForeground(UITheme.TEXT_SECONDARY);
            btn.setBackground(UITheme.BG_SIDEBAR);
            btn.setHorizontalAlignment(SwingConstants.LEFT);
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.setOpaque(true);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
            btn.setBorder(new EmptyBorder(11, 14, 11, 14));
            btn.addActionListener(e -> showPanel(idx));
            navBtns[i] = btn;
            p.add(btn);
            p.add(Box.createVerticalStrut(2));
        }
        return p;
    }

    private JPanel buildBottom() {
        JPanel p = new JPanel();
        p.setBackground(UITheme.BG_SIDEBAR);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(10, 14, 16, 14));

        // Utilization card
        JPanel card = new JPanel();
        card.setBackground(UITheme.BG_CARD);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER, 1),
            new EmptyBorder(12, 12, 12, 12)));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lbl = new JLabel("UTILIZATION");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 8));
        lbl.setForeground(UITheme.TEXT_MUTED);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        utilLabel = new JLabel("0 %");
        utilLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        utilLabel.setForeground(UITheme.TEXT_PRIMARY);
        utilLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        utilBar = new JProgressBar(0, 100);
        utilBar.setValue(0);
        utilBar.setStringPainted(false);
        utilBar.setBackground(UITheme.BG_DARK);
        utilBar.setForeground(UITheme.ACCENT);
        utilBar.setBorderPainted(false);
        utilBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 6));
        utilBar.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(lbl);
        card.add(Box.createVerticalStrut(4));
        card.add(utilLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(utilBar);

        JButton changeBg = UITheme.makeSecondaryButton("⚙  Change Background");
        changeBg.setForeground(UITheme.TEXT_SECONDARY);
        changeBg.setFont(UITheme.FONT_SMALL);
        changeBg.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        changeBg.setAlignmentX(Component.LEFT_ALIGNMENT);
        changeBg.addActionListener(e -> cycleBackground());

        p.add(card);
        p.add(Box.createVerticalStrut(8));
        p.add(changeBg);
        return p;
    }

    // ── Navigation ────────────────────────────────────────────────────────────
    private void showPanel(int idx) {
        for (int i = 0; i < navBtns.length; i++) {
            boolean active = (i == idx);
            navBtns[i].setBackground(active ? UITheme.ACCENT      : UITheme.BG_SIDEBAR);
            navBtns[i].setForeground(active ? Color.WHITE          : UITheme.TEXT_SECONDARY);
            navBtns[i].setBorder(new EmptyBorder(11, active ? 10 : 14, 11, 14));
        }
        cardLayout.show(contentArea, NAV[idx][1]);
    }

    // ── Background cycling ────────────────────────────────────────────────────
    private int bgIdx = 0;
    private static final Color[] BG_CYCLE = {
        UITheme.BG_DARK,
        new Color(10, 15, 10),
        new Color(20, 10, 28),
        new Color(12, 18, 28),
    };

    private void cycleBackground() {
        bgIdx = (bgIdx + 1) % BG_CYCLE.length;
        contentArea.setBackground(BG_CYCLE[bgIdx]);
    }

    /** Called by child panels to update the sidebar utilization indicator. */
    public void setUtilization(int pct) {
        pct = Math.max(0, Math.min(100, pct));
        utilLabel.setText(pct + " %");
        utilBar.setValue(pct);
        utilBar.setForeground(pct < 60 ? UITheme.SUCCESS : pct < 85 ? UITheme.WARNING : UITheme.DANGER);
    }
}
