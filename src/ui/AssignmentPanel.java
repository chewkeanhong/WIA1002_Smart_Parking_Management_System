package ui;

import assignment.PriorityAllocator;
import gate_control.GateProcessor;
import management.RecordManager;
import models.ParkingSlot;
import models.ParkingMap;
import models.Vehicle;
import navigation.DijkstraPathfinder;
import navigation.RouteGraph;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Module 3 — Parking Slot Assignment
 * Data Structure: Binary Min-Heap (Priority Queue)
 * Insert: O(log n)  |  Extract-min: O(log n)  |  vs O(n) linear scan
 */
public class AssignmentPanel extends JPanel {

    private static final String DEFAULT_ACCESS_NODE = "ENTRANCE";

    private final ActivityLog      log;
    private final RecordManager    records;
    private final ParkingMap       parkingMap;
    private final GateProcessor    gate;
    private UserPanel              userPanel;
    private RetrievalPanel         retrievalPanel;

    public void setUserPanel(UserPanel up)           { this.userPanel      = up; }
    public void setRetrievalPanel(RetrievalPanel rp) { this.retrievalPanel = rp; }
    private final PriorityAllocator allocator = new PriorityAllocator();
    private final RouteGraph       routeGraph = new RouteGraph();
    private final List<ParkingSlot> registeredSlots = new ArrayList<>();

    // Assign vehicle form
    private JTextField tfVPlate, tfVOwner;
    // Add slot form
    private JTextField tfSlotId;
    private JComboBox<String> gateChoice;

    private JLabel statusLabel;
    private JLabel resultLabel;

    // Heap visualiser panel
    private JPanel heapCanvas;
    private JComboBox<String> heapGateChoice;

    // Slot table
    private final DefaultTableModel heapTableModel = new DefaultTableModel(
        new String[]{"Heap Pos", "Slot ID", "Route Cost (m)", "Status"}, 0) {
        public boolean isCellEditable(int r, int c) { return false; }
    };

    // Comparison table (static)
    private final String[][] compData = {
        { "Insert slot",         "O(log n)", "O(1)  — just append",    "Heap stays sorted; linear list may need shifting"   },
        { "Find nearest slot",   "O(1)",     "O(n)  — full scan",      "Heap root is always min; linear needs full traverse" },
        { "Remove nearest slot", "O(log n)", "O(n)  — remove + shift", "Heap restores order via sift-down; list shifts all"  },
    };

    public AssignmentPanel(ActivityLog log) {
        this(log, null, null, null);
    }

    public AssignmentPanel(ActivityLog log, RecordManager records, ParkingMap parkingMap) {
        this(log, records, parkingMap, null);
    }

    public AssignmentPanel(ActivityLog log, RecordManager records, ParkingMap parkingMap, GateProcessor gate) {
        this.log = log;
        this.records = records;
        this.parkingMap = parkingMap;
        this.gate = gate;
        routeGraph.initializeDashboardLayout();
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
        left.add(UITheme.makeLabel("Gate-aware shortest route + Binary Min-Heap  ·  nearest slot in O(log n)"));

        statusLabel = new JLabel(" ");
        statusLabel.setFont(UITheme.FONT_SMALL);
        statusLabel.setForeground(UITheme.SUCCESS);
        statusLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        JButton resetBtn = UITheme.makeButton("Reset All", UITheme.DANGER);
        resetBtn.setFont(UITheme.FONT_SMALL);
        resetBtn.addActionListener(e -> resetAll());

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(statusLabel);
        right.add(resetBtn);

        p.add(left,  BorderLayout.WEST);
        p.add(right, BorderLayout.EAST);
        return p;
    }

    // ── Body ──────────────────────────────────────────────────────────────────
    private JPanel buildBody() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = 0; g.weighty = 1.0;

        // Left: fills full height, narrower width
        g.gridx = 0; g.weightx = 0.27;
        g.fill = GridBagConstraints.BOTH;
        g.anchor = GridBagConstraints.NORTH;
        p.add(buildLeftColumn(), g);

