package jp.jaxa.iss.kibo.rpc.encoders.PathPlanning;

import gov.nasa.arc.astrobee.types.Point;

/**
 * This class will use the NodeGraph to generate paths on the field
 */
public class PathSolver {
    private NodeGraph graph;

    public enum TargetToPointRelativeDistances{ //Distance from a target to a given point
        ONE_TO_ONE,
        ONE_TO_TWO,


    }

    public PathSolver() {
        graph = new NodeGraph();


    }
    public Point getTarget(int i){
        switch (i){
            case 1: //Target 1
                return new Point(11.2625, -10.58, 5.3625);
            case 2: //Target 2
                return new Point(10.513384, -9.085172, 3.76203);
            case 3: //Target 3
                return new Point(10.6031, -7.71007, 3.76093);
            case 4: //Target 4
                return new Point(9.866984, -6.673972, 5.09531);
            case 5: //Target 5
                return new Point(11.102, -8.0304, 5.9076);
            case 6: //Target 6
                return new Point(12.023, -8.989, 4.8305);
            default: //Home Position
                return new Point(9.815, 9.806, 4.293);
        }
    }

    public Point getPoint(int i){
        switch (i){
            case 1: //Point 1
                return new Point(11.2746, -9.92284, 5.2988);
            case 2: //Point 2
                return new Point(10.612, -9.0709 , 4.48);
            case 3: //Point 3
                return new Point(10.71, -7.7, 4.48);
            case 4: //Point 4
                return new Point(10.51, -6.7185, 5.1804);
            case 5: //Point 5
                return new Point(11.355, -8.9929, 4.7818);
            case 6: //Point 6
                return new Point(11.369, -8.5518, 4.48);
            case 7: //Point 7
                return new Point();
            default: //Home Position
                return new Point(9.815, 9.806, 4.293);
        }
    }

}
