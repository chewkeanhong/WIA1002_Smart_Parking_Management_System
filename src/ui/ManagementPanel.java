package ui;

import gate_control.GateProcessor;
import management.RecordManager;
import models.Vehicle;
import models.ParkingSlot;
import models.ParkingMap;
import navigation.DijkstraPathfinder;
import navigation.RouteGraph;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

// Management screen for vehicles and slots. Everything lives in the
// RecordLinkedList (our own linked list) behind RecordManager.
public class ManagementPanel extends JPanel {

    private final ActivityLog    log;
    private final DashboardPanel dashboard;
    private final RecordManager  mgr;
    private final GateProcessor  gate;
    private final UserPanel      userPanel;
    private final ParkingMap     parkingMap;
    private RetrievalPanel       retrievalPanel;

    public void setRetrievalPanel(RetrievalPanel rp) { this.retrievalPanel = rp; }
    private final RouteGraph     routeGraph = new RouteGraph();

    // Vehicle table model
    private final DefaultTableModel vehicleModel = new DefaultTableModel(
        new String[]{"Licence Plate", "Owner Name", "Entry Gate", "Entry Time", "Assigned Slot"}, 0) {
        public boolean isCellEditable(int r, int c) { return false; }
    };

    // Slot table model
    private final DefaultTableModel slotModel = new DefaultTableModel(
        new String[]{"Slot ID", "Distance (m)", "Occupied", "Vehicle"}, 0) {
        public boolean isCellEditable(int r, int c) { return false; }
    };

    // Input fields
    private JTextField tfPlate, tfOwner, tfSlotId, tfSlotPlate;
    private JComboBox<String> gateChoice;
    private JLabel statusLabel;
    private JTable vehicleTable;
    private JTable slotTable;

    public ManagementPanel(ActivityLog log, DashboardPanel dashboard, RecordManager mgr, GateProcessor gate, UserPanel userPanel, ParkingMap parkingMap) {
        this.log       = log;
        this.dashboard = dashboard;
        this.mgr       = mgr;
        this.gate      = gate;
        this.userPanel = userPanel;
        this.parkingMap = parkingMap;
        routeGraph.initializeDashboardLayout();
        setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout(0, 16));
        setBorder(new EmptyBorder(28, 28, 28, 28));

        add(buildHeader(),  BorderLayout.NORTH);
        add(buildBody(),    BorderLayout.CENTER);
        add(buildComplexity(), BorderLayout.SOUTH);

