package ui;

import assignment.PriorityAllocator;
import models.ParkingSlot;
import models.Vehicle;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Module 3 — Parking Slot Assignment
 * Data Structure: Binary Min-Heap (Priority Queue)
 * Insert: O(log n)  |  Extract-min: O(log n)  |  vs O(n) linear scan
 */
public class AssignmentPanel extends JPanel {

    private final ActivityLog      log;
    private final PriorityAllocator allocator = new PriorityAllocator();

    // Assign vehicle form
    private JTextField tfVPlate, tfVOwner;
    // Add slot form
    private JTextField tfSlotId, tfDist;

    private JLabel statusLabel;
    private JLabel resultLabel;

    // Heap visualiser panel
    private JPanel heapCanvas;

    // Slot table
    private final DefaultTableModel heapTableModel = new DefaultTableModel(
        new String[]{"Heap Pos", "Slot ID", "Distance (m)", "Status"}, 0) {
        public boolean isCellEditable(int r, int c) { return false; }
    };

    // Comparison table (static)
    private final String[][] compData = {
        { "Insert slot",         "O(log n)", "O(1)  — just append",    "Heap stays sorted; linear list may need shifting"   },
        { "Find nearest slot",   "O(1)",     "O(n)  — full scan",      "Heap root is always min; linear needs full traverse" },
        { "Remove nearest slot", "O(log n)", "O(n)  — remove + shift", "Heap restores order via sift-down; list shifts all"  },
    };

    public AssignmentPanel(ActivityLog log) {
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
        left.add(UITheme.makeSectionTitle("Parking Slot Assignment"));
        left.add(UITheme.makeLabel("Data Structure: Binary Min-Heap (Priority Queue)  ·  nearest slot in O(log n)"));

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

    // ── Left: add slots + assign ──────────────────────────────────────────────
    private JPanel buildLeftColumn() {
        JPanel col = new JPanel(new BorderLayout(0, 12));
        col.setOpaque(false);

        // Add slot card
        JPanel addCard = UITheme.makeCard(new BorderLayout(0, 10));
        addCard.add(UITheme.makeSectionHeader("Add Available Slot", "Heap Insert O(log n)", UITheme.SUCCESS),
                    BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(3, 3, 3, 3);

        tfSlotId = UITheme.makeTextField(10);
        tfDist   = UITheme.makeTextField(10);

        gc.gridx = 0; gc.gridy = 0; gc.weightx = 0;
        form.add(UITheme.makeLabel("Slot ID"), gc);
        gc.gridx = 1; gc.weightx = 1;
        form.add(tfSlotId, gc);

        gc.gridx = 0; gc.gridy = 1; gc.weightx = 0;
        form.add(UITheme.makeLabel("Distance to Gate (m)"), gc);
        gc.gridx = 1; gc.weightx = 1;
        form.add(tfDist, gc);

        JButton addBtn = UITheme.makePrimaryButton("Add to Heap");
        addBtn.addActionListener(e -> addSlot());

        gc.gridx = 0; gc.gridy = 2; gc.gridwidth = 2;
        form.add(addBtn, gc);

        addCard.add(form, BorderLayout.CENTER);
        col.add(addCard, BorderLayout.NORTH);

        // Assign card
        JPanel assignCard = UITheme.makeCard(new BorderLayout(0, 10));
        assignCard.add(UITheme.makeSectionHeader("Assign Best Slot", "Extract-Min O(log n)", UITheme.WARNING),
                       BorderLayout.NORTH);

        JPanel aForm = new JPanel(new GridBagLayout());
        aForm.setOpaque(false);
        GridBagConstraints ag = new GridBagConstraints();
        ag.fill = GridBagConstraints.HORIZONTAL;
        ag.insets = new Insets(3, 3, 3, 3);

        tfVPlate = UITheme.makeTextField(10);
        tfVOwner = UITheme.makeTextField(10);

        ag.gridx = 0; ag.gridy = 0; ag.weightx = 0;
        aForm.add(UITheme.makeLabel("Licence Plate"), ag);
        ag.gridx = 1; ag.weightx = 1;
        aForm.add(tfVPlate, ag);

        ag.gridx = 0; ag.gridy = 1; ag.weightx = 0;
        aForm.add(UITheme.makeLabel("Owner Name"), ag);
        ag.gridx = 1; ag.weightx = 1;
        aForm.add(tfVOwner, ag);

        JButton assignBtn = UITheme.makeButton("Assign Nearest Slot →", new Color(120, 60, 0));
        assignBtn.setForeground(UITheme.WARNING);
        assignBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        assignBtn.addActionListener(e -> assignSlot());

        ag.gridx = 0; ag.gridy = 2; ag.gridwidth = 2;
        aForm.add(assignBtn, ag);

        resultLabel = new JLabel(" ");
        resultLabel.setFont(UITheme.FONT_BODY);
        resultLabel.setForeground(UITheme.WARNING);
        resultLabel.setBorder(new EmptyBorder(8, 0, 0, 0));

        assignCard.add(aForm,        BorderLayout.CENTER);
        assignCard.add(resultLabel,  BorderLayout.SOUTH);
        col.add(assignCard, BorderLayout.CENTER);

        return col;
    }

    // ── Right: heap visual + comparison table ─────────────────────────────────
    private JPanel buildRightColumn() {
        JPanel col = new JPanel(new BorderLayout(0, 12));
        col.setOpaque(false);

        // Heap visualiser
        JPanel heapCard = UITheme.makeCard(new BorderLayout(0, 8));
        heapCard.add(UITheme.makeSectionHeader("Min-Heap State", "smallest dist = root", UITheme.ACCENT),
                     BorderLayout.NORTH);

        heapCanvas = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawHeap((Graphics2D) g);
            }
        };
        heapCanvas.setBackground(UITheme.BG_INPUT);
        heapCanvas.setPreferredSize(new Dimension(0, 170));
        heapCard.add(heapCanvas, BorderLayout.CENTER);

        // Heap array table
        JTable ht = new JTable(heapTableModel);
        UITheme.styleTable(ht);
        ht.setPreferredScrollableViewportSize(new Dimension(0, 90));
        heapCard.add(UITheme.wrapScroll(ht), BorderLayout.SOUTH);

        col.add(heapCard, BorderLayout.CENTER);

        // Comparison table
        JPanel cmpCard = UITheme.makeCard(new BorderLayout(0, 8));
        cmpCard.add(UITheme.makeSectionHeader("Min-Heap vs Linear Search", null, null), BorderLayout.NORTH);

        String[] cols = { "Operation", "Min-Heap", "Linear List", "Reason" };
        DefaultTableModel cmpModel = new DefaultTableModel(compData, cols) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable cmpTable = new JTable(cmpModel);
        UITheme.styleTable(cmpTable);
        cmpTable.getColumnModel().getColumn(0).setPreferredWidth(120);
        cmpTable.getColumnModel().getColumn(1).setPreferredWidth(70);
        cmpTable.getColumnModel().getColumn(2).setPreferredWidth(90);
        cmpTable.getColumnModel().getColumn(3).setPreferredWidth(200);
        cmpCard.add(UITheme.wrapScroll(cmpTable), BorderLayout.CENTER);

        col.add(cmpCard, BorderLayout.SOUTH);
        return col;
    }

