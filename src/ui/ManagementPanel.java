package ui;

import management.RecordManager;
import models.Vehicle;
import models.ParkingSlot;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Module 1 — Parking & Vehicle Management
 * Data Structures: Singly Linked List (RecordLinkedList)
 * Operations: add O(1) | remove O(n) | display O(n)
 */
public class ManagementPanel extends JPanel {

    private final ActivityLog    log;
    private final DashboardPanel dashboard;
    private final RecordManager  mgr;

    // Vehicle table model
    private final DefaultTableModel vehicleModel = new DefaultTableModel(
        new String[]{"Licence Plate", "Owner Name", "Entry Time", "Assigned Slot"}, 0) {
        public boolean isCellEditable(int r, int c) { return false; }
    };

    // Slot table model
    private final DefaultTableModel slotModel = new DefaultTableModel(
        new String[]{"Slot ID", "Distance (m)", "Occupied", "Vehicle"}, 0) {
        public boolean isCellEditable(int r, int c) { return false; }
    };

    // Input fields
    private JTextField tfPlate, tfOwner, tfSlotId, tfDist;
    private JLabel statusLabel;

    public ManagementPanel(ActivityLog log, DashboardPanel dashboard, RecordManager mgr) {
        this.log       = log;
        this.dashboard = dashboard;
        this.mgr       = mgr;
        setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout(0, 16));
        setBorder(new EmptyBorder(28, 28, 28, 28));

