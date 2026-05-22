package ui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class DashboardPanel extends JPanel {

    // Stat label references for live refresh
    private JLabel statVehicles, statSlots, statUtil, statSearches;

    public DashboardPanel(ActivityLog log) {
        setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout(0, 20));
        setBorder(new EmptyBorder(28, 28, 28, 28));

        add(buildHeader(),  BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
    }

    // ── Header ────────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);

        JLabel title = new JLabel("System Dashboard");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_PRIMARY);

        JLabel sub = new JLabel("Smart Parking & Traffic Management — Real-time overview");
        sub.setFont(UITheme.FONT_SMALL);
        sub.setForeground(UITheme.TEXT_MUTED);

        JPanel left = new JPanel(new GridLayout(2, 1, 0, 2));
        left.setOpaque(false);
        left.add(title);
        left.add(sub);

        JLabel status = UITheme.makeBadge("● SYSTEM ONLINE", UITheme.SUCCESS);
        p.add(left,   BorderLayout.WEST);
        p.add(status, BorderLayout.EAST);
        return p;
    }

    // ── Main content ──────────────────────────────────────────────────────────
    private JPanel buildContent() {
        JPanel p = new JPanel(new BorderLayout(0, 18));
        p.setOpaque(false);
        p.add(buildStatRow(),    BorderLayout.NORTH);
        p.add(buildMiddleRow(),  BorderLayout.CENTER);
        return p;
    }

    // ── Stat cards row ────────────────────────────────────────────────────────
    private JPanel buildStatRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 14, 0));
        row.setOpaque(false);

        statVehicles = valueLabel("0",   UITheme.ACCENT);
        statSlots    = valueLabel("0",   UITheme.SUCCESS);
        statUtil     = valueLabel("0 %", UITheme.WARNING);
        statSearches = valueLabel("0",   UITheme.INFO);

        row.add(statCard("Active Vehicles",     statVehicles, "↑ Linked List (O1 add)"));
        row.add(statCard("Available Slots",     statSlots,    "★ Min Heap (Olog n)"));
        row.add(statCard("Lot Utilization",     statUtil,     "⇌ Graph / Dijkstra"));
        row.add(statCard("Index Entries",       statSearches, "⌕ AVL Tree  |  ⚡ HashMap"));
        return row;
    }

    private JPanel statCard(String label, JLabel valLabel, String ds) {
        JPanel card = UITheme.makeCard(new BorderLayout(0, 6));

        JLabel lbl = new JLabel(label.toUpperCase());
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 9));
        lbl.setForeground(UITheme.TEXT_MUTED);

        JLabel dsLabel = new JLabel(ds);
        dsLabel.setFont(UITheme.FONT_SMALL);
        dsLabel.setForeground(UITheme.TEXT_MUTED);

        card.add(lbl,      BorderLayout.NORTH);
        card.add(valLabel, BorderLayout.CENTER);
        card.add(dsLabel,  BorderLayout.SOUTH);
        return card;
    }

    private JLabel valueLabel(String text, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 30));
        l.setForeground(color);
        return l;
    }

    // ── Middle row: modules + info ────────────────────────────────────────────
    private JPanel buildMiddleRow() {
        JPanel row = new JPanel(new GridLayout(1, 2, 14, 0));
        row.setOpaque(false);
        row.add(buildModulesCard());
        row.add(buildInfoCard());
        return row;
    }

    private JPanel buildModulesCard() {
        JPanel card = UITheme.makeCard(new BorderLayout(0, 12));

        JLabel title = UITheme.makeSectionTitle("System Modules");
        title.setBorder(new EmptyBorder(0, 0, 8, 0));
        card.add(title, BorderLayout.NORTH);

        String[][] modules = {
            {"Management",  "Array / Linked List",   "O(1) add · O(n) remove · O(n) display",   "#2563eb"},
            {"Entry/Exit",  "Queue + Stack",          "FIFO processing · LIFO undo",              "#16a34a"},
            {"Slot Priority","Priority Queue (Heap)", "O(log n) insert & extract-min",            "#d97706"},
            {"Route Nav",   "Graph + Dijkstra",       "O((V+E) log V) shortest path",             "#7c3aed"},
            {"Search",      "AVL Tree (BST)",         "O(log n) insert / search / delete",        "#0891b2"},
            {"Fast Access", "Hash Table (HashMap)",   "O(1) average put / get / remove",          "#be185d"},
        };

        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));

        for (String[] m : modules) {
            JPanel row = new JPanel(new BorderLayout(12, 0));
            row.setOpaque(false);
            row.setBorder(new EmptyBorder(5, 0, 5, 0));

            Color accent = Color.decode(m[3]);
            JPanel dot = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g.setColor(accent);
                    g.fillOval(0, 4, 10, 10);
                }
            };
            dot.setOpaque(false);
            dot.setPreferredSize(new Dimension(14, 18));

            JLabel name = new JLabel(m[0]);
            name.setFont(UITheme.FONT_SUBTITLE);
            name.setForeground(UITheme.TEXT_PRIMARY);
            name.setPreferredSize(new Dimension(100, 18));

            JLabel ds = new JLabel(m[1]);
            ds.setFont(UITheme.FONT_SMALL);
            ds.setForeground(accent);
            ds.setPreferredSize(new Dimension(145, 18));

            JLabel cplx = new JLabel(m[2]);
            cplx.setFont(UITheme.FONT_SMALL);
            cplx.setForeground(UITheme.TEXT_MUTED);

            JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            left.setOpaque(false);
            left.add(dot); left.add(name); left.add(ds);

            row.add(left,  BorderLayout.WEST);
            row.add(cplx, BorderLayout.EAST);
            list.add(row);
        }

        card.add(list, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildInfoCard() {
        JPanel card = UITheme.makeCard(new BorderLayout(0, 12));

        JLabel title = UITheme.makeSectionTitle("How to Use");
        title.setBorder(new EmptyBorder(0, 0, 8, 0));
        card.add(title, BorderLayout.NORTH);

        String html =
          "<html><body style='font-family:Segoe UI;font-size:12px;color:#8b949e;line-height:1.6'>" +
          "<b style='color:#e6edf3'>1. Management</b> — Add/remove vehicles &amp; parking slots using the Linked List module.<br><br>" +
          "<b style='color:#e6edf3'>2. Entry / Exit</b> — Enqueue arriving vehicles (FIFO queue), process them in order, " +
          "and undo mistakes via the LIFO undo stack.<br><br>" +
          "<b style='color:#e6edf3'>3. Slot Priority</b> — Add available slots to the Min Heap; assign the nearest slot " +
          "in O(log n) instead of O(n) linear scan.<br><br>" +
          "<b style='color:#e6edf3'>4. Routes</b> — Visualise the parking graph and run Dijkstra's algorithm to find " +
          "the shortest path to an available spot.<br><br>" +
          "<b style='color:#e6edf3'>5. Search</b> — Insert vehicles into the AVL tree; search by licence plate in O(log n).<br><br>" +
          "<b style='color:#e6edf3'>6. Fast Access</b> — Cache vehicle &amp; slot data in the HashMap for O(1) retrieval.<br><br>" +
          "<b style='color:#e6edf3'>Logs</b> — All system events are timestamped in the Logs view." +
          "</body></html>";

        JLabel info = new JLabel(html);
        card.add(new JScrollPane(info) {{
            setBorder(null);
            getViewport().setBackground(UITheme.BG_CARD);
        }}, BorderLayout.CENTER);

        return card;
    }

    /** Called by MainFrame to refresh the stat counters. */
    public void refresh(int vehicles, int slotsAvail, int totalSlots, int indexEntries) {
        statVehicles.setText(String.valueOf(vehicles));
        statSlots.setText(String.valueOf(slotsAvail));
        int pct = totalSlots == 0 ? 0 : (vehicles * 100 / Math.max(totalSlots, 1));
        statUtil.setText(pct + " %");
        statSearches.setText(String.valueOf(indexEntries));
    }
}
