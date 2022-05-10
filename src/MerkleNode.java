public class MerkleNode extends Node {
    private Node left;
    private Node right;

    public MerkleNode(Node left, Node right, String hash){
        super(hash);
        this.left = left;
        this.right = right;
    }

    public Node getLeft() {
        return left;
    }

    public Node getRight(){
        return right;
    }
}
