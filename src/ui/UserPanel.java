package ui;

import gate_control.GateProcessor;
import management.RecordManager;
import models.ParkingMap;
import models.Vehicle;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class UserPanel extends JPanel {

    private final GateProcessor      gate;
    private final ActivityLog        log;
    private final ParkingMap         parkingMap;
    private final RecordManager      records;
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

        setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout(0, 0));
        setBorder(new EmptyBorder(28, 28, 28, 28));

        bubbleContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 16));
        bubbleContainer.setBackground(UITheme.BG_DARK);

        JScrollPane scroll = UITheme.wrapScroll(bubbleContainer);
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
        bubble.setPreferredSize(new Dimension(240, 195));
        bubble.setBackground(UITheme.BG_CARD);
        bubble.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1));

        bubble.add(buildInputCard(bubble, cl, number), "INPUT");
        cl.show(bubble, "INPUT");

        bubbleContainer.add(bubble);
        bubbleContainer.revalidate();
        bubbleContainer.repaint();
    }

    // ── State 1: Input form ───────────────────────────────────────────────────

    private JPanel buildInputCard(JPanel bubble, CardLayout cl, int number) {
        JPanel p = new JPanel(null);
        p.setBackground(UITheme.BG_CARD);

        JLabel title = new JLabel("Vehicle #" + number);
        title.setFont(UITheme.FONT_SUBTITLE);
        title.setForeground(UITheme.TEXT_PRIMARY);
        title.setBounds(12, 10, 140, 18);

        JButton closeBtn = makeXBtn(UITheme.BG_CARD);
        closeBtn.setBounds(204, 4, 28, 28);
        closeBtn.addActionListener(e -> removeBubble(bubble));

        JLabel plateLabel = UITheme.makeLabel("Vehicle Plate");
        plateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        plateLabel.setBounds(12, 38, 110, 14);

        JTextField plateField = makeField();
        plateField.setBounds(12, 54, 214, 32);

        JLabel nameLabel = UITheme.makeLabel("Name");
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        nameLabel.setBounds(12, 94, 110, 14);

        JTextField nameField = makeField();
        nameField.setBounds(12, 110, 214, 32);

        JButton submitBtn = UITheme.makeButton("Submit  →", new Color(22, 100, 50));
        submitBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        submitBtn.setBounds(12, 152, 214, 32);
        submitBtn.addActionListener(e -> submitToQueue(bubble, cl, plateField, nameField, number));

        plateField.addActionListener(e -> submitToQueue(bubble, cl, plateField, nameField, number));
        nameField.addActionListener(e  -> submitToQueue(bubble, cl, plateField, nameField, number));

        p.add(title); p.add(closeBtn);
        p.add(plateLabel); p.add(plateField);
        p.add(nameLabel);  p.add(nameField);
        p.add(submitBtn);

        SwingUtilities.invokeLater(plateField::requestFocusInWindow);
        return p;
    }

    // ── State 2: Waiting card ─────────────────────────────────────────────────

    private JPanel buildWaitingCard(JPanel bubble, int number, String plate, String name,
                                    JLabel[] posLabelRef, JLabel[] totalLabelRef) {
        JPanel p = new JPanel(null);
        p.setBackground(UITheme.BG_CARD);

        JLabel title = new JLabel("Vehicle #" + number);
        title.setFont(UITheme.FONT_SUBTITLE);
        title.setForeground(UITheme.TEXT_PRIMARY);
        title.setBounds(12, 10, 140, 18);

        JButton closeBtn = makeXBtn(UITheme.BG_CARD);
        closeBtn.setBounds(204, 4, 28, 28);
        closeBtn.addActionListener(e -> removeBubble(bubble));

        JLabel badge = UITheme.makeBadge("⏳  PENDING", UITheme.WARNING);
        badge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        badge.setForeground(UITheme.BG_DARK);
        badge.setBounds(12, 36, 90, 18);

        JLabel info = new JLabel(plate + "  ·  " + name);
        info.setFont(UITheme.FONT_MONO);
        info.setForeground(UITheme.TEXT_PRIMARY);
        info.setBounds(12, 64, 214, 16);

        JSeparator sep = new JSeparator();
        sep.setForeground(UITheme.BORDER);
        sep.setBounds(12, 88, 214, 2);

        JLabel posLabel = new JLabel("Checking queue...");
        posLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        posLabel.setForeground(UITheme.ACCENT);
        posLabel.setBounds(12, 98, 214, 18);

        JLabel totalLabel = UITheme.makeLabel("");
        totalLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        totalLabel.setBounds(12, 118, 214, 14);

        JLabel waitMsg = UITheme.makeLabel("Waiting for admin to approve...");
        waitMsg.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        waitMsg.setForeground(UITheme.TEXT_MUTED);
        waitMsg.setBounds(12, 160, 214, 14);

        posLabelRef[0]   = posLabel;
        totalLabelRef[0] = totalLabel;

        p.add(title); p.add(closeBtn); p.add(badge);
        p.add(info);  p.add(sep);
        p.add(posLabel); p.add(totalLabel); p.add(waitMsg);
        return p;
    }

    // ── State 3: Assigned card ────────────────────────────────────────────────

    private JPanel buildAssignedCard(JPanel bubble, int number,
                                     String plate, String name, String slot) {
        JPanel p = new JPanel(null);
        p.setBackground(new Color(18, 46, 26));

        JLabel title = new JLabel("Vehicle #" + number);
        title.setFont(UITheme.FONT_SUBTITLE);
        title.setForeground(new Color(150, 220, 160));
        title.setBounds(12, 10, 140, 18);

        JButton closeBtn = makeXBtn(new Color(18, 46, 26));
        closeBtn.setForeground(new Color(100, 180, 110));
        closeBtn.setBounds(204, 4, 28, 28);
        closeBtn.addActionListener(e -> removeBubble(bubble));

        JLabel badge = UITheme.makeBadge("✓  APPROVED", UITheme.SUCCESS);
        badge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        badge.setBounds(12, 36, 90, 18);

        JLabel slotLabel = new JLabel("Slot  " + slot);
        slotLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        slotLabel.setForeground(UITheme.SUCCESS);
        slotLabel.setBounds(12, 62, 214, 30);

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(34, 80, 46));
        sep.setBounds(12, 100, 214, 2);

        JLabel plateRow = makeInfoLabel("Plate:", plate);
        plateRow.setBounds(12, 110, 214, 16);

        JLabel nameRow = makeInfoLabel("Name:", name);
        nameRow.setBounds(12, 130, 214, 16);

        JLabel note = new JLabel("Admin approved — O(1) dequeue");
        note.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        note.setForeground(new Color(80, 180, 100));
        note.setBounds(12, 162, 214, 14);

        p.add(title); p.add(closeBtn); p.add(badge);
        p.add(slotLabel); p.add(sep);
        p.add(plateRow); p.add(nameRow); p.add(note);
        return p;
    }

    // ── Submit → state-machine timer ─────────────────────────────────────────

    private void submitToQueue(JPanel bubble, CardLayout cl,
                               JTextField plateField, JTextField nameField, int number) {
        String plate = plateField.getText().trim().toUpperCase();
        String name  = nameField.getText().trim();

        if (plate.isEmpty()) { highlightError(plateField); return; }
        if (name.isEmpty())  { highlightError(nameField);  return; }

        Vehicle v = new Vehicle(plate, name, System.currentTimeMillis());
        gate.vehicleArrives(v);
        bubbleVehicles.put(bubble, v);
        log.log("USER  Joined queue: " + plate + " (" + name + ")");

        JLabel[] posLabelRef   = {null};
        JLabel[] totalLabelRef = {null};
        JPanel waitingCard = buildWaitingCard(bubble, number, plate, name, posLabelRef, totalLabelRef);
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
                if (!inQueue) {
                    if (gate.wasApproved(plate)) {
                        // Admin approved → pick first free slot and assign
                        currentState[0] = 2;
                        String slot = parkingMap.findFirstFree();
                        if (slot == null) slot = "FULL";
                        slotRef[0] = slot;
                        parkingMap.markOccupied(slot);
                        bubbleSlots.put(bubble, slot);
                        v.setAssignedSlotId(slot);
                        if (records.findVehicleByPlate(plate) == null) {
                            records.addVehicleRecord(v);
                        }

                        assignedCardRef[0] = buildAssignedCard(bubble, number, plate, name, slot);
                        bubble.add(assignedCardRef[0], "ASSIGNED");
                        bubble.setBorder(BorderFactory.createLineBorder(UITheme.SUCCESS, 1));
                        cl.show(bubble, "ASSIGNED");
                        log.log("USER  Slot assigned: " + plate + " → " + slot);
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
                // ── ASSIGNED — watch for admin undo ───────────────────────
                if (inQueue) {
                    // Vehicle returned to queue → undo of approval, free the slot
                    currentState[0] = 1;
                    if (slotRef[0] != null) {
                        parkingMap.markFree(slotRef[0]);
                        bubbleSlots.remove(bubble);
                        slotRef[0] = null;
                    }
                    v.setAssignedSlotId(null);
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
        if (slot != null) parkingMap.markFree(slot);
        Vehicle v = bubbleVehicles.remove(bubble);
        if (v != null) {
            v.setAssignedSlotId(null);
            Vehicle existing = records.findVehicleByPlate(v.getLicensePlate());
            if (existing != null) records.removeVehicleRecord(existing);
            gate.purgeVehicle(v);   // clear from queue + undo stack so no ghost resurrection
        }
        bubbleContainer.remove(bubble);
        bubbleContainer.revalidate();
        bubbleContainer.repaint();
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

    private JButton makeXBtn(Color bg) {
        JButton b = new JButton("×");
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setForeground(UITheme.TEXT_SECONDARY);
        b.setBackground(bg);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JLabel makeInfoLabel(String key, String value) {
        JLabel l = new JLabel("<html><span style='color:#8b949e'>" + key +
                              "&nbsp;</span><span style='color:#e6edf3'>" + value + "</span></html>");
        l.setFont(UITheme.FONT_BODY);
        return l;
    }
}
