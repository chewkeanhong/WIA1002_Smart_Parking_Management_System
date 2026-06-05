package search;

import models.Vehicle;
import java.util.ArrayList;
import java.util.List;

// AVL tree of vehicles keyed by license plate. It rebalances itself on
// every insert/delete so it stays roughly balanced and searches stay fast.
// An in-order walk gives the vehicles back sorted by plate.
public class VehicleBST {

    private TreeNode root;

    public void insert(Vehicle vehicle) {
        root = insert(root, vehicle.getLicensePlate(), vehicle);
    }

    // find a vehicle by plate, or null if it's not in the tree
    public Vehicle search(String plate) {
        TreeNode n = find(root, plate);
        return n == null ? null : n.vehicle;
    }

    // delete by plate. returns false if there was nothing to delete.
    public boolean delete(String plate) {
        if (find(root, plate) == null) return false;
        root = delete(root, plate);
        return true;
    }

    // all vehicles, sorted alphabetically by plate
    public List<Vehicle> inOrder() {
        List<Vehicle> out = new ArrayList<>();
        inOrder(root, out);
        return out;
    }

    public TreeNode getRoot()   { return root; }
    public boolean  isEmpty()   { return root == null; }

    // recursive insert, then fix the height and rebalance on the way back up
    private TreeNode insert(TreeNode n, String key, Vehicle v) {
        if (n == null) return new TreeNode(key, v);
        int c = key.compareTo(n.key);
        if      (c < 0) n.left  = insert(n.left,  key, v);
        else if (c > 0) n.right = insert(n.right, key, v);
        else { n.vehicle = v; return n; }   // same plate already exists, just update it
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

    // check the balance factor and rotate if a side got too tall.
    // bf > 1 means left-heavy, bf < -1 means right-heavy.
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
