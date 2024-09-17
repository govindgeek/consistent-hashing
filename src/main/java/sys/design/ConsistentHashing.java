package sys.design;

import java.util.Arrays;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * 1. Basic Concept
 * Hashing Nodes and Keys: In consistent hashing, both nodes and keys are hashed to a circular space or ring.
 * The idea is to use a hash function to map both nodes and keys to positions on this ring.
 * Key Assignment: Each key is assigned to the first node that appears clockwise from the key’s position on the ring.
 * This minimizes the number of keys that need to be relocated when nodes are added or removed.
 * <p>
 * 2. How It Works
 * Hash Ring Construction: The hash ring is a circular space where positions are determined by hashing the node IDs and key IDs.
 * Key Placement: To determine which node is responsible for a key, hash the key to find its position on the ring and then move
 * clockwise to find the first node that appears at or after this position.
 * <p>
 * 3. Advantages
 * Scalability: When a new node is added, only a small fraction of keys need to be relocated to the new node. The same is
 * true when nodes are removed.
 * Minimal Disruption: This minimizes the impact on the system compared to traditional hashing techniques, where adding or
 * removing nodes might require rehashing all keys.
 * <p>
 * 4. Implementation Details
 * Virtual Nodes: To improve load distribution and avoid scenarios where nodes are unevenly distributed (if nodes have different capacities)
 * , you can use virtual nodes. Each physical node is mapped to multiple positions on the ring.
 * Hash Function: A good hash function is crucial to evenly distribute keys and nodes. Common choices include MD5 or SHA-1,
 * but modern applications might use more specialized hash functions.
 * Handling Failures: When a node fails, its keys are reassigned to the next node on the ring, ensuring that the system can recover gracefully.
 * <p>
 * 5. Java Implementation Considerations
 * Libraries: Java provides several libraries that can help with consistent hashing, such as Guava’s Hashing class or Apache’s commons-hash library.
 * Custom Implementation: If you need a custom implementation, you can use Java’s ConcurrentHashMap to maintain a mapping of
 * nodes to their positions and handle the ring logic.
 */


public class ConsistentHashing {
    /// node hash code and node number map
    private final TreeMap<Integer, String> ring = new TreeMap<>();
    private final int numberOfReplicas;
    private final List<String> nodes = new ArrayList<>();

    public ConsistentHashing(int numberOfReplicas, List<String> nodes) {
        this.numberOfReplicas = numberOfReplicas;
        for (String node : nodes) {
            addNode(node);
        }
    }

    private void addNode(String node) {
        nodes.add(node);
        for (int i = 0; i < numberOfReplicas; i++) {
            int hash = hash(node + i);
            ring.put(hash, node);
        }
    }

    private void removeNode(String node) {
        nodes.remove(node);
        for (int i = 0; i < numberOfReplicas; i++) {
            int hash = hash(node + i);
            ring.remove(hash);
        }
    }

    public String getNode(String key) {
        if (ring.isEmpty()) {
            return null;
        }
        int hash = hash(key);
       // System.out.println(hash + " for " + key);
        SortedMap<Integer, String> tailMap = ring.tailMap(hash);
        // if node is not available  then return next node clock wise
        Integer hashKey = tailMap.isEmpty() ? ring.firstKey() : tailMap.firstKey();
        return ring.get(hashKey);
    }

    private int hash(String key) {
        return key.hashCode(); // Use a better hash function in production
    }

    public static void main(String[] args) {
        List<String> initialNodes = Arrays.asList("Node1", "Node2", "Node3", "Node4", "Node5");
        ConsistentHashing ch = new ConsistentHashing(3, new ArrayList<>(initialNodes));

        // Simulate adding and removing nodes
        System.out.println("Initial nodes: " + initialNodes);

        // Adding more nodes
        ch.addNode("Node6");
        System.out.println("Added Node6");

        // Removing a node
        ch.removeNode("Node3");
        System.out.println("Removed Node3");

        // Generate a large number of keys and assign them to nodes
        Random random = new Random();
        for (int i = 1; i < 10000; i++) {
            String key = "Key" + random.nextInt(10000);
            if (i % 999 == 0) {
                ch.removeNode("Node4");
                System.out.println("Removed Node4");
            }

            String node = ch.getNode(key);

          System.out.println("Key: " + key + " is assigned to Node: " + node);
        }
    }
}