    // ── Complexity banner ─────────────────────────────────────────────────────
    private JPanel buildComplexity() {
        return UITheme.makeComplexityBanner(
            "<b>Min-Heap Advantage:</b> &nbsp;" +
            "The heap property guarantees heap[0] is always the minimum (nearest slot). &nbsp;" +
            "Insert bubbles up → <b>O(log n)</b>. &nbsp; Extract-min sifts down → <b>O(log n)</b>. &nbsp;" +
            "A linear list requires scanning all n slots to find the minimum → <b>O(n)</b>. &nbsp;" +
            "For 200 slots: heap ≈ 8 comparisons vs linear ≈ 200 comparisons."
        );
    }

    // ── Actions ───────────────────────────────────────────────────────────────
    private void addSlot() {
        String id = tfSlotId.getText().trim().toUpperCase();
        String ds = tfDist.getText().trim();
        if (id.isEmpty() || ds.isEmpty()) { status("Fill both fields.", UITheme.DANGER); return; }
        int dist;
        try { dist = Integer.parseInt(ds); } catch (NumberFormatException ex) {
            status("Distance must be a number.", UITheme.DANGER); return;
        }
        ParkingSlot slot = new ParkingSlot(id, dist);
        allocator.addSlot(slot);
        log.log("HEAP  Inserted slot " + id + " (dist=" + dist + "m) — O(log n)");
        status("Slot " + id + " inserted into heap — O(log n).", UITheme.SUCCESS);
        tfSlotId.setText(""); tfDist.setText("");
        refreshHeap();
    }