        add(buildHeader(),  BorderLayout.NORTH);
        add(buildBody(),    BorderLayout.CENTER);
        add(buildComplexity(), BorderLayout.SOUTH);
    }

    // ── Header ────────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);

        JPanel left = new JPanel(new GridLayout(2, 1, 0, 2));
        left.setOpaque(false);
        left.add(UITheme.makeSectionTitle("Parking & Vehicle Management"));
        left.add(UITheme.makeLabel("Data Structure: Singly Linked List  ·  add O(1) · remove O(n) · display O(n)"));

        statusLabel = new JLabel(" ");
        statusLabel.setFont(UITheme.FONT_SMALL);
        statusLabel.setForeground(UITheme.SUCCESS);
        statusLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        p.add(left,        BorderLayout.WEST);
        p.add(statusLabel, BorderLayout.EAST);
        return p;
    }

    // ── Main body ─────────────────────────────────────────────────────────────
    private JPanel buildBody() {
        JPanel p = new JPanel(new GridLayout(1, 2, 14, 0));
        p.setOpaque(false);
        p.add(buildVehicleSection());
        p.add(buildSlotSection());
        return p;
    }

    // ── Vehicle section ───────────────────────────────────────────────────────
    private JPanel buildVehicleSection() {
        JPanel card = UITheme.makeCard(new BorderLayout(0, 10));

        // Title
        JPanel header = UITheme.makeSectionHeader("Vehicle Records", "Linked List", UITheme.ACCENT);
        card.add(header, BorderLayout.NORTH);

        // Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(0, 0, 8, 0));
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

        JButton addV = UITheme.makePrimaryButton("Add Vehicle");
        addV.addActionListener(e -> addVehicle());
        JButton remV = UITheme.makeDangerButton("Remove Selected");
        JTable vehicleTable = buildTable(vehicleModel);
        remV.addActionListener(e -> removeVehicle(vehicleTable));

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        btns.setOpaque(false);
        btns.add(addV);
        btns.add(remV);

        gc.gridx = 0; gc.gridy = 2; gc.gridwidth = 2;
        form.add(btns, gc);

        card.add(form, BorderLayout.NORTH);
        card.add(UITheme.wrapScroll(vehicleTable), BorderLayout.CENTER);
        return card;
    }

    // ── Slot section ──────────────────────────────────────────────────────────
    private JPanel buildSlotSection() {
        JPanel card = UITheme.makeCard(new BorderLayout(0, 10));

        JPanel header = UITheme.makeSectionHeader("Parking Slot Records", "Linked List", UITheme.SUCCESS);
        card.add(header, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(0, 0, 8, 0));
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

        JButton addS = UITheme.makePrimaryButton("Add Slot");
        addS.addActionListener(e -> addSlot());
        JTable slotTable = buildTable(slotModel);
        JButton remS = UITheme.makeDangerButton("Remove Selected");
        remS.addActionListener(e -> removeSlot(slotTable));

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        btns.setOpaque(false);
        btns.add(addS);
        btns.add(remS);

        gc.gridx = 0; gc.gridy = 2; gc.gridwidth = 2;
        form.add(btns, gc);

        card.add(form, BorderLayout.NORTH);
        card.add(UITheme.wrapScroll(slotTable), BorderLayout.CENTER);
        return card;
    }

    // ── Table builder ─────────────────────────────────────────────────────────
    private JTable buildTable(DefaultTableModel model) {
        JTable t = new JTable(model);
        UITheme.styleTable(t);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        return t;
    }

    // ── Complexity banner ─────────────────────────────────────────────────────
    private JPanel buildComplexity() {
        return UITheme.makeComplexityBanner(
            "<b>Linked List Complexity:</b> &nbsp;" +
            "add() → <b>O(1)</b> (tail pointer maintained) &nbsp;|&nbsp; " +
            "remove() → <b>O(n)</b> (linear search to find node) &nbsp;|&nbsp; " +
            "display() → <b>O(n)</b> (traverse all nodes) &nbsp;|&nbsp; " +
            "Space → <b>O(n)</b> &nbsp;·&nbsp; " +
            "Advantage over arrays: <b>dynamic size</b>, no pre-allocation needed."
        );
    }

    // ── Actions ───────────────────────────────────────────────────────────────
    private void addVehicle() {
        String plate = tfPlate.getText().trim().toUpperCase();
        String owner = tfOwner.getText().trim();
        if (plate.isEmpty() || owner.isEmpty()) { status("Fill both fields.", UITheme.DANGER); return; }
        if (mgr.findVehicleByPlate(plate) != null) { status("Plate already exists.", UITheme.WARNING); return; }

        Vehicle v = new Vehicle(plate, owner, System.currentTimeMillis());
        mgr.addVehicleRecord(v);
        vehicleModel.addRow(new Object[]{ v.getLicensePlate(), v.getOwnerName(),
            new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date(v.getEntryTime())),
            v.getAssignedSlotId() != null ? v.getAssignedSlotId() : "—" });

        log.log("MANAGEMENT  Added vehicle: " + plate + " / " + owner);
        status("Vehicle added — O(1) insertion.", UITheme.SUCCESS);
        tfPlate.setText(""); tfOwner.setText("");
        refreshDashboard();
    }

    private void removeVehicle(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) { status("Select a vehicle to remove.", UITheme.WARNING); return; }
        String plate = (String) vehicleModel.getValueAt(row, 0);
        Vehicle v = mgr.findVehicleByPlate(plate);
        if (v != null) mgr.removeVehicleRecord(v);
        vehicleModel.removeRow(row);
        log.log("MANAGEMENT  Removed vehicle: " + plate);
        status("Vehicle removed — O(n) traversal.", UITheme.SUCCESS);
        refreshDashboard();
    }

    private void addSlot() {
        String id = tfSlotId.getText().trim().toUpperCase();
        String ds = tfDist.getText().trim();
        if (id.isEmpty() || ds.isEmpty()) { status("Fill both fields.", UITheme.DANGER); return; }
        int dist;
        try { dist = Integer.parseInt(ds); } catch (NumberFormatException ex) {
            status("Distance must be a number.", UITheme.DANGER); return;
        }
        if (mgr.findSlotById(id) != null) { status("Slot ID already exists.", UITheme.WARNING); return; }

        ParkingSlot slot = new ParkingSlot(id, dist);
        mgr.addParkingSlotRecord(slot);
        slotModel.addRow(new Object[]{ slot.getSlotId(), slot.getDistanceToGate(),
            slot.isOccupied() ? "Yes" : "No", "—" });

        log.log("MANAGEMENT  Added slot: " + id + " dist=" + dist + "m");
        status("Slot added — O(1) insertion.", UITheme.SUCCESS);
        tfSlotId.setText(""); tfDist.setText("");
        refreshDashboard();
    }

    private void removeSlot(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) { status("Select a slot to remove.", UITheme.WARNING); return; }
        String id = (String) slotModel.getValueAt(row, 0);
        ParkingSlot s = mgr.findSlotById(id);
        if (s != null) mgr.removeParkingSlotRecord(s);
        slotModel.removeRow(row);
        log.log("MANAGEMENT  Removed slot: " + id);
        status("Slot removed — O(n) traversal.", UITheme.SUCCESS);
        refreshDashboard();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private void status(String msg, Color color) {
        statusLabel.setText(msg);
        statusLabel.setForeground(color);
    }

    private void refreshDashboard() {
        int vehicles = mgr.getVehicleCount();
        int slots    = mgr.getParkingSlotCount();
        dashboard.refresh(vehicles, Math.max(slots - vehicles, 0), slots, vehicles + slots);
    }

    public RecordManager getRecordManager() { return mgr; }
}
