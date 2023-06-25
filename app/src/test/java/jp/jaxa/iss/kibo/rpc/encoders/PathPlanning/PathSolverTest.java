package jp.jaxa.iss.kibo.rpc.encoders.PathPlanning;

import org.junit.Test;

import static jp.jaxa.iss.kibo.rpc.encoders.PathPlanning.PathSolver.Location.GOAL;
import static jp.jaxa.iss.kibo.rpc.encoders.PathPlanning.PathSolver.Location.START;

public class PathSolverTest {
    @Test
    public void testPathGeneration() {
        PathSolver pathSolver = new PathSolver();

        NodePath path = pathSolver.getPaths(START, new PathSolver.Location[] {GOAL,})[0];
        for(Node n : path.getNodes()) {
            System.out.println("n.getId() = " + n.getId());
        }
    }
}
