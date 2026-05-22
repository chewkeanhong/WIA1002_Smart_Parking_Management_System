package ui;

import gate_control.GateProcessor;
import gate_control.UndoStack;
import models.Vehicle;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Module 2 — Entry & Exit Processing
 * Data Structures: Queue (FIFO) + Stack (LIFO undo)
 */
public class GateControlPanel extends JPanel {

    private final ActivityLog   log;
    private final GateProcessor gate = new GateProcessor();
    private static final SimpleDateFormat SDF = new SimpleDateFormat("HH:mm:ss");

    // Form fields
    private JTextField tfPlate, tfOwner;
    private JLabel     statusLabel;

    // Live display areas
    private JPanel queueVisual;
    private JPanel stackVisual;
    private JLabel queueCountLabel, stackCountLabel;

    public GateControlPanel(ActivityLog log) {
        this.log = log;
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
        left.add(UITheme.makeSectionTitle("Entry & Exit Processing"));
        left.add(UITheme.makeLabel("Queue (FIFO arrival order)  +  Stack (LIFO undo mechanism)"));

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
        p.add(buildQueueSection());
        p.add(buildStackSection());
        return p;
    }

    // ── Queue section (FIFO) ──────────────────────────────────────────────────
    private JPanel buildQueueSection() {
        JPanel card = UITheme.makeCard(new BorderLayout(0, 10));

        // Header
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setOpaque(false);
        JPanel titleRow = UITheme.makeSectionHeader("Entry Queue", "FIFO", UITheme.ACCENT);
        queueCountLabel = UITheme.makeLabel("0 vehicles waiting");
        hdr.add(titleRow,         BorderLayout.WEST);
        hdr.add(queueCountLabel,  BorderLayout.EAST);
        card.add(hdr, BorderLayout.NORTH);

        // Input form
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(4, 0, 8, 0));
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(3, 3, 3, 3);

        tfPlate = UITheme.makeTextField(11);
        tfOwner = UITheme.makeTextField(11);

        gc.gridx = 0; gc.gridy = 0; gc.weightx = 0;
        form.add(UITheme.makeLabel("Licence Plate"), gc);
        gc.gridx = 1; gc.weightx = 1;
        form.add(tfPlate, gc);

        gc.gridx = 0; gc.gridy = 1; gc.weightx = 0;
        form.add(UITheme.makeLabel("Owner Name"), gc);
        gc.gridx = 1; gc.weightx = 1;
        form.add(tfOwner, gc);

        JButton enqBtn = UITheme.makePrimaryButton("Enqueue Vehicle");
        JButton deqBtn = UITheme.makeButton("Process Next →", new Color(22, 100, 50));
        enqBtn.addActionListener(e -> enqueue());
        deqBtn.addActionListener(e -> processNext());

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        btns.setOpaque(false);
        btns.add(enqBtn);
        btns.add(deqBtn);

        gc.gridx = 0; gc.gridy = 2; gc.gridwidth = 2;
        form.add(btns, gc);
        card.add(form, BorderLayout.NORTH);

        // Queue visualiser
        JLabel vizTitle = UITheme.makeLabel("Queue state  (front → rear):");
        vizTitle.setBorder(new EmptyBorder(4, 0, 4, 0));

        queueVisual = new JPanel();
        queueVisual.setOpaque(false);
        queueVisual.setLayout(new BoxLayout(queueVisual, BoxLayout.Y_AXIS));

        JScrollPane sp = UITheme.wrapScroll(queueVisual);
        sp.setPreferredSize(new Dimension(0, 200));

        JPanel vizPanel = new JPanel(new BorderLayout(0, 4));
        vizPanel.setOpaque(false);
        vizPanel.add(vizTitle, BorderLayout.NORTH);
        vizPanel.add(sp,       BorderLayout.CENTER);

