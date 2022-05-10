package Merkle;

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

    /**
     * Generates the leafNodes for the tree from the given data
     * @param dataBlocks tree data
     * @return List of leaf nodes
     */
    private List<Node> generateLeaves(List<String> dataBlocks){
        List<Node> nodes = new ArrayList<Node>();
        for (String data : dataBlocks){
            nodes.add(generateLeaf(data));
        }
        return nodes;
    }

    public String getRootHash(){
        if(this.root != null) {
            return this.root.getHash();
        } else {
            return "";
        }
    }

    public Node getRootNode() { return this.root; }

    private Node generateLeaf(String dataBlock){
        return new Node(createSHAHash(dataBlock));
    }

    public void appendDataBlock(String dataBlock){
        this.leafNodes.add(generateLeaf(dataBlock));
    }

    public void appendDataBlocks(List<String> dataBlocks){
        for (String dataBlock : dataBlocks){
            this.appendDataBlock(dataBlock);
        }
    }

    /**
     * Builds Tree using leaf nodes
     */
    public void buildTree(){
        if (!this.leafNodes.isEmpty()) {
            buildTree(this.leafNodes);
        }
    }

    /**
     * Recursive method to build tree from the bottom up
     * @param layerNodes the previous layer to generate a new layer off of.
     */
    private void buildTree(List<Node> layerNodes){
        if(layerNodes.size()==1){
            //Stops Recursion once the list of nodes is one and sets the root node.
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
            buildTree(nextLayer);
        }
    }

    /**
     * Creates a proof list given a leafHash
     * @param leafHash the hash of a leaf
     * @return List of Proof segments (empty of leaf not in set)
     */
    public List<MerkleProofSegment> createProof(String leafHash){
        Node leafNode = findLeafNode(leafHash);
        if (leafNode == null || leafNode.getParent() == null){
            return new ArrayList<MerkleProofSegment>();
        } else {
            return buildProof(leafNode.getParent(), leafNode);
        }
    }

    /**
     * Builds and return the Merkle Proof For a Given leaf node
     * This is a bubble-up algorithm
     * @param parentNode the parent of the child
     * @param child the starting leaf node
     * @return
     */
    private List<MerkleProofSegment> buildProof(MerkleNode parentNode, Node child){
        List<MerkleProofSegment> proof = new ArrayList<MerkleProofSegment>();
        do{
            Direction dir = Direction.LEFT;
            String otherNodeHash = parentNode.getLeft().getHash();
            if (otherNodeHash == child.getHash()){
                dir = Direction.RIGHT;
                otherNodeHash = parentNode.getRight().getHash();
            }

            proof.add(new MerkleProofSegment(otherNodeHash, dir));

            //Move up the tree
            child = parentNode;
            parentNode = parentNode.getParent();

        }while (parentNode != null);
        return proof;
    }

    /**
     * Build a rootHash from the proof and compares the proof rootHash against the tress rootHash
     * @param leafHash the hash the proof was created based on
     * @param proof The list of Proof Segments to build a test root hash
     * @return True if trees are the same, false otherwise
     */
    public Boolean verifyTree(String leafHash, List<MerkleProofSegment> proof){
        String testRoot = buildRootFromProof(leafHash, proof);

        return testRoot.equals(this.root.getHash());
    }

    /**
     * Builds a root hash from a proof and starting leafhash
     * @param leafHash the lead hash of the proof
     * @param proof The list of hashes leading to the root
     * @return the rootHash
     */
    public static String buildRootFromProof(String leafHash, List<MerkleProofSegment> proof){
        String tempHash = leafHash;

        for (MerkleProofSegment segment : proof){
            if (segment.getDirection() == Direction.LEFT){
                tempHash = createSHAHash(segment.getHash().concat(tempHash));
            } else {
                tempHash = createSHAHash(tempHash.concat(segment.getHash()));
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

    public static String createSHAHash(String input) {
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

    private static String convertToHex(final byte[] messageDigest) {
        BigInteger bigint = new BigInteger(1, messageDigest);
        String hexText = bigint.toString(16);
        while (hexText.length() < 32) {
            hexText = "0".concat(hexText);
        }
        return hexText;
    }
}
