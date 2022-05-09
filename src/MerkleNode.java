public class MerkleNode extends Node {
    private Node left;
    private Node right;

    public MerkleNode(Node left, Node right, String hash){
        super(hash);
        this.left = left;
        this.right = right;
    }
}
