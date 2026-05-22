package ui;

import gate_control.GateProcessor;
import models.ParkingMap;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class MainFrame extends JFrame {

    // ── Nav entries: [display text, card key, icon] ───────────────────────────
    private static final String[][] NAV = {
        { "Dashboard",    "Dashboard",    "🏠" },
        { "Entry / Exit", "Entry / Exit", "🚗" },
        { "Slot Priority","Slot Priority","⭐" },
        { "Search",       "Search",       "🔍" },
        { "Routes",       "Routes",       "🗺️" },
        { "Logs",         "Logs",         "📋" },
        { "User",         "User",         "👤" },
    };

    private final CardLayout    cardLayout  = new CardLayout();
    private final JPanel        contentArea = new JPanel(cardLayout);
    private final JButton[]     navBtns     = new JButton[NAV.length];
    private final ActivityLog   log         = new ActivityLog();
    private final GateProcessor gate        = new GateProcessor();
    private final ParkingMap    parkingMap  = new ParkingMap();

    // Panels
    private DashboardPanel   dashboardPanel;
    private ManagementPanel  managementPanel;
    private GateControlPanel gateControlPanel;
    private AssignmentPanel  assignmentPanel;
    private NavigationPanel  navigationPanel;
    private SearchPanel      searchPanel;
    private LogsPanel        logsPanel;
    private UserPanel        userPanel;


    public MainFrame() {
        super("SmartPark");
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

        dashboardPanel   = new DashboardPanel(log, parkingMap);
        gateControlPanel = new GateControlPanel(log, gate);
        assignmentPanel  = new AssignmentPanel(log);
        searchPanel      = new SearchPanel(log);
        navigationPanel  = new NavigationPanel(log);
        logsPanel        = new LogsPanel(log);
        managementPanel  = new ManagementPanel(log, dashboardPanel);
        userPanel        = new UserPanel(log, gate, parkingMap);

        contentArea.add(dashboardPanel,   "Dashboard");
        contentArea.add(gateControlPanel, "Entry / Exit");
        contentArea.add(assignmentPanel,  "Slot Priority");
        contentArea.add(searchPanel,      "Search");
        contentArea.add(navigationPanel,  "Routes");
        contentArea.add(logsPanel,        "Logs");
        contentArea.add(userPanel,        "User");
        contentArea.add(managementPanel,  "Management");

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
        return sidebar;
    }

    private JPanel buildBrand() {
        JPanel p = new JPanel(new BorderLayout(12, 0));
        p.setBackground(UITheme.BG_SIDEBAR);
        p.setBorder(new EmptyBorder(22, 18, 20, 18));

        // Colored badge: blue rounded rect with white car emoji
        Font emojiFont = new Font("Segoe UI Emoji", Font.PLAIN, 18);
        JPanel badge = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setColor(UITheme.ACCENT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setFont(emojiFont);
                g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth()  - fm.stringWidth("🚗")) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString("🚗", x, y);
                g2.dispose();
            }
        };
        badge.setOpaque(false);
        badge.setPreferredSize(new Dimension(40, 40));

        JLabel name = new JLabel("SmartPark");
        name.setFont(new Font("Segoe UI", Font.BOLD, 22));
        name.setForeground(UITheme.TEXT_PRIMARY);

        p.add(badge, BorderLayout.WEST);
        p.add(name,  BorderLayout.CENTER);
        return p;
    }

    private JPanel buildNav() {
        JPanel p = new JPanel();
        p.setBackground(UITheme.BG_SIDEBAR);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(4, 10, 4, 10));

        Font emojiFont = new Font("Segoe UI Emoji", Font.PLAIN, 14);

        for (int i = 0; i < NAV.length; i++) {
            final int idx = i;
            final String emoji = NAV[i][2];

            JButton btn = new JButton(NAV[i][0]);
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
            btn.setIconTextGap(10);
            btn.setIcon(new javax.swing.Icon() {
                public void paintIcon(Component c, Graphics g, int x, int y) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g2.setFont(emojiFont);
                    g2.setColor(c.getForeground());
                    g2.drawString(emoji, x, y + 14);
                    g2.dispose();
                }
                public int getIconWidth()  { return 18; }
                public int getIconHeight() { return 16; }
            });
            btn.addActionListener(e -> showPanel(idx));
            navBtns[i] = btn;
            p.add(btn);
            p.add(Box.createVerticalStrut(2));
        }
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

}