        // Right: stretches to fill all available vertical space
        g.gridx = 1; g.weightx = 0.73;
        g.fill = GridBagConstraints.BOTH;
        g.anchor = GridBagConstraints.NORTH;
        g.insets = new Insets(0, 14, 0, 0);
        p.add(buildRightColumn(), g);
        return p;
    }

    // ── Left: add slots + assign ──────────────────────────────────────────────
    private JPanel buildLeftColumn() {
        JPanel col = new JPanel(new GridBagLayout());
        col.setOpaque(false);
        GridBagConstraints lc = new GridBagConstraints();
        lc.fill = GridBagConstraints.HORIZONTAL;
        lc.weightx = 1.0; lc.gridx = 0; lc.anchor = GridBagConstraints.NORTH;

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

        gc.gridx = 0; gc.gridy = 0; gc.weightx = 0;
        form.add(UITheme.makeLabel("Slot ID"), gc);
        gc.gridx = 1; gc.weightx = 1;
        form.add(tfSlotId, gc);

        gc.gridx = 0; gc.gridy = 1; gc.weightx = 0;
        form.add(UITheme.makeLabel("Register Slot"), gc);
        gc.gridx = 1; gc.weightx = 1;
        form.add(UITheme.makeLabel("Will be scored from the selected gate"), gc);

        JButton addBtn = UITheme.makePrimaryButton("Add to Heap");
        addBtn.addActionListener(e -> addSlot());

        gc.gridx = 0; gc.gridy = 2; gc.gridwidth = 2;
        form.add(addBtn, gc);

        addCard.add(form, BorderLayout.CENTER);
        lc.gridy = 0; lc.insets = new Insets(0, 0, 0, 0);
        col.add(addCard, lc);

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
        gateChoice = new JComboBox<>(new String[] {
            "Nearest Entrance", "Gate A", "Gate B", "Gate C"
        });
        gateChoice.setBackground(UITheme.BG_INPUT);
        gateChoice.setForeground(UITheme.TEXT_PRIMARY);


        ag.gridx = 0; ag.gridy = 0; ag.weightx = 0;
        aForm.add(UITheme.makeLabel("Licence Plate"), ag);
        ag.gridx = 1; ag.weightx = 1;
        aForm.add(tfVPlate, ag);

        ag.gridx = 0; ag.gridy = 1; ag.weightx = 0;
        aForm.add(UITheme.makeLabel("Owner Name"), ag);
        ag.gridx = 1; ag.weightx = 1;
        aForm.add(tfVOwner, ag);

        ag.gridx = 0; ag.gridy = 2; ag.weightx = 0;
        aForm.add(UITheme.makeLabel("Choose Gate"), ag);
        ag.gridx = 1; ag.weightx = 1;
        aForm.add(gateChoice, ag);

        JButton assignBtn = UITheme.makeButton("Assign Nearest Slot →", new Color(120, 60, 0));
        assignBtn.setForeground(UITheme.WARNING);
        assignBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        assignBtn.addActionListener(e -> assignSlot());

        ag.gridx = 0; ag.gridy = 3; ag.gridwidth = 2;
        aForm.add(assignBtn, ag);

        resultLabel = new JLabel(" ");
        resultLabel.setFont(UITheme.FONT_BODY);
        resultLabel.setForeground(UITheme.WARNING);
        resultLabel.setBorder(new EmptyBorder(8, 0, 0, 0));

        assignCard.add(aForm,        BorderLayout.CENTER);
        assignCard.add(resultLabel,  BorderLayout.SOUTH);
        lc.gridy = 1; lc.insets = new Insets(12, 0, 0, 0);
        lc.weighty = 1.0; lc.fill = GridBagConstraints.BOTH;
        col.add(assignCard, lc);

        return col;
    }

    // ── Right: heap visual + comparison table ─────────────────────────────────
    private JPanel buildRightColumn() {
        JPanel col = new JPanel(new BorderLayout(0, 12));
        col.setOpaque(false);

        // Heap visualiser
        JPanel heapCard = UITheme.makeCard(new BorderLayout(0, 8));

        heapGateChoice = new JComboBox<>(new String[]{"Nearest Entrance", "Gate A", "Gate B", "Gate C"});
        heapGateChoice.setBackground(UITheme.BG_INPUT);
        heapGateChoice.setForeground(UITheme.TEXT_PRIMARY);
        heapGateChoice.addActionListener(e -> refreshHeapForGate());

        JPanel gateRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        gateRow.setOpaque(false);
        gateRow.add(UITheme.makeLabel("Gate:"));
        gateRow.add(heapGateChoice);

        JPanel heapNorth = new JPanel(new BorderLayout(0, 4));
        heapNorth.setOpaque(false);
        heapNorth.add(UITheme.makeSectionHeader("Min-Heap State", "smallest dist = root", UITheme.ACCENT), BorderLayout.NORTH);
        heapNorth.add(gateRow, BorderLayout.SOUTH);
        heapCard.add(heapNorth, BorderLayout.NORTH);

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

        String[] cols = { "Operation", "Min-Heap", "Linear Search", "Reason" };
        DefaultTableModel cmpModel = new DefaultTableModel(compData, cols) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable cmpTable = new JTable(cmpModel);
        UITheme.styleTable(cmpTable);
        cmpTable.setRowSelectionAllowed(false);
        cmpTable.setColumnSelectionAllowed(false);
        cmpTable.setCellSelectionEnabled(false);
        cmpTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        cmpTable.setPreferredScrollableViewportSize(new Dimension(0, 200));
        cmpTable.getColumnModel().getColumn(0).setPreferredWidth(160);
        cmpTable.getColumnModel().getColumn(1).setPreferredWidth(140);
        cmpTable.getColumnModel().getColumn(2).setPreferredWidth(220);
        cmpTable.getColumnModel().getColumn(3).setPreferredWidth(500);

        // Wrapping cell renderer so long text never gets clipped
        TableCellRenderer wrapRenderer = (table, value, isSelected, hasFocus, row, colIdx) -> {
            JTextArea area = new JTextArea(value == null ? "" : value.toString());
            area.setLineWrap(true);
            area.setWrapStyleWord(true);
            area.setFont(UITheme.FONT_BODY);
            area.setForeground(isSelected ? Color.WHITE : UITheme.TEXT_PRIMARY);
            area.setBackground(isSelected ? UITheme.ACCENT : UITheme.BG_TABLE_ROW);
            area.setBorder(new EmptyBorder(8, 10, 8, 10));
            int colWidth = table.getColumnModel().getColumn(colIdx).getWidth();
            area.setSize(new Dimension(colWidth, Short.MAX_VALUE));
            int needed = area.getPreferredSize().height + 8;
            if (table.getRowHeight(row) < needed) {
                table.setRowHeight(row, needed);
            }
            return area;
        };
        for (int i = 0; i < cmpTable.getColumnCount(); i++) {
            cmpTable.getColumnModel().getColumn(i).setCellRenderer(wrapRenderer);
        }
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
        if (id.isEmpty()) { status("Enter a slot ID.", UITheme.DANGER); return; }
        if (routeGraph.getNode(id) == null) { status("Slot ID must exist on the dashboard map.", UITheme.DANGER); return; }

        ParkingSlot slot = findRegisteredSlot(id);
        RouteGraph.Node node = routeGraph.getNode(id);
        boolean liveOccupied = parkingMap != null && parkingMap.isOccupied(id);
        if ((slot != null && slot.isOccupied()) || (node != null && node.isOccupied) || liveOccupied) {
            status("Slot " + id + " is currently occupied and cannot be added to the heap.", UITheme.DANGER);
            return;
        }

        if (slot == null) {
            slot = new ParkingSlot(id, 0);
            registeredSlots.add(slot);
        }
        slot.setOccupied(false);

        refreshHeapForGate();
        log.log("HEAP  Registered slot " + id + " for gate-aware assignment — O(log n)");
        status("Slot " + id + " registered. Heap will score it from the selected gate.", UITheme.SUCCESS);
        tfSlotId.setText("");
    }

    private void assignSlot() {
        String plate = tfVPlate.getText().trim().toUpperCase();
        String owner = tfVOwner.getText().trim();
        if (plate.isEmpty() || owner.isEmpty()) { status("Fill vehicle fields.", UITheme.DANGER); return; }

        String gateNode = normalizeGate((String) gateChoice.getSelectedItem());
        Vehicle existing = records == null ? null : records.findVehicleByPlate(plate);

        // Plate must belong to a registered vehicle — no slots for made-up plates.
        if (existing == null) {
            status("No registered vehicle with plate " + plate + ". Register/approve it at the gate first.", UITheme.DANGER);
            return;
        }
        // Owner name must match the one registered for this plate.
        if (!existing.getOwnerName().equalsIgnoreCase(owner)) {
            status("Owner name does not match plate " + plate + " (registered to " + existing.getOwnerName() + ").", UITheme.DANGER);
            return;
        }
        // Gate must match the one the vehicle registered/entered with.
        String registeredGate = existing.getPreferredGateId();
        if (registeredGate != null && !registeredGate.equalsIgnoreCase(gateNode)) {
            status("Gate does not match plate " + plate + " (registered at " + prettyGate(registeredGate) + ").", UITheme.DANGER);
            return;
        }

        String oldSlotId = existing.getAssignedSlotId();

        refreshHeapForGate();
        if (!allocator.hasAvailableSlots()) { status("No available slots for the selected gate.", UITheme.WARNING); return; }

        int currentCost = Integer.MAX_VALUE;
        if (oldSlotId != null) {
            currentCost = computeRouteCost(gateNode, oldSlotId);
        }

        Vehicle v = existing;
        v.setPreferredGateId(gateNode);
        ParkingSlot best = allocator.peekBestSlot();
        if (best == null) {
            status("No available slots for the selected gate.", UITheme.WARNING);
            return;
        }

        int candidateCost = best.getDistanceToGate();
        if (existing != null && oldSlotId != null && candidateCost >= currentCost) {
            status("Current slot " + oldSlotId + " is already nearer or equal to " + prettyGate(gateNode) + ". No change made.", UITheme.WARNING);
            resultLabel.setText("✓  " + plate + "  remains at slot " + oldSlotId +
                                "  (current route: " + currentCost + " m, candidate: " + candidateCost + " m)");
            refreshHeap();
            return;
        }

        best = allocator.assignBestSlot(v);
        if (best == null) {
            status("No available slots for the selected gate.", UITheme.WARNING);
            return;
        }

        ParkingSlot oldSlot = oldSlotId == null ? null : findRegisteredSlot(oldSlotId);
        if (oldSlotId != null) {
            if (parkingMap != null) {
                parkingMap.markFree(oldSlotId);
            }
            routeGraph.setOccupancy(oldSlotId, false);
            if (records != null) {
                ParkingSlot oldRecord = records.findSlotById(oldSlotId);
                if (oldRecord != null) {
                    oldRecord.setParkedVehicle(null);
                }
            }
        }
        if (oldSlot != null) {
            oldSlot.setParkedVehicle(null);
        }

        if (records != null && existing == null) {
            records.addVehicleRecord(v);
        }

        if (parkingMap != null) {
            parkingMap.markOccupied(best.getSlotId());
        }
        routeGraph.setOccupancy(best.getSlotId(), true);
        if (records != null) {
            ParkingSlot slotRecord = records.findSlotById(best.getSlotId());
            if (slotRecord == null) {
                slotRecord = new ParkingSlot(best.getSlotId(), best.getDistanceToGate());
                records.addParkingSlotRecord(slotRecord);
            } else {
                slotRecord.setDistanceToGate(best.getDistanceToGate());
            }
            slotRecord.setParkedVehicle(v);
        }

        // Remove from gate queue and push to User panel as approved
        if (gate != null) gate.purgeVehicle(v);
        if (userPanel != null) userPanel.addManagedVehicle(v);
        if (retrievalPanel != null) retrievalPanel.syncCaches();

        String priorText = oldSlotId == null ? "" : " (reassigned from " + oldSlotId + ")";
        log.log("HEAP  Assigned slot " + best.getSlotId() + " (dist=" + best.getDistanceToGate() +
                "m) to " + plate + " from " + prettyGate(gateNode) + priorText + " — extract-min O(log n)");
        status((oldSlotId == null ? "Assigned: " : "Reassigned: ") + best.getSlotId() + " from " + prettyGate(gateNode) + " — O(log n).", UITheme.SUCCESS);
        resultLabel.setText("✓  " + plate + "  →  Slot " + best.getSlotId() +
                            "  (route: " + best.getDistanceToGate() + " m, from " + prettyGate(gateNode) +
                            (oldSlotId == null ? "" : ", reassigned from " + oldSlotId) + ")");
        tfVPlate.setText(""); tfVOwner.setText("");
        refreshHeap();
    }

    private void refreshHeapForGate() {
        allocator.clearSlots();
        String gateNode = normalizeGate((String) heapGateChoice.getSelectedItem());
        for (ParkingSlot slot : registeredSlots) {
            if (!slot.isOccupied()) {
                int routeCost = computeRouteCost(gateNode, slot.getSlotId());
                slot.setDistanceToGate(routeCost);
                allocator.addSlot(slot);
            }
        }
        refreshHeap();
    }

    private int computeRouteCost(String gateNode, String slotId) {
        return DijkstraPathfinder.calculateShortestPathCost(routeGraph, gateNode, slotId);
    }

    private ParkingSlot findRegisteredSlot(String slotId) {
        for (ParkingSlot slot : registeredSlots) {
            if (slot.getSlotId().equalsIgnoreCase(slotId)) {
                return slot;
            }
        }
        return null;
    }

    private String normalizeGate(String selection) {
        if (selection == null) {
            return DEFAULT_ACCESS_NODE;
        }
        switch (selection) {
            case "Gate A": return "GATE_A";
            case "Gate B": return "GATE_B";
            case "Gate C": return "GATE_C";
            default: return DEFAULT_ACCESS_NODE;
        }
    }

    private String prettyGate(String gateNode) {
        if (gateNode == null) {
            return "Nearest Entrance";
        }
        switch (gateNode) {
            case "GATE_A": return "Gate A";
            case "GATE_B": return "Gate B";
            case "GATE_C": return "Gate C";
            default: return "Nearest Entrance";
        }
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

    public void resetAll() {
        registeredSlots.clear();
        allocator.clearSlots();
        gateChoice.setSelectedIndex(0);
        heapGateChoice.setSelectedIndex(0);
        tfSlotId.setText("");
        tfVPlate.setText("");
        tfVOwner.setText("");
        resultLabel.setText(" ");
        heapTableModel.setRowCount(0);
        heapCanvas.repaint();
        log.log("HEAP  Panel reset to default state.");
        status("Reset complete.", UITheme.SUCCESS);
    }

    /** Quietly reset without logging — used when navigating away from the panel. */
    public void resetQuiet() {
        registeredSlots.clear();
        allocator.clearSlots();
        gateChoice.setSelectedIndex(0);
        heapGateChoice.setSelectedIndex(0);
        tfSlotId.setText("");
        tfVPlate.setText("");
        tfVOwner.setText("");
        resultLabel.setText(" ");
        statusLabel.setText(" ");
        heapTableModel.setRowCount(0);
        heapCanvas.repaint();
    }

    private void status(String msg, Color color) {
        statusLabel.setText(msg);
        statusLabel.setForeground(color);
    }
}
