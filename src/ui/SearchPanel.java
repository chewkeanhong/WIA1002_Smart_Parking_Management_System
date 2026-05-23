package ui;

import management.RecordManager;
import models.Vehicle;
import search.SearchEngine;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Module 5 — Search System
 * Data Structure: AVL Tree (self-balancing BST). Search is O(log n).
 *
 * Lists every currently-parked vehicle and offers an AVL-backed lookup by plate.
 * The tree is rebuilt automatically whenever the shared {@code RecordManager} changes.
 */
public class SearchPanel extends JPanel {

    private final ActivityLog   log;
    private final RecordManager records;
    private SearchEngine        engine = new SearchEngine();

    private JTextField tfSearch;
    private JLabel     statusLabel;
    private JLabel     resultLabel;
    private JTable     vehicleTable;

    private final DefaultTableModel tableModel = new DefaultTableModel(
        new String[]{"#", "Licence Plate", "Owner", "Entry Time", "Slot"}, 0) {
        public boolean isCellEditable(int r, int c) { return false; }
    };

    private String lastVehicleSignature = "";

    public SearchPanel(ActivityLog log, RecordManager records) {
        this.log     = log;
        this.records = records;
        setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout(0, 14));
        setBorder(new EmptyBorder(28, 28, 28, 28));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(),   BorderLayout.CENTER);

        rebuildTree();
        new Timer(1000, e -> syncFromRecords()).start();
    }

    // ── Header ────────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);

        JPanel left = new JPanel(new GridLayout(2, 1, 0, 2));
        left.setOpaque(false);
        left.add(UITheme.makeSectionTitle("Vehicle Search"));
        left.add(UITheme.makeLabel("AVL Tree (self-balancing BST) · search O(log n)"));

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
        JPanel body = new JPanel(new BorderLayout(0, 14));
        body.setOpaque(false);
        body.add(buildSearchCard(), BorderLayout.NORTH);
        body.add(buildListCard(),   BorderLayout.CENTER);
        return body;
    }

    private JPanel buildSearchCard() {
        JPanel card = UITheme.makeCard(new BorderLayout(0, 8));
        card.add(UITheme.makeSectionHeader("Search by Licence Plate", "O(log n)", UITheme.WARNING),
                 BorderLayout.NORTH);

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        row.setOpaque(false);

        tfSearch = UITheme.makeTextField(16);
        JButton searchBtn = UITheme.makePrimaryButton("Search");
        searchBtn.addActionListener(e -> searchVehicle());
        tfSearch.addActionListener(e -> searchVehicle());

        resultLabel = new JLabel(" ");
        resultLabel.setFont(UITheme.FONT_BODY);
        resultLabel.setForeground(UITheme.TEXT_SECONDARY);

        row.add(UITheme.makeLabel("Plate:"));
        row.add(tfSearch);
        row.add(searchBtn);
        row.add(resultLabel);

        card.add(row, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildListCard() {
        JPanel card = UITheme.makeCard(new BorderLayout(0, 8));
        card.add(UITheme.makeSectionHeader("Parked Vehicles", "sorted A→Z (in-order)", UITheme.INFO),
                 BorderLayout.NORTH);

        vehicleTable = new JTable(tableModel);
        UITheme.styleTable(vehicleTable);
        vehicleTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        card.add(UITheme.wrapScroll(vehicleTable), BorderLayout.CENTER);
        return card;
    }

    // ── Sync with shared RecordManager ───────────────────────────────────────
    private void syncFromRecords() {
        String sig = signatureOf(records.getAllVehiclesList());
        if (!sig.equals(lastVehicleSignature)) {
            rebuildTree();
        }
    }

    private void rebuildTree() {
        engine = new SearchEngine();
        List<Vehicle> vs = records.getAllVehiclesList();
        for (Vehicle v : vs) engine.addVehicle(v);
        lastVehicleSignature = signatureOf(vs);
        refreshTable();
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        List<Vehicle> sorted = engine.getSortedVehicles();
        for (int i = 0; i < sorted.size(); i++) {
            Vehicle v = sorted.get(i);
            tableModel.addRow(new Object[]{
                i + 1,
                v.getLicensePlate(),
                v.getOwnerName(),
                new java.text.SimpleDateFormat("HH:mm:ss")
                    .format(new java.util.Date(v.getEntryTime())),
                v.getAssignedSlotId() != null ? v.getAssignedSlotId() : "—"
            });
        }
    }

    private String signatureOf(List<Vehicle> vs) {
        StringBuilder sb = new StringBuilder();
        for (Vehicle v : vs) {
            sb.append(v.getLicensePlate()).append('|')
              .append(v.getAssignedSlotId() == null ? "" : v.getAssignedSlotId())
              .append(';');
        }
        return sb.toString();
    }

    // ── Search action ────────────────────────────────────────────────────────
    private void searchVehicle() {
        String plate = tfSearch.getText().trim().toUpperCase();
        if (plate.isEmpty()) {
            resultLabel.setText("Enter a plate.");
            resultLabel.setForeground(UITheme.DANGER);
            status("Enter a plate to search.", UITheme.DANGER);
            return;
        }

        long t0 = System.nanoTime();
        Vehicle v = engine.findVehicle(plate);
        long us = (System.nanoTime() - t0) / 1000;

        if (v == null) {
            resultLabel.setText("✗  Not found: " + plate);
            resultLabel.setForeground(UITheme.DANGER);
            status("Not found — O(log n) traversal · " + us + " µs", UITheme.DANGER);
            log.log("SEARCH  Not found: " + plate + " (" + us + " µs)");
            vehicleTable.clearSelection();
            return;
        }

        resultLabel.setText("✓  " + v.getLicensePlate()
            + "  —  " + v.getOwnerName()
            + "  —  slot " + (v.getAssignedSlotId() != null ? v.getAssignedSlotId() : "—"));
        resultLabel.setForeground(UITheme.SUCCESS);
        status("Found " + plate + " — O(log n) · " + us + " µs", UITheme.SUCCESS);
        log.log("SEARCH  Found: " + plate + " (" + us + " µs)");

        int row = engine.getSortedVehicles().indexOf(v);
        if (row >= 0 && row < vehicleTable.getRowCount()) {
            vehicleTable.setRowSelectionInterval(row, row);
            vehicleTable.scrollRectToVisible(vehicleTable.getCellRect(row, 0, true));
        }
    }

    private void status(String msg, Color color) {
        statusLabel.setText(msg);
        statusLabel.setForeground(color);
    }
}
