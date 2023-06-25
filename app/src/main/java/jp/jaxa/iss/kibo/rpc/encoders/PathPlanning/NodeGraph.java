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
     * Construct a NodeGraph with no initial edges
     *
     * @param initialNodes The initial nodes
     */
    public NodeGraph(List<Node> initialNodes) {
        this(initialNodes, new HashMap<Integer, List<Node>>());
    }

    /**
     * Construct a NodeGraph with no initial nodes or edges
     */
    public NodeGraph() {
        this(new ArrayList<Node>());
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
     * Add a bidirectional connection between two nodes
     *
     * @param node
     * @param connected
     */
    public void addConnection(Node node, Node connected) {
        if(edges.get(node.getId()) == null) {
            edges.put(node.getId(), new ArrayList<Node>());
        }
        edges.get(node.getId()).add(connected);

        if(edges.get(connected.getId()) == null) {
            edges.put(connected.getId(), new ArrayList<Node>());
        }
        edges.get(connected.getId()).add(node);
    }


    /**
     * Determine the shortest path from one node to another
     *
     * @param startId The ID of the start node (This must already be part of the graph)
     * @param targetIds An array of IDs to return paths to
     * @param durationFunction A function to be used to determine the distance between two nodes
     * @return An array of NodePaths that lead to the nodes specified by targetIds
     */
    public NodePath[] shortestPath(int startId, Set<Integer> targetIds, ToDoubleBiFunction<Node, Node> durationFunction) {
        final Map<Integer, Double> durationMap = new HashMap<>();
        Map<Integer, Node> previousNodes = new HashMap<>();
        Set<Integer> visitedNodes = new HashSet<>();

        // a priority queue ordered by travel duration from the start point
        PriorityQueue<Node> pq = new PriorityQueue<>(new Comparator<Node>() {
            @Override
            public int compare(Node o1, Node o2) {
                return (int)Math.signum(durationMap.get(o1.getId()) - durationMap.get(o2.getId()));
            }
        });

        Node startNode = null;

        for(Node n : nodes) {
            previousNodes.put(n.getId(), null);

            if(n.getId() == startId) {
                durationMap.put(n.getId(), 0.0);
                pq.add(n);
                startNode = n;
            } else {
                durationMap.put(n.getId(), Double.POSITIVE_INFINITY);
                pq.add(n);
            }
        }

        if(startNode == null) {
            throw new IllegalArgumentException("A node with ID [" + startId + "] does not exist in this graph!");
        }

        // Go through all unvisited nodes
        while(!pq.isEmpty()) {
            Node current = pq.poll();
            double currentDuration = durationMap.get(current.getId());

            // Iterate through unvisited neighbors
            for(Node n : edges.get(current.getId())) {
                if(visitedNodes.contains(n.getId())) {
                    continue;
                }

                double duration = currentDuration + durationFunction.applyAsDouble(current, n);
                if(duration < durationMap.get(n.getId())) {
                    durationMap.put(n.getId(), duration);
                    previousNodes.put(n.getId(), current);

                    // Force the priority queue to update
                    pq.remove(n);
                    pq.add(n);
                }
            }

            visitedNodes.add(current.getId());
        }

        // Collect the actual paths
        NodePath[] paths = new NodePath[targetIds.size()];
        int insertIdx = 0;
        for(Node n : nodes) {
            if(targetIds.contains(n.getId())) {
                double duration = durationMap.get(n.getId());

                // Set to null if the node is unreachable
                if(duration == Double.POSITIVE_INFINITY) {
                    paths[insertIdx++] = null;
                    continue;
                }

                // Backtrack through previous nodes to get the full path
                List<Node> pathNodes = new ArrayList<>();
                Node pathNode = n;
                while(pathNode != null) {
                    pathNodes.add(0, pathNode);
                    pathNode = previousNodes.get(pathNode.getId());
                }
                paths[insertIdx++] = new NodePath(pathNodes, durationMap.get(n.getId()));
            }
        }

        return paths;
    }
}