        card.add(vizPanel, BorderLayout.CENTER);
        return card;
    }

    // ── Stack section (LIFO undo) ─────────────────────────────────────────────
    private JPanel buildStackSection() {
        JPanel card = UITheme.makeCard(new BorderLayout(0, 10));

        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setOpaque(false);
        JPanel titleRow = UITheme.makeSectionHeader("Undo Stack", "LIFO", UITheme.WARNING);
        stackCountLabel = UITheme.makeLabel("0 actions");
        hdr.add(titleRow,         BorderLayout.WEST);
        hdr.add(stackCountLabel,  BorderLayout.EAST);
        card.add(hdr, BorderLayout.NORTH);

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setBorder(new EmptyBorder(0, 0, 8, 0));

        JLabel infoTxt = new JLabel(
            "<html>Every action (enqueue / process / exit) is pushed onto the stack.<br>" +
            "Undo pops the most recent — <b style='color:#e6edf3'>O(1) LIFO</b>.</html>");
        infoTxt.setFont(UITheme.FONT_SMALL);
        infoTxt.setForeground(UITheme.TEXT_SECONDARY);
        info.add(infoTxt);
        info.add(Box.createVerticalStrut(8));

        JButton undoBtn = UITheme.makeButton("↩  Undo Last Action", UITheme.WARNING);
        undoBtn.setForeground(UITheme.BG_DARK);
        undoBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        undoBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        undoBtn.addActionListener(e -> undoLast());
        info.add(undoBtn);

        card.add(info, BorderLayout.NORTH);

        // Stack visualiser
        JLabel vizTitle = UITheme.makeLabel("Stack state  (top is most recent):");
        vizTitle.setBorder(new EmptyBorder(4, 0, 4, 0));

        stackVisual = new JPanel();
        stackVisual.setOpaque(false);
        stackVisual.setLayout(new BoxLayout(stackVisual, BoxLayout.Y_AXIS));

        JScrollPane sp = UITheme.wrapScroll(stackVisual);

        JPanel vizPanel = new JPanel(new BorderLayout(0, 4));
        vizPanel.setOpaque(false);
        vizPanel.add(vizTitle, BorderLayout.NORTH);
        vizPanel.add(sp,       BorderLayout.CENTER);

        card.add(vizPanel, BorderLayout.CENTER);
        return card;
    }

    // ── Complexity banner ─────────────────────────────────────────────────────
    private JPanel buildComplexity() {
        return UITheme.makeComplexityBanner(
            "<b>Queue (FIFO):</b> &nbsp; enqueue → <b>O(1)</b> · dequeue → <b>O(1)</b> · " +
            "Guarantees vehicles are processed in arrival order. &nbsp;|&nbsp; " +
            "<b>Stack (LIFO):</b> &nbsp; push → <b>O(1)</b> · pop → <b>O(1)</b> · " +
            "Enables instant undo of the most recent gate action without scanning history."
        );
    }

    // ── Actions ───────────────────────────────────────────────────────────────
    private void enqueue() {
        String plate = tfPlate.getText().trim().toUpperCase();
        String owner = tfOwner.getText().trim();
        if (plate.isEmpty() || owner.isEmpty()) { status("Fill both fields.", UITheme.DANGER); return; }

        Vehicle v = new Vehicle(plate, owner, System.currentTimeMillis());
        gate.vehicleArrives(v);
        log.log("GATE  Enqueued: " + plate + " (" + owner + ")");
        status("Enqueued " + plate + " — O(1).", UITheme.SUCCESS);
        tfPlate.setText(""); tfOwner.setText("");
        refreshVisuals();
    }

    private void processNext() {
        Vehicle v = gate.processNext();
        if (v == null) { status("Queue is empty.", UITheme.WARNING); return; }
        log.log("GATE  Processed (dequeued): " + v.getLicensePlate());
        status("Processed " + v.getLicensePlate() + " — O(1).", UITheme.SUCCESS);
        refreshVisuals();
    }

    private void undoLast() {
        UndoStack.Action action = gate.undoLast();
        if (action == null) { status("Nothing to undo.", UITheme.WARNING); return; }
        log.log("GATE  UNDO → " + action);
        status("Undone: " + action + " — O(1).", UITheme.WARNING);
        refreshVisuals();
    }

    // ── Visual refresh ────────────────────────────────────────────────────────
    private void refreshVisuals() {
        refreshQueueVisual();
        refreshStackVisual();
    }

    private void refreshQueueVisual() {
        queueVisual.removeAll();
        Vehicle[] queue = gate.getEntryQueue().toArray();
        queueCountLabel.setText(queue.length + " vehicle" + (queue.length != 1 ? "s" : "") + " waiting");

        if (queue.length == 0) {
            JLabel empty = UITheme.makeLabel("Queue is empty");
            empty.setAlignmentX(Component.LEFT_ALIGNMENT);
            queueVisual.add(empty);
        } else {
            for (int i = 0; i < queue.length; i++) {
                Vehicle v = queue[i];
                JPanel row = queueRow(
                    (i == 0 ? "▶ FRONT  " : "       ") + v.getLicensePlate(),
                    v.getOwnerName(),
                    SDF.format(new Date(v.getEntryTime())),
                    i == 0 ? UITheme.SUCCESS : UITheme.TEXT_SECONDARY
                );
                queueVisual.add(row);
                queueVisual.add(Box.createVerticalStrut(4));
            }
        }
        queueVisual.revalidate();
        queueVisual.repaint();
    }

    private void refreshStackVisual() {
        stackVisual.removeAll();
        UndoStack.Action[] actions = gate.getUndoStack().toArray();
        stackCountLabel.setText(actions.length + " action" + (actions.length != 1 ? "s" : ""));

        if (actions.length == 0) {
            JLabel empty = UITheme.makeLabel("Stack is empty");
            empty.setAlignmentX(Component.LEFT_ALIGNMENT);
            stackVisual.add(empty);
        } else {
            // Show from top (most recent) down
            for (int i = actions.length - 1; i >= 0; i--) {
                UndoStack.Action a = actions[i];
                boolean isTop = (i == actions.length - 1);
                JPanel row = queueRow(
                    (isTop ? "▶ TOP  " : "       ") + a.type,
                    a.vehicle.getLicensePlate(),
                    a.vehicle.getOwnerName(),
                    isTop ? UITheme.WARNING : UITheme.TEXT_SECONDARY
                );
                stackVisual.add(row);
                stackVisual.add(Box.createVerticalStrut(3));
            }
        }
        stackVisual.revalidate();
        stackVisual.repaint();
    }

    private JPanel queueRow(String col1, String col2, String col3, Color textColor) {
        JPanel p = new JPanel(new GridLayout(1, 3, 8, 0));
        p.setBackground(UITheme.BG_INPUT);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER, 1),
            new EmptyBorder(7, 10, 7, 10)));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        for (String txt : new String[]{col1, col2, col3}) {
            JLabel l = new JLabel(txt);
            l.setFont(UITheme.FONT_MONO);
            l.setForeground(textColor);
            p.add(l);
        }
        return p;
    }

    private void status(String msg, Color color) {
        statusLabel.setText(msg);
        statusLabel.setForeground(color);
    }
}
