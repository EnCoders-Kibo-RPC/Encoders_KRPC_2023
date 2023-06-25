package jp.jaxa.iss.kibo.rpc.encoders;

import java.util.List;
import java.util.Map;

import jp.jaxa.iss.kibo.rpc.api.KiboRpcService;
import jp.jaxa.iss.kibo.rpc.encoders.pathPlanning.NodePath;
import jp.jaxa.iss.kibo.rpc.encoders.pathPlanning.PathFollower;
import jp.jaxa.iss.kibo.rpc.encoders.pathPlanning.PathSolver;
import jp.jaxa.iss.kibo.rpc.encoders.pathPlanning.PathSolver.Location;
import jp.jaxa.iss.kibo.rpc.encoders.utilities.QrCodeUtility;

//import org.opencv.aruco.*;

/**
 * Class meant to handle commands from the Ground Data System and execute them in Astrobee
 */

public class EnCodersService extends KiboRpcService {
    private final String TAG = this.getClass().getSimpleName();

    private static final double TARGET_PROCESSING_SECONDS = 5;
    private static final double GOAL_PROCESSING_SECONDS = 5;

    @Override
    protected void runPlan1(){
        api.startMission();

        // Initialize objects
        PathSolver pathSolver = new PathSolver();
        PathFollower pathFollower = new PathFollower(api);
        QrCodeUtility qrUtility = new QrCodeUtility(api);

        // Go to and read QR Code
        pathFollower.followPath(pathSolver.getPaths(Location.START, new Location[] {Location.QR_CODE}).get(Location.QR_CODE.id));
        String qrReport = qrUtility.decodeQrMessage(qrUtility.readQrMessage());

        Location currentLocation = Location.QR_CODE;
        NodePath goalPath = null;

        // Main loop:
        while(true) {
            List<Integer> activeTargets = api.getActiveTargets();
            Location[] targetLocations = new Location[activeTargets.size() + 1];
            targetLocations[0] = Location.GOAL;
            for(int i = 1; i < targetLocations.length; i++) {
                targetLocations[i] = pathSolver.targetIdToLocation(activeTargets.get(i - 1));
            }

            Map<Integer, NodePath> possiblePaths = pathSolver.getPaths(currentLocation, targetLocations);

            // Find the shortest path that is doable in the remaining time
            NodePath shortestPath = null;
            double shortestDuration = Double.POSITIVE_INFINITY;

            for(int id : possiblePaths.keySet()) {
                NodePath path = possiblePaths.get(id);
                if(id == Location.GOAL.id) {
                    goalPath = possiblePaths.get(Location.GOAL.id);
                    continue;
                }
                // Is this path the shortest so far?
                if(path.getDuration() < shortestDuration) {
                    // Is this path doable?
                    NodePath relativeGoalPath = pathSolver.getPaths(pathSolver.nodeIdToLocation(id), new Location[] {Location.GOAL}).get(Location.GOAL.id);
                    double remainingSeconds = (double)(api.getTimeRemaining().get(1)) / 1000;
                    if(path.getDuration() + TARGET_PROCESSING_SECONDS + relativeGoalPath.getDuration() + GOAL_PROCESSING_SECONDS < remainingSeconds) {
                        shortestPath = path;
                        shortestDuration = path.getDuration();
                    }
                }
            }

            // If we can't do any more targets...
            if(shortestPath == null) {
                break;
            }

            pathFollower.followPath(shortestPath);
            currentLocation = pathSolver.nodeIdToLocation(shortestPath.getNodes().get(shortestPath.getNodes().size() - 1).getId());

            // TODO: Secondary alignment procedure

            api.laserControl(true);
            api.takeTargetSnapshot(pathSolver.locationToTargetId(currentLocation));
            api.laserControl(false);
        }

        // Finish the mission
        api.notifyGoingToGoal();

        if(goalPath != null) {
            pathFollower.followPath(goalPath);
        }

        api.reportMissionCompletion(qrReport);
    }

}
