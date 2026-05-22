package ui;

import navigation.RouteGraph;
import navigation.DijkstraPathfinder;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;

/**
 * Module 4 — Route Navigation
 * Data Structure: Weighted Directed Graph + Dijkstra's Algorithm
 * Complexity: O((V + E) log V) with a binary-heap priority queue
 */
public class NavigationPanel extends JPanel {

    private final ActivityLog log;
    private final RouteGraph  graph = new RouteGraph();

    private List<String> lastPath = new ArrayList<>();
    private JLabel       statusLabel;
    private JLabel       pathLabel;
    private JTextArea    pathStepsArea;
    private GraphCanvas  canvas;
    private JLabel       statsLabel;

    public NavigationPanel(ActivityLog log) {
        this.log = log;
        graph.initializeLargeMallLayout();

        setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout(0, 16));
        setBorder(new EmptyBorder(28, 28, 28, 28));

        add(buildHeader(),  BorderLayout.NORTH);
        add(buildBody(),    BorderLayout.CENTER);
        add(buildComplexity(), BorderLayout.SOUTH);

        updateStats();
    }

    // ── Header ────────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);

        JPanel left = new JPanel(new GridLayout(2, 1, 0, 2));
        left.setOpaque(false);
        left.add(UITheme.makeSectionTitle("Route Navigation"));
        left.add(UITheme.makeLabel("Data Structure: Weighted Graph + Dijkstra's Algorithm  ·  O((V+E) log V)"));

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
        p.add(buildGraphCard());
        p.add(buildControlCard());
        return p;
    }

    // ── Graph canvas card ─────────────────────────────────────────────────────
    private JPanel buildGraphCard() {
        JPanel card = UITheme.makeCard(new BorderLayout(0, 8));
        card.add(UITheme.makeSectionHeader("Parking Lot Graph", "174+ nodes", UITheme.ACCENT),
                 BorderLayout.NORTH);

        canvas = new GraphCanvas();
        canvas.setBackground(UITheme.BG_INPUT);
        canvas.setPreferredSize(new Dimension(0, 400));
        card.add(canvas, BorderLayout.CENTER);

        statsLabel = UITheme.makeLabel("Nodes: — · Edges: — · Parking slots: —");
        statsLabel.setBorder(new EmptyBorder(4, 0, 0, 0));
        card.add(statsLabel, BorderLayout.SOUTH);

        return card;
    }

    // ── Control card ──────────────────────────────────────────────────────────
    private JPanel buildControlCard() {
        JPanel card = UITheme.makeCard(new BorderLayout(0, 12));
        card.add(UITheme.makeSectionHeader("Dijkstra Pathfinder", "Shortest Path", UITheme.WARNING),
                 BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        // Info
        JLabel info = new JLabel(
            "<html><body style='width:260px;font-family:Segoe UI;font-size:12px;color:#8b949e'>" +
            "The parking lot is modelled as a <b style='color:#e6edf3'>weighted directed graph</b>:<br>" +
            "• <b style='color:#e6edf3'>Nodes</b> = drive lanes, junctions, and individual parking bays<br>" +
            "• <b style='color:#e6edf3'>Edges</b> = road segments with travel-distance weights<br><br>" +
            "Dijkstra greedily expands the lowest-cost frontier using a <b style='color:#e6edf3'>min-heap priority queue</b>, " +
            "guaranteeing the shortest path to the nearest available slot.<br><br>" +
            "<b style='color:#3b82f6'>Start node:</b></body></html>");
        info.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(info);
        content.add(Box.createVerticalStrut(6));

        // Start node field
        JPanel startRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        startRow.setOpaque(false);
        startRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JTextField tfStart = UITheme.makeTextField(16);
        tfStart.setText("MAIN_ENTRANCE");
        JButton findBtn = UITheme.makePrimaryButton("Find Shortest Path");
        startRow.add(tfStart);
        startRow.add(findBtn);
        content.add(startRow);
        content.add(Box.createVerticalStrut(10));

        // Occupation toggle
        JPanel occ = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        occ.setOpaque(false);
        occ.setAlignmentX(Component.LEFT_ALIGNMENT);
        JTextField tfOccNode = UITheme.makeTextField(14);
        tfOccNode.setToolTipText("e.g. W_WALL_01");
        JButton markOcc  = UITheme.makeButton("Mark Occupied",  UITheme.DANGER);
        JButton markFree = UITheme.makeButton("Mark Free",      new Color(20, 80, 30));
        occ.add(UITheme.makeLabel("Slot ID:"));
        occ.add(tfOccNode);
        occ.add(markOcc);
        occ.add(markFree);
        content.add(UITheme.makeLabel("Toggle slot occupancy:"));
        content.add(Box.createVerticalStrut(4));
        content.add(occ);
        content.add(Box.createVerticalStrut(10));

        // Path result
        pathLabel = new JLabel("Path: —");
        pathLabel.setFont(UITheme.FONT_SUBTITLE);
        pathLabel.setForeground(UITheme.WARNING);
        pathLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(pathLabel);
        content.add(Box.createVerticalStrut(6));

        pathStepsArea = UITheme.makeLogArea();
        pathStepsArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        JScrollPane sp = UITheme.wrapScroll(pathStepsArea);
        sp.setAlignmentX(Component.LEFT_ALIGNMENT);
        sp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
        content.add(sp);

        card.add(content, BorderLayout.CENTER);

        // Wire buttons
        findBtn.addActionListener(e -> findPath(tfStart.getText().trim()));
        markOcc.addActionListener(e -> {
            String id = tfOccNode.getText().trim().toUpperCase();
            if (id.isEmpty()) return;
            graph.setOccupancy(id, true);
            log.log("NAV  Marked " + id + " as OCCUPIED");
            status("Marked " + id + " occupied.", UITheme.DANGER);
            canvas.repaint();
        });
        markFree.addActionListener(e -> {
            String id = tfOccNode.getText().trim().toUpperCase();
            if (id.isEmpty()) return;
            graph.setOccupancy(id, false);
            log.log("NAV  Marked " + id + " as FREE");
            status("Marked " + id + " free.", UITheme.SUCCESS);
            canvas.repaint();
        });

        return card;
    }

    // ── Complexity banner ─────────────────────────────────────────────────────
    private JPanel buildComplexity() {
        return UITheme.makeComplexityBanner(
            "<b>Dijkstra Complexity:</b> &nbsp; O((V + E) log V) with a binary min-heap. &nbsp; " +
            "V = nodes (junctions + bays), E = directed road segments. &nbsp; " +
            "The heap always extracts the lowest-cost unvisited node, " +
            "so once a parking slot is popped it is guaranteed to be the nearest reachable one. &nbsp; " +
            "Brute-force BFS ignores weights; Dijkstra respects road distances for accurate guidance."
        );
    }

    // ── Pathfinding action ────────────────────────────────────────────────────
    private void findPath(String start) {
        if (start.isEmpty()) { status("Enter a start node.", UITheme.DANGER); return; }

        long t0 = System.nanoTime();
        lastPath = DijkstraPathfinder.findShortestPathToAvailableSpot(graph, start);
        long elapsed = (System.nanoTime() - t0) / 1000; // µs

        if (lastPath.isEmpty()) {
            pathLabel.setText("Result: Lot is FULL — no available slots.");
            pathLabel.setForeground(UITheme.DANGER);
            pathStepsArea.setText("No path found. All parking slots are occupied.");
            log.log("NAV  Dijkstra from " + start + " — LOT FULL (" + elapsed + " µs)");
            status("Lot full.", UITheme.DANGER);
        } else {
            String dest = lastPath.get(lastPath.size() - 1);
            pathLabel.setText("Target: " + dest + "  (" + (lastPath.size() - 1) + " hops, " + elapsed + " µs)");
            pathLabel.setForeground(UITheme.SUCCESS);

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < lastPath.size(); i++) {
                sb.append(String.format("%2d. %s%n", i + 1, lastPath.get(i)));
            }
            pathStepsArea.setText(sb.toString());
            pathStepsArea.setCaretPosition(0);

            log.log("NAV  Dijkstra: " + start + " → " + dest +
                    " (" + (lastPath.size() - 1) + " hops, " + elapsed + " µs)");
            status("Path found — " + (lastPath.size() - 1) + " hops.", UITheme.SUCCESS);
        }
        canvas.repaint();
    }

    private void updateStats() {
        long total    = graph.getAllNodes().stream().count();
        long parking  = graph.getAllNodes().stream().filter(n -> n.isParkingSlot).count();
        long occupied = graph.getAllNodes().stream().filter(n -> n.isParkingSlot && n.isOccupied).count();
        statsLabel.setText("Nodes: " + total + "  ·  Parking slots: " + parking +
                           "  ·  Occupied: " + occupied + "  ·  Available: " + (parking - occupied));
    }

    private void status(String msg, Color color) {
        statusLabel.setText(msg);
        statusLabel.setForeground(color);
        updateStats();
    }

    // ── Graph canvas (schematic view) ─────────────────────────────────────────
    private class GraphCanvas extends JPanel {

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int W = getWidth(), H = getHeight();
            int pad = 28;

            // ── Draw lot outline ──────────────────────────────────────────────
            g2.setColor(UITheme.BORDER);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRect(pad, pad, W - 2*pad, H - 2*pad);

            // ── Drive lanes ───────────────────────────────────────────────────
            int laneW = 18;
            g2.setColor(new Color(40, 50, 70));
            // North lane (horizontal, near top)
            g2.fillRect(pad, pad, W - 2*pad, laneW);
            // South lane (horizontal, near bottom)
            g2.fillRect(pad, H - pad - laneW, W - 2*pad, laneW);
            // West lane (vertical, left)
            g2.fillRect(pad, pad, laneW, H - 2*pad);
            // East lane (vertical, right)
            g2.fillRect(W - pad - laneW, pad, laneW, H - 2*pad);
            // Center cross aisle (horizontal middle)
            int midY = H / 2;
            g2.fillRect(pad + laneW, midY - laneW/2, W - 2*(pad + laneW), laneW);

            // Lane labels
            g2.setColor(UITheme.TEXT_MUTED);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
            g2.drawString("NORTH LANE",    pad + 4, pad + 12);
            g2.drawString("SOUTH LANE",    pad + 4, H - pad - 5);
            g2.drawString("CENTER AISLE",  pad + laneW + 4, midY + 4);

            // ── Wall slots (perimeter) ────────────────────────────────────────
            drawSlotZone(g2, "W WALL\n(10 slots)", pad + 2, pad + laneW + 10, laneW - 4, H - 2*pad - 2*laneW - 20, UITheme.BG_CARD);
            drawSlotZone(g2, "E WALL\n(10 slots)", W - pad - laneW + 2, pad + laneW + 10, laneW - 4, H - 2*pad - 2*laneW - 20, UITheme.BG_CARD);
            drawSlotZone(g2, "BACK WALL  (10 slots)", pad + laneW + 10, pad + 2, W - 2*(pad + laneW) - 20, laneW - 4, UITheme.BG_CARD);

            // ── Central stacks ────────────────────────────────────────────────
            int stkW = (W - 2*(pad + laneW) - 20) / 6 - 6;
            int topStkH = midY - (pad + laneW) - 10;
            int botStkH = (H - pad - laneW) - (midY + laneW/2) - 10;

            for (int s = 0; s < 6; s++) {
                int sx = pad + laneW + 10 + s * (stkW + 6);
                boolean isPath = false;
                // Highlight if stack is in path
                if (!lastPath.isEmpty()) {
                    for (String node : lastPath) {
                        if (node.startsWith("STK" + (s+1) + "_")) { isPath = true; break; }
                    }
                }

                Color fillColor = isPath ? new Color(37, 99, 235, 80) : new Color(35, 45, 65);
                Color borderColor = isPath ? UITheme.ACCENT : UITheme.BORDER;

                // Upper stacks 1-3
                if (s < 3) drawSlotZone(g2, "STK"+(s+1)+"\n(24 slots)", sx, pad + laneW + 6, stkW, topStkH - 6, fillColor, borderColor);
                // Lower stacks 4-6
                else       drawSlotZone(g2, "STK"+(s+1)+"\n(24 slots)", sx, midY + laneW/2 + 4, stkW, botStkH - 4, fillColor, borderColor);
            }

            // ── Entrance / Exit markers ───────────────────────────────────────
            drawMarker(g2, "ENTRANCE", W/2 - 30, H - pad - laneW/2, UITheme.SUCCESS);
            drawMarker(g2, "EXIT",     W/2 + 30, H - pad - laneW/2, UITheme.DANGER);

            // ── Highlight path ────────────────────────────────────────────────
            if (!lastPath.isEmpty()) {
                g2.setColor(UITheme.SUCCESS);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                String dest = lastPath.get(lastPath.size() - 1);
                g2.drawString("→ " + dest, W/2 - 60, 18);
            }
        }

        private void drawSlotZone(Graphics2D g, String label, int x, int y, int w, int h, Color fill) {
            drawSlotZone(g, label, x, y, w, h, fill, UITheme.BORDER);
        }

        private void drawSlotZone(Graphics2D g, String label, int x, int y, int w, int h, Color fill, Color border) {
            if (w <= 0 || h <= 0) return;
            g.setColor(fill);
            g.fillRect(x, y, w, h);
            g.setColor(border);
            g.setStroke(new BasicStroke(1f));
            g.drawRect(x, y, w, h);

            g.setColor(UITheme.TEXT_SECONDARY);
            g.setFont(new Font("Segoe UI", Font.PLAIN, 8));
            String[] lines = label.split("\n");
            int ly = y + 11;
            for (String line : lines) {
                if (ly < y + h - 2) {
                    g.drawString(line, x + 3, ly);
                    ly += 10;
                }
            }
        }

        private void drawMarker(Graphics2D g, String label, int x, int y, Color color) {
            g.setColor(color);
            g.setStroke(new BasicStroke(2f));
            g.fillOval(x - 6, y - 6, 12, 12);
            g.setFont(new Font("Segoe UI", Font.BOLD, 8));
            g.drawString(label, x - 20, y + 16);
        }
    }
}
