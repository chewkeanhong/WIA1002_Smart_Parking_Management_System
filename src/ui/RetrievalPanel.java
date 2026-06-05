package ui;

import retrieval.FastAccessor;
import management.RecordManager;
import models.Vehicle;
import models.ParkingSlot;
import models.ParkingMap;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

// Retrieval screen. Looks vehicles and slots up instantly through our custom
// hash table (FastAccessor) and shows what's sitting in each bucket.
public class RetrievalPanel extends JPanel {

    private final ActivityLog   log;
    private final RecordManager records;
    private final FastAccessor  fa = new FastAccessor();
    private ParkingMap          parkingMap;

    public void setParkingMap(ParkingMap pm) { this.parkingMap = pm; }

    // Input fields
    private JTextField tfPlate, tfOwner, tfSlotId, tfDist;
    private JTextField tfLookupPlate, tfLookupSlot;
    private JLabel     statusLabel, lookupResultLabel;

    // Table models
    private final DefaultTableModel vehicleMapModel = new DefaultTableModel(
        new String[]{"Bucket #", "Key (Plate)", "Value (Owner | Time | Slot)"}, 0) {
        public boolean isCellEditable(int r, int c) { return false; }
    };
    private final DefaultTableModel slotMapModel = new DefaultTableModel(
        new String[]{"Bucket #", "Key (Slot ID)", "Value (Distance | Occupied)"}, 0) {
        public boolean isCellEditable(int r, int c) { return false; }
    };

    private JLabel vehicleMapStats, slotMapStats;

    public RetrievalPanel(ActivityLog log) {
        this(log, null);
    }

    public RetrievalPanel(ActivityLog log, RecordManager records) {
        this.log = log;
        this.records = records;
        setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout(0, 16));
        setBorder(new EmptyBorder(28, 28, 28, 28));

        syncCachesFromRecords();
        add(buildHeader(),     BorderLayout.NORTH);
        add(buildBody(),       BorderLayout.CENTER);
        add(buildComplexity(), BorderLayout.SOUTH);

