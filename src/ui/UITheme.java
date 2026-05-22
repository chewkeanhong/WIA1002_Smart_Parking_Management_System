package ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

public class UITheme {

    // ── Backgrounds ──────────────────────────────────────────────────────────
    public static final Color BG_DARK      = new Color(13,  17,  23);
    public static final Color BG_SIDEBAR   = new Color(21,  27,  43);
    public static final Color BG_CARD      = new Color(28,  36,  54);
    public static final Color BG_INPUT     = new Color(18,  23,  36);
    public static final Color BG_TABLE_ROW = new Color(22,  29,  44);
    public static final Color BG_TABLE_ALT = new Color(26,  34,  52);

    // ── Accent ───────────────────────────────────────────────────────────────
    public static final Color ACCENT       = new Color(37,  99, 235);
    public static final Color ACCENT_HOVER = new Color(59, 130, 246);
    public static final Color ACCENT_DIM   = new Color(37,  99, 235,  60);

    // ── Text ─────────────────────────────────────────────────────────────────
    public static final Color TEXT_PRIMARY  = new Color(230, 237, 243);
    public static final Color TEXT_SECONDARY= new Color(139, 148, 158);
    public static final Color TEXT_MUTED    = new Color( 88,  96, 105);

    // ── Status ───────────────────────────────────────────────────────────────
    public static final Color SUCCESS = new Color( 34, 197,  94);
    public static final Color WARNING = new Color(234, 179,   8);
    public static final Color DANGER  = new Color(239,  68,  68);
    public static final Color INFO    = new Color( 56, 189, 248);

    // ── Borders ──────────────────────────────────────────────────────────────
    public static final Color BORDER       = new Color(48, 54, 61);
    public static final Color BORDER_LIGHT = new Color(68, 74, 81);

    // ── Fonts ─────────────────────────────────────────────────────────────────
    public static final Font FONT_BRAND    = new Font("Segoe UI", Font.BOLD,  22);
    public static final Font FONT_TITLE    = new Font("Segoe UI", Font.BOLD,  18);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD,  13);
    public static final Font FONT_BODY     = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL    = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_LABEL    = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_MONO     = new Font("Consolas",  Font.PLAIN, 12);

    // ── Button factory ────────────────────────────────────────────────────────
    public static JButton makeButton(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(TEXT_PRIMARY);
        b.setFont(FONT_BODY);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(8, 18, 8, 18));
        b.setOpaque(true);
        return b;
    }

    public static JButton makePrimaryButton(String text) {
        return makeButton(text, ACCENT);
    }

    public static JButton makeDangerButton(String text) {
        return makeButton(text, DANGER);
    }

    public static JButton makeSecondaryButton(String text) {
        return makeButton(text, BG_CARD);
    }

    // ── Field factory ─────────────────────────────────────────────────────────
    public static JTextField makeTextField(int cols) {
        JTextField f = new JTextField(cols);
        f.setBackground(BG_INPUT);
        f.setForeground(TEXT_PRIMARY);
        f.setCaretColor(TEXT_PRIMARY);
        f.setFont(FONT_BODY);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            new EmptyBorder(6, 10, 6, 10)));
        return f;
    }

    // ── Label factories ───────────────────────────────────────────────────────
    public static JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(TEXT_SECONDARY);
        l.setFont(FONT_LABEL);
        return l;
    }

    public static JLabel makeSectionTitle(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(TEXT_PRIMARY);
        l.setFont(FONT_TITLE);
        return l;
    }

    public static JLabel makeBadge(String text, Color bg) {
        JLabel l = new JLabel(text);
        l.setForeground(Color.WHITE);
        l.setFont(new Font("Segoe UI", Font.BOLD, 10));
        l.setBackground(bg);
        l.setOpaque(true);
        l.setBorder(new EmptyBorder(3, 8, 3, 8));
        return l;
    }

    // ── Card panel factory ────────────────────────────────────────────────────
    public static JPanel makeCard() {
        JPanel p = new JPanel();
        p.setBackground(BG_CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            new EmptyBorder(16, 16, 16, 16)));
        return p;
    }

    public static JPanel makeCard(LayoutManager lm) {
        JPanel p = makeCard();
        p.setLayout(lm);
        return p;
    }

    // ── Stat card factory ─────────────────────────────────────────────────────
    public static JPanel makeStatCard(String label, String value, Color accent) {
        JPanel card = makeCard(new BorderLayout(0, 6));
        JLabel lbl = new JLabel(label.toUpperCase());
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 9));
        lbl.setForeground(TEXT_MUTED);
        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI", Font.BOLD, 28));
        val.setForeground(accent);
        card.add(lbl, BorderLayout.NORTH);
        card.add(val, BorderLayout.CENTER);
        return card;
    }

    // ── Log area factory ──────────────────────────────────────────────────────
    public static JTextArea makeLogArea() {
        JTextArea ta = new JTextArea();
        ta.setBackground(BG_INPUT);
        ta.setForeground(new Color(80, 220, 100));
        ta.setFont(FONT_MONO);
        ta.setEditable(false);
        ta.setBorder(new EmptyBorder(8, 10, 8, 10));
        return ta;
    }

    // ── Scroll pane factory ───────────────────────────────────────────────────
    public static JScrollPane wrapScroll(Component c) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBorder(BorderFactory.createLineBorder(BORDER, 1));
        sp.getViewport().setBackground(BG_DARK);
        sp.getVerticalScrollBar().setBackground(BG_DARK);
        sp.getHorizontalScrollBar().setBackground(BG_DARK);
        return sp;
    }

    // ── Table styling ─────────────────────────────────────────────────────────
    public static void styleTable(JTable table) {
        table.setBackground(BG_TABLE_ROW);
        table.setForeground(TEXT_PRIMARY);
        table.setFont(FONT_BODY);
        table.setGridColor(BORDER);
        table.setRowHeight(32);
        table.setSelectionBackground(ACCENT);
        table.setSelectionForeground(Color.WHITE);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.getTableHeader().setBackground(BG_SIDEBAR);
        table.getTableHeader().setForeground(TEXT_SECONDARY);
        table.getTableHeader().setFont(FONT_SUBTITLE);
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
    }

    // ── Section header ────────────────────────────────────────────────────────
    public static JPanel makeSectionHeader(String title, String badge, Color badgeColor) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        p.setOpaque(false);
        p.add(makeSectionTitle(title));
        if (badge != null) p.add(makeBadge(badge, badgeColor));
        return p;
    }

    // ── Complexity info banner ────────────────────────────────────────────────
    public static JPanel makeComplexityBanner(String text) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(20, 40, 80));
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ACCENT_DIM, 1),
            new EmptyBorder(10, 14, 10, 14)));
        JLabel l = new JLabel("<html>" + text + "</html>");
        l.setForeground(new Color(147, 197, 253));
        l.setFont(FONT_SMALL);
        p.add(l, BorderLayout.CENTER);
        return p;
    }

    // ── Apply global UI defaults ──────────────────────────────────────────────
    public static void applyGlobalDefaults() {
        UIManager.put("Panel.background",          BG_DARK);
        UIManager.put("ScrollPane.background",     BG_DARK);
        UIManager.put("Viewport.background",       BG_DARK);
        UIManager.put("ScrollBar.background",      BG_SIDEBAR);
        UIManager.put("ScrollBar.thumb",           BORDER_LIGHT);
        UIManager.put("ScrollBar.thumbDarkShadow", BORDER);
        UIManager.put("OptionPane.background",     BG_CARD);
        UIManager.put("OptionPane.messageForeground", TEXT_PRIMARY);
    }
}
