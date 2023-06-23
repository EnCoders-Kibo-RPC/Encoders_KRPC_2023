package jp.jaxa.iss.kibo.rpc.encoders.PathPlanning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.ToDoubleBiFunction;

/**
 * This class represents a bidirectional weighted graph of locations in the kibo
 * <p></p>
 * It is used to find the shortest path from one location of interest to another
 */
public class NodeGraph {
    private List<Node> nodes;
    private Map<Integer, List<Node>> edges;

    /**
     * Construct a NodeGraph
     *
     * @param initialNodes The initial nodes in the graph
     * @param edges A hashmap allowing a node's neighbors to be accessed through its ID
     */
    public NodeGraph(List<Node> initialNodes, Map<Integer, List<Node>> edges) {
        nodes = initialNodes;
        this.edges = edges;
    }

    /**
     * Construct a NodeGraph with no initial nodes or edges
     */
    public NodeGraph() {
        nodes = new ArrayList<>();
        edges = new HashMap<>();
    }

    /**
     * Add a node and its connections to the graph
     * <p></p>
     * This will not add reverse connections, so if bidirectional edges are desired, they should be
     * specified both ways.
     *
     * @param node The node to add
     * @param connections The nodes that this node is connected to
     */
    public void addNode(Node node, List<Node> connections) {
        nodes.add(node);
        edges.put(node.getId(), connections);
    }

    /**
     * Add a node without any connections
     *
     * @param node
     */
    public void addNode(Node node) {
        addNode(node, new ArrayList<Node>());
    }

    /**
     * Determine the shortest path from one node to another
     *
     * @param startId The ID of the start node (This must already be part of the graph)
     * @param endId The ID of the end node (This must already be part of the graph)
     * @param distanceFunction A function to be used to determine the distance between two nodes
     * @return A pathResult object if the path was successfully generated or null if the path is
     * impossible
     */
    public PathResult shortestPath(int startId, int endId, ToDoubleBiFunction<Node, Node> distanceFunction) {
        // TODO
        return null;
    }

    /**
     * This class is to store the results of a path generation
     */
    public static class PathResult {
        private List<Node> pathNodes;
        private double time;

        /**
         * Construct a new PathResult
         *
         * @param pathNodes The nodes of the path, index 0 is the start and the last index is the end
         * @param time The time to complete the path in seconds
         */
        public PathResult(List<Node> pathNodes, double time) {
            this.pathNodes = pathNodes;
            this.time = time;
        }

        public List<Node> getNodes() {
            return pathNodes;
        }

        public double getTime() {
            return time;
        }
    }

}
