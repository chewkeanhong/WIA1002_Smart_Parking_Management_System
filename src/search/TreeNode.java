package search;

import models.Vehicle;

/** AVL tree node keyed by license plate string. */
public class TreeNode {
    public String   key;     // license plate
    public Vehicle  vehicle;
    public TreeNode left, right;
    public int      height;  // AVL balance tracking

    public TreeNode(String key, Vehicle vehicle) {
        this.key     = key;
        this.vehicle = vehicle;
        this.height  = 1;
    }
}
