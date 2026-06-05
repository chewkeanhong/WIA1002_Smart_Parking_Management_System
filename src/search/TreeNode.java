package search;

import models.Vehicle;

// one node of the AVL tree, keyed by license plate
public class TreeNode {
    public String   key;     // license plate
    public Vehicle  vehicle;
    public TreeNode left, right;
    public int      height;  // used to keep the tree balanced

    public TreeNode(String key, Vehicle vehicle) {
        this.key     = key;
        this.vehicle = vehicle;
        this.height  = 1;
    }
}
