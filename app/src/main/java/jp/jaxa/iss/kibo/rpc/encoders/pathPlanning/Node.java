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
     * @param n1 The first node
     * @param n2 The second node
     * @return The distance in meters
     */
    public static double distance(Node n1, Node n2) {
        return Math.sqrt(
          Math.pow(n1.getLocation().getX() - n2.getLocation().getX(), 2) +
          Math.pow(n1.getLocation().getY() - n2.getLocation().getY(), 2) +
          Math.pow(n1.getLocation().getZ() - n2.getLocation().getZ(), 2)
        );
    }

    /**
     * Calculates the time it takes for a robot to travel between two coordinates in a zero-gravity environment.
     *
     * @param startNode the starting node containing the coordinate as a Point object
     * @param endNode   the ending node containing the coordinate as a Point object
     * @return the time taken for the robot to travel between the coordinates in seconds
     */
    public static double calculateTravelTime(Node startNode, Node endNode) {
        double distance = distance(startNode, endNode);
        double acceleration = 0.04;

        // Calculate the time taken for the robot to travel the distance
        double time = Math.sqrt((2 * distance) / acceleration);

        return time;
    }
}
