package models;

/**
 * Shared live state of the 40-slot parking lot (A01–A40), arranged as
 * two 2×10 blocks separated by a central driving aisle.
 * All mutations happen on the EDT so no synchronization is needed.
 */
public class ParkingMap {

    private static final int BLOCKS         = 2;
    private static final int ROWS_PER_BLOCK = 2;
    private static final int COLS           = 10;
    private static final int ROWS           = BLOCKS * ROWS_PER_BLOCK;   // 4
    private static final int TOTAL          = ROWS * COLS;               // 40

    private final boolean[] occupied = new boolean[TOTAL];

    public static int rows()  { return ROWS; }
    public static int cols()  { return COLS; }
    public static int total() { return TOTAL; }

    /** Canonical slot ID for zero-based index i → "A01" … "A30". */
    public static String slotId(int i) {
        return String.format("A%02d", i + 1);
    }

    public void markOccupied(String id) { int i = idx(id); if (i >= 0) occupied[i] = true;  }
    public void markFree    (String id) { int i = idx(id); if (i >= 0) occupied[i] = false; }

    public void clearOccupancy() {
        for (int i = 0; i < occupied.length; i++) {
            occupied[i] = false;
        }
    }

    public boolean isOccupied(int i) { return i >= 0 && i < TOTAL && occupied[i]; }

    /** Returns the slot ID of the first free bay, or null if the lot is full. */
    public String findFirstFree() {
        for (int i = 0; i < TOTAL; i++) if (!occupied[i]) return slotId(i);
        return null;
    }

    public int getFreeCount()     { int c = 0; for (boolean b : occupied) if (!b) c++; return c; }
    public int getOccupiedCount() { return TOTAL - getFreeCount(); }

    private static int idx(String id) {
        if (id == null) return -1;
        try {
            int n = Integer.parseInt(id.replaceAll("[^0-9]", ""));
            if (n >= 1 && n <= TOTAL) return n - 1;
        } catch (NumberFormatException ignored) {}
        return -1;
    }
}
