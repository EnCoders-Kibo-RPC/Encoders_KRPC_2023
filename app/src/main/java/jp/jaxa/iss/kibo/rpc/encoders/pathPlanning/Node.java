package jp.jaxa.iss.kibo.rpc.encoders.pathPlanning;

import gov.nasa.arc.astrobee.types.Point;
import gov.nasa.arc.astrobee.types.Quaternion;

/**
 * A Node is a point along a planned path.
 */
public class Node {
    private int id;
    private Point location;
    private Quaternion rotation;

    /**
     * Construct a Node
     *
     * @param id A unique ID for this node
     * @param location The location of this node in meters
     * @param rotation The rotation of this node as a quaternion
     */
    public Node(int id, Point location, Quaternion rotation) {
        this.id = id;
        this.location = location;
        this.rotation = rotation;
    }

    /**
     * Get this node's unique ID
     *
     * @return The ID
     */
    public int getId() {
        return id;
    }

    /**
     * Get the location of this node in 3d space
     *
     * @return This node's location
     */
    public Point getLocation() {
        return location;
    }

    /**
     * Get the rotation of this node
     *
     * @return This node's rotation as a quaternion
     */
    public Quaternion getRotation() {
        return rotation;
    }

    /**
     * Get the distance from this node to another node
     *
     * @param n2 The second node
     * @return The distance in meters
     */
    public double distance(Node n2) {
        return Math.sqrt(
          Math.pow(location.getX() - n2.getLocation().getX(), 2) +
          Math.pow(location.getY() - n2.getLocation().getY(), 2) +
          Math.pow(location.getZ() - n2.getLocation().getZ(), 2)
        );
    }
}
