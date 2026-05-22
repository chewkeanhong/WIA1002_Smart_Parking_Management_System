package ui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.List;

public class LogsPanel extends JPanel {

    private final ActivityLog log;
    private final JTextArea   logArea;
    private final JLabel      countLabel;

    public LogsPanel(ActivityLog log) {
        this.log     = log;
        this.logArea = UITheme.makeLogArea();
        this.countLabel = UITheme.makeLabel("0 entries");

        setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout(0, 16));
        setBorder(new EmptyBorder(28, 28, 28, 28));

        add(buildHeader(),  BorderLayout.NORTH);
        add(buildBody(),    BorderLayout.CENTER);

        // Refresh whenever a new log entry arrives
        log.addListener(this::refresh);
    }

    // ── Header ────────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);

        JPanel left = new JPanel(new GridLayout(2, 1, 0, 2));
        left.setOpaque(false);
        JLabel title = UITheme.makeSectionTitle("Activity Logs");
        JLabel sub   = UITheme.makeLabel("Timestamped record of all system events");
        left.add(title);
        left.add(sub);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(countLabel);

        JButton clearBtn = UITheme.makeDangerButton("Clear");
        clearBtn.addActionListener(e -> {
            log.clear();
            logArea.setText("");
            countLabel.setText("0 entries");
        });
        right.add(clearBtn);

        p.add(left,  BorderLayout.WEST);
        p.add(right, BorderLayout.EAST);
        return p;
    }

    // ── Body ──────────────────────────────────────────────────────────────────
    private JPanel buildBody() {
        JPanel card = UITheme.makeCard(new BorderLayout());

        JLabel hint = UITheme.makeLabel("All actions performed in Entry/Exit, Management, Search, Retrieval, and Route modules are logged here.");
        hint.setBorder(new EmptyBorder(0, 0, 8, 0));
        card.add(hint, BorderLayout.NORTH);

        JScrollPane sp = UITheme.wrapScroll(logArea);
        card.add(sp, BorderLayout.CENTER);

        return card;
    }

    // ── Refresh ───────────────────────────────────────────────────────────────
    private void refresh() {
        List<String> entries = log.getEntries();
        StringBuilder sb = new StringBuilder();
        for (String e : entries) sb.append(e).append("\n");
        logArea.setText(sb.toString());
        logArea.setCaretPosition(logArea.getDocument().getLength());
        countLabel.setText(entries.size() + " entries");
    }
}
