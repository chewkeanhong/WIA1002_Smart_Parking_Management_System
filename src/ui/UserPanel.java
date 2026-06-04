package ui;

import gate_control.GateProcessor;
import management.RecordManager;
import models.ParkingMap;
import models.ParkingSlot;
import models.Vehicle;
import navigation.DijkstraPathfinder;
import navigation.RouteGraph;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class UserPanel extends JPanel {

    private static final String DEFAULT_ACCESS_NODE = "ENTRANCE";

    private final GateProcessor      gate;
    private final ActivityLog        log;
    private final ParkingMap         parkingMap;
    private final RecordManager      records;
    private final RouteGraph         graph = new RouteGraph();
    private final JPanel             bubbleContainer;
    private final JLabel             statusLabel;
    private final Map<JPanel, Timer>   bubbleTimers   = new HashMap<>();
    private final Map<JPanel, String>  bubbleSlots    = new HashMap<>();
    private final Map<JPanel, Vehicle> bubbleVehicles = new HashMap<>();

    private int bubbleCounter = 0;

    public UserPanel(ActivityLog log, GateProcessor gate, ParkingMap parkingMap, RecordManager records) {
        this.gate       = gate;
        this.log        = log;
        this.parkingMap = parkingMap;
        this.records    = records;
        graph.initializeDashboardLayout();
        for (Vehicle v : records.getAllVehiclesList()) {
            if (v.getAssignedSlotId() != null) {
                parkingMap.markOccupied(v.getAssignedSlotId());
                graph.setOccupancy(v.getAssignedSlotId(), true);
            }
        }

        setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout(0, 0));
        setBorder(new EmptyBorder(28, 28, 28, 28));

        bubbleContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 16)) {
            @Override
            public Dimension getPreferredSize() {
                int w = (getParent() instanceof JViewport)
                        ? getParent().getWidth() : super.getPreferredSize().width;
                FlowLayout fl = (FlowLayout) getLayout();
                int hgap = fl.getHgap(), vgap = fl.getVgap();
                int x = hgap, rowH = 0, totalH = vgap;
                for (Component c : getComponents()) {
                    if (!c.isVisible()) continue;
                    Dimension d = c.getPreferredSize();
                    if (x + d.width + hgap > w && x > hgap) {
                        totalH += rowH + vgap; x = hgap; rowH = 0;
                    }
                    x += d.width + hgap;
                    rowH = Math.max(rowH, d.height);
                }
                totalH += rowH + vgap;
                return new Dimension(w, totalH);
            }
        };
        bubbleContainer.setBackground(UITheme.BG_DARK);

        JScrollPane scroll = new JScrollPane(bubbleContainer,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(UITheme.BG_DARK);

        statusLabel = UITheme.makeLabel(" ");
        statusLabel.setFont(UITheme.FONT_SMALL);
        statusLabel.setBorder(new EmptyBorder(8, 0, 0, 0));

        add(buildHeader(), BorderLayout.NORTH);
        add(scroll,        BorderLayout.CENTER);
        add(statusLabel,   BorderLayout.SOUTH);
    }

    // ── Header ────────────────────────────────────────────────────────────────

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(0, 0, 20, 0));

        JPanel left = new JPanel(new GridLayout(2, 1, 0, 2));
        left.setOpaque(false);
        JLabel title = UITheme.makeSectionTitle("User Entry");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        JLabel sub = UITheme.makeLabel("Submit a vehicle — wait for admin approval — receive your slot.");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        left.add(title);
        left.add(sub);

        JButton addBtn = UITheme.makeButton("+ Add Vehicle", UITheme.ACCENT);
        addBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        addBtn.addActionListener(e -> addBubble());

        p.add(left,   BorderLayout.WEST);
        p.add(addBtn, BorderLayout.EAST);
        return p;
    }

    // ── Bubble shell ──────────────────────────────────────────────────────────

    private void addBubble() {
        bubbleCounter++;
        final int number = bubbleCounter;

        CardLayout cl = new CardLayout();
        JPanel bubble = new JPanel(cl);
        bubble.setPreferredSize(new Dimension(270, 320));
        bubble.setBackground(UITheme.BG_CARD);
        bubble.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1));

        bubble.add(buildInputCard(bubble, cl, number), "INPUT");
        cl.show(bubble, "INPUT");

        bubbleContainer.add(bubble);
        bubbleContainer.revalidate();
        bubbleContainer.repaint();
    }

    public void addManagedVehicle(Vehicle vehicle) {
        if (vehicle == null || findBubbleByPlate(vehicle.getLicensePlate()) != null) {
            return;
        }

        addTrackedWaitingBubble(vehicle, true);
    }

    // ── State 1: Input form ───────────────────────────────────────────────────

    private JPanel buildInputCard(JPanel bubble, CardLayout cl, int number) {
        JPanel p = new JPanel(new BorderLayout(0, 0));
        p.setBackground(UITheme.BG_CARD);
        p.setBorder(new EmptyBorder(16, 18, 18, 18));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 14, 0));
        JLabel title = new JLabel("Vehicle Entry");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(UITheme.TEXT_PRIMARY);
        JButton closeBtn = makeXBtn(UITheme.BG_CARD);
        closeBtn.addActionListener(e -> removeBubble(bubble));
        header.add(title,    BorderLayout.WEST);
        header.add(closeBtn, BorderLayout.EAST);

        // Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1.0;
        gc.gridx = 0;
        gc.insets = new Insets(0, 0, 4, 0);

        JTextField plateField = makeField();
        plateField.setFont(UITheme.FONT_BODY);
        JTextField nameField = makeField();
        nameField.setFont(UITheme.FONT_BODY);
        JComboBox<String> gateChoice = new JComboBox<>(new String[]{
            "Nearest Entrance", "Gate A", "Gate B", "Gate C"
        });
        gateChoice.setBackground(UITheme.BG_INPUT);
        gateChoice.setForeground(UITheme.TEXT_PRIMARY);
        gateChoice.setFont(UITheme.FONT_BODY);

        gc.gridy = 0; form.add(makeFormLabel("Vehicle Plate"), gc);
        gc.gridy = 1; gc.insets = new Insets(0, 0, 12, 0); form.add(plateField, gc);
        gc.gridy = 2; gc.insets = new Insets(0, 0, 4, 0);  form.add(makeFormLabel("Name"), gc);
        gc.gridy = 3; gc.insets = new Insets(0, 0, 12, 0); form.add(nameField, gc);
        gc.gridy = 4; gc.insets = new Insets(0, 0, 4, 0);  form.add(makeFormLabel("Entry Gate"), gc);
        gc.gridy = 5; gc.insets = new Insets(0, 0, 18, 0); form.add(gateChoice, gc);

        JButton submitBtn = UITheme.makeButton("Submit  →", new Color(22, 100, 50));
        submitBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        gc.gridy = 6; gc.insets = new Insets(0, 0, 0, 0); form.add(submitBtn, gc);

        submitBtn.addActionListener(e -> submitToQueue(bubble, cl, plateField, nameField, gateChoice, number));
        plateField.addActionListener(e -> submitToQueue(bubble, cl, plateField, nameField, gateChoice, number));
        nameField.addActionListener(e  -> submitToQueue(bubble, cl, plateField, nameField, gateChoice, number));

        p.add(header, BorderLayout.NORTH);
        p.add(form,   BorderLayout.CENTER);

        SwingUtilities.invokeLater(plateField::requestFocusInWindow);
        return p;
    }

    private JLabel makeFormLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        l.setForeground(UITheme.TEXT_SECONDARY);
        return l;
    }

    // ── State 2: Waiting card ─────────────────────────────────────────────────

    private JPanel buildWaitingCard(JPanel bubble, int number, String plate, String name,
                                    String gateLabelText, JLabel[] posLabelRef, JLabel[] totalLabelRef) {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setBackground(UITheme.BG_CARD);
        p.setBorder(new EmptyBorder(16, 18, 18, 18));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 4, 0));
        JLabel title = new JLabel("Vehicle Entry");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(UITheme.TEXT_PRIMARY);
        JButton closeBtn = makeXBtn(UITheme.BG_CARD);
        closeBtn.addActionListener(e -> removeBubble(bubble));
        header.add(title,    BorderLayout.WEST);
        header.add(closeBtn, BorderLayout.EAST);

        // Body
        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1.0; gc.gridx = 0;

        JLabel badge = UITheme.makeBadge("PENDING", UITheme.WARNING);
        badge.setFont(new Font("Segoe UI", Font.BOLD, 14));
        badge.setForeground(UITheme.BG_DARK);
        badge.setBorder(new EmptyBorder(6, 16, 6, 16));

        JLabel lblPlate = new JLabel(plate);
        lblPlate.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblPlate.setForeground(UITheme.TEXT_PRIMARY);

        JLabel lblName = new JLabel(name);
        lblName.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblName.setForeground(UITheme.TEXT_SECONDARY);

        JLabel lblGate = new JLabel(gateLabelText);
        lblGate.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblGate.setForeground(UITheme.TEXT_SECONDARY);

        JSeparator sep = new JSeparator();
        sep.setForeground(UITheme.BORDER);

        JLabel posLabel = new JLabel("Checking queue...");
        posLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        posLabel.setForeground(UITheme.ACCENT);

        JLabel totalLabel = new JLabel("");
        totalLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        totalLabel.setForeground(UITheme.SUCCESS);

        JLabel waitMsg = new JLabel("Waiting for admin to approve...");
        waitMsg.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        waitMsg.setForeground(UITheme.TEXT_MUTED);

        GridBagConstraints badgeGc = new GridBagConstraints();
        badgeGc.gridx = 0; badgeGc.gridy = 0;
        badgeGc.anchor = GridBagConstraints.WEST;
        badgeGc.fill = GridBagConstraints.NONE;
        badgeGc.insets = new Insets(0, 0, 10, 0);
        body.add(badge, badgeGc);
        gc.gridy = 1; gc.insets = new Insets(0, 0, 2, 0);  body.add(lblPlate, gc);
        gc.gridy = 2; gc.insets = new Insets(0, 0, 2, 0);  body.add(lblName, gc);
        gc.gridy = 3; gc.insets = new Insets(0, 0, 10, 0); body.add(lblGate, gc);
        gc.gridy = 4; gc.insets = new Insets(0, 0, 10, 0); body.add(sep, gc);
        gc.gridy = 5; gc.insets = new Insets(0, 0, 4, 0);  body.add(posLabel, gc);
        gc.gridy = 6; gc.insets = new Insets(0, 0, 12, 0); body.add(totalLabel, gc);
        gc.gridy = 7; gc.insets = new Insets(0, 0, 0, 0);  body.add(waitMsg, gc);

        posLabelRef[0]   = posLabel;
        totalLabelRef[0] = totalLabel;

        p.add(header, BorderLayout.NORTH);
        p.add(body,   BorderLayout.CENTER);
        return p;
    }

    // ── State 3: Assigned card ────────────────────────────────────────────────

    private JPanel buildAssignedCard(JPanel bubble, int number,
                                     String plate, String name, String slot, String gate) {
        Color bg     = new Color(18, 46, 26);
        Color green  = new Color(100, 220, 130);
        Color dimGreen = new Color(60, 140, 80);

        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setBackground(bg);
        p.setBorder(new EmptyBorder(16, 18, 18, 18));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 4, 0));
        JLabel title = new JLabel("Vehicle Entry");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(green);
        JButton closeBtn = makeXBtn(bg);
        closeBtn.setForeground(green);
        closeBtn.addActionListener(e -> removeBubble(bubble));
        header.add(title,    BorderLayout.WEST);
        header.add(closeBtn, BorderLayout.EAST);

        // Body
        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1.0; gc.gridx = 0;

        JLabel badge = UITheme.makeBadge("APPROVED", UITheme.SUCCESS);
        badge.setFont(new Font("Segoe UI", Font.BOLD, 14));
        badge.setBorder(new EmptyBorder(6, 16, 6, 16));

        JLabel slotLabel = new JLabel("Slot  " + slot);
        slotLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        slotLabel.setForeground(UITheme.SUCCESS);

        JSeparator sep = new JSeparator();
        sep.setForeground(dimGreen);

        JLabel plateRow = makeInfoLabel("Car Plate:", plate, green);
        JLabel nameRow  = makeInfoLabel("Driver Name:", name, green);
        JLabel gateRow  = makeInfoLabel("Entry Gate:", gate, green);

        GridBagConstraints badgeGc = new GridBagConstraints();
        badgeGc.gridx = 0; badgeGc.gridy = 0;
        badgeGc.anchor = GridBagConstraints.WEST;
        badgeGc.fill = GridBagConstraints.NONE;
        badgeGc.insets = new Insets(0, 0, 10, 0);
        body.add(badge, badgeGc);

        gc.gridy = 1; gc.insets = new Insets(0, 0, 8, 0);  body.add(slotLabel, gc);
        gc.gridy = 2; gc.insets = new Insets(0, 0, 12, 0); body.add(sep, gc);
        gc.gridy = 3; gc.insets = new Insets(0, 0, 4, 0);  body.add(plateRow, gc);
        gc.gridy = 4; gc.insets = new Insets(0, 0, 4, 0);  body.add(nameRow, gc);
        gc.gridy = 5; gc.insets = new Insets(0, 0, 0, 0);  body.add(gateRow, gc);

        p.add(header, BorderLayout.NORTH);
        p.add(body,   BorderLayout.CENTER);
        return p;
    }

    private void addTrackedWaitingBubble(Vehicle vehicle, boolean managedByAdmin) {
        bubbleCounter++;
        final int number = bubbleCounter;
        final String plate = vehicle.getLicensePlate();
        final String name = vehicle.getOwnerName();
        final String preferredGateId = vehicle.getPreferredGateId();

        // If the vehicle already has a slot assigned (e.g. from Management), show assigned card directly
        if (vehicle.getAssignedSlotId() != null) {
            CardLayout cl = new CardLayout();
            JPanel bubble = new JPanel(cl);
            bubble.setPreferredSize(new Dimension(270, 320));
            bubble.setBackground(UITheme.BG_CARD);
            bubble.setBorder(BorderFactory.createLineBorder(UITheme.SUCCESS, 1));
            JPanel assignedCard = buildAssignedCard(bubble, number, plate, name, vehicle.getAssignedSlotId(), prettyGateLabel(vehicle.getPreferredGateId()));
            bubble.add(assignedCard, "ASSIGNED");
            cl.show(bubble, "ASSIGNED");
            bubbleVehicles.put(bubble, vehicle);
            bubbleSlots.put(bubble, vehicle.getAssignedSlotId());
            bubbleContainer.add(bubble);
            bubbleContainer.revalidate();
            bubbleContainer.repaint();
            return;
        }

        CardLayout cl = new CardLayout();
        JPanel bubble = new JPanel(cl);
        bubble.setPreferredSize(new Dimension(270, 320));
        bubble.setBackground(UITheme.BG_CARD);
        bubble.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1));

        JLabel[] posLabelRef   = {null};
        JLabel[] totalLabelRef = {null};
        JPanel waitingCard = buildWaitingCard(bubble, number, plate, name,
                                              prettyGateLabel(preferredGateId), posLabelRef, totalLabelRef);
        bubble.add(waitingCard, "WAITING");
        bubble.setBorder(BorderFactory.createLineBorder(UITheme.WARNING, 1));
        cl.show(bubble, "WAITING");

        bubbleContainer.add(bubble);
        bubbleContainer.revalidate();
        bubbleContainer.repaint();

        bubbleVehicles.put(bubble, vehicle);
        int pos = gate.getEntryQueue().getSize();
        setStatus((managedByAdmin ? "Admin-added " : "") + plate + " joined queue at position #" + pos + ".", UITheme.WARNING);

        int[]    currentState    = {1};
        String[] slotRef         = {null};
        JPanel[] assignedCardRef = {null};

        Timer timer = new Timer(1000, e -> {
            Vehicle[] queue = gate.getEntryQueue().toArray();
            boolean inQueue = false;
            int qPos = -1;
            for (int i = 0; i < queue.length; i++) {
                if (queue[i].getLicensePlate().equals(plate)) {
                    inQueue = true; qPos = i + 1; break;
                }
            }

            if (currentState[0] == 1) {
                // Check if management already assigned a slot directly
                Vehicle latest = records == null ? null : records.findVehicleByPlate(plate);
                String managedSlot = (latest != null) ? latest.getAssignedSlotId() : null;

                if (managedSlot != null) {
                    currentState[0] = 2;
                    slotRef[0] = managedSlot;
                    bubbleSlots.put(bubble, managedSlot);
                    assignedCardRef[0] = buildAssignedCard(bubble, number, plate, name, managedSlot, prettyGateLabel(preferredGateId));
                    bubble.add(assignedCardRef[0], "ASSIGNED");
                    bubble.setBorder(BorderFactory.createLineBorder(UITheme.SUCCESS, 1));
                    cl.show(bubble, "ASSIGNED");
                    bubble.revalidate();
                    bubble.repaint();
                    log.log("USER  Auto-approved: " + plate + " → " + managedSlot + " (assigned by management)");
                    setStatus("Slot " + managedSlot + " assigned to " + plate + " by management.", UITheme.SUCCESS);
                } else if (!inQueue) {
                    if (gate.wasApproved(plate)) {
                        String slot = assignNearestSlot(vehicle);
                        if (slot == null) {
                            setStatus("All slots are currently full — waiting for a free bay.", UITheme.WARNING);
                            return;
                        }

                        currentState[0] = 2;
                        slotRef[0] = slot;
                        bubbleSlots.put(bubble, slot);

                        assignedCardRef[0] = buildAssignedCard(bubble, number, plate, name, slot, prettyGateLabel(preferredGateId));
                        bubble.add(assignedCardRef[0], "ASSIGNED");
                        bubble.setBorder(BorderFactory.createLineBorder(UITheme.SUCCESS, 1));
                        cl.show(bubble, "ASSIGNED");
                        log.log("USER  Slot assigned: " + plate + " → " + slot + " via " + prettyGateLabel(preferredGateId));
                        setStatus("Slot " + slot + " assigned to " + plate + " — approved by admin.", UITheme.SUCCESS);
                    } else {
                        log.log("USER  Entry cancelled: " + plate + " removed by undo.");
                        setStatus("Entry cancelled for " + plate + " — undo removed it from queue.", UITheme.WARNING);
                        removeBubble(bubble);
                    }
                } else {
                    int total = queue.length;
                    int ahead = qPos - 1;
                    posLabelRef[0].setText("Position  #" + qPos + "  of  " + total + "  in queue");
                    totalLabelRef[0].setText(ahead == 0 ? "You are next!" :
                        ahead + " vehicle" + (ahead > 1 ? "s" : "") + " ahead of you");
                    totalLabelRef[0].setForeground(ahead == 0 ? UITheme.SUCCESS : UITheme.TEXT_SECONDARY);
                }

            } else if (currentState[0] == 2) {
                // Detect slot reassignment by management
                Vehicle latestForSlot = records == null ? null : records.findVehicleByPlate(plate);
                String newSlot = (latestForSlot != null) ? latestForSlot.getAssignedSlotId() : null;
                if (newSlot != null && !newSlot.equals(slotRef[0])) {
                    slotRef[0] = newSlot;
                    bubbleSlots.put(bubble, newSlot);
                    if (assignedCardRef[0] != null) bubble.remove(assignedCardRef[0]);
                    assignedCardRef[0] = buildAssignedCard(bubble, number, plate, name, newSlot, prettyGateLabel(preferredGateId));
                    bubble.add(assignedCardRef[0], "ASSIGNED");
                    cl.show(bubble, "ASSIGNED");
                    bubble.revalidate();
                    bubble.repaint();
                    setStatus("Slot reassigned to " + newSlot + " for " + plate + " by management.", UITheme.SUCCESS);
                } else if (inQueue) {
                    currentState[0] = 1;
                    if (slotRef[0] != null) {
                        releaseAssignedSlot(vehicle);
                        bubbleSlots.remove(bubble);
                        slotRef[0] = null;
                    }
                    Vehicle existing = records.findVehicleByPlate(plate);
                    if (existing != null) records.removeVehicleRecord(existing);
                    if (assignedCardRef[0] != null) {
                        bubble.remove(assignedCardRef[0]);
                        assignedCardRef[0] = null;
                    }
                    bubble.setBorder(BorderFactory.createLineBorder(UITheme.WARNING, 1));
                    cl.show(bubble, "WAITING");
                    setStatus("Approval undone for " + plate + " — back in queue.", UITheme.WARNING);
                }
            }
        });
        timer.start();
        bubbleTimers.put(bubble, timer);
    }

    // ── Submit → state-machine timer ─────────────────────────────────────────

    private void submitToQueue(JPanel bubble, CardLayout cl,
                               JTextField plateField, JTextField nameField,
                               JComboBox<String> gateChoice, int number) {
        String plate = Vehicle.normalizePlate(plateField.getText().trim());
        String name  = nameField.getText().trim();
        String preferredGateId = normalizeGateSelection((String) gateChoice.getSelectedItem());

        if (plate.isEmpty()) { highlightError(plateField); return; }
        if (name.isEmpty())  { highlightError(nameField);  return; }

        if (records.findVehicleByPlate(plate) != null || queueContainsPlate(plate)) {
            setStatus("Plate " + plate + " is already registered. Duplicate entries are not allowed.", UITheme.DANGER);
            highlightError(plateField);
            return;
        }

        Vehicle v = new Vehicle(plate, name, System.currentTimeMillis());
        v.setPreferredGateId(preferredGateId);
        gate.vehicleArrives(v);
        // Register in records immediately so Slot Priority and Management can find and assign it
        if (records != null && records.findVehicleByPlate(plate) == null) {
            records.addVehicleRecord(v);
        }
        bubbleVehicles.put(bubble, v);
        log.log("USER  Joined queue: " + plate + " (" + name + ") via " + prettyGateLabel(preferredGateId));

        JLabel[] posLabelRef   = {null};
        JLabel[] totalLabelRef = {null};
        JPanel waitingCard = buildWaitingCard(bubble, number, plate, name,
                                              prettyGateLabel(preferredGateId), posLabelRef, totalLabelRef);
        bubble.add(waitingCard, "WAITING");
        bubble.setBorder(BorderFactory.createLineBorder(UITheme.WARNING, 1));
        cl.show(bubble, "WAITING");

        int pos = gate.getEntryQueue().getSize();
        setStatus(plate + " joined queue at position #" + pos + ".", UITheme.WARNING);

        int[]    currentState    = {1};
        String[] slotRef         = {null};
        JPanel[] assignedCardRef = {null};

        Timer timer = new Timer(1000, e -> {
            Vehicle[] queue = gate.getEntryQueue().toArray();
            boolean inQueue = false;
            int qPos = -1;
            for (int i = 0; i < queue.length; i++) {
                if (queue[i].getLicensePlate().equals(plate)) {
                    inQueue = true; qPos = i + 1; break;
                }
            }

            if (currentState[0] == 1) {
                // ── WAITING ───────────────────────────────────────────────
                // Check if management assigned a slot directly
                Vehicle latestV = records == null ? null : records.findVehicleByPlate(plate);
                String managedSlot2 = (latestV != null) ? latestV.getAssignedSlotId() : null;

                if (managedSlot2 != null) {
                    currentState[0] = 2;
                    slotRef[0] = managedSlot2;
                    bubbleSlots.put(bubble, managedSlot2);
                    assignedCardRef[0] = buildAssignedCard(bubble, number, plate, name, managedSlot2, prettyGateLabel(preferredGateId));
                    bubble.add(assignedCardRef[0], "ASSIGNED");
                    bubble.setBorder(BorderFactory.createLineBorder(UITheme.SUCCESS, 1));
                    cl.show(bubble, "ASSIGNED");
                    bubble.revalidate();
                    bubble.repaint();
                    log.log("USER  Auto-approved: " + plate + " → " + managedSlot2 + " (assigned by management)");
                    setStatus("Slot " + managedSlot2 + " assigned to " + plate + " by management.", UITheme.SUCCESS);
                } else if (!inQueue) {
                    if (gate.wasApproved(plate)) {
                        // Admin approved → pick the nearest available slot from the chosen access point.
                        String slot = assignNearestSlot(v);
                        if (slot == null) {
                            setStatus("All slots are currently full — waiting for a free bay.", UITheme.WARNING);
                            return;
                        }

                        currentState[0] = 2;
                        slotRef[0] = slot;
                        bubbleSlots.put(bubble, slot);
                        if (records.findVehicleByPlate(plate) == null) {
                            records.addVehicleRecord(v);
                        }

                        assignedCardRef[0] = buildAssignedCard(bubble, number, plate, name, slot, prettyGateLabel(preferredGateId));
                        bubble.add(assignedCardRef[0], "ASSIGNED");
                        bubble.setBorder(BorderFactory.createLineBorder(UITheme.SUCCESS, 1));
                        cl.show(bubble, "ASSIGNED");
                        log.log("USER  Slot assigned: " + plate + " → " + slot + " via " + prettyGateLabel(preferredGateId));
                        setStatus("Slot " + slot + " assigned to " + plate + " — approved by admin.", UITheme.SUCCESS);
                    } else {
                        // Admin undid the enqueue → remove bubble entirely
                        log.log("USER  Entry cancelled: " + plate + " removed by undo.");
                        setStatus("Entry cancelled for " + plate + " — undo removed it from queue.", UITheme.WARNING);
                        removeBubble(bubble);
                    }
                } else {
                    // Still waiting — update position labels
                    int total = queue.length;
                    int ahead = qPos - 1;
                    posLabelRef[0].setText("Position  #" + qPos + "  of  " + total + "  in queue");
                    totalLabelRef[0].setText(ahead == 0 ? "You are next!" :
                        ahead + " vehicle" + (ahead > 1 ? "s" : "") + " ahead of you");
                    totalLabelRef[0].setForeground(ahead == 0 ? UITheme.SUCCESS : UITheme.TEXT_SECONDARY);
                }

            } else if (currentState[0] == 2) {
                // ── ASSIGNED — watch for management slot reassignment ─────
                Vehicle latestV2 = records == null ? null : records.findVehicleByPlate(plate);
                String newSlot2 = (latestV2 != null) ? latestV2.getAssignedSlotId() : null;
                if (newSlot2 != null && !newSlot2.equals(slotRef[0])) {
                    slotRef[0] = newSlot2;
                    bubbleSlots.put(bubble, newSlot2);
                    if (assignedCardRef[0] != null) bubble.remove(assignedCardRef[0]);
                    assignedCardRef[0] = buildAssignedCard(bubble, number, plate, name, newSlot2, prettyGateLabel(preferredGateId));
                    bubble.add(assignedCardRef[0], "ASSIGNED");
                    cl.show(bubble, "ASSIGNED");
                    bubble.revalidate();
                    bubble.repaint();
                    setStatus("Slot reassigned to " + newSlot2 + " for " + plate + " by management.", UITheme.SUCCESS);
                } else if (inQueue) {
                    // Vehicle returned to queue → undo of approval, free the slot
                    currentState[0] = 1;
                    if (slotRef[0] != null) {
                        releaseAssignedSlot(v);
                        bubbleSlots.remove(bubble);
                        slotRef[0] = null;
                    }
                    Vehicle existing = records.findVehicleByPlate(plate);
                    if (existing != null) records.removeVehicleRecord(existing);
                    if (assignedCardRef[0] != null) {
                        bubble.remove(assignedCardRef[0]);
                        assignedCardRef[0] = null;
                    }
                    bubble.setBorder(BorderFactory.createLineBorder(UITheme.WARNING, 1));
                    cl.show(bubble, "WAITING");
                    setStatus("Approval undone for " + plate + " — back in queue.", UITheme.WARNING);
                }
            }
        });
        timer.start();
        bubbleTimers.put(bubble, timer);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void removeBubble(JPanel bubble) {
        Timer t = bubbleTimers.remove(bubble);
        if (t != null) t.stop();
        String slot = bubbleSlots.remove(bubble);
        Vehicle v = bubbleVehicles.remove(bubble);
        if (v != null) {
            if (slot != null) {
                releaseAssignedSlot(v);
            }
            Vehicle existing = records.findVehicleByPlate(v.getLicensePlate());
            if (existing != null) records.removeVehicleRecord(existing);
            gate.purgeVehicle(v);   // clear from queue + undo stack so no ghost resurrection
        }
        bubbleContainer.remove(bubble);
        bubbleContainer.revalidate();
        bubbleContainer.repaint();
    }

    public boolean removeVehicleByPlate(String plate) {
        JPanel bubble = findBubbleByPlate(plate);
        if (bubble == null) {
            return false;
        }
        removeBubble(bubble);
        return true;
    }

    public boolean removeVehicleBySlotId(String slotId) {
        if (slotId == null) {
            return false;
        }

        String normalized = slotId.trim().toUpperCase();
        for (Map.Entry<JPanel, String> entry : bubbleSlots.entrySet()) {
            if (normalized.equalsIgnoreCase(entry.getValue())) {
                removeBubble(entry.getKey());
                return true;
            }
        }

        for (Map.Entry<JPanel, Vehicle> entry : bubbleVehicles.entrySet()) {
            Vehicle v = entry.getValue();
            if (v != null && normalized.equalsIgnoreCase(v.getAssignedSlotId())) {
                removeBubble(entry.getKey());
                return true;
            }
        }
        return false;
    }

    private JPanel findBubbleByPlate(String plate) {
        String normalized = Vehicle.normalizePlate(plate);
        for (Map.Entry<JPanel, Vehicle> entry : bubbleVehicles.entrySet()) {
            Vehicle vehicle = entry.getValue();
            if (vehicle != null && Vehicle.normalizePlate(vehicle.getLicensePlate()).equals(normalized)) {
                return entry.getKey();
            }
        }
        return null;
    }

    private void setStatus(String msg, Color color) {
        statusLabel.setText(msg);
        statusLabel.setForeground(color);
    }

    private void highlightError(JTextField f) {
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.DANGER, 1),
            new EmptyBorder(5, 9, 5, 9)
        ));
        f.requestFocusInWindow();
    }

    private JTextField makeField() {
        JTextField f = new JTextField();
        f.setBackground(UITheme.BG_INPUT);
        f.setForeground(UITheme.TEXT_PRIMARY);
        f.setCaretColor(UITheme.TEXT_PRIMARY);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER, 1),
            new EmptyBorder(5, 9, 5, 9)
        ));
        return f;
    }

    private boolean queueContainsPlate(String plate) {
        String normalizedPlate = Vehicle.normalizePlate(plate);
        for (Vehicle queued : gate.getEntryQueue().toArray()) {
            if (Vehicle.normalizePlate(queued.getLicensePlate()).equals(normalizedPlate)) {
                return true;
            }
        }
        return false;
    }

    private JButton makeXBtn(Color bg) {
        JButton b = new JButton("X") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(getForeground());
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                String text = getText();
                int tx = (getWidth()  - fm.stringWidth(text)) / 2;
                int ty = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(text, tx, ty);
                g2.dispose();
            }
        };
        b.setFont(new Font("Arial", Font.BOLD, 16));
        b.setForeground(new Color(220, 60, 60));
        b.setBackground(bg);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setOpaque(true);
        b.setMargin(new java.awt.Insets(0, 0, 0, 0));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { b.setForeground(new Color(255, 80, 80)); b.repaint(); }
            public void mouseExited(java.awt.event.MouseEvent e)  { b.setForeground(new Color(220, 60, 60)); b.repaint(); }
        });
        return b;
    }

    private String normalizeGateSelection(String selection) {
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

    private String resolveStartNode(String preferredGateId) {
        if (preferredGateId == null) {
            return DEFAULT_ACCESS_NODE;
        }

        String normalized = preferredGateId.trim().toUpperCase();
        if (normalized.isEmpty()) {
            return DEFAULT_ACCESS_NODE;
        }

        switch (normalized) {
            case "ENTRANCE":
            case "GATE_A":
            case "GATE_B":
            case "GATE_C":
                return normalized;
            default:
                return DEFAULT_ACCESS_NODE;
        }
    }

    private String assignNearestSlot(Vehicle vehicle) {
        if (parkingMap == null || graph == null || vehicle == null) {
            return null;
        }

        String startNode = resolveStartNode(vehicle.getPreferredGateId());
        java.util.List<String> path = navigation.DijkstraPathfinder.findShortestPathToAvailableSpot(graph, startNode);
        if (path.isEmpty()) {
            return null;
        }

        String slotId = path.get(path.size() - 1);
        int routeCost = DijkstraPathfinder.calculatePathCost(graph, path);
        parkingMap.markOccupied(slotId);
        graph.setOccupancy(slotId, true);
        vehicle.setAssignedSlotId(slotId);
        if (records != null) {
            ParkingSlot slotRecord = records.findSlotById(slotId);
            if (slotRecord == null) {
                slotRecord = new ParkingSlot(slotId, routeCost);
                records.addParkingSlotRecord(slotRecord);
            } else {
                slotRecord.setDistanceToGate(routeCost);
            }
            slotRecord.setParkedVehicle(vehicle);
        }
        return slotId;
    }

    private void releaseAssignedSlot(Vehicle vehicle) {
        if (vehicle == null) {
            return;
        }

        String slotId = vehicle.getAssignedSlotId();
        if (slotId != null) {
            if (parkingMap != null) {
                parkingMap.markFree(slotId);
            }
            if (graph != null) {
                graph.setOccupancy(slotId, false);
            }
            if (records != null) {
                ParkingSlot slotRecord = records.findSlotById(slotId);
                if (slotRecord != null) {
                    slotRecord.setParkedVehicle(null);
                    // Remove the slot record entirely so it disappears from every panel
                    records.removeParkingSlotRecord(slotRecord);
                }
            }
            vehicle.setAssignedSlotId(null);
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

    private JLabel makeInfoLabel(String key, String value) {
        JLabel l = new JLabel("<html><span style='color:#8b949e'>" + key +
                              "&nbsp;</span><span style='color:#e6edf3'>" + value + "</span></html>");
        l.setFont(UITheme.FONT_BODY);
        return l;
    }

    private JLabel makeInfoLabel(String key, String value, Color valueColor) {
        String hex = String.format("#%02x%02x%02x",
                valueColor.getRed(), valueColor.getGreen(), valueColor.getBlue());
        JLabel l = new JLabel("<html><span style='color:#8b949e'>" + key +
                              "&nbsp;</span><span style='color:" + hex + "'><b>" + value + "</b></span></html>");
        l.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        return l;
    }
}
