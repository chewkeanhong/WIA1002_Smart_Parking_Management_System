package ui;

import management.RecordManager;
import models.ParkingMap;
import models.ParkingSlot;
import models.Vehicle;
import navigation.DijkstraPathfinder;
import navigation.RouteGraph;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Module 4 — Route Navigation
 * Big lot map at the centre. Bottom strip of clickable vehicle bubbles —
 * one click selects a vehicle and instantly draws its route from the entrance.
 */
public class NavigationPanel extends JPanel {

    private static final String DEFAULT_ACCESS_NODE = "ENTRANCE";

    private final ActivityLog   log;
    private final RecordManager records;
    private final ParkingMap    parkingMap;
    private final RouteGraph    graph = new RouteGraph();

    private LotMapCanvas         canvas;
    private JLabel               statusLabel;
    private JPanel               bubbleContainer;
    private final Map<String, JPanel> bubblesByPlate = new HashMap<>();

    private List<String>         currentPath = new ArrayList<>();
    private List<String>         entranceToSlotPath = new ArrayList<>();
    private String               lastVehicleSignature = "";
    private String               routedPlate = null;
    private String               routedSlot  = null;
    private String               routedStartNode = DEFAULT_ACCESS_NODE;
    private String               lastDirections = "";
    private JTextArea            directionsArea;