    private void assignSlot() {
        String plate = tfVPlate.getText().trim().toUpperCase();
        String owner = tfVOwner.getText().trim();
        if (plate.isEmpty() || owner.isEmpty()) { status("Fill vehicle fields.", UITheme.DANGER); return; }
        if (!allocator.hasAvailableSlots()) { status("No available slots in heap.", UITheme.WARNING); return; }

        Vehicle v = new Vehicle(plate, owner, System.currentTimeMillis());
        ParkingSlot best = allocator.assignBestSlot(v);
        log.log("HEAP  Assigned slot " + best.getSlotId() + " (dist=" + best.getDistanceToGate() +
                "m) to " + plate + " — extract-min O(log n)");
        status("Assigned: " + best.getSlotId() + " — O(log n).", UITheme.SUCCESS);
        resultLabel.setText("✓  " + plate + "  →  Slot " + best.getSlotId() +
                            "  (dist: " + best.getDistanceToGate() + " m)");
        tfVPlate.setText(""); tfVOwner.setText("");
        refreshHeap();
    }

    private void refreshHeap() {
        heapTableModel.setRowCount(0);
        ParkingSlot[] slots = allocator.getHeap().toArray();
        for (int i = 0; i < slots.length; i++) {
            ParkingSlot s = slots[i];
            heapTableModel.addRow(new Object[]{
                i == 0 ? "1 (root)" : String.valueOf(i + 1),
                s.getSlotId(),
                s.getDistanceToGate(),
                s.isOccupied() ? "Occupied" : "Available"
            });
        }
        heapCanvas.repaint();
    }

    // ── Heap tree painter ─────────────────────────────────────────────────────
    private void drawHeap(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        ParkingSlot[] slots = allocator.getHeap().toArray();

        if (slots.length == 0) {
            g.setColor(UITheme.TEXT_MUTED);
            g.setFont(UITheme.FONT_BODY);
            g.drawString("Heap is empty — add slots to visualise", 20, heapCanvas.getHeight() / 2);
            return;
        }

        int W = heapCanvas.getWidth();
        int nodeR = 22;
        int levelH = 54;
        // Only draw up to 7 nodes (3 levels) for clarity
        int drawCount = Math.min(slots.length, 7);

        for (int i = 0; i < drawCount; i++) {
            int level = (int)(Math.log(i + 1) / Math.log(2));
            int posInLevel = i - ((1 << level) - 1);
            int nodesInLevel = 1 << level;
            int x = (int)(W * (posInLevel + 0.5) / nodesInLevel);
            int y = 30 + level * levelH;

            // Draw edge to parent
            if (i > 0) {
                int parent = (i - 1) / 2;
                int pLevel = (int)(Math.log(parent + 1) / Math.log(2));
                int pPosInLevel = parent - ((1 << pLevel) - 1);
                int pNodesInLevel = 1 << pLevel;
                int px = (int)(W * (pPosInLevel + 0.5) / pNodesInLevel);
                int py = 30 + pLevel * levelH;
                g.setColor(UITheme.BORDER_LIGHT);
                g.setStroke(new BasicStroke(1.5f));
                g.drawLine(x, y - nodeR, px, py + nodeR);
            }

            // Draw circle
            boolean isRoot = (i == 0);
            g.setColor(isRoot ? UITheme.ACCENT : UITheme.BG_CARD);
            g.fillOval(x - nodeR, y - nodeR, nodeR * 2, nodeR * 2);
            g.setColor(isRoot ? UITheme.ACCENT_HOVER : UITheme.BORDER_LIGHT);
            g.setStroke(new BasicStroke(1.5f));
            g.drawOval(x - nodeR, y - nodeR, nodeR * 2, nodeR * 2);

            // Label: slot id on top, dist below
            g.setFont(new Font("Segoe UI", Font.BOLD, 9));
            g.setColor(Color.WHITE);
            FontMetrics fm = g.getFontMetrics();
            String idText = slots[i].getSlotId();
            if (idText.length() > 6) idText = idText.substring(0, 6);
            g.drawString(idText, x - fm.stringWidth(idText) / 2, y - 2);
            String distText = slots[i].getDistanceToGate() + "m";
            g.setFont(new Font("Segoe UI", Font.PLAIN, 8));
            fm = g.getFontMetrics();
            g.setColor(isRoot ? new Color(200, 230, 255) : UITheme.TEXT_SECONDARY);
            g.drawString(distText, x - fm.stringWidth(distText) / 2, y + 10);
        }

        if (slots.length > 7) {
            g.setColor(UITheme.TEXT_MUTED);
            g.setFont(UITheme.FONT_SMALL);
            g.drawString("... +" + (slots.length - 7) + " more nodes (shown in table)", 8, heapCanvas.getHeight() - 8);
        }
    }

    private void status(String msg, Color color) {
        statusLabel.setText(msg);
        statusLabel.setForeground(color);
    }
}
