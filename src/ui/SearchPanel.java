package ui;

import search.SearchEngine;
import search.TreeNode;
import models.Vehicle;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Module 5 — Search System
 * Data Structure: AVL Tree (self-balancing BST)
 * Insert / Search / Delete: O(log n) guaranteed
 * In-order traversal: O(n) — produces alphabetically sorted output
 */
public class SearchPanel extends JPanel {

    private final ActivityLog log;
    private final SearchEngine engine = new SearchEngine();

    private JTextField tfPlate, tfOwner, tfSearch;
    private JLabel     statusLabel, searchResultLabel;
    private TreeCanvas treeCanvas;

    private final DefaultTableModel sortedModel = new DefaultTableModel(
        new String[]{"#", "Licence Plate", "Owner", "Entry Time", "Slot"}, 0) {
        public boolean isCellEditable(int r, int c) { return false; }
    };

    public SearchPanel(ActivityLog log) {
        this.log = log;
        setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout(0, 16));
        setBorder(new EmptyBorder(28, 28, 28, 28));

        add(buildHeader(),     BorderLayout.NORTH);
        add(buildBody(),       BorderLayout.CENTER);
        add(buildComplexity(), BorderLayout.SOUTH);
    }

    // ── Header ────────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);

        JPanel left = new JPanel(new GridLayout(2, 1, 0, 2));
        left.setOpaque(false);
        left.add(UITheme.makeSectionTitle("Vehicle Search System"));
        left.add(UITheme.makeLabel("Data Structure: AVL Tree (self-balancing BST)  ·  O(log n) search vs O(n) linear scan"));

        statusLabel = new JLabel(" ");
        statusLabel.setFont(UITheme.FONT_SMALL);
        statusLabel.setForeground(UITheme.SUCCESS);
        statusLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        p.add(left,        BorderLayout.WEST);
        p.add(statusLabel, BorderLayout.EAST);
        return p;
    }

    // ── Body ──────────────────────────────────────────────────────────────────
    private JPanel buildBody() {
        JPanel p = new JPanel(new GridLayout(1, 2, 14, 0));
        p.setOpaque(false);
        p.add(buildLeftColumn());
        p.add(buildRightColumn());
        return p;
    }

    // ── Left: tree canvas + search ────────────────────────────────────────────
    private JPanel buildLeftColumn() {
        JPanel col = new JPanel(new BorderLayout(0, 10));
        col.setOpaque(false);

        // AVL Tree canvas
        JPanel treeCard = UITheme.makeCard(new BorderLayout(0, 8));
        treeCard.add(UITheme.makeSectionHeader("AVL Tree Visualisation", "auto-balanced", UITheme.ACCENT),
                     BorderLayout.NORTH);

        treeCanvas = new TreeCanvas();
        treeCanvas.setBackground(UITheme.BG_INPUT);
        treeCard.add(treeCanvas, BorderLayout.CENTER);
        col.add(treeCard, BorderLayout.CENTER);

        // Search card
        JPanel searchCard = UITheme.makeCard(new BorderLayout(0, 8));
        searchCard.add(UITheme.makeSectionHeader("Search by Licence Plate", "O(log n)", UITheme.WARNING),
                       BorderLayout.NORTH);

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        row.setOpaque(false);
        tfSearch = UITheme.makeTextField(14);
        JButton searchBtn = UITheme.makePrimaryButton("Search");
        JButton deleteBtn = UITheme.makeDangerButton("Delete");
        searchBtn.addActionListener(e -> searchVehicle());
        deleteBtn.addActionListener(e -> deleteVehicle());
        row.add(UITheme.makeLabel("Plate:"));
        row.add(tfSearch);
        row.add(searchBtn);
        row.add(deleteBtn);
        searchCard.add(row, BorderLayout.NORTH);

        searchResultLabel = new JLabel(" ");
        searchResultLabel.setFont(UITheme.FONT_BODY);
        searchResultLabel.setForeground(UITheme.TEXT_SECONDARY);
        searchResultLabel.setBorder(new EmptyBorder(4, 0, 0, 0));
        searchCard.add(searchResultLabel, BorderLayout.CENTER);

        col.add(searchCard, BorderLayout.SOUTH);
        return col;
    }

    // ── Right: insert form + sorted list ─────────────────────────────────────
    private JPanel buildRightColumn() {
        JPanel col = new JPanel(new BorderLayout(0, 10));
        col.setOpaque(false);

        // Insert form
        JPanel insertCard = UITheme.makeCard(new BorderLayout(0, 8));
        insertCard.add(UITheme.makeSectionHeader("Insert Vehicle", "O(log n)", UITheme.SUCCESS),
                       BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(3, 3, 3, 3);

        tfPlate = UITheme.makeTextField(12);
        tfOwner = UITheme.makeTextField(12);

        gc.gridx = 0; gc.gridy = 0; gc.weightx = 0;
        form.add(UITheme.makeLabel("Licence Plate"), gc);
        gc.gridx = 1; gc.weightx = 1;
        form.add(tfPlate, gc);

        gc.gridx = 0; gc.gridy = 1; gc.weightx = 0;
        form.add(UITheme.makeLabel("Owner Name"), gc);
        gc.gridx = 1; gc.weightx = 1;
        form.add(tfOwner, gc);

        JButton insertBtn = UITheme.makePrimaryButton("Insert into AVL Tree");
        JButton traverseBtn = UITheme.makeSecondaryButton("In-Order Traversal");
        insertBtn.addActionListener(e -> insertVehicle());
        traverseBtn.addActionListener(e -> refreshSortedTable());

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        btns.setOpaque(false);
        btns.add(insertBtn);
        btns.add(traverseBtn);

        gc.gridx = 0; gc.gridy = 2; gc.gridwidth = 2;
        form.add(btns, gc);

        insertCard.add(form, BorderLayout.CENTER);
        col.add(insertCard, BorderLayout.NORTH);

        // Sorted list (in-order traversal output)
        JPanel listCard = UITheme.makeCard(new BorderLayout(0, 8));
        listCard.add(UITheme.makeSectionHeader("In-Order Traversal Result",
                     "sorted A→Z · O(n)", UITheme.INFO), BorderLayout.NORTH);

        JLabel hint = UITheme.makeLabel("In-order traversal visits left → root → right, yielding alphabetically sorted plates.");
        hint.setBorder(new EmptyBorder(0, 0, 6, 0));
        listCard.add(hint, BorderLayout.NORTH);

        JTable sortedTable = new JTable(sortedModel);
        UITheme.styleTable(sortedTable);
        listCard.add(UITheme.wrapScroll(sortedTable), BorderLayout.CENTER);

        // Comparison note
        JPanel cmpNote = new JPanel(new GridLayout(2, 3, 6, 4));
        cmpNote.setOpaque(false);
        cmpNote.setBorder(new EmptyBorder(8, 0, 0, 0));
        String[][] cmp = {
            {"Operation", "AVL Tree", "Unsorted List"},
            {"Search",    "O(log n)", "O(n)"},
            {"Insert",    "O(log n)", "O(1)  (append)"},
            {"Delete",    "O(log n)", "O(n)"},
            {"Sorted out","O(n)",     "O(n log n) sort"},
        };
        for (String[] row2 : cmp) {
            for (int ci = 0; ci < 3; ci++) {
                JLabel l = new JLabel(row2[ci]);
                l.setFont(UITheme.FONT_SMALL);
                l.setForeground(ci == 0 ? UITheme.TEXT_SECONDARY :
                                ci == 1 ? UITheme.SUCCESS : UITheme.WARNING);
                l.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(UITheme.BORDER, 1),
                    new EmptyBorder(4, 6, 4, 6)));
                l.setBackground(UITheme.BG_INPUT);
                l.setOpaque(true);
                cmpNote.add(l);
            }
        }
        listCard.add(cmpNote, BorderLayout.SOUTH);

        col.add(listCard, BorderLayout.CENTER);
        return col;
    }

    // ── Complexity banner ─────────────────────────────────────────────────────
    private JPanel buildComplexity() {
        return UITheme.makeComplexityBanner(
            "<b>AVL Tree (self-balancing BST) Complexity:</b> &nbsp;" +
            "Insert → <b>O(log n)</b> · Search → <b>O(log n)</b> · Delete → <b>O(log n)</b>. &nbsp;" +
            "Balance factor kept in [−1, 0, +1] via rotations, guaranteeing height ≤ 1.44 log₂(n+2). &nbsp;" +
            "In-order traversal → <b>O(n)</b> sorted output. &nbsp;" +
            "Compared to an unsorted list: search is <b>O(log n) vs O(n)</b> — " +
            "for 1 000 vehicles: ~10 comparisons vs ~1 000."
        );
    }

    // ── Actions ───────────────────────────────────────────────────────────────
    private void insertVehicle() {
        String plate = tfPlate.getText().trim().toUpperCase();
        String owner = tfOwner.getText().trim();
        if (plate.isEmpty() || owner.isEmpty()) { status("Fill both fields.", UITheme.DANGER); return; }

        Vehicle v = new Vehicle(plate, owner, System.currentTimeMillis());
        engine.addVehicle(v);
        log.log("SEARCH  Inserted into AVL: " + plate + " (" + owner + ")");
        status("Inserted " + plate + " — O(log n), tree auto-balanced.", UITheme.SUCCESS);
        tfPlate.setText(""); tfOwner.setText("");
        treeCanvas.repaint();
        refreshSortedTable();
    }

    private void searchVehicle() {
        String plate = tfSearch.getText().trim().toUpperCase();
        if (plate.isEmpty()) { status("Enter a plate to search.", UITheme.DANGER); return; }

        Vehicle v = engine.findVehicle(plate);
        if (v == null) {
            searchResultLabel.setText("✗  Not found: " + plate);
            searchResultLabel.setForeground(UITheme.DANGER);
            log.log("SEARCH  Not found: " + plate + " — O(log n) traversal");
            status("Not found.", UITheme.DANGER);
        } else {
            searchResultLabel.setText("✓  Found: " + v.getLicensePlate() + "  |  " +
                v.getOwnerName() + "  |  slot: " + (v.getAssignedSlotId() != null ? v.getAssignedSlotId() : "—"));
            searchResultLabel.setForeground(UITheme.SUCCESS);
            log.log("SEARCH  Found: " + plate + " — O(log n) traversal");
            status("Found " + plate + " — O(log n).", UITheme.SUCCESS);
        }
    }

    private void deleteVehicle() {
        String plate = tfSearch.getText().trim().toUpperCase();
        if (plate.isEmpty()) { status("Enter a plate to delete.", UITheme.DANGER); return; }

        boolean ok = engine.removeVehicle(plate);
        if (ok) {
            searchResultLabel.setText("Deleted: " + plate);
            searchResultLabel.setForeground(UITheme.WARNING);
            log.log("SEARCH  Deleted from AVL: " + plate + " — O(log n), re-balanced");
            status("Deleted " + plate + " — O(log n).", UITheme.WARNING);
            treeCanvas.repaint();
            refreshSortedTable();
        } else {
            searchResultLabel.setText("✗  Not found: " + plate);
            searchResultLabel.setForeground(UITheme.DANGER);
            status("Not found.", UITheme.DANGER);
        }
    }

    private void refreshSortedTable() {
        sortedModel.setRowCount(0);
        List<Vehicle> sorted = engine.getSortedVehicles();
        for (int i = 0; i < sorted.size(); i++) {
            Vehicle v = sorted.get(i);
            sortedModel.addRow(new Object[]{
                i + 1,
                v.getLicensePlate(),
                v.getOwnerName(),
                new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date(v.getEntryTime())),
                v.getAssignedSlotId() != null ? v.getAssignedSlotId() : "—"
            });
        }
        status("In-order traversal: " + sorted.size() + " vehicles (sorted A→Z) — O(n).", UITheme.INFO);
        log.log("SEARCH  In-order traversal: " + sorted.size() + " vehicles");
    }

    private void status(String msg, Color color) {
        statusLabel.setText(msg);
        statusLabel.setForeground(color);
    }

    // ── AVL Tree canvas ───────────────────────────────────────────────────────
    private class TreeCanvas extends JPanel {

        TreeCanvas() {
            setPreferredSize(new Dimension(0, 220));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            TreeNode root = engine.getTree().getRoot();
            if (root == null) {
                g2.setColor(UITheme.TEXT_MUTED);
                g2.setFont(UITheme.FONT_BODY);
                g2.drawString("Tree is empty — insert vehicles to visualise", 20, getHeight() / 2);
                return;
            }
            drawNode(g2, root, getWidth() / 2, 30, getWidth() / 4);
        }

        private void drawNode(Graphics2D g, TreeNode node, int x, int y, int hGap) {
            if (node == null) return;

            int nr = 20; // node radius

            // Draw edges first
            g.setStroke(new BasicStroke(1.5f));
            if (node.left != null) {
                g.setColor(UITheme.BORDER_LIGHT);
                g.drawLine(x, y + nr, x - hGap, y + 58 - nr);
                drawNode(g, node.left,  x - hGap, y + 58, Math.max(hGap / 2, 20));
            }
            if (node.right != null) {
                g.setColor(UITheme.BORDER_LIGHT);
                g.drawLine(x, y + nr, x + hGap, y + 58 - nr);
                drawNode(g, node.right, x + hGap, y + 58, Math.max(hGap / 2, 20));
            }

            // Draw node circle
            g.setColor(UITheme.ACCENT);
            g.fillOval(x - nr, y - nr, nr * 2, nr * 2);
            g.setColor(UITheme.ACCENT_HOVER);
            g.setStroke(new BasicStroke(1.5f));
            g.drawOval(x - nr, y - nr, nr * 2, nr * 2);

            // Label: shortened plate
            String label = node.key.length() > 7 ? node.key.substring(0, 7) : node.key;
            g.setFont(new Font("Segoe UI", Font.BOLD, 8));
            g.setColor(Color.WHITE);
            FontMetrics fm = g.getFontMetrics();
            g.drawString(label, x - fm.stringWidth(label) / 2, y + 3);

            // Height badge (AVL)
            g.setFont(new Font("Segoe UI", Font.PLAIN, 7));
            g.setColor(UITheme.TEXT_MUTED);
            g.drawString("h=" + node.height, x + nr - 2, y - nr + 8);
        }
    }
}
