public class Node {
    private String hash;
    private MerkleNode parent = null;

    public Node(String hash){
        this.hash = hash;
    }

    public String getHash(){
        return this.hash;
    }

    public void setParent(MerkleNode parent){
        this.parent = parent;
    }

    public MerkleNode getParent(){
        return parent;
    }
}