        // Auto-refresh so approvals from any panel (User gate flow, Management, Slot Priority) show up
        new javax.swing.Timer(1000, e -> refreshTables()).start();
    }

    // Header
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);

        JPanel left = new JPanel(new GridLayout(2, 1, 0, 2));
        left.setOpaque(false);
        left.add(UITheme.makeSectionTitle("Fast Data Retrieval"));
        left.add(UITheme.makeLabel("Data Structure: Hash Table (separate chaining)  ·  O(1) average put / get / remove"));

        statusLabel = new JLabel(" ");
        statusLabel.setFont(UITheme.FONT_SMALL);
        statusLabel.setForeground(UITheme.SUCCESS);
        statusLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        p.add(left,        BorderLayout.WEST);
        p.add(statusLabel, BorderLayout.EAST);
        return p;
    }

    // Body
    private JPanel buildBody() {
        JPanel p = new JPanel(new GridLayout(1, 2, 14, 0));
        p.setOpaque(false);
        p.add(buildLeftColumn());
        p.add(buildRightColumn());
        return p;
    }

    // Left: cache forms + lookup
    private JPanel buildLeftColumn() {
        // GridBagLayout with equal vertical weights keeps all three cards visible
        // and shrinks them proportionally — a BorderLayout would collapse the middle one.
        JPanel col = new JPanel(new GridBagLayout());
        col.setOpaque(false);
        GridBagConstraints cgc = new GridBagConstraints();
        cgc.fill = GridBagConstraints.BOTH;
        cgc.gridx = 0; cgc.weightx = 1.0; cgc.weighty = 1.0;

        // Cache vehicle
        JPanel vCard = UITheme.makeCard(new BorderLayout(0, 8));
        vCard.add(UITheme.makeSectionHeader("Cache Vehicle", "put O(1)", UITheme.ACCENT), BorderLayout.NORTH);

        JPanel vForm = buildForm2(
            new String[]{"Licence Plate", "Owner Name"},
            tf -> { tfPlate = tf[0]; tfOwner = tf[1]; });

        JButton addVBtn = UITheme.makePrimaryButton("Cache Vehicle");
        JButton remVBtn = UITheme.makeDangerButton("Remove by Plate");
        addVBtn.addActionListener(e -> cacheVehicle());
        remVBtn.addActionListener(e -> removeVehicle());

        JPanel vBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        vBtns.setOpaque(false);
        vBtns.add(addVBtn); vBtns.add(remVBtn);

        vCard.add(vForm,  BorderLayout.CENTER);
        vCard.add(vBtns,  BorderLayout.SOUTH);
        cgc.gridy = 0; cgc.insets = new Insets(0, 0, 5, 0); col.add(vCard, cgc);

        // Cache slot
        JPanel sCard = UITheme.makeCard(new BorderLayout(0, 8));
        sCard.add(UITheme.makeSectionHeader("Cache Parking Slot", "put O(1)", UITheme.SUCCESS), BorderLayout.NORTH);

        JPanel sForm = buildForm2(
            new String[]{"Slot ID", "Distance (m)"},
            tf -> { tfSlotId = tf[0]; tfDist = tf[1]; });

        JButton addSBtn = UITheme.makePrimaryButton("Cache Slot");
        addSBtn.addActionListener(e -> cacheSlot());
        JButton rmSBtn = UITheme.makeButton("Remove Slot", UITheme.DANGER);
        rmSBtn.addActionListener(e -> removeSlot());
        JPanel sBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        sBtns.setOpaque(false);
        sBtns.add(addSBtn);
        sBtns.add(rmSBtn);

        sCard.add(sForm,  BorderLayout.CENTER);
        sCard.add(sBtns,  BorderLayout.SOUTH);
        cgc.gridy = 1; cgc.insets = new Insets(5, 0, 5, 0); col.add(sCard, cgc);

        // Lookup card
        JPanel lCard = UITheme.makeCard(new BorderLayout(0, 8));
        lCard.add(UITheme.makeSectionHeader("O(1) Lookup", "get(key)", UITheme.WARNING), BorderLayout.NORTH);

        JPanel lForm = new JPanel(new GridBagLayout());
        lForm.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL; gc.insets = new Insets(3,3,3,3);

        tfLookupPlate = UITheme.makeTextField(10);
        tfLookupSlot  = UITheme.makeTextField(10);
        JButton lVBtn = UITheme.makePrimaryButton("Lookup Vehicle");
        JButton lSBtn = UITheme.makeSecondaryButton("Lookup Slot");
        lVBtn.addActionListener(e -> lookupVehicle());
        lSBtn.addActionListener(e -> lookupSlot());

        gc.gridx=0; gc.gridy=0; gc.weightx=0; lForm.add(UITheme.makeLabel("Plate:"),   gc);
        gc.gridx=1; gc.weightx=1;             lForm.add(tfLookupPlate, gc);
        gc.gridx=2; gc.weightx=0;             lForm.add(lVBtn, gc);
        gc.gridx=0; gc.gridy=1; gc.weightx=0; lForm.add(UITheme.makeLabel("Slot ID:"), gc);
        gc.gridx=1; gc.weightx=1;             lForm.add(tfLookupSlot,  gc);
        gc.gridx=2; gc.weightx=0;             lForm.add(lSBtn, gc);

        lCard.add(lForm, BorderLayout.CENTER);

        lookupResultLabel = new JLabel(" ");
        lookupResultLabel.setFont(UITheme.FONT_BODY);
        lookupResultLabel.setForeground(UITheme.TEXT_SECONDARY);
        lookupResultLabel.setBorder(new EmptyBorder(4, 0, 0, 0));
        lCard.add(lookupResultLabel, BorderLayout.SOUTH);

        cgc.gridy = 2; cgc.insets = new Insets(5, 0, 0, 0); col.add(lCard, cgc);
        return col;
    }

    // Right: hash table views
    private JPanel buildRightColumn() {
        JPanel col = new JPanel(new GridLayout(2, 1, 0, 10));
        col.setOpaque(false);

        // Vehicle HashMap
        JPanel vMapCard = UITheme.makeCard(new BorderLayout(0, 6));
        vehicleMapStats = UITheme.makeLabel("HashMap — 0 entries");
        JPanel vmHdr = UITheme.makeSectionHeader("Vehicle HashMap", "key = plate", UITheme.ACCENT);
        vmHdr.add(vehicleMapStats);
        vMapCard.add(vmHdr, BorderLayout.NORTH);
        JTable vt = new JTable(vehicleMapModel);
        UITheme.styleTable(vt);
        vt.getColumnModel().getColumn(0).setPreferredWidth(60);
        vMapCard.add(UITheme.wrapScroll(vt), BorderLayout.CENTER);

        // Slot HashMap
        JPanel sMapCard = UITheme.makeCard(new BorderLayout(0, 6));
        slotMapStats = UITheme.makeLabel("HashMap — 0 entries");
        JPanel smHdr = UITheme.makeSectionHeader("Slot HashMap", "key = slot ID", UITheme.SUCCESS);
        smHdr.add(slotMapStats);
        sMapCard.add(smHdr, BorderLayout.NORTH);
        JTable st = new JTable(slotMapModel);
        UITheme.styleTable(st);
        st.getColumnModel().getColumn(0).setPreferredWidth(60);
        sMapCard.add(UITheme.wrapScroll(st), BorderLayout.CENTER);

        col.add(vMapCard);
        col.add(sMapCard);
        return col;
    }

    // Complexity banner
    private JPanel buildComplexity() {
        return UITheme.makeComplexityBanner(
            "<b>Hash Table (separate chaining):</b> &nbsp;" +
            "Hash function maps key → bucket index in O(1). &nbsp;" +
            "Average-case: put/get/remove → <b>O(1)</b>. &nbsp;" +
            "Worst-case (all keys collide): <b>O(n)</b> — resolved by resizing at 75% load factor. &nbsp;" +
            "Compared to AVL tree search O(log n): HashMap is faster on average but has no sorted order. &nbsp;" +
            "Use HashMap for raw speed; use BST when sorted traversal is needed."
        );
    }

    // Actions
    private void cacheVehicle() {
        String plate = tfPlate.getText().trim().toUpperCase();
        String owner = tfOwner.getText().trim();
        if (plate.isEmpty() || owner.isEmpty()) { status("Fill both fields.", UITheme.DANGER); return; }

        Vehicle v = new Vehicle(plate, owner, System.currentTimeMillis());
        if (records != null && records.findVehicleByPlate(plate) == null) {
            records.addVehicleRecord(v);
        }
        fa.cacheVehicle(v);
        log.log("CACHE  put(vehicle, " + plate + ") — O(1)");
        status("Cached vehicle " + plate + " — O(1).", UITheme.SUCCESS);
        tfPlate.setText(""); tfOwner.setText("");
        refreshTables();
    }

    private void removeVehicle() {
        String plate = tfLookupPlate.getText().trim().toUpperCase();
        if (plate.isEmpty()) { status("Enter plate in lookup field.", UITheme.WARNING); return; }
        syncCachesFromRecords();
        boolean ok = fa.removeVehicle(plate);
        if (ok) {
            log.log("CACHE  remove(vehicle, " + plate + ") — O(1)");
            status("Removed " + plate + " — O(1).", UITheme.WARNING);
            if (records != null) {
                Vehicle existing = records.findVehicleByPlate(plate);
                if (existing != null) {
                    records.removeVehicleRecord(existing);
                }
            }
        } else {
            status(plate + " not in cache.", UITheme.DANGER);
        }
        refreshTables();
    }

    private void cacheSlot() {
        String id = tfSlotId.getText().trim().toUpperCase();
        String ds = tfDist.getText().trim();
        if (id.isEmpty() || ds.isEmpty()) { status("Fill both fields.", UITheme.DANGER); return; }
        int dist;
        try { dist = Integer.parseInt(ds); } catch (NumberFormatException ex) {
            status("Distance must be a number.", UITheme.DANGER); return;
        }
        ParkingSlot slot = records == null ? null : records.findSlotById(id);
        if (slot == null) {
            slot = new ParkingSlot(id, dist);
            if (records != null) {
                records.addParkingSlotRecord(slot);
            }
        } else {
            slot.setDistanceToGate(dist);
        }
        fa.cacheSlot(slot);
        log.log("CACHE  put(slot, " + id + ") — O(1)");
        status("Cached slot " + id + " — O(1).", UITheme.SUCCESS);
        tfSlotId.setText(""); tfDist.setText("");
        refreshTables();
    }

    private void removeSlot() {
        String id = tfSlotId.getText().trim().toUpperCase();
        if (id.isEmpty()) { status("Enter a Slot ID to remove.", UITheme.DANGER); return; }
        syncCachesFromRecords();
        boolean ok = fa.removeSlot(id);
        if (ok) {
            log.log("CACHE  remove(slot, " + id + ") — O(1)");
            status("Removed slot " + id + " — O(1).", UITheme.WARNING);
            if (records != null) {
                ParkingSlot existing = records.findSlotById(id);
                if (existing != null) {
                    // Detach any vehicle parked in this slot
                    Vehicle parked = existing.getParkedVehicle();
                    if (parked != null) {
                        parked.setAssignedSlotId(null);
                        existing.setParkedVehicle(null);
                    }
                    records.removeParkingSlotRecord(existing);
                }
            }
            // Free the live parking map so every panel reflects the removal
            if (parkingMap != null) parkingMap.markFree(id);
        } else {
            status("Slot " + id + " not in cache.", UITheme.DANGER);
        }
        tfSlotId.setText(""); tfDist.setText("");
        refreshTables();
    }

    private void lookupVehicle() {
        String plate = tfLookupPlate.getText().trim().toUpperCase();
        if (plate.isEmpty()) { status("Enter plate.", UITheme.DANGER); return; }
        syncCachesFromRecords();
        Vehicle v = fa.getVehicle(plate);
        if (v != null) {
            lookupResultLabel.setText("<html>Found:Vehicle: <b>" + v.getLicensePlate() + "</b>  |  " +
                v.getOwnerName() + "  |  slot: " + (v.getAssignedSlotId() != null ? v.getAssignedSlotId() : "—") + "</html>");
            lookupResultLabel.setForeground(UITheme.SUCCESS);
            log.log("CACHE  get(vehicle, " + plate + ") → HIT — O(1)");
            status("Cache HIT for " + plate + " — O(1).", UITheme.SUCCESS);
        } else {
            lookupResultLabel.setText("Cache MISS: " + plate + " not found");
            lookupResultLabel.setForeground(UITheme.DANGER);
            log.log("CACHE  get(vehicle, " + plate + ") → MISS — O(1)");
            status("Cache MISS — O(1).", UITheme.DANGER);
        }
    }

    private void lookupSlot() {
        String id = tfLookupSlot.getText().trim().toUpperCase();
        if (id.isEmpty()) { status("Enter slot ID.", UITheme.DANGER); return; }
        syncCachesFromRecords();
        ParkingSlot s = fa.getSlot(id);
        if (s != null) {
            lookupResultLabel.setText("<html>Found:Slot: <b>" + s.getSlotId() + "</b>  |  dist: " +
                s.getDistanceToGate() + "m  |  occupied: " + s.isOccupied() + "</html>");
            lookupResultLabel.setForeground(UITheme.SUCCESS);
            log.log("CACHE  get(slot, " + id + ") → HIT — O(1)");
            status("Cache HIT for slot " + id + " — O(1).", UITheme.SUCCESS);
        } else {
            lookupResultLabel.setText("Cache MISS: slot " + id + " not found");
            lookupResultLabel.setForeground(UITheme.DANGER);
            log.log("CACHE  get(slot, " + id + ") → MISS — O(1)");
            status("Cache MISS — O(1).", UITheme.DANGER);
        }
    }

    // Table refresh
    private void refreshTables() {
        syncCachesFromRecords();
        vehicleMapModel.setRowCount(0);
        List<String[]> ve = fa.getVehicleMap().getEntries();
        for (String[] e : ve) {
            Vehicle v = fa.getVehicle(e[1]);
            String val = v == null ? e[2] : v.getOwnerName() + " | " +
                new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date(v.getEntryTime())) +
                " | " + (v.getAssignedSlotId() != null ? v.getAssignedSlotId() : "—");
            vehicleMapModel.addRow(new Object[]{ e[0], e[1], val });
        }
        vehicleMapStats.setText("HashMap — " + ve.size() + " entries, capacity: " +
                                fa.getVehicleMap().getCapacity());

        slotMapModel.setRowCount(0);
        List<String[]> se = fa.getSlotMap().getEntries();
        for (String[] e : se) {
            ParkingSlot s = fa.getSlot(e[1]);
            String val = s == null ? e[2] : s.getDistanceToGate() + "m | " +
                (s.isOccupied() ? "Occupied" : "Available");
            slotMapModel.addRow(new Object[]{ e[0], e[1], val });
        }
        slotMapStats.setText("HashMap — " + se.size() + " entries, capacity: " +
                             fa.getSlotMap().getCapacity());
    }

    /** Called externally after any approval/assignment to keep cache + tables in sync. */
    public void syncCaches() { refreshTables(); }

    private void syncCachesFromRecords() {
        if (records == null) {
            return;
        }

        fa.clear();

        for (Vehicle v : records.getAllVehiclesList()) {
            fa.cacheVehicle(v);
            if (v.getAssignedSlotId() != null) {
                fa.mapVehicleToSlot(v.getLicensePlate(), v.getAssignedSlotId());
            }
        }

        for (ParkingSlot s : records.getAllParkingSlotsList()) {
            fa.cacheSlot(s);
        }
    }

    // Form helper
    @FunctionalInterface
    private interface FieldSetter { void set(JTextField[] fields); }

    private JPanel buildForm2(String[] labels, FieldSetter setter) {
        JTextField[] fields = { UITheme.makeTextField(10), UITheme.makeTextField(10) };
        setter.set(fields);
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL; gc.insets = new Insets(3,3,3,3);
        for (int i = 0; i < labels.length; i++) {
            gc.gridx = 0; gc.gridy = i; gc.weightx = 0; form.add(UITheme.makeLabel(labels[i]), gc);
            gc.gridx = 1; gc.weightx = 1;                form.add(fields[i], gc);
        }
        return form;
    }

    private void status(String msg, Color color) {
        statusLabel.setText(msg);
        statusLabel.setForeground(color);
    }
}
