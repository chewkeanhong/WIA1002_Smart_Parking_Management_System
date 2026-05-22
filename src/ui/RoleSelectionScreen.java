package ui;

import gate_control.GateProcessor;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class RoleSelectionScreen extends JFrame {

    private final GateProcessor sharedGate = new GateProcessor();
    private final ActivityLog   sharedLog  = new ActivityLog();

    public RoleSelectionScreen() {
        super("SmartPark — Select Role");
        UITheme.applyGlobalDefaults();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(720, 500);
        setMinimumSize(new Dimension(620, 420));
        setLocationRelativeTo(null);
        setResizable(false);
        buildUI();
        setVisible(true);
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(UITheme.BG_DARK);
        setContentPane(root);
        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildCards(),  BorderLayout.CENTER);
        root.add(buildFooter(), BorderLayout.SOUTH);
    }

    private JPanel buildHeader() {
        JPanel p = new JPanel(new GridLayout(2, 1, 0, 6));
        p.setBackground(UITheme.BG_DARK);
        p.setBorder(new EmptyBorder(44, 0, 24, 0));

        JLabel title = new JLabel("SmartPark", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 34));
        title.setForeground(UITheme.TEXT_PRIMARY);

        JLabel sub = new JLabel("Smart Parking Management System  —  Select your role to continue", SwingConstants.CENTER);
        sub.setFont(UITheme.FONT_BODY);
        sub.setForeground(UITheme.TEXT_SECONDARY);

        p.add(title);
        p.add(sub);
        return p;
    }

    private JPanel buildCards() {
        JPanel p = new JPanel(new GridLayout(1, 2, 24, 0));
        p.setBackground(UITheme.BG_DARK);
        p.setBorder(new EmptyBorder(8, 64, 8, 64));

        p.add(buildRoleCard(
            "Admin",
            "🖥",
            "Dashboard  •  Queue Control  •  All Modules",
            "View system stats, process the vehicle queue, and manage all modules.",
            UITheme.ACCENT,
            this::openAdmin
        ));

        p.add(buildRoleCard(
            "User",
            "🚗",
            "Entry  •  Join Queue",
            "Enter the parking lot queue and check your current position.",
            UITheme.SUCCESS,
            this::openUser
        ));

        return p;
    }

    private JPanel buildRoleCard(String role, String icon, String subtitle,
                                  String desc, Color accent, Runnable action) {
        JPanel card = new JPanel(new BorderLayout(0, 14));
        card.setBackground(UITheme.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER, 1),
            new EmptyBorder(30, 24, 26, 24)
        ));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel iconLabel = new JLabel(icon, SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 42));
        iconLabel.setBorder(new EmptyBorder(0, 0, 4, 0));

        JPanel textPanel = new JPanel(new GridLayout(3, 1, 0, 6));
        textPanel.setOpaque(false);

        JLabel roleLabel = new JLabel(role, SwingConstants.CENTER);
        roleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        roleLabel.setForeground(UITheme.TEXT_PRIMARY);

        JLabel subLabel = new JLabel(subtitle, SwingConstants.CENTER);
        subLabel.setFont(UITheme.FONT_SMALL);
        subLabel.setForeground(accent);

        JLabel descLabel = new JLabel("<html><center>" + desc + "</center></html>", SwingConstants.CENTER);
        descLabel.setFont(UITheme.FONT_BODY);
        descLabel.setForeground(UITheme.TEXT_SECONDARY);

        textPanel.add(roleLabel);
        textPanel.add(subLabel);
        textPanel.add(descLabel);

        JButton btn = UITheme.makeButton("Enter as " + role + "  →", accent);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.addActionListener(e -> action.run());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnPanel.setOpaque(false);
        btnPanel.add(btn);

        card.add(iconLabel, BorderLayout.NORTH);
        card.add(textPanel, BorderLayout.CENTER);
        card.add(btnPanel,  BorderLayout.SOUTH);

        card.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                card.setBackground(new Color(36, 46, 66));
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(accent, 1),
                    new EmptyBorder(30, 24, 26, 24)
                ));
            }
            @Override public void mouseExited(MouseEvent e) {
                card.setBackground(UITheme.BG_CARD);
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(UITheme.BORDER, 1),
                    new EmptyBorder(30, 24, 26, 24)
                ));
            }
            @Override public void mouseClicked(MouseEvent e) { action.run(); }
        });

        return card;
    }

    private JPanel buildFooter() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER));
        p.setBackground(UITheme.BG_DARK);
        p.setBorder(new EmptyBorder(6, 0, 18, 0));
        JLabel l = new JLabel("WIA1002 Data Structures — Group Assignment");
        l.setFont(UITheme.FONT_SMALL);
        l.setForeground(UITheme.TEXT_MUTED);
        p.add(l);
        return p;
    }

    private void openAdmin() {
        new MainFrame(sharedGate, sharedLog);
    }

    private void openUser() {
        new UserFrame(sharedGate, sharedLog);
    }
}
