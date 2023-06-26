package jp.jaxa.iss.kibo.rpc.encoders.pathPlanning;

import android.util.Log;

import java.util.function.BooleanSupplier;

import gov.nasa.arc.astrobee.Result;
import gov.nasa.arc.astrobee.types.Point;
import gov.nasa.arc.astrobee.types.Quaternion;
import jp.jaxa.iss.kibo.rpc.api.KiboRpcApi;

/**
 * The PathFollower is used to make the robot safely follow a NodePath
 */
public class PathFollower {
    private final String TAG = this.getClass().getSimpleName();

    private static final int MAX_TRAVEL_ATTEMPTS = 5;

    private final KiboRpcApi api;

    /**
     * Create a new PathFollower
     *
     * @param api A reference to the KiboRpcApi from the KiboRpcService
     */
    public PathFollower(KiboRpcApi api) {
        this.api = api;
    }

    /**
     * Follow a node path by moving to each of its nodes
     *
     * @param path The Node path
     * @param breakoutCondition Stop following the path if this condition evaluates to true.
     * @return The node that the robot is at following this method
     */
    public Node followPath(NodePath path, BooleanSupplier breakoutCondition) {
        Node lastNode = path.getNodes().get(path.getNodes().size() - 1);
        Node prevNode = null;
        for(int i = 0; i < path.getNodes().size(); i++) {
            Node currentNode = path.getNodes().get(i);

            if(breakoutCondition.getAsBoolean()) {
                return prevNode;
            }

            Log.wtf(TAG, "Going to node with id: " + currentNode.getId());
            if(!safeGoToPosition(currentNode.getLocation(), lastNode.getRotation())) {
                return prevNode;
            }
            prevNode = currentNode;
        }

        return lastNode;
    }

    /**
     * Follow a node path by moving to each of its nodes
     *
     * @param path The Node path
     * @return The node that the robot is at following this method
     */
    public Node followPath(NodePath path) {
        return followPath(path, new BooleanSupplier() {
            @Override
            public boolean getAsBoolean() {
                return false;
            }
        });
    }



    /**
     * Attempt to move to a position, retry if there is an error.
     * <p></p>
     * This will attempt to move a maximum of MAX_TRAVEL_ATTEMPTS times.
     *
     * @param position The position to move to
     * @param rotation The rotation to rotate to
     * @return True if the movement was a success, else, false
     */
    public boolean safeGoToPosition(Point position, Quaternion rotation) {
        for(int i = 0; i < MAX_TRAVEL_ATTEMPTS; i++) {
            Result result = api.moveTo(position, rotation, true);

            if(result != null && result.hasSucceeded()) {
                return true;
            } else {
                Log.wtf(TAG, "Go to position failed with message [" + result.getMessage() + "]. Reattempting...");
            }
        }

        Log.wtf(TAG, "Maximum attempts reached.");
        return false;
    }
}
