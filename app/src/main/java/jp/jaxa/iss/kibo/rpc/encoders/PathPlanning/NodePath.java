package jp.jaxa.iss.kibo.rpc.encoders.PathPlanning;

import java.util.List;

/**
 * A path of nodes
 */
public class NodePath {
    private List<Node> nodes;
    private double duration;

    /**
     * Construct a new NodePath
     *
     * @param nodes The nodes that are in the path
     * @param duration The duration of the path in seconds
     */
    public NodePath(List<Node> nodes, double duration) {
        this.nodes = nodes;
        this.duration = duration;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public double getDuration() {
        return duration;
    }
}
