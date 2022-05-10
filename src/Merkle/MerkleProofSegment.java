package Merkle;

/**
 * A Merkle Proof segment contains a hash code and a direction
 * The Direction denotes whether the hash code comes from the left or right child
 */
public class MerkleProofSegment {
    private String hash;
    private Direction dir;

    public MerkleProofSegment(String hash, Direction dir){
        this.hash = hash;
        this.dir = dir;
    }

    public String getHash(){
        return hash;
    }

    public Direction getDirection(){
        return dir;
    }
}

enum Direction{
    LEFT,
    RIGHT
}
