package ui;

import gate_control.GateProcessor;
import gate_control.UndoStack;
import models.Vehicle;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GateControlPanel extends JPanel {

    private final ActivityLog   log;
    private final GateProcessor gate;
    private static final SimpleDateFormat SDF = new SimpleDateFormat("HH:mm:ss");

    private JLabel  statusLabel;
    private JPanel  queueVisual, stackVisual;
    private JLabel  queueCountLabel, stackCountLabel;
    private JLabel  approveNextLabel;
    private JButton approveBtn;

    public GateControlPanel(ActivityLog log, GateProcessor gate) {
        this.log  = log;
        this.gate = gate;
        setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout(0, 16));
        setBorder(new EmptyBorder(28, 28, 28, 28));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(),   BorderLayout.CENTER);

        // Auto-refresh so the approve section picks up vehicles added from UserPanel
        Timer autoRefresh = new Timer(1000, e -> { refreshQueue(); refreshStackVisual(); });
        autoRefresh.start();
    }

    public GateControlPanel(ActivityLog log) {
        this(log, new GateProcessor());
    }

    // ── Header ────────────────────────────────────────────────────────────────

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);

        JPanel left = new JPanel(new GridLayout(2, 1, 0, 2));
        left.setOpaque(false);
        JLabel title = UITheme.makeSectionTitle("Entry & Exit Processing");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        JLabel sub = UITheme.makeLabel("Queue (FIFO arrival order)  +  Stack (LIFO undo mechanism)");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        left.add(title);
        left.add(sub);

        statusLabel = new JLabel(" ");
        statusLabel.setFont(UITheme.FONT_SMALL);
        statusLabel.setForeground(UITheme.SUCCESS);
        statusLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        p.add(left,        BorderLayout.WEST);
        p.add(statusLabel, BorderLayout.EAST);
        return p;
    }

    // ── Body ─────────────────────────────────────────────────────────────────

    private JPanel buildBody() {
        JPanel p = new JPanel(new GridLayout(1, 2, 14, 0));
        p.setOpaque(false);
        p.add(buildQueueCard());
        p.add(buildUndoCard());
        return p;
    }

    // ── Left: Admin Approve + Queue list ─────────────────────────────────────

    private JPanel buildQueueCard() {
        JPanel card = UITheme.makeCard(new BorderLayout(0, 10));

        // ── top section (header + approve block) ──
        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

        // Header row
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setOpaque(false);
        hdr.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        queueCountLabel = UITheme.makeLabel("0 vehicles waiting");
        hdr.add(UITheme.makeSectionHeader("Entry Queue", "FIFO", UITheme.ACCENT), BorderLayout.WEST);
        hdr.add(queueCountLabel, BorderLayout.EAST);
        top.add(hdr);
        top.add(Box.createVerticalStrut(12));

        // Approve block
        JPanel approveSection = new JPanel(new BorderLayout(14, 0));
        approveSection.setBackground(new Color(20, 30, 50));
        approveSection.setMaximumSize(new Dimension(Integer.MAX_VALUE, 62));
        approveSection.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.ACCENT, 1),
            new EmptyBorder(10, 14, 10, 14)
        ));

        JPanel approveLeft = new JPanel(new GridLayout(2, 1, 0, 4));
        approveLeft.setOpaque(false);

        JLabel approveTitle = new JLabel("Admin — Approve Next Vehicle");
        approveTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        approveTitle.setForeground(UITheme.ACCENT);

        approveNextLabel = new JLabel("No vehicles in queue");
        approveNextLabel.setFont(UITheme.FONT_MONO);
        approveNextLabel.setForeground(UITheme.TEXT_MUTED);

        approveLeft.add(approveTitle);
        approveLeft.add(approveNextLabel);

        approveBtn = UITheme.makeButton("✓  Approve", new Color(22, 100, 50));
        approveBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        approveBtn.setEnabled(false);
        approveBtn.setPreferredSize(new Dimension(110, 36));
        approveBtn.addActionListener(e -> processNext());

        approveSection.add(approveLeft, BorderLayout.CENTER);
        approveSection.add(approveBtn,  BorderLayout.EAST);
        top.add(approveSection);
        top.add(Box.createVerticalStrut(10));

        card.add(top, BorderLayout.NORTH);

        // ── Queue state list ──
        JLabel vizTitle = UITheme.makeLabel("Queue state  (front → rear):");
        vizTitle.setBorder(new EmptyBorder(0, 0, 4, 0));

        queueVisual = new JPanel();
        queueVisual.setOpaque(false);
        queueVisual.setLayout(new BoxLayout(queueVisual, BoxLayout.Y_AXIS));

        JScrollPane sp = UITheme.wrapScroll(queueVisual);

        JPanel vizPanel = new JPanel(new BorderLayout(0, 4));
        vizPanel.setOpaque(false);
        vizPanel.add(vizTitle, BorderLayout.NORTH);
        vizPanel.add(sp,       BorderLayout.CENTER);
        card.add(vizPanel, BorderLayout.CENTER);

        return card;
    }

    // ── Right: Undo Stack ─────────────────────────────────────────────────────

    private JPanel buildUndoCard() {
        JPanel card = UITheme.makeCard(new BorderLayout(0, 10));

        // ── top: header + undo button (compact, stacked) ──
        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setOpaque(false);
        hdr.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        stackCountLabel = UITheme.makeLabel("0 actions");
        hdr.add(UITheme.makeSectionHeader("Undo Stack", "LIFO", UITheme.WARNING), BorderLayout.WEST);
        hdr.add(stackCountLabel, BorderLayout.EAST);
        top.add(hdr);
        top.add(Box.createVerticalStrut(10));

        JButton undoBtn = UITheme.makeButton("↩  Undo Last Action", UITheme.WARNING);
        undoBtn.setForeground(UITheme.BG_DARK);
        undoBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        undoBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        undoBtn.addActionListener(e -> undoLast());

        JPanel btnWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        btnWrap.setOpaque(false);
        btnWrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnWrap.add(undoBtn);
        top.add(btnWrap);
        top.add(Box.createVerticalStrut(10));

        card.add(top, BorderLayout.NORTH);

        // ── centre: stack console (fills remaining space) ──
        JLabel vizTitle = UITheme.makeLabel("Stack state  (top is most recent):");
        vizTitle.setBorder(new EmptyBorder(0, 0, 4, 0));

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

    // ── Actions ───────────────────────────────────────────────────────────────

    private void processNext() {
        Vehicle v = gate.processNext();
        if (v == null) { status("Queue is empty.", UITheme.WARNING); return; }
        log.log("GATE  Approved: " + v.getLicensePlate() + " (" + v.getOwnerName() + ")");
        status("Approved " + v.getLicensePlate() + " — O(1) dequeue.", UITheme.SUCCESS);
        refreshQueue();
        refreshStackVisual();
    }

    private void undoLast() {
        UndoStack.Action action = gate.undoLast();
        if (action == null) { status("Nothing to undo.", UITheme.WARNING); return; }
        log.log("GATE  UNDO → " + action);
        status("Undone: " + action + " — O(1).", UITheme.WARNING);
        refreshQueue();
        refreshStackVisual();
    }

    // ── Refresh ───────────────────────────────────────────────────────────────

    private void refreshQueue() {
        queueVisual.removeAll();
        Vehicle[] queue = gate.getEntryQueue().toArray();
        int total = queue.length;
        queueCountLabel.setText(total + " vehicle" + (total != 1 ? "s" : "") + " waiting");

        if (total == 0) {
            approveNextLabel.setText("No vehicles in queue");
            approveNextLabel.setForeground(UITheme.TEXT_MUTED);
            approveBtn.setEnabled(false);
            approveBtn.setBackground(UITheme.BG_INPUT);

            JLabel empty = UITheme.makeLabel("Queue is empty");
            empty.setAlignmentX(Component.LEFT_ALIGNMENT);
            queueVisual.add(empty);
        } else {
            Vehicle next = queue[0];
            approveNextLabel.setText(next.getLicensePlate() + "  ·  " + next.getOwnerName()
                + "   (" + total + " in queue)");
            approveNextLabel.setForeground(UITheme.SUCCESS);
            approveBtn.setEnabled(true);
            approveBtn.setBackground(new Color(22, 100, 50));

            for (int i = 0; i < total; i++) {
                Vehicle v = queue[i];
                JPanel row = makeRow(
                    (i == 0 ? "▶ FRONT  " : "          ") + v.getLicensePlate(),
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
        int count = actions.length;
        stackCountLabel.setText(count + " action" + (count != 1 ? "s" : ""));

        if (count == 0) {
            JLabel empty = UITheme.makeLabel("Stack is empty");
            empty.setAlignmentX(Component.LEFT_ALIGNMENT);
            stackVisual.add(empty);
        } else {
            for (int i = count - 1; i >= 0; i--) {
                UndoStack.Action a = actions[i];
                boolean isTop = (i == count - 1);
                JPanel row = makeRow(
                    (isTop ? "▶ TOP  " : "         ") + a.type,
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

    private JPanel makeRow(String col1, String col2, String col3, Color color) {
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
            l.setForeground(color);
            p.add(l);
        }
        return p;
    }

    private void status(String msg, Color color) {
        statusLabel.setText(msg);
        statusLabel.setForeground(color);
    }
}
