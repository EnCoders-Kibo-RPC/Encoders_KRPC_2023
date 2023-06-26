package jp.jaxa.iss.kibo.rpc.encoders;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;

import gov.nasa.arc.astrobee.types.Point;
import gov.nasa.arc.astrobee.types.Quaternion;
import jp.jaxa.iss.kibo.rpc.api.KiboRpcService;
import jp.jaxa.iss.kibo.rpc.encoders.pathPlanning.Node;
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

    private static final double TARGET_PROCESSING_SECONDS = 2;
    private static final double GOAL_PROCESSING_SECONDS = 1;

    // Robot constraints in m/s and m/s^2 respectively
    private static final double MAX_VELOCITY = 0.5;
    private static final double MAX_ACCELERATION = 0.008;

    private static final int GAME_DURATION = 300;
    private static final int PHASE_DURATION = 120;

    private static final int MAX_LOOP_EXECUTIONS = 20;

    private HashMap<Location, Integer> targetPointValues;

    public EnCodersService() {
        super();

        targetPointValues = new HashMap<>();
        targetPointValues.put(Location.TARGET_ONE, 30);
        targetPointValues.put(Location.TARGET_TWO, 20);
        targetPointValues.put(Location.TARGET_THREE, 40);
        targetPointValues.put(Location.TARGET_FOUR, 20);
        targetPointValues.put(Location.TARGET_FIVE, 30);
        targetPointValues.put(Location.TARGET_SIX, 30);
    }

    @Override
    protected void runPlan1(){
        api.startMission();

        // Initialize objects
        PathSolver pathSolver = new PathSolver(MAX_VELOCITY, MAX_ACCELERATION);
        PathFollower pathFollower = new PathFollower(api);
        QrCodeUtility qrUtility = new QrCodeUtility(api);

        // Go to and read QR Code
        pathFollower.followPath(pathSolver.getPaths(Location.START, new Location[] {Location.QR_CODE}).get(Location.QR_CODE.id));
        String qrReport = qrUtility.decodeQrMessage(qrUtility.readQrMessage());

        Location currentLocation = Location.QR_CODE;
        NodePath goalPath = null;

        int phase = 1;
        double lastPhaseStartSeconds = 0;

        // Main loop:
        for(int j = 0; j < MAX_LOOP_EXECUTIONS; j++) {

            List<Integer> activeTargets = api.getActiveTargets();
            Location[] targetLocations = new Location[activeTargets.size() + 1];
            targetLocations[0] = Location.GOAL;
            for(int i = 1; i < targetLocations.length; i++) {
                targetLocations[i] = pathSolver.targetIdToLocation(activeTargets.get(i - 1));
            }

            Map<Integer, NodePath> paths = pathSolver.getPaths(currentLocation, targetLocations);

            Map<Integer, NodePath> possiblePaths = new HashMap<>();
            double remainingSeconds = (double)(api.getTimeRemaining().get(1)) / 1000;
            double phaseTimeLeft = PHASE_DURATION - (getTimeSeconds() - lastPhaseStartSeconds);

            if(phaseTimeLeft <= 0) {
                lastPhaseStartSeconds = getTimeSeconds();
                phase++;
                phaseTimeLeft = PHASE_DURATION;
            }

            final int currentPhase = (GAME_DURATION - (int)remainingSeconds)/PHASE_DURATION;
            boolean isLastPhase = currentPhase == 2;


            // Filter to only paths that can be completed in the time limit
            for(int id : paths.keySet()) {
                NodePath path = paths.get(id);
                if(id == Location.GOAL.id) {
                    goalPath = paths.get(Location.GOAL.id);
                    continue;
                }
                // Is this path doable from a goal standpoint?
                NodePath relativeGoalPath = pathSolver.getPaths(pathSolver.nodeIdToLocation(id), new Location[] {Location.GOAL}).get(Location.GOAL.id);
                if(path.getDuration() + TARGET_PROCESSING_SECONDS + relativeGoalPath.getDuration() + GOAL_PROCESSING_SECONDS < remainingSeconds) {

                    // Is this path doable from a phase standpoint?
                    if(path.getDuration() + TARGET_PROCESSING_SECONDS <= phaseTimeLeft) {
                        possiblePaths.put(id, path);
                    }
                }
            }

            Log.wtf(TAG, "Current location: " + currentLocation.toString());
            for(int key : possiblePaths.keySet()) {
                Log.wtf(TAG, "POSSIBLE PATH!!! --> " + key);
                Log.wtf(TAG, key + ": " + possiblePaths.get(key).getNodes().size());
            }
            NodePath idealPath = null;

            if(possiblePaths.size() == 1) {
                for(int key : possiblePaths.keySet()) {
                    idealPath = possiblePaths.get(key);
                }
            } else if(possiblePaths.size() == 2) {
                NodePath[] bothPaths = new NodePath[2];

                int insertIdx = 0;
                for(int id : possiblePaths.keySet()) {
                    bothPaths[insertIdx++] = possiblePaths.get(id);
                }

                Location dest1 = pathSolver.nodeIdToLocation(bothPaths[0].getNodes().get(bothPaths[0].getNodes().size() - 1).getId());
                Location dest2 = pathSolver.nodeIdToLocation(bothPaths[1].getNodes().get(bothPaths[1].getNodes().size() - 1).getId());

                Map<Integer, NodePath> nextPaths = pathSolver.getPaths(dest1, new Location[] {dest2, Location.GOAL});

                double timeToDoBoth =
                    bothPaths[0].getDuration() +
                    nextPaths.get(dest2.id).getDuration() +
                    TARGET_PROCESSING_SECONDS * 2
                ;

                // Can you do both and then the goal?
                if(timeToDoBoth + nextPaths.get(Location.GOAL.id).getDuration() + GOAL_PROCESSING_SECONDS <= remainingSeconds) {
                    // Check phase stuff
                    if(timeToDoBoth <= phaseTimeLeft) {
                        // Do both, do the closest one
                        if(bothPaths[0].getDuration() <= bothPaths[1].getDuration()) {
                            idealPath = bothPaths[0];
                        } else {
                            idealPath = bothPaths[1];
                        }
                    } else {
                        // Choose the one worth more points
                        if(targetPointValues.get(dest1) > targetPointValues.get(dest2)) {
                            idealPath = bothPaths[0];
                        } else if(targetPointValues.get(dest2) > targetPointValues.get(dest1)) {
                            idealPath = bothPaths[1];
                        } else {
                            // Choose the closer one if they are equal
                            if(bothPaths[0].getDuration() <= bothPaths[1].getDuration()) {
                                idealPath = bothPaths[0];
                            } else {
                                idealPath = bothPaths[1];
                            }
                        }
                    }
                } else {
                    // Choose the one worth more points
                    if(targetPointValues.get(dest1) > targetPointValues.get(dest2)) {
                        idealPath = bothPaths[0];
                    } else if(targetPointValues.get(dest2) > targetPointValues.get(dest1)) {
                        idealPath = bothPaths[1];
                    } else {
                        // Choose the closer one if they are equal
                        if(bothPaths[0].getDuration() <= bothPaths[1].getDuration()) {
                            idealPath = bothPaths[0];
                        } else {
                            idealPath = bothPaths[1];
                        }
                    }
                }
            } else {
                if((GAME_DURATION - getTimeSeconds()) - goalPath.getDuration() <= 30) {
                    break;
                } else {
                    if(currentLocation != Location.NAV_6) {
                        // Move to a central area
                        NodePath path = pathSolver.getPaths(currentLocation, new Location[] {Location.NAV_6}).get(Location.NAV_6.id);
                        Node fin = pathFollower.followPath(path, new BooleanSupplier() {
                            @Override
                            public boolean getAsBoolean() {
                                // Abort if the phase changes
                                return (GAME_DURATION - (int)(api.getTimeRemaining().get(1)/1000))/PHASE_DURATION != currentPhase;
                            }
                        });

                        if(fin != null) {
                            currentLocation = pathSolver.nodeIdToLocation(fin.getId());
                        }
                    }

                    // Wait out the rest of the phase in the central location
                    if((api.getTimeRemaining().get(1)/1000)/PHASE_DURATION == currentPhase) {
                        try {

                            int phaseSecondsLeft = (int)Math.ceil(PHASE_DURATION - (getTimeSeconds() - lastPhaseStartSeconds));
                            Thread.sleep(phaseSecondsLeft * 1000);
                            continue;
                        } catch(Exception ex) {
                            ex.printStackTrace();
                        }
                    } else {
                        continue;
                    }

                }
            }

            Node finish = pathFollower.followPath(idealPath);
            if(finish != null) {
                currentLocation = pathSolver.nodeIdToLocation(finish.getId());
            }

            // TODO: Secondary alignment procedure

            api.laserControl(true);
            try {
                Thread.sleep(1000);
            } catch(Exception ex) {
                ex.printStackTrace();
            }
            if(activeTargets.size() == 1) {

            }
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

    private double getTimeSeconds() {
        return (GAME_DURATION - api.getTimeRemaining().get(1)) / 1000.0;
    }


}