        new Timer(1000, e -> syncFromRecords()).start();
        syncFromRecords();
    }

    public ManagementPanel(ActivityLog log, DashboardPanel dashboard, RecordManager mgr) {
        this(log, dashboard, mgr, null, null, null);
    }

    public ManagementPanel(ActivityLog log, DashboardPanel dashboard, RecordManager mgr, ParkingMap parkingMap) {
        this(log, dashboard, mgr, null, null, parkingMap);
    }

    // Header
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

    // Main body
    private JPanel buildBody() {
        JPanel p = new JPanel(new GridLayout(1, 2, 14, 0));
        p.setOpaque(false);
        p.add(buildVehicleSection());
        p.add(buildSlotSection());
        return p;
    }

    // Vehicle section
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
        gateChoice = new JComboBox<>(new String[]{
            "Nearest Entrance", "Gate A", "Gate B", "Gate C"
        });
        gateChoice.setBackground(UITheme.BG_INPUT);
        gateChoice.setForeground(UITheme.TEXT_PRIMARY);

        gc.gridx = 0; gc.gridy = 0; gc.weightx = 0;
        form.add(UITheme.makeLabel("Licence Plate"), gc);
        gc.gridx = 1; gc.weightx = 1;
        form.add(tfPlate, gc);

        gc.gridx = 0; gc.gridy = 1; gc.weightx = 0;
        form.add(UITheme.makeLabel("Owner Name"), gc);
        gc.gridx = 1; gc.weightx = 1;
        form.add(tfOwner, gc);

        gc.gridx = 0; gc.gridy = 2; gc.weightx = 0;
        form.add(UITheme.makeLabel("Entry Gate"), gc);
        gc.gridx = 1; gc.weightx = 1;
        form.add(gateChoice, gc);

        JButton addV = UITheme.makePrimaryButton("Add Vehicle");
        addV.addActionListener(e -> addVehicle());
        JButton remV = UITheme.makeDangerButton("Remove Selected");
        vehicleTable = buildTable(vehicleModel);
        remV.addActionListener(e -> removeVehicle(vehicleTable));

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        btns.setOpaque(false);
        btns.add(addV);
        btns.add(remV);

        gc.gridx = 0; gc.gridy = 3; gc.gridwidth = 2;
        form.add(btns, gc);

        card.add(form, BorderLayout.NORTH);
        card.add(UITheme.wrapScroll(vehicleTable), BorderLayout.CENTER);
        return card;
    }

    // Slot section
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
        tfSlotPlate = UITheme.makeTextField(10);

        gc.gridx = 0; gc.gridy = 0; gc.weightx = 0;
        form.add(UITheme.makeLabel("Slot ID"), gc);
        gc.gridx = 1; gc.weightx = 1;
        form.add(tfSlotId, gc);

        gc.gridx = 0; gc.gridy = 1; gc.weightx = 0;
        form.add(UITheme.makeLabel("Vehicle Plate"), gc);
        gc.gridx = 1; gc.weightx = 1;
        form.add(tfSlotPlate, gc);

        JButton addS = UITheme.makePrimaryButton("Assign Slot");
        addS.addActionListener(e -> assignSlot());
        slotTable = buildTable(slotModel);
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

    // Table builder
    private JTable buildTable(DefaultTableModel model) {
        JTable t = new JTable(model);
        UITheme.styleTable(t);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        return t;
    }

    // Complexity banner
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

    // Actions
    private void addVehicle() {
        String plate = tfPlate.getText().trim().toUpperCase();
        String owner = tfOwner.getText().trim();
        if (plate.isEmpty() || owner.isEmpty()) { status("Fill both fields.", UITheme.DANGER); return; }
        if (mgr.findVehicleByPlate(plate) != null || queueContainsPlate(plate)) {
            status("Plate already exists or is already queued.", UITheme.WARNING);
            return;
        }

        String preferredGateId = normalizeGateSelection((String) gateChoice.getSelectedItem());
        Vehicle v = new Vehicle(plate, owner, System.currentTimeMillis(), preferredGateId);
        mgr.addVehicleRecord(v);
        if (gate != null) {
            gate.vehicleArrives(v);
        }
        if (userPanel != null) {
            userPanel.addManagedVehicle(v);
        }
        vehicleModel.addRow(new Object[]{ v.getLicensePlate(), v.getOwnerName(),
            prettyGateLabel(v.getPreferredGateId()),
            new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date(v.getEntryTime())),
            v.getAssignedSlotId() != null ? v.getAssignedSlotId() : "—" });

        log.log("MANAGEMENT  Added vehicle: " + plate + " / " + owner);
        status("Vehicle added via " + prettyGateLabel(preferredGateId) + " — O(1) insertion.", UITheme.SUCCESS);
        tfPlate.setText(""); tfOwner.setText("");
        syncFromRecords();
        refreshDashboard();
    }

    private void removeVehicle(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) { status("Select a vehicle to remove.", UITheme.WARNING); return; }
        String plate = (String) vehicleModel.getValueAt(row, 0);
        Vehicle v = mgr.findVehicleByPlate(plate);
        boolean removed = false;
        if (userPanel != null) {
            removed = userPanel.removeVehicleByPlate(plate);
        }
        if (!removed && v != null) {
            String slotId = v.getAssignedSlotId();
            if (slotId != null) {
                ParkingSlot slot = mgr.findSlotById(slotId);
                if (slot != null) {
                    slot.setParkedVehicle(null);
                }
                if (parkingMap != null) {
                    parkingMap.markFree(slotId);
                }
                routeGraph.setOccupancy(slotId, false);
                v.setAssignedSlotId(null);
            }
            mgr.removeVehicleRecord(v);
        }
        vehicleModel.removeRow(row);
        log.log("MANAGEMENT  Removed vehicle: " + plate);
        status("Vehicle removed — O(n) traversal.", UITheme.SUCCESS);
        syncFromRecords();
        refreshDashboard();
    }

    private void assignSlot() {
        String id = tfSlotId.getText().trim().toUpperCase();
        String plate = tfSlotPlate.getText().trim().toUpperCase();
        if (id.isEmpty() || plate.isEmpty()) { status("Fill both fields.", UITheme.DANGER); return; }

        if (routeGraph.getNode(id) == null) {
            status("Slot ID must exist on the parking map.", UITheme.DANGER);
            return;
        }

        Vehicle v = mgr.findVehicleByPlate(plate);
        if (v == null) { status("Vehicle plate not found. Add the vehicle first.", UITheme.WARNING); return; }

        ParkingSlot existingSlot = mgr.findSlotById(id);
        if (existingSlot != null && existingSlot.isOccupied()) {
            status("That slot is currently occupied and cannot be assigned.", UITheme.DANGER);
            return;
        }

        String startNode = resolveStartNode(v.getPreferredGateId());
        int dist = DijkstraPathfinder.calculateShortestPathCost(routeGraph, startNode, id);
        if (dist == Integer.MAX_VALUE) { status("Slot is not reachable from the selected route.", UITheme.DANGER); return; }

        String oldSlotId = v.getAssignedSlotId();
        if (oldSlotId != null && !oldSlotId.equalsIgnoreCase(id)) {
            ParkingSlot oldSlot = mgr.findSlotById(oldSlotId);
            if (oldSlot != null) {
                oldSlot.setParkedVehicle(null);
            }
            if (parkingMap != null) {
                parkingMap.markFree(oldSlotId);
            }
            routeGraph.setOccupancy(oldSlotId, false);
        }

        ParkingSlot slot = existingSlot;
        if (slot == null) {
            slot = new ParkingSlot(id, dist);
            mgr.addParkingSlotRecord(slot);
        } else {
            slot.setDistanceToGate(dist);
        }
        slot.setParkedVehicle(v);
        v.setAssignedSlotId(id);

        // Remove the vehicle from the gate queue so the User panel auto-approves
        if (gate != null) gate.purgeVehicle(v);

        if (parkingMap != null) {
            parkingMap.markOccupied(id);
        }
        routeGraph.setOccupancy(id, true);

        if (retrievalPanel != null) retrievalPanel.syncCaches();
        log.log("MANAGEMENT  Assigned slot " + id + " to vehicle " + plate + " (" + dist + "m)");
        status("Assigned " + id + " to " + plate + " — distance calculated by system.", UITheme.SUCCESS);
        tfSlotId.setText(""); tfSlotPlate.setText("");
        syncFromRecords();
        refreshDashboard();
    }

    private void removeSlot(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) { status("Select a slot to remove.", UITheme.WARNING); return; }
        String id = (String) slotModel.getValueAt(row, 0);
        ParkingSlot s = mgr.findSlotById(id);
        if (s != null) {
            if (userPanel != null) {
                userPanel.removeVehicleBySlotId(id);
            }
            Vehicle parked = s.getParkedVehicle();
            if (parked != null) {
                parked.setAssignedSlotId(null);
            }
            s.setParkedVehicle(null);
            if (parkingMap != null) {
                parkingMap.markFree(id);
            }
            routeGraph.setOccupancy(id, false);
            mgr.removeParkingSlotRecord(s);
        }
        slotModel.removeRow(row);
        log.log("MANAGEMENT  Removed slot: " + id);
        status("Slot removed — O(n) traversal.", UITheme.SUCCESS);
        syncFromRecords();
        refreshDashboard();
    }

    // Helpers
    private void status(String msg, Color color) {
        statusLabel.setText(msg);
        statusLabel.setForeground(color);
    }

    private void refreshDashboard() {
        int vehicles = mgr.getVehicleCount();
        int occupiedSlots = parkingMap == null ? 0 : parkingMap.getOccupiedCount();
        int totalSlots = parkingMap == null ? mgr.getParkingSlotCount() : ParkingMap.total();
        int availableSlots = Math.max(totalSlots - occupiedSlots, 0);
        dashboard.refresh(vehicles, availableSlots, totalSlots, vehicles + mgr.getParkingSlotCount());
    }

    private void syncFromRecords() {
        String selectedVehicleKey = selectedVehicleKey();
        String selectedSlotKey = selectedSlotKey();

        vehicleModel.setRowCount(0);
        for (Vehicle v : mgr.getAllVehiclesList()) {
            vehicleModel.addRow(new Object[]{
                v.getLicensePlate(),
                v.getOwnerName(),
                prettyGateLabel(v.getPreferredGateId()),
                new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date(v.getEntryTime())),
                v.getAssignedSlotId() != null ? v.getAssignedSlotId() : "—"
            });
        }

        slotModel.setRowCount(0);
        for (ParkingSlot s : mgr.getAllParkingSlotsList()) {
            slotModel.addRow(new Object[]{
                s.getSlotId(),
                s.getDistanceToGate(),
                s.isOccupied() ? "Yes" : "No",
                s.getParkedVehicle() != null ? s.getParkedVehicle().getLicensePlate() : "—"
            });
        }

        restoreVehicleSelection(selectedVehicleKey);
        restoreSlotSelection(selectedSlotKey);
    }

    private String selectedVehicleKey() {
        if (vehicleTable == null || vehicleTable.getSelectedRow() < 0) {
            return null;
        }
        int row = vehicleTable.getSelectedRow();
        return normalizeKey(valueAt(vehicleTable, row, 0)) + "|" + normalizeKey(valueAt(vehicleTable, row, 1));
    }

    private String selectedSlotKey() {
        if (slotTable == null || slotTable.getSelectedRow() < 0) {
            return null;
        }
        int row = slotTable.getSelectedRow();
        return normalizeKey(valueAt(slotTable, row, 0)) + "|" + normalizeKey(valueAt(slotTable, row, 3));
    }

    private void restoreVehicleSelection(String key) {
        if (key == null || vehicleTable == null) {
            return;
        }

        for (int i = 0; i < vehicleModel.getRowCount(); i++) {
            String candidate = normalizeKey(valueAt(vehicleTable, i, 0)) + "|" + normalizeKey(valueAt(vehicleTable, i, 1));
            if (candidate.equals(key)) {
                vehicleTable.getSelectionModel().setSelectionInterval(i, i);
                vehicleTable.scrollRectToVisible(vehicleTable.getCellRect(i, 0, true));
                return;
            }
        }
    }

    private void restoreSlotSelection(String key) {
        if (key == null || slotTable == null) {
            return;
        }

        for (int i = 0; i < slotModel.getRowCount(); i++) {
            String candidate = normalizeKey(valueAt(slotTable, i, 0)) + "|" + normalizeKey(valueAt(slotTable, i, 3));
            if (candidate.equals(key)) {
                slotTable.getSelectionModel().setSelectionInterval(i, i);
                slotTable.scrollRectToVisible(slotTable.getCellRect(i, 0, true));
                return;
            }
        }
    }

    private String normalizeKey(String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("\\s+", "").toUpperCase();
    }

    private String valueAt(JTable table, int row, int col) {
        Object value = table.getValueAt(row, col);
        return value == null ? "" : value.toString().trim();
    }

    private String resolveStartNode(String preferredGateId) {
        if (preferredGateId == null || preferredGateId.trim().isEmpty()) {
            return "ENTRANCE";
        }

        String normalized = preferredGateId.trim().toUpperCase();
        switch (normalized) {
            case "ENTRANCE":
            case "GATE_A":
            case "GATE_B":
            case "GATE_C":
                return normalized;
            default:
                return "ENTRANCE";
        }
    }

    private String normalizeGateSelection(String selection) {
        if (selection == null) {
            return "ENTRANCE";
        }

        switch (selection.trim()) {
            case "Gate A": return "GATE_A";
            case "Gate B": return "GATE_B";
            case "Gate C": return "GATE_C";
            default: return "ENTRANCE";
        }
    }

    private String prettyGateLabel(String gateId) {
        if (gateId == null) {
            return "Nearest Entrance";
        }

        switch (gateId) {
            case "GATE_A": return "Gate A";
            case "GATE_B": return "Gate B";
            case "GATE_C": return "Gate C";
            default: return "Nearest Entrance";
        }
    }

    private boolean queueContainsPlate(String plate) {
        if (gate == null) {
            return false;
        }

        String normalized = Vehicle.normalizePlate(plate);
        for (Vehicle queued : gate.getEntryQueue().toArray()) {
            if (Vehicle.normalizePlate(queued.getLicensePlate()).equals(normalized)) {
                return true;
            }
        }
        return false;
    }

    public RecordManager getRecordManager() { return mgr; }
}
