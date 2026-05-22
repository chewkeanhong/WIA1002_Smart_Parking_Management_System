package ui;

import gate_control.GateProcessor;
import models.Vehicle;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class UserFrame extends JFrame {

    private final GateProcessor gate;
    private final ActivityLog   log;

    private JTextField tfPlate, tfOwner;
    private JLabel     statusLabel, queueCountLabel;
    private JPanel     queueListPanel;
    private Timer      refreshTimer;

    private static final SimpleDateFormat SDF = new SimpleDateFormat("HH:mm:ss");

    public UserFrame(GateProcessor gate, ActivityLog log) {
        super("SmartPark — User Entry");
        this.gate = gate;
        this.log  = log;

        UITheme.applyGlobalDefaults();
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(560, 680);
        setMinimumSize(new Dimension(480, 560));
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.BG_DARK);

        buildUI();
        refreshQueue();

        refreshTimer = new Timer(2000, e -> refreshQueue());
        refreshTimer.start();

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosed(java.awt.event.WindowEvent e) {
                refreshTimer.stop();
            }
        });

        setVisible(true);
    }

    private void buildUI() {
        setLayout(new BorderLayout(0, 0));
        add(buildTopBar(),     BorderLayout.NORTH);
        add(buildMainContent(), BorderLayout.CENTER);
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout(12, 0));
        bar.setBackground(UITheme.BG_SIDEBAR);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER),
            new EmptyBorder(16, 24, 16, 24)
        ));

        JLabel title = new JLabel("SmartPark");
        title.setFont(UITheme.FONT_BRAND);
        title.setForeground(UITheme.TEXT_PRIMARY);

        JLabel badge = UITheme.makeBadge("USER MODE", UITheme.SUCCESS);

        bar.add(title, BorderLayout.WEST);
        bar.add(badge, BorderLayout.EAST);
        return bar;
    }

    private JPanel buildMainContent() {
        JPanel p = new JPanel(new BorderLayout(0, 16));
        p.setBackground(UITheme.BG_DARK);
        p.setBorder(new EmptyBorder(24, 24, 24, 24));
        p.add(buildEnqueueCard(), BorderLayout.NORTH);
        p.add(buildQueueCard(),   BorderLayout.CENTER);
        return p;
    }

    // ── Enqueue form ─────────────────────────────────────────────────────────

    private JPanel buildEnqueueCard() {
        JPanel card = UITheme.makeCard(new BorderLayout(0, 16));

        JPanel hdr = new JPanel(new GridLayout(2, 1, 0, 2));
        hdr.setOpaque(false);
        JLabel title = UITheme.makeSectionTitle("Vehicle Entry");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        JLabel sub = UITheme.makeLabel("Join the parking queue — O(1) enqueue");
        hdr.add(title);
        hdr.add(sub);
        card.add(hdr, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill   = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(6, 4, 6, 4);

        tfPlate = UITheme.makeTextField(20);
        tfOwner = UITheme.makeTextField(20);
        tfPlate.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        tfOwner.setFont(new Font("Segoe UI", Font.PLAIN, 15));

        addRow(form, gc, 0, "Licence Plate", tfPlate);
        addRow(form, gc, 1, "Owner Name",    tfOwner);
        card.add(form, BorderLayout.CENTER);

        JButton joinBtn = UITheme.makeButton("Join Queue  →", UITheme.SUCCESS);
        joinBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        joinBtn.setForeground(UITheme.BG_DARK);
        joinBtn.addActionListener(e -> enqueue());

        statusLabel = new JLabel(" ");
        statusLabel.setFont(UITheme.FONT_SMALL);
        statusLabel.setForeground(UITheme.SUCCESS);

        JPanel bottom = new JPanel(new BorderLayout(10, 0));
        bottom.setOpaque(false);
        bottom.add(joinBtn,     BorderLayout.WEST);
        bottom.add(statusLabel, BorderLayout.CENTER);
        card.add(bottom, BorderLayout.SOUTH);

        return card;
    }

    private void addRow(JPanel form, GridBagConstraints gc, int row, String label, JTextField field) {
        gc.gridx = 0; gc.gridy = row; gc.weightx = 0;
        JLabel lbl = UITheme.makeLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setPreferredSize(new Dimension(110, 28));
        form.add(lbl, gc);
        gc.gridx = 1; gc.weightx = 1;
        form.add(field, gc);
    }

    // ── Queue display ─────────────────────────────────────────────────────────

    private JPanel buildQueueCard() {
        JPanel card = UITheme.makeCard(new BorderLayout(0, 10));

        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setOpaque(false);
        queueCountLabel = UITheme.makeLabel("0 vehicles waiting");
        queueCountLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        hdr.add(UITheme.makeSectionHeader("Current Queue", "LIVE", UITheme.SUCCESS), BorderLayout.WEST);
        hdr.add(queueCountLabel, BorderLayout.EAST);
        card.add(hdr, BorderLayout.NORTH);

        queueListPanel = new JPanel();
        queueListPanel.setOpaque(false);
        queueListPanel.setLayout(new BoxLayout(queueListPanel, BoxLayout.Y_AXIS));

        JScrollPane sp = UITheme.wrapScroll(queueListPanel);
        card.add(sp, BorderLayout.CENTER);

        JLabel hint = UITheme.makeLabel("Refreshes every 2 s — admin processes the queue from the dashboard");
        hint.setFont(UITheme.FONT_SMALL);
        hint.setForeground(UITheme.TEXT_MUTED);
        hint.setBorder(new EmptyBorder(6, 0, 0, 0));
        card.add(hint, BorderLayout.SOUTH);

        return card;
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    private void enqueue() {
        String plate = tfPlate.getText().trim().toUpperCase();
        String owner = tfOwner.getText().trim();
        if (plate.isEmpty() || owner.isEmpty()) {
            setStatus("Please fill in both fields.", UITheme.DANGER);
            return;
        }
        Vehicle v = new Vehicle(plate, owner, System.currentTimeMillis());
        gate.vehicleArrives(v);
        log.log("USER  Joined queue: " + plate + " (" + owner + ")");
        setStatus("Joined the queue successfully — O(1).", UITheme.SUCCESS);
        tfPlate.setText("");
        tfOwner.setText("");
        refreshQueue();
    }

    private void refreshQueue() {
        queueListPanel.removeAll();
        Vehicle[] queue = gate.getEntryQueue().toArray();
        int count = queue.length;
        queueCountLabel.setText(count + " vehicle" + (count != 1 ? "s" : "") + " waiting");

        if (count == 0) {
            JLabel empty = UITheme.makeLabel("Queue is empty — be the first to join!");
            empty.setAlignmentX(Component.LEFT_ALIGNMENT);
            queueListPanel.add(Box.createVerticalStrut(8));
            queueListPanel.add(empty);
        } else {
            for (int i = 0; i < count; i++) {
                queueListPanel.add(makeRow(i + 1, queue[i], i == 0));
                queueListPanel.add(Box.createVerticalStrut(4));
            }
        }
        queueListPanel.revalidate();
        queueListPanel.repaint();
    }

    private JPanel makeRow(int pos, Vehicle v, boolean isNext) {
        JPanel p = new JPanel(new BorderLayout(10, 0));
        p.setBackground(isNext ? new Color(22, 60, 35) : UITheme.BG_INPUT);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(isNext ? UITheme.SUCCESS : UITheme.BORDER, 1),
            new EmptyBorder(10, 14, 10, 14)
        ));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);

        JLabel posL = new JLabel("#" + pos);
        posL.setFont(new Font("Segoe UI", Font.BOLD, 13));
        posL.setForeground(isNext ? UITheme.SUCCESS : UITheme.TEXT_MUTED);
        posL.setPreferredSize(new Dimension(28, 18));

        JLabel plateL = new JLabel(v.getLicensePlate());
        plateL.setFont(UITheme.FONT_MONO);
        plateL.setForeground(isNext ? UITheme.SUCCESS : UITheme.TEXT_PRIMARY);

        JLabel ownerL = new JLabel(v.getOwnerName());
        ownerL.setFont(UITheme.FONT_BODY);
        ownerL.setForeground(UITheme.TEXT_SECONDARY);

        JLabel timeL = new JLabel(SDF.format(new Date(v.getEntryTime())));
        timeL.setFont(UITheme.FONT_SMALL);
        timeL.setForeground(UITheme.TEXT_MUTED);

        left.add(posL); left.add(plateL); left.add(ownerL); left.add(timeL);
        p.add(left, BorderLayout.CENTER);

        if (isNext) p.add(UITheme.makeBadge("NEXT", UITheme.SUCCESS), BorderLayout.EAST);

        return p;
    }

    private void setStatus(String msg, Color color) {
        statusLabel.setText(msg);
        statusLabel.setForeground(color);
    }
}
