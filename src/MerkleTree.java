import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

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

    private Node generateLeaF(String dataBlock){
        return new Node(createSHAHash(dataBlock));
    }

    public void appendDataBlock(String dataBlock){
        this.leafNodes.add(generateLeaF(dataBlock));
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
                Node parent = null;
                if (i+1 < layerNodes.size()){
                    Node left = layerNodes.get(i);
                    Node right = layerNodes.get(i+1);
                    String parentHash = createSHAHash(left.getHash().concat(right.getHash()));
                    parent = new MerkleNode(left, right, parentHash);
                } else {
                    Node left = layerNodes.get(i);
                    Node right = layerNodes.get(i);
                    String parentHash = createSHAHash(left.getHash().concat(right.getHash()));
                    parent = new MerkleNode(left, right, parentHash);
                }
                nextLayer.add(parent);
            }
            buildTree(layerNodes);
        }
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
