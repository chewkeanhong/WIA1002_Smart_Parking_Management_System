package ui;

import java.text.SimpleDateFormat;
import java.util.*;

public class ActivityLog {

    private final List<String> entries = new ArrayList<>();
    private final List<Runnable> listeners = new ArrayList<>();
    private static final SimpleDateFormat SDF = new SimpleDateFormat("HH:mm:ss");

    public void log(String message) {
        String entry = "[" + SDF.format(new Date()) + "]  " + message;
        entries.add(entry);
        for (Runnable r : listeners) r.run();
    }

    public List<String> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    public void addListener(Runnable r) {
        listeners.add(r);
    }

    public void clear() {
        entries.clear();
        for (Runnable r : listeners) r.run();
    }
}
