package test;

import Merkle.MerkleProofSegment;
import Merkle.MerkleTree;
import Merkle.Node;
import org.junit.*;

import java.util.Arrays;
import java.util.List;

public class MerkleTreeTests {

    @Test
    public void buildTreeTest(){
        List<String> dataBlocks = Arrays.asList("hello", "my");
        MerkleTree tree = new MerkleTree(dataBlocks);
        tree.buildTree();

        String rootNode = tree.getRootHash();

        String leaf1Hash = MerkleTree.createSHAHash("hello");
        String leaf2Hssh = MerkleTree.createSHAHash("my");

        String trueHash = MerkleTree.createSHAHash(leaf1Hash.concat(leaf2Hssh));

        Assert.assertTrue(rootNode.equals(trueHash));
    }

    @Test
    public void buildTreeSingle(){
        MerkleTree tree = new MerkleTree();
        tree.appendDataBlock("test");

        tree.buildTree();

        String rootHash = tree.getRootHash();

        Assert.assertTrue(rootHash.equals(MerkleTree.createSHAHash("test")));
    }

    @Test
    public void testProof() {
        MerkleTree tree = new MerkleTree();

        List<String> dataBlocks = Arrays.asList("hello","my", "name","is", "tyler");

        tree.appendDataBlocks(dataBlocks);

        tree.buildTree();



        for (String dataBlock : dataBlocks){
            String hash = MerkleTree.createSHAHash(dataBlock);
            List<MerkleProofSegment> proof = tree.createProof(hash);
            Assert.assertTrue(proof.size()==3);
            Assert.assertTrue(tree.verifyTree(hash, proof));
        }
    }

    @Test
    public void testProofBetweenSameTrees(){
        List<String> dataBlocks = Arrays.asList("hello","my", "name","is", "tyler");
        MerkleTree tree1 = new MerkleTree();
        MerkleTree tree2 = new MerkleTree();

        tree1.appendDataBlocks(dataBlocks);
        tree2.appendDataBlocks(dataBlocks);

        tree1.buildTree();
        tree2.buildTree();

        String leafHash = MerkleTree.createSHAHash("name");

        List<MerkleProofSegment> proof = tree1.createProof(leafHash);

        Assert.assertTrue(tree2.verifyTree(leafHash, proof));
    }

    @Test
    public void testProofBetweenDifferentTrees(){
        List<String> dataBlocks = Arrays.asList("hello","my", "name","is", "tyler");
        List<String> dataBlocks2 = Arrays.asList("hello","my", "name","is", "tyler", "faulkner");

        MerkleTree tree1 = new MerkleTree();
        MerkleTree tree2 = new MerkleTree();

        tree1.appendDataBlocks(dataBlocks);
        tree2.appendDataBlocks(dataBlocks2);

        tree1.buildTree();
        tree2.buildTree();

        String leafHash = MerkleTree.createSHAHash("name");

        List<MerkleProofSegment> proof = tree1.createProof(leafHash);

        Assert.assertFalse(tree2.verifyTree(leafHash, proof));
    }
}
