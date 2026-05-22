package ui;

import models.ParkingMap;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class DashboardPanel extends JPanel {

    private JLabel statVehicles, statSlots, statUtil, statSearches;

    private final ParkingMap  parkingMap;
    private JPanel[]          slotCells;
    private JLabel[]          slotLabels;
    private JLabel            freeCountLabel, occupiedCountLabel;

    public DashboardPanel(ActivityLog log, ParkingMap parkingMap) {
        this.parkingMap = parkingMap;
        setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout(0, 16));
        setBorder(new EmptyBorder(28, 28, 28, 28));

        add(buildHeader(),  BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);

        new Timer(1000, e -> refreshMap()).start();
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

        p.add(left, BorderLayout.WEST);
        p.add(UITheme.makeBadge("● SYSTEM ONLINE", UITheme.SUCCESS), BorderLayout.EAST);
        return p;
    }

    // ── Main content ──────────────────────────────────────────────────────────
    private JPanel buildContent() {
        JPanel p = new JPanel(new BorderLayout(0, 14));
        p.setOpaque(false);
        p.add(buildStatRow(),   BorderLayout.NORTH);
        p.add(buildMiddleRow(), BorderLayout.CENTER);
        return p;
    }

    // ── Stat cards ────────────────────────────────────────────────────────────
    private JPanel buildStatRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 14, 0));
        row.setOpaque(false);

        statVehicles = valueLabel("0",   UITheme.ACCENT);
        statSlots    = valueLabel("0",   UITheme.SUCCESS);
        statUtil     = valueLabel("0 %", UITheme.WARNING);
        statSearches = valueLabel("0",   UITheme.INFO);

        row.add(statCard("Active Vehicles",  statVehicles, "↑ Linked List (O1 add)"));
        row.add(statCard("Available Slots",  statSlots,    "★ Min Heap (Olog n)"));
        row.add(statCard("Lot Utilization",  statUtil,     "⇌ Graph / Dijkstra"));
        row.add(statCard("Index Entries",    statSearches, "⌕ AVL Tree  |  ⚡ HashMap"));
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

    // ── Middle: parking map (62%) + modules (38%) ────────────────────────────
    private JPanel buildMiddleRow() {
        JPanel row = new JPanel(new GridBagLayout());
        row.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.BOTH;
        g.weighty = 1.0;
        g.gridy = 0;

        g.gridx = 0; g.weightx = 0.62;
        row.add(buildParkingMapCard(), g);

        g.gridx = 1; g.weightx = 0.38;
        g.insets = new Insets(0, 14, 0, 0);
        row.add(buildModulesCard(), g);
        return row;
    }

    // ── Live Parking Map card ─────────────────────────────────────────────────
    private JPanel buildParkingMapCard() {
        JPanel card = UITheme.makeCard(new BorderLayout(0, 10));

        // ── title row + legend
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        titleRow.add(UITheme.makeSectionTitle("Live Parking Map"), BorderLayout.WEST);
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        legend.setOpaque(false);
        legend.add(legendChip(new Color(22, 80, 40), UITheme.SUCCESS, "Free"));
        legend.add(legendChip(new Color(70, 20, 20), new Color(220, 80, 80), "Occupied"));
        titleRow.add(legend, BorderLayout.EAST);
        card.add(titleRow, BorderLayout.NORTH);

        // ── Build all 30 slot cells
        int total = ParkingMap.total();
        slotCells  = new JPanel[total];
        slotLabels = new JLabel[total];
        for (int i = 0; i < total; i++) {
            JPanel cell = new JPanel(new BorderLayout());
            cell.setBackground(new Color(14, 80, 36));
            cell.setBorder(BorderFactory.createLineBorder(new Color(22, 130, 58), 1));
            JLabel lbl = new JLabel(ParkingMap.slotId(i), SwingConstants.CENTER);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lbl.setForeground(UITheme.SUCCESS);
            cell.add(lbl, BorderLayout.CENTER);
            slotCells[i]  = cell;
            slotLabels[i] = lbl;
        }

        // ── Slot rows with road lanes between them
        int cellH  = 58;
        int laneH  = 18;
        int totalH = 3 * cellH + 2 * laneH;   // 210 px

        JPanel slotsPanel = new JPanel();
        slotsPanel.setOpaque(false);
        slotsPanel.setLayout(new BoxLayout(slotsPanel, BoxLayout.Y_AXIS));
        // no fixed preferred width → BoxLayout fills available horizontal space
        slotsPanel.setPreferredSize(new Dimension(0, totalH));
        slotsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, totalH));
        slotsPanel.add(makeSlotRow(0,  10, cellH));
        slotsPanel.add(makeRoadLane(laneH));
        slotsPanel.add(makeSlotRow(10, 20, cellH));
        slotsPanel.add(makeRoadLane(laneH));
        slotsPanel.add(makeSlotRow(20, 30, cellH));

        // ── Gate A / B vertically centred beside the slot rows
        JPanel gateAWrap = centreV(gateLabel("Gate A"), totalH);
        JPanel gateBWrap = centreV(gateLabel("Gate B"), totalH);

        JPanel mapRow = new JPanel();
        mapRow.setOpaque(false);
        mapRow.setLayout(new BoxLayout(mapRow, BoxLayout.X_AXIS));
        mapRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        mapRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, totalH));
        mapRow.add(gateAWrap);
        mapRow.add(Box.createHorizontalStrut(6));
        mapRow.add(slotsPanel);
        mapRow.add(Box.createHorizontalStrut(6));
        mapRow.add(gateBWrap);

        // ── Gate C label centred above the grid
        JLabel gateCLabel = gateLabel("Gate C");
        gateCLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gateCLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        gateCLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));

        // ── Entrance / Exit driveway directly below the grid
        JPanel driveBar = buildDriveBar();
        driveBar.setAlignmentX(Component.LEFT_ALIGNMENT);

        // ── BoxLayout Y_AXIS with vertical glue centres content, fills width
        JPanel centerWrapper = new JPanel();
        centerWrapper.setOpaque(false);
        centerWrapper.setLayout(new BoxLayout(centerWrapper, BoxLayout.Y_AXIS));
        centerWrapper.add(Box.createVerticalGlue());
        centerWrapper.add(gateCLabel);
        centerWrapper.add(Box.createVerticalStrut(6));
        centerWrapper.add(mapRow);
        centerWrapper.add(driveBar);
        centerWrapper.add(Box.createVerticalGlue());
        card.add(centerWrapper, BorderLayout.CENTER);

        // ── Stats bar
        freeCountLabel     = UITheme.makeLabel("Free: " + ParkingMap.total());
        freeCountLabel.setForeground(UITheme.SUCCESS);
        occupiedCountLabel = UITheme.makeLabel("Occupied: 0");
        occupiedCountLabel.setForeground(new Color(220, 80, 80));
        JLabel totalLbl    = UITheme.makeLabel("Total: " + ParkingMap.total());

        JPanel statsBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 2));
        statsBar.setOpaque(false);
        statsBar.add(freeCountLabel);
        statsBar.add(occupiedCountLabel);
        statsBar.add(totalLbl);
        card.add(statsBar, BorderLayout.SOUTH);

        return card;
    }

    private JPanel makeSlotRow(int from, int to, int height) {
        JPanel row = new JPanel(new GridLayout(1, to - from, 4, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
        row.setPreferredSize(new Dimension(400, height));
        for (int i = from; i < to; i++) row.add(slotCells[i]);
        return row;
    }

    private JPanel makeRoadLane(int height) {
        JPanel lane = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                // asphalt surface
                g2.setColor(new Color(32, 35, 42));
                g2.fillRect(0, 0, getWidth(), getHeight());
                // dashed centre line
                g2.setColor(new Color(210, 175, 0, 180));
                int y = getHeight() / 2;
                for (int x = 6; x < getWidth() - 6; x += 18) {
                    g2.fillRect(x, y - 1, 10, 2);
                }
                g2.dispose();
            }
        };
        lane.setPreferredSize(new Dimension(0, height));
        lane.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
        return lane;
    }

    /** Driveway bar: green dot ENTRANCE  ·  red dot EXIT — centred. */
    private JPanel buildDriveBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 8));
        bar.setBackground(new Color(26, 28, 34));
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(50, 55, 65)));
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        bar.setPreferredSize(new Dimension(0, 38));

        bar.add(colorDot(UITheme.SUCCESS));
        JLabel entr = new JLabel("ENTRANCE");
        entr.setFont(new Font("Segoe UI", Font.BOLD, 11));
        entr.setForeground(UITheme.SUCCESS);
        bar.add(entr);

        bar.add(Box.createHorizontalStrut(24));

        bar.add(colorDot(new Color(220, 60, 60)));
        JLabel exit = new JLabel("EXIT");
        exit.setFont(new Font("Segoe UI", Font.BOLD, 11));
        exit.setForeground(new Color(220, 60, 60));
        bar.add(exit);

        return bar;
    }

    private JPanel colorDot(Color color) {
        JPanel dot = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        dot.setOpaque(false);
        dot.setPreferredSize(new Dimension(13, 13));
        return dot;
    }

    private JLabel gateLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(UITheme.ACCENT);
        return l;
    }

    private JPanel legendChip(Color bg, Color fg, String text) {
        JPanel chip = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        chip.setBackground(bg);
        chip.setBorder(new EmptyBorder(2, 6, 2, 6));
        JLabel l = new JLabel(text);
        l.setFont(UITheme.FONT_SMALL);
        l.setForeground(fg);
        chip.add(l);
        return chip;
    }

    private JPanel centreV(JLabel label, int fixedHeight) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setPreferredSize(new Dimension(58, fixedHeight));
        p.setMaximumSize(new Dimension(58, fixedHeight));
        p.add(Box.createVerticalGlue());
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(label);
        p.add(Box.createVerticalGlue());
        return p;
    }

    private void refreshMap() {
        if (slotCells == null) return;
        for (int i = 0; i < ParkingMap.total(); i++) {
            boolean occ = parkingMap.isOccupied(i);
            slotCells[i].setBackground(occ ? new Color(60, 18, 18) : new Color(14, 80, 36));
            slotCells[i].setBorder(BorderFactory.createLineBorder(
                    occ ? new Color(150, 40, 40) : new Color(22, 130, 58), 1));
            slotLabels[i].setForeground(occ ? new Color(220, 80, 80) : UITheme.SUCCESS);
        }
        freeCountLabel.setText("Free: " + parkingMap.getFreeCount());
        occupiedCountLabel.setText("Occupied: " + parkingMap.getOccupiedCount());
    }

    // ── System Modules card ───────────────────────────────────────────────────
    private JPanel buildModulesCard() {
        JPanel card = UITheme.makeCard(new BorderLayout(0, 12));
        JLabel title = UITheme.makeSectionTitle("System Modules");
        title.setBorder(new EmptyBorder(0, 0, 8, 0));
        card.add(title, BorderLayout.NORTH);

        String[][] modules = {
            {"Management",    "Array / Linked List",   "O(1) add · O(n) remove",      "#2563eb"},
            {"Entry/Exit",    "Queue + Stack",          "FIFO processing · LIFO undo", "#16a34a"},
            {"Slot Priority", "Priority Queue (Heap)",  "O(log n) insert & min",       "#d97706"},
            {"Route Nav",     "Graph + Dijkstra",       "O((V+E) log V) path",         "#7c3aed"},
            {"Search",        "AVL Tree (BST)",         "O(log n) insert / search",    "#0891b2"},
            {"Fast Access",   "Hash Table (HashMap)",   "O(1) avg get / put",          "#be185d"},
        };

        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));

        for (String[] m : modules) {
            JPanel row = new JPanel(new BorderLayout(12, 0));
            row.setOpaque(false);
            row.setBorder(new EmptyBorder(6, 0, 6, 0));

            Color accent = Color.decode(m[3]);
            JPanel dot = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    ((Graphics2D) g).setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g.setColor(accent);
                    g.fillOval(0, 4, 10, 10);
                }
            };
            dot.setOpaque(false);
            dot.setPreferredSize(new Dimension(14, 18));

            JLabel name = new JLabel(m[0]);
            name.setFont(UITheme.FONT_SUBTITLE);
            name.setForeground(UITheme.TEXT_PRIMARY);
            name.setPreferredSize(new Dimension(105, 18));

            JLabel ds = new JLabel(m[1]);
            ds.setFont(UITheme.FONT_SMALL);
            ds.setForeground(accent);
            ds.setPreferredSize(new Dimension(140, 18));

            JLabel cplx = new JLabel(m[2]);
            cplx.setFont(UITheme.FONT_SMALL);
            cplx.setForeground(UITheme.TEXT_MUTED);

            JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            left.setOpaque(false);
            left.add(dot); left.add(name); left.add(ds);

            row.add(left, BorderLayout.WEST);
            row.add(cplx, BorderLayout.EAST);
            list.add(row);
        }

        card.add(list, BorderLayout.CENTER);
        return card;
    }

    /** Called by ManagementPanel to push live stat numbers. */
    public void refresh(int vehicles, int slotsAvail, int totalSlots, int indexEntries) {
        statVehicles.setText(String.valueOf(vehicles));
        statSlots.setText(String.valueOf(slotsAvail));
        int pct = totalSlots == 0 ? 0 : (vehicles * 100 / Math.max(totalSlots, 1));
        statUtil.setText(pct + " %");
        statSearches.setText(String.valueOf(indexEntries));
    }
}
