public class MerkleProofSegment {
    private String hash;
    private Direction dir;

    public MerkleProofSegment(String hash, Direction dir){
        this.hash = hash;
        this.dir = dir;
    }
}

enum Direction{
    LEFT,
    RIGHT
}
