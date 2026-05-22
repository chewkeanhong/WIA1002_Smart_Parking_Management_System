package search;

import models.Vehicle;
import java.util.ArrayList;
import java.util.List;

/**
 * Self-balancing AVL Tree keyed by license plate.
 * Insert / Search / Delete: O(log n) guaranteed.
 * In-order traversal yields alphabetically sorted vehicles: O(n).
 */
public class VehicleBST {

    private TreeNode root;

    // ── Public API ────────────────────────────────────────────────────────────

    public void insert(Vehicle vehicle) {
        root = insert(root, vehicle.getLicensePlate(), vehicle);
    }

    /** Returns the Vehicle matching the plate, or null if not found — O(log n). */
    public Vehicle search(String plate) {
        TreeNode n = find(root, plate);
        return n == null ? null : n.vehicle;
    }

    /** Deletes by plate — O(log n). Returns false if not found. */
    public boolean delete(String plate) {
        if (find(root, plate) == null) return false;
        root = delete(root, plate);
        return true;
    }

    /** Returns all vehicles sorted alphabetically by plate — O(n). */
    public List<Vehicle> inOrder() {
        List<Vehicle> out = new ArrayList<>();
        inOrder(root, out);
        return out;
    }

    public TreeNode getRoot()   { return root; }
    public boolean  isEmpty()   { return root == null; }

    // ── Recursive helpers ─────────────────────────────────────────────────────

    private TreeNode insert(TreeNode n, String key, Vehicle v) {
        if (n == null) return new TreeNode(key, v);
        int c = key.compareTo(n.key);
        if      (c < 0) n.left  = insert(n.left,  key, v);
        else if (c > 0) n.right = insert(n.right, key, v);
        else { n.vehicle = v; return n; }   // duplicate → update
        n.height = 1 + Math.max(h(n.left), h(n.right));
        return balance(n);
    }

    private TreeNode find(TreeNode n, String key) {
        if (n == null) return null;
        int c = key.compareTo(n.key);
        return c < 0 ? find(n.left, key) : c > 0 ? find(n.right, key) : n;
    }

    private TreeNode delete(TreeNode n, String key) {
        if (n == null) return null;
        int c = key.compareTo(n.key);
        if      (c < 0) n.left  = delete(n.left,  key);
        else if (c > 0) n.right = delete(n.right, key);
        else {
            if (n.left  == null) return n.right;
            if (n.right == null) return n.left;
            TreeNode succ = min(n.right);
            n.key     = succ.key;
            n.vehicle = succ.vehicle;
            n.right   = delete(n.right, succ.key);
        }
        n.height = 1 + Math.max(h(n.left), h(n.right));
        return balance(n);
    }

    private void inOrder(TreeNode n, List<Vehicle> out) {
        if (n == null) return;
        inOrder(n.left,  out);
        out.add(n.vehicle);
        inOrder(n.right, out);
    }

    // ── AVL rotation / balance ────────────────────────────────────────────────

    private TreeNode balance(TreeNode n) {
        int bf = h(n.left) - h(n.right);
        if (bf > 1) {
            if (h(n.left.left) < h(n.left.right)) n.left = rotL(n.left);
            return rotR(n);
        }
        if (bf < -1) {
            if (h(n.right.right) < h(n.right.left)) n.right = rotR(n.right);
            return rotL(n);
        }
        return n;
    }

    private TreeNode rotR(TreeNode y) {
        TreeNode x = y.left, t = x.right;
        x.right = y; y.left = t;
        y.height = 1 + Math.max(h(y.left), h(y.right));
        x.height = 1 + Math.max(h(x.left), h(x.right));
        return x;
    }

    private TreeNode rotL(TreeNode x) {
        TreeNode y = x.right, t = y.left;
        y.left = x; x.right = t;
        x.height = 1 + Math.max(h(x.left), h(x.right));
        y.height = 1 + Math.max(h(y.left), h(y.right));
        return y;
    }

    private int      h(TreeNode n) { return n == null ? 0 : n.height; }
    private TreeNode min(TreeNode n) { while (n.left != null) n = n.left; return n; }
}
