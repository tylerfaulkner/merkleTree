import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * A Merkle Tree implementation to verify file integrity
 */
public class MerkleTree {

    private List<Node> leafNodes;
    private Node root = null;

    public MerkleTree(){
        this.leafNodes = new ArrayList<Node>();
    }

    public MerkleTree(List<String> dataBlocks){
        leafNodes = generateLeaves(dataBlocks);
    }

    private List<Node> generateLeaves(List<String> dataBlocks){
        List<Node> nodes = new ArrayList<Node>();
        for (String data : dataBlocks){
            nodes.add(generateLeaF(data));
        }
        return nodes;
    }

    public String getRootHash(){
        return this.root.getHash();
    }

    private Node generateLeaF(String dataBlock){
        return new Node(createSHAHash(dataBlock));
    }

    public void appendDataBlock(String dataBlock){
        this.leafNodes.add(generateLeaF(dataBlock));
    }

    public void appendDataBlocks(List<String> dataBlocks){
        for (String dataBlock : dataBlocks){
            this.appendDataBlock(dataBlock);
        }
    }

    public void buildTree(){
        if (!this.leafNodes.isEmpty()) {
            buildTree(this.leafNodes);
        }
    }
    
    private void buildTree(List<Node> layerNodes){
        if(layerNodes.size()==1){
            this.root = layerNodes.get(0);
        } else {
            List<Node> nextLayer = new ArrayList<Node>();
            for (int i =0; i<layerNodes.size(); i+=2){
                MerkleNode parent = null;
                if (i+1 < layerNodes.size()){
                    Node left = layerNodes.get(i);
                    Node right = layerNodes.get(i+1);
                    String parentHash = createSHAHash(left.getHash().concat(right.getHash()));
                    parent = new MerkleNode(left, right, parentHash);
                    left.setParent(parent);
                    right.setParent(parent);
                } else {
                    Node left = layerNodes.get(i);
                    Node right = layerNodes.get(i);
                    String parentHash = createSHAHash(left.getHash().concat(right.getHash()));
                    parent = new MerkleNode(left, right, parentHash);
                    left.setParent(parent);
                }
                nextLayer.add(parent);
            }
            buildTree(layerNodes);
        }
    }

    public List<MerkleProofSegment> createProof(String leafHash){
        Node leafNode = findLeafNode(leafHash);
        if (leafNode == null){
            return new ArrayList<MerkleProofSegment>();
        } else {
            return buildProof(leafNode.getParent(), leafNode);
        }
    }

    private List<MerkleProofSegment> buildProof(MerkleNode parentNode, Node child){
        List<MerkleProofSegment> proof = new ArrayList<MerkleProofSegment>();
        do{
            Direction dir = Direction.LEFT;
            String otherNodeHash = parentNode.getLeft().getHash();
            if (parentNode.getLeft().getHash() == child.getHash()){
                dir = Direction.RIGHT;
                otherNodeHash = parentNode.getRight().getHash();
            }

            proof.add(new MerkleProofSegment(otherNodeHash, dir));

            parentNode = parentNode.getParent();
            child = parentNode;

        }while (parentNode != null);
        return proof;
    }

    public Boolean verifyTree(String leafHash, List<MerkleProofSegment> proof){
        String testRoot = buildRootFromProof(leafHash, proof);

        return testRoot.equals(this.root.getHash());
    }

    public String buildRootFromProof(String leafHash, List<MerkleProofSegment> proof){
        String tempHash = leafHash;

        for (MerkleProofSegment segment : proof){
            if (segment.getDirection() == Direction.LEFT){
                tempHash = createSHAHash(segment.getHash().concat(tempHash));
            } else {
                tempHash = createSHAHash(leafHash.concat(segment.getHash()));
            }
        }

        return tempHash;
    }

    private Node findLeafNode(String leafHash){
        Node leafNode = null;
        for(int i = 0; i< this.leafNodes.size() && leafNode==null; i++){
            Node leaf = leafNodes.get(i);
            if (leaf.getHash().equals(leafHash)) {
                leafNode = leaf;
            }
        }
        return leafNode;
    }

    public String createSHAHash(String input) {
        try {
            String hashtext = null;
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] messageDigest =
                    md.digest(input.getBytes(StandardCharsets.UTF_8));

            hashtext = convertToHex(messageDigest);
            return hashtext;
        } catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        }
        return null;
    }

    private String convertToHex(final byte[] messageDigest) {
        BigInteger bigint = new BigInteger(1, messageDigest);
        String hexText = bigint.toString(16);
        while (hexText.length() < 32) {
            hexText = "0".concat(hexText);
        }
        return hexText;
    }
}
