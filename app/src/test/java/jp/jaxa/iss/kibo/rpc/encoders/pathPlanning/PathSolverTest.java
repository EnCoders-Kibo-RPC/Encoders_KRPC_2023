package jp.jaxa.iss.kibo.rpc.encoders.pathPlanning;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static jp.jaxa.iss.kibo.rpc.encoders.pathPlanning.PathSolver.Location.GOAL;
import static jp.jaxa.iss.kibo.rpc.encoders.pathPlanning.PathSolver.Location.QR_CODE;
import static jp.jaxa.iss.kibo.rpc.encoders.pathPlanning.PathSolver.Location.START;
import static org.junit.Assert.assertEquals;

public class PathSolverTest {
    @Test
    public void testPathGeneration() {
        PathSolver pathSolver = new PathSolver();

        NodePath path = pathSolver.getPaths(START, new PathSolver.Location[] {GOAL,}).get(GOAL.id);
        for(Node n : path.getNodes()) {
            System.out.println("n.getId() = " + n.getId());
        }
    }

    @Test
    public void testTargetIdToLocation() {
        PathSolver pathSolver = new PathSolver();

        assertEquals(pathSolver.targetIdToLocation(1), PathSolver.Location.TARGET_ONE);
        assertEquals(pathSolver.targetIdToLocation(2), PathSolver.Location.TARGET_TWO);
        assertEquals(pathSolver.targetIdToLocation(5), PathSolver.Location.TARGET_FIVE);
        assertEquals(pathSolver.targetIdToLocation(6), PathSolver.Location.TARGET_SIX);
    }

    @Test
    public void testTargetIdPathGen() {
        PathSolver pathSolver = new PathSolver();

        List<Integer> activeTargets = new ArrayList<>();
        activeTargets.add(2);
        activeTargets.add(5);
        activeTargets.add(1);

        PathSolver.Location[] targetLocations = new PathSolver.Location[activeTargets.size() + 1];
        targetLocations[0] = PathSolver.Location.GOAL;
        for(int i = 1; i < targetLocations.length; i++) {
            targetLocations[i] = pathSolver.targetIdToLocation(activeTargets.get(i - 1));
        }
        
        for(PathSolver.Location loc : targetLocations) {
            System.out.println("loc = " + loc);
        }

        Map<Integer, NodePath> possiblePaths = pathSolver.getPaths(QR_CODE, targetLocations);
    }
}