    public NavigationPanel(ActivityLog log, RecordManager records, ParkingMap parkingMap) {
        this.log        = log;
        this.records    = records;
        this.parkingMap = parkingMap;
        graph.initializeDashboardLayout();

        setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout(0, 16));
        setBorder(new EmptyBorder(28, 28, 28, 28));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(),   BorderLayout.CENTER);

        rebuildBubbles();

        new Timer(1000, e -> {
            syncVehicles();
            if (canvas != null) canvas.repaint();
        }).start();
    }

    // ── Header ────────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);

        JPanel left = new JPanel(new GridLayout(2, 1, 0, 2));
        left.setOpaque(false);
        left.add(UITheme.makeSectionTitle("Route Navigation"));
        left.add(UITheme.makeLabel("Click a vehicle bubble to see its route from the entrance to its slot."));

        statusLabel = new JLabel(" ");
        statusLabel.setFont(UITheme.FONT_SMALL);
        statusLabel.setForeground(UITheme.SUCCESS);
        statusLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        p.add(left,        BorderLayout.WEST);
        p.add(statusLabel, BorderLayout.EAST);
        return p;
    }

    // ── Body: big map (center) + directions (east) + bubble strip (south) ──────
    private JPanel buildBody() {
        JPanel body = new JPanel(new BorderLayout(0, 14));
        body.setOpaque(false);

        JPanel centerRow = new JPanel(new BorderLayout(14, 0));
        centerRow.setOpaque(false);
        centerRow.add(buildMapCard(),         BorderLayout.CENTER);
        centerRow.add(buildDirectionsPanel(), BorderLayout.EAST);

        body.add(centerRow,          BorderLayout.CENTER);
        body.add(buildBubbleStrip(), BorderLayout.SOUTH);
        return body;
    }

    private JPanel buildMapCard() {
        JPanel card = UITheme.makeCard(new BorderLayout(0, 10));
        card.add(UITheme.makeSectionTitle("Lot Map"), BorderLayout.NORTH);
        canvas = new LotMapCanvas();
        card.add(canvas, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildDirectionsPanel() {
        JPanel card = UITheme.makeCard(new BorderLayout(0, 10));
        card.setPreferredSize(new Dimension(300, 0));

        JLabel title = new JLabel("Directions");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.ACCENT_HOVER);
        card.add(title, BorderLayout.NORTH);

        directionsArea = new JTextArea("Select a vehicle bubble\nto see its directions.");
        directionsArea.setEditable(false);
        directionsArea.setLineWrap(true);
        directionsArea.setWrapStyleWord(true);
        directionsArea.setFont(UITheme.FONT_BODY);
        directionsArea.setBackground(UITheme.BG_CARD);
        directionsArea.setForeground(UITheme.TEXT_PRIMARY);
        directionsArea.setBorder(new EmptyBorder(8, 4, 8, 4));

        JScrollPane scroll = new JScrollPane(directionsArea,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(UITheme.BG_CARD);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildBubbleStrip() {
        JPanel card = UITheme.makeCard(new BorderLayout(0, 8));
        card.add(UITheme.makeSectionTitle("Vehicles"), BorderLayout.NORTH);

        bubbleContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        bubbleContainer.setOpaque(false);

        JScrollPane scroll = new JScrollPane(bubbleContainer,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(UITheme.BG_CARD);
        scroll.setPreferredSize(new Dimension(0, 170));

        card.add(scroll, BorderLayout.CENTER);
        card.setPreferredSize(new Dimension(0, 230));
        return card;
    }

    // ── Bubble rebuild + selection ────────────────────────────────────────────
    private void rebuildBubbles() {
        bubbleContainer.removeAll();
        bubblesByPlate.clear();
        parkingMap.clearOccupancy();
        graph.clearOccupancy();

        List<Vehicle> vs = records.getAllVehiclesList();
        if (vs.isEmpty()) {
            bubbleContainer.add(buildPlaceholderBubble());
        } else {
            for (Vehicle v : vs) {
                if (v.getAssignedSlotId() != null) {
                    parkingMap.markOccupied(v.getAssignedSlotId());
                    graph.setOccupancy(v.getAssignedSlotId(), true);
                }
                JPanel b = buildVehicleBubble(v);
                bubblesByPlate.put(v.getLicensePlate(), b);
                bubbleContainer.add(b);
            }
            applySelectionBorders();
        }
        lastVehicleSignature = signatureOf(vs);
        bubbleContainer.revalidate();
        bubbleContainer.repaint();
    }

    private JPanel buildPlaceholderBubble() {
        JPanel b = new JPanel(new GridBagLayout());
        b.setPreferredSize(new Dimension(280, 110));
        b.setBackground(UITheme.BG_CARD);
        b.setBorder(BorderFactory.createDashedBorder(UITheme.BORDER, 1.5f, 4f, 4f, true));
        JLabel l = UITheme.makeLabel("No vehicles yet — add and approve in Entry/Exit.");
        l.setForeground(UITheme.TEXT_MUTED);
        b.add(l);
        return b;
    }

    private JPanel buildVehicleBubble(Vehicle v) {
        JPanel b = new JPanel(null);
        b.setPreferredSize(new Dimension(250, 140));
        b.setBackground(UITheme.BG_CARD);
        b.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1));

        JLabel plate = new JLabel(v.getLicensePlate());
        plate.setFont(new Font("Segoe UI", Font.BOLD, 22));
        plate.setForeground(UITheme.TEXT_PRIMARY);
        plate.setBounds(14, 12, 222, 28);

        JLabel owner = new JLabel(v.getOwnerName());
        owner.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        owner.setForeground(UITheme.TEXT_SECONDARY);
        owner.setBounds(14, 46, 222, 22);

        String slotId = v.getAssignedSlotId();
        JLabel slot;
        if (slotId != null) {
            slot = new JLabel("Slot  " + slotId);
            slot.setFont(new Font("Consolas", Font.BOLD, 17));
            slot.setForeground(UITheme.SUCCESS);
        } else {
            slot = new JLabel("(no slot assigned)");
            slot.setFont(new Font("Segoe UI", Font.PLAIN, 15));
            slot.setForeground(UITheme.TEXT_MUTED);
        }
        slot.setBounds(14, 98, 222, 24);

        b.add(plate);
        b.add(owner);
        b.add(slot);

        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        MouseAdapter click = new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { selectVehicle(v); }
            @Override public void mouseEntered(MouseEvent e) {
                if (!v.getLicensePlate().equals(routedPlate)) {
                    b.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_LIGHT, 1));
                }
            }
            @Override public void mouseExited(MouseEvent e) {
                if (!v.getLicensePlate().equals(routedPlate)) {
                    b.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1));
                }
            }
        };
        b.addMouseListener(click);
        for (Component c : b.getComponents()) c.addMouseListener(click);
        return b;
    }

    private void applySelectionBorders() {
        for (Map.Entry<String, JPanel> e : bubblesByPlate.entrySet()) {
            boolean sel = e.getKey().equals(routedPlate);
            e.getValue().setBorder(sel
                ? BorderFactory.createLineBorder(UITheme.ACCENT, 2)
                : BorderFactory.createLineBorder(UITheme.BORDER, 1));
        }
    }

    // ── Click → route ────────────────────────────────────────────────────────
    private void selectVehicle(Vehicle v) {
        // Clicking the already-selected bubble deselects it
        if (v.getLicensePlate().equals(routedPlate)) {
            routedPlate    = null;
            routedSlot     = null;
            lastDirections = "";
            currentPath    = new java.util.ArrayList<>();
            if (directionsArea != null)
                directionsArea.setText("Select a vehicle bubble\nto see its directions.");
            status(" ", UITheme.TEXT_MUTED);
            applySelectionBorders();
            canvas.repaint();
            return;
        }

        String startNode = DEFAULT_ACCESS_NODE;
        routedStartNode = startNode;

        String slotId = v.getAssignedSlotId();
        if (slotId == null) {
            slotId = assignNearestSlot(v);
            if (slotId == null) { status("Lot is full — no free slot to route to.", UITheme.DANGER); return; }
            log.log("NAV  Auto-assigned " + slotId + " to " + v.getLicensePlate() +
                " via Entrance");
        }

        String norm = slotId.trim().toUpperCase();
        if (graph.getNode(norm) == null) {
            status("Slot '" + slotId + "' is not on the parking map.", UITheme.DANGER);
            return;
        }

        long t0 = System.nanoTime();
        entranceToSlotPath = DijkstraPathfinder.findShortestPath(graph, DEFAULT_ACCESS_NODE, norm);
        currentPath = buildFullRoutePath(DEFAULT_ACCESS_NODE, norm);
        long us = (System.nanoTime() - t0) / 1000;

        if (currentPath.isEmpty()) {
            status("No path found to " + norm + ".", UITheme.DANGER);
            return;
        }

        int totalDist = DijkstraPathfinder.calculatePathCost(graph, currentPath);
        routedPlate    = v.getLicensePlate();
        routedSlot     = norm;
        lastDirections = buildDirections(currentPath, DEFAULT_ACCESS_NODE, totalDist);
        if (directionsArea != null) directionsArea.setText(lastDirections);

        status("Route to " + norm + "  (" + (currentPath.size() - 1) + " hops · " + us + " µs)",
               UITheme.SUCCESS);
                            log.log("NAV  Dijkstra Entrance → " + norm + " for " + v.getLicensePlate()
              + "  (" + (currentPath.size() - 1) + " hops, " + us + " µs)");

        applySelectionBorders();
        canvas.repaint();
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
        List<String> path = DijkstraPathfinder.findShortestPathToAvailableSpot(graph, startNode);
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

    // ── Auto-sync (1 s tick) ─────────────────────────────────────────────────
    private void syncVehicles() {
        String sig = signatureOf(records.getAllVehiclesList());
        if (!sig.equals(lastVehicleSignature)) {
            rebuildBubbles();
        }
        invalidateRouteIfStale();
    }

    private void invalidateRouteIfStale() {
        if (routedPlate == null || currentPath.isEmpty()) return;
        Vehicle v = records.findVehicleByPlate(routedPlate);
        boolean stale = (v == null)
                     || v.getAssignedSlotId() == null
                     || !routedSlot.equalsIgnoreCase(v.getAssignedSlotId());
        if (stale) {
            currentPath    = new ArrayList<>();
            routedPlate    = null;
            routedSlot     = null;
            lastDirections = "";
            if (directionsArea != null) directionsArea.setText("Select a vehicle bubble\nto see its directions.");
            status("Route cleared — vehicle no longer assigned to that slot.", UITheme.WARNING);
            applySelectionBorders();
            if (canvas != null) canvas.repaint();
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

    // ── Path → human directions ──────────────────────────────────────────────
    private String buildDirections(List<String> path, String startNode, int distMetres) {
        if (path.size() < 2) return "Already at destination.";
        StringBuilder sb = new StringBuilder();

        int step = 1;
        sb.append(step++)
          .append(". Depart ")
          .append(prettyAccessPoint(startNode))
          .append('.').append('\n');

        sb.append(step++).append(". Follow the blue route to your assigned parking slot.").append('\n');

        String lastFamily = "";
        for (int i = 1; i < path.size() - 1; i++) {
            String id = path.get(i);
            String family = familyOf(id);
            String desc = describeWaypoint(id);
            if (desc == null) continue;
            if (family.equals(lastFamily)) continue;
            sb.append(step++).append(". ").append(desc).append('\n');
            lastFamily = family;
        }
        sb.append(step).append(". Arrive at ").append(path.get(path.size() - 1)).append('.');
        return sb.toString();
    }

    private List<String> buildFullRoutePath(String startNode, String slotId) {
        return DijkstraPathfinder.findShortestPath(graph, startNode, slotId);
    }

    private String prettyAccessPoint(String nodeId) {
        if (nodeId == null) {
            return "Entrance";
        }

        switch (nodeId) {
            case "ENTRANCE": return "Entrance";
            case "GATE_A": return "Gate A";
            case "GATE_B": return "Gate B";
            case "GATE_C": return "Gate C";
            default: return "Entrance";
        }
    }

    private String familyOf(String id) {
        if (id.startsWith("TOP_C")) return "TOP";
        if (id.startsWith("BOT_C")) return "BOT";
        if (id.startsWith("MID_C")) return "MID";
        return id;
    }

    private String describeWaypoint(String id) {
        switch (id) {
            case "NW": return "Reach the north-west corner.";
            case "NE": return "Reach the north-east corner.";
            case "SW": return "Turn at the south-west corner.";
            case "SE": return "Turn at the south-east corner.";
            case "GATE_A": return "Pass Gate A (left side).";
            case "GATE_B": return "Pass Gate B (right side).";
            case "GATE_C": return "Pass Gate C (top).";
            default: break;
        }
        if (id.startsWith("TOP_C")) return "Continue along the top road.";
        if (id.startsWith("BOT_C")) return "Continue along the bottom road.";
        if (id.startsWith("MID_C")) return "Enter the central aisle.";
        return null;
    }

    private void status(String msg, Color color) {
        statusLabel.setText(msg);
        statusLabel.setForeground(color);
    }

    // ── Canvas: dashboard-style lot with road frame and route polyline ───────
    private class LotMapCanvas extends JPanel {
        private static final double LOGICAL_W = 1000.0;
        private static final double LOGICAL_H = 640.0;

        LotMapCanvas() {
            setBackground(UITheme.BG_INPUT);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int cw = getWidth(), ch = getHeight();
            double s   = Math.min(cw / LOGICAL_W, ch / LOGICAL_H);
            double ox  = (cw - LOGICAL_W * s) / 2.0;
            double oy  = (ch - LOGICAL_H * s) / 2.0;

            // Slot-block backgrounds first (darker areas the slot cards sit on).
            g2.setColor(new Color(22, 24, 29));
            fillLogicalRect(g2, 80, 110, 920, 270, s, ox, oy);
            fillLogicalRect(g2, 80, 370, 920, 530, s, ox, oy);

            // Asphalt only on the road bands — leaves canvas margin outside the roads
            // for gate labels. Top/bottom roads are trimmed so they don't extend over the side roads.
            double laneThk = 22;
            double half    = laneThk / 2;
            g2.setColor(new Color(32, 35, 42));
            fillLogicalRect(g2, 50 + half,        60 - half,  950 - half,        60 + half, s, ox, oy); // top road (trimmed)
            fillLogicalRect(g2, 50 + half,       580 - half,  950 - half,       580 + half, s, ox, oy); // bottom road (trimmed)
            fillLogicalRect(g2, 50 - half,        60 - half,   50 + half,       580 + half, s, ox, oy); // left road (extends through corners)
            fillLogicalRect(g2, 950 - half,       60 - half,  950 + half,       580 + half, s, ox, oy); // right road (extends through corners)
            fillLogicalRect(g2, 50,              320 - half,  950,              320 + half, s, ox, oy); // central aisle

            drawDash(g2, 50 + half, 60,  950 - half, 60,  s, ox, oy);
            drawDash(g2, 50 + half, 580, 950 - half, 580, s, ox, oy);
            drawDash(g2, 50,        60,  50,         580, s, ox, oy);
            drawDash(g2, 950,       60,  950,        580, s, ox, oy);
            drawDash(g2, 50,        320, 950,        320, s, ox, oy);

            drawSlots(g2, s, ox, oy);

            // Gate labels — placed outside the road, in the canvas margin
            drawGateLabel(g2, "Gate A",  20, 320, s, ox, oy);
            drawGateLabel(g2, "Gate B", 980, 320, s, ox, oy);
            drawGateLabel(g2, "Gate C", 500,  30, s, ox, oy);

            if (currentPath != null && currentPath.size() >= 2) {
                drawPath(g2, s, ox, oy);
            }

            drawPin(g2, "ENTRANCE", 470, 580, UITheme.SUCCESS,        s, ox, oy);
            drawPin(g2, "EXIT",     530, 580, new Color(220, 60, 60), s, ox, oy);

            g2.dispose();
        }

        private void fillLogicalRect(Graphics2D g, double x1, double y1, double x2, double y2,
                                     double s, double ox, double oy) {
            int xa = toCanvas(x1, s, ox), ya = toCanvas(y1, s, oy);
            int xb = toCanvas(x2, s, ox), yb = toCanvas(y2, s, oy);
            g.fillRect(xa, ya, xb - xa, yb - ya);
        }

        private void drawDash(Graphics2D g, double x1, double y1, double x2, double y2,
                              double s, double ox, double oy) {
            g.setColor(new Color(210, 175, 0, 180));
            Stroke prev = g.getStroke();
            g.setStroke(new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                10f, new float[]{ (float)(10*s), (float)(8*s) }, 0f));
            g.drawLine(toCanvas(x1, s, ox), toCanvas(y1, s, oy),
                       toCanvas(x2, s, ox), toCanvas(y2, s, oy));
            g.setStroke(prev);
        }

        private void drawSlots(Graphics2D g, double s, double ox, double oy) {
            double cellW = 62, cellH = 54;
            for (int i = 0; i < 40; i++) {
                String id = String.format("A%02d", i + 1);
                RouteGraph.Node n = graph.getNode(id);
                if (n == null) continue;
                int cx = toCanvas(n.x, s, ox);
                int cy = toCanvas(n.y, s, oy);
                int w  = (int)(cellW * s);
                int h  = (int)(cellH * s);
                boolean occ = parkingMap.isOccupied(i);
                boolean isDest = !currentPath.isEmpty()
                              && currentPath.get(currentPath.size() - 1).equals(id);

                // Fill — always use original occupied/free colour
                g.setColor(occ ? new Color(60, 18, 18) : new Color(14, 80, 36));
                g.fillRoundRect(cx - w/2, cy - h/2, w, h, 8, 8);

                // Normal border (untouched)
                g.setColor(occ ? new Color(150, 40, 40) : new Color(22, 130, 58));
                g.setStroke(new BasicStroke(1.4f));
                g.drawRoundRect(cx - w/2, cy - h/2, w, h, 8, 8);

                // Destination: extra outer ring drawn OUTSIDE the slot — never touches fill or text
                if (isDest) {
                    int pad = 5;
                    g.setColor(UITheme.ACCENT_HOVER);
                    g.setStroke(new BasicStroke(3.0f));
                    g.drawRoundRect(cx - w/2 - pad, cy - h/2 - pad, w + pad*2, h + pad*2, 14, 14);
                }

                // Text — unchanged
                g.setColor(occ ? new Color(220, 80, 80) : UITheme.SUCCESS);
                g.setFont(new Font("Segoe UI", Font.BOLD, (int)Math.max(10, 12 * s)));
                FontMetrics fm = g.getFontMetrics();
                g.drawString(id, cx - fm.stringWidth(id)/2, cy + fm.getAscent()/2 - 2);
            }
        }


        private void drawGateLabel(Graphics2D g, String text, double lx, double ly,
                                   double s, double ox, double oy) {
            g.setColor(UITheme.TEXT_PRIMARY);
            g.setFont(new Font("Segoe UI", Font.BOLD, (int)Math.max(10, 12 * s)));
            FontMetrics fm = g.getFontMetrics();
            int x = toCanvas(lx, s, ox) - fm.stringWidth(text)/2;
            int y = toCanvas(ly, s, oy) + fm.getAscent()/2 - 2;
            g.drawString(text, x, y);
        }

        private void drawPath(Graphics2D g, double s, double ox, double oy) {
            drawColoredPath(g, currentPath, s, ox, oy, new Color(59, 130, 246), -5.0);
        }

        private void drawColoredPath(Graphics2D g, List<String> path, double s, double ox, double oy,
                                     Color color, double offsetPx) {
            if (path == null || path.size() < 2) {
                return;
            }

            double[] xs = new double[path.size()];
            double[] ys = new double[path.size()];
            for (int i = 0; i < path.size(); i++) {
                RouteGraph.Node n = graph.getNode(path.get(i));
                if (n == null) { xs[i] = ys[i] = 0; continue; }
                xs[i] = toCanvas(n.x, s, ox);
                ys[i] = toCanvas(n.y, s, oy);
            }

            int[] drawX = new int[path.size()];
            int[] drawY = new int[path.size()];
            for (int i = 0; i < path.size(); i++) {
                double nx = 0.0;
                double ny = 0.0;

                if (i > 0) {
                    double dx = xs[i] - xs[i - 1];
                    double dy = ys[i] - ys[i - 1];
                    double len = Math.hypot(dx, dy);
                    if (len > 0.0) {
                        nx += -dy / len;
                        ny +=  dx / len;
                    }
                }

                if (i < path.size() - 1) {
                    double dx = xs[i + 1] - xs[i];
                    double dy = ys[i + 1] - ys[i];
                    double len = Math.hypot(dx, dy);
                    if (len > 0.0) {
                        nx += -dy / len;
                        ny +=  dx / len;
                    }
                }

                double len = Math.hypot(nx, ny);
                if (len > 0.0) {
                    nx = (nx / len) * offsetPx;
                    ny = (ny / len) * offsetPx;
                }

                drawX[i] = (int) Math.round(xs[i] + nx);
                drawY[i] = (int) Math.round(ys[i] + ny);
            }

            g.setColor(new Color(0, 0, 0, 130));
            g.setStroke(new BasicStroke((float)Math.max(7, 8*s),
                BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawPolyline(drawX, drawY, drawX.length);
            g.setColor(color);
            g.setStroke(new BasicStroke((float)Math.max(4, 5*s),
                BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawPolyline(drawX, drawY, drawX.length);
            g.setColor(color.brighter());
            g.setStroke(new BasicStroke((float)Math.max(1.5, 1.8*s),
                BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawPolyline(drawX, drawY, drawX.length);

        }

        private void drawPin(Graphics2D g, String label, double lx, double ly, Color color,
                             double s, double ox, double oy) {
            int x = toCanvas(lx, s, ox);
            int y = toCanvas(ly, s, oy);
            int r = (int)Math.max(6, 8 * s);
            g.setColor(color);
            g.fillOval(x - r, y - r, r*2, r*2);
            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(1.5f));
            g.drawOval(x - r, y - r, r*2, r*2);
            g.setFont(new Font("Segoe UI", Font.BOLD, (int)Math.max(9, 10 * s)));
            FontMetrics fm = g.getFontMetrics();
            g.setColor(UITheme.TEXT_PRIMARY);
            g.drawString(label, x - fm.stringWidth(label)/2, y + r + (int)(13 * s));
        }

        private int toCanvas(double v, double s, double off) {
            return (int)Math.round(off + v * s);
        }
    }
}
