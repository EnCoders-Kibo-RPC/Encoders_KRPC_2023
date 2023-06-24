package jp.jaxa.iss.kibo.rpc.encoders.PathPlanning;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
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
     * @param targetIds An array of IDs to return paths to
     * @param distanceFunction A function to be used to determine the distance between two nodes
     * @return An array of NodePaths that lead to the nodes specified by targetIds
     */
    public NodePath[] shortestPath(int startId, int[] targetIds, ToDoubleBiFunction<Node, Node> distanceFunction) {
        final Map<Integer, Double> distanceMap = new HashMap<>();
        Map<Integer, Node> previousNodes = new HashMap<>();
        Set<Integer> visitedNodes = new HashSet<>();

        PriorityQueue<Node> pq = new PriorityQueue<>(new Comparator<Node>() {
            @Override
            public int compare(Node o1, Node o2) {
                return (int)Math.signum(distanceMap.get(o1.getId()) - distanceMap.get(o2.getId()));
            }
        });

        Node startNode = null;

        for(Node n : nodes) {
            previousNodes.put(n.getId(), null);

            if(n.getId() == startId) {
                distanceMap.put(n.getId(), 0.0);
                startNode = n;
            } else {
                distanceMap.put(n.getId(), Double.POSITIVE_INFINITY);
                pq.add(n);
            }
        }

        if(startNode == null) {
            throw new IllegalArgumentException("A node with ID [" + startId + "] does not exist in this graph!");
        }

        while(!pq.isEmpty()) {
            Node current = pq.poll();
            double currentDistance = distanceMap.get(current.getId());

            for(Node n : edges.get(current.getId())) {
                if(visitedNodes.contains(n.getId())) {
                    continue;
                }

                double distance = currentDistance + distanceFunction.applyAsDouble(current, n);
                if(distance < distanceMap.get(n.getId())) {
                    distanceMap.put(n.getId(), distance);
                    previousNodes.put(n.getId(), current);
                }
            }

            visitedNodes.add(current.getId());
        }

        return null; // REMOVE THIS
    }
}
