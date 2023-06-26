package jp.jaxa.iss.kibo.rpc.encoders.pathPlanning;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.ToDoubleBiFunction;

import gov.nasa.arc.astrobee.types.Point;
import gov.nasa.arc.astrobee.types.Quaternion;
import jp.jaxa.iss.kibo.rpc.encoders.utilities.TrapezoidProfiler;

/**
 * This class will use the NodeGraph to generate paths on the field
 */
public class PathSolver {
    private static final double ANGLE_BUFFER_SECONDS = 7;

    private final TrapezoidProfiler profiler;
    private NodeGraph graph;

    public PathSolver(double maxVelocity, double maxAcceleration) {
        profiler = new TrapezoidProfiler(maxVelocity, maxAcceleration);

        List<Node> nodes = new ArrayList<Node>();

        // Location Nodes
        Node START = new Node(
            Location.START.id,
            new Point(9.815, -9.806, 4.293),
            new Quaternion(1, 0, 0, 0)
        );
        nodes.add(START);
        Node GOAL = new Node(
            Location.GOAL.id,
            new Point(11.143, -6.7607, 4.9654),
            new Quaternion(0.0f, 0.0f, -0.707f, 0.707f)
        );
        nodes.add(GOAL);
        Node TARGET_ONE = new Node(
            Location.TARGET_ONE.id,
            new Point(11.151, -9.946, 5.3053),
            new Quaternion(0.5f, -0.5f, -0.5f, 0.5f)
        );
        nodes.add(TARGET_ONE);
        Node TARGET_TWO = new Node(
            Location.TARGET_TWO.id,
            new Point(10.625, -9.1424, 4.542),
            new Quaternion(0, 0.707f, 0, 0.707f)
        );
        nodes.add(TARGET_TWO);
        Node TARGET_THREE = new Node(
            Location.TARGET_THREE.id,
            new Point(10.546, -7.8245, 4.542),
            new Quaternion(0.5f, 0.5f, -0.5f, 0.5f)
        );
        nodes.add(TARGET_THREE);
        Node TARGET_FOUR = new Node(
            Location.TARGET_FOUR.id,
            new Point(10.507, -6.6168, 5.2064),
            new Quaternion(0, 0, 1, 0)
        );
        nodes.add(TARGET_FOUR);
        Node TARGET_FIVE = new Node(
            Location.TARGET_FIVE.id,
            new Point(10.991, -8.0876, 5.3776),
            new Quaternion(0, -0.707f, 0, 0.707f)
        );
        nodes.add(TARGET_FIVE);
        Node TARGET_SIX = new Node(
            Location.TARGET_SIX.id,
            new Point(11.333, -8.9318, 4.7194),
            new Quaternion(1, 0, 0, 0)
        );
        nodes.add(TARGET_SIX);
        Node QR_CODE = new Node(
            Location.QR_CODE.id,
            new Point(11.268, -8.6146, 4.502),
            new Quaternion(-0.707f, 0, 0.707f, 0)
        );
        nodes.add(QR_CODE);

        // Navigation Nodes
        Node nav1 = new Node(
            10,
            new Point(10.503, -9.806, 4.6492),
            new Quaternion(1, 0, 0, 0)
        );
        nodes.add(nav1);
        Node nav2 = new Node(
            11,
            new Point(10.503, -9.806, 5.3049),
            new Quaternion(0f, 0f, 0f, 0f)
        );
        nodes.add(nav2);
        Node nav3 = new Node(
            12,
            new Point(11.307, -9.5879, 4.5656),
            new Quaternion(0f, 0f, 0f, 0f)
        );
        nodes.add(nav3);
        Node nav4 = new Node(
            13,
            new Point(11.321, -9.5582, 5.3049),
            new Quaternion(0f, 0f, 0f, 0f)
        );
        nodes.add(nav4);
        Node nav5 = new Node(
            14,
            new Point(10.503, -7.8296, 5.3517),
            new Quaternion(0f, 0f, 0f, 0f)
        );
        nodes.add(nav5);
        Node nav6 = new Node(
            15,
            new Point(10.503, -8.7436, 5.3049),
            new Quaternion(0f, 0f, 0f, 0f)
        );
        nodes.add(nav6);
        Node nav7 = new Node(
            16,
            new Point(11.256, -8.864, 5.3049),
            new Quaternion(0f, 0f, 0f, 0f)
        );
        nodes.add(nav7);
        Node nav8 = new Node(
            17,
            new Point(11.328, -8.6004, 4.9295),
            new Quaternion(0f, 0f, 0f, 0f)
        );
        nodes.add(nav8);
        Node nav9 = new Node(
            18,
            new Point(11.326, -9.9899, 4.9033),
            new Quaternion(0f, 0f, 0f, 0f)
        );
        nodes.add(nav9);
        Node nav10 = new Node(
            19,
            new Point(11.147, -7.3401, 5.3603),
            new Quaternion(0f, 0f, 0f, 0f)
        );
        nodes.add(nav10);
        Node nav11 = new Node(
            20,
            new Point(11.147, -7.3401, 4.8316),
            new Quaternion(0f, 0f, 0f, 0f)
        );
        nodes.add(nav11);

        graph = new NodeGraph(nodes);

        // Add connections
        graph.addConnection(START, nav1);

        graph.addConnection(nav1, TARGET_TWO);
        graph.addConnection(nav1, nav2);
        graph.addConnection(nav1, nav3);
        graph.addConnection(nav1, nav6);

        graph.addConnection(nav2, TARGET_ONE);
        graph.addConnection(nav2, TARGET_FOUR);
        graph.addConnection(nav2, TARGET_FIVE);
        graph.addConnection(nav2, nav4);
        graph.addConnection(nav2, nav5);
        graph.addConnection(nav2, nav6);
        graph.addConnection(nav2, nav7);
        graph.addConnection(nav2, nav10);

        graph.addConnection(nav3, QR_CODE);
        graph.addConnection(nav3, TARGET_SIX);
        graph.addConnection(nav3, nav4);
        graph.addConnection(nav3, nav6);
        graph.addConnection(nav3, nav7);
        graph.addConnection(nav3, nav8);

        graph.addConnection(nav4, QR_CODE);
        graph.addConnection(nav4, TARGET_ONE);
        graph.addConnection(nav4, TARGET_FIVE);
        graph.addConnection(nav4, TARGET_SIX);
        graph.addConnection(nav4, nav5);
        graph.addConnection(nav4, nav6);
        graph.addConnection(nav4, nav7);
        graph.addConnection(nav4, nav8);
        graph.addConnection(nav4, nav10);

        graph.addConnection(nav5, TARGET_ONE);
        graph.addConnection(nav5, TARGET_THREE);
        graph.addConnection(nav5, TARGET_FOUR);
        graph.addConnection(nav5, TARGET_FIVE);
        graph.addConnection(nav5, nav6);
        graph.addConnection(nav5, nav7);
        graph.addConnection(nav5, nav10);

        graph.addConnection(nav6, TARGET_ONE);
        graph.addConnection(nav6, TARGET_TWO);
        graph.addConnection(nav6, TARGET_FOUR);
        graph.addConnection(nav6, TARGET_FIVE);
        graph.addConnection(nav6, nav7);
        graph.addConnection(nav6, nav8);
        graph.addConnection(nav6, nav10);

        graph.addConnection(nav7, QR_CODE);
        graph.addConnection(nav7, TARGET_ONE);
        graph.addConnection(nav7, TARGET_FIVE);
        graph.addConnection(nav7, TARGET_SIX);
        graph.addConnection(nav7, nav8);
        graph.addConnection(nav7, nav10);

        graph.addConnection(nav8, QR_CODE);
        graph.addConnection(nav8, TARGET_SIX);
        graph.addConnection(nav8, nav9);
        graph.addConnection(nav8, nav11);

        graph.addConnection(nav9, TARGET_THREE);
        graph.addConnection(nav9, nav11);
        graph.addConnection(nav8, TARGET_SIX);

        graph.addConnection(nav10, GOAL);
        graph.addConnection(nav10, TARGET_FOUR);
        graph.addConnection(nav10, TARGET_FIVE);
        graph.addConnection(nav10, nav11);

        graph.addConnection(nav11, GOAL);
        graph.addConnection(nav11, TARGET_THREE);

        graph.addConnection(TARGET_ONE, TARGET_FIVE);

        graph.addConnection(TARGET_FOUR, GOAL);

        graph.addConnection(TARGET_SIX, QR_CODE);
    }

    public Map<Integer, NodePath> getPaths(Location start, Location[] endpoints) {
        Set<Integer> endIdSet = new HashSet<>();
        for(Location loc : endpoints) {
            endIdSet.add(loc.id);
        }

        return graph.shortestPath(start.id, endIdSet, new ToDoubleBiFunction<Node, Node>() {
            @Override
            public double applyAsDouble(Node node, Node node2) {
                return profiler.calculateTravelTime(node.distance(node2)) + ANGLE_BUFFER_SECONDS;
            }
        });
    }

    public Location targetIdToLocation(int targetId) {
        if(targetId >= 1 && targetId <= 6) {
            return Location.values()[targetId + 1];
        } else {
            return Location.INVALID;
        }
    }

    public int locationToTargetId(Location location) {
        return location.ordinal() - 1;
    }

    public Location nodeIdToLocation(int nodeId) {
        if(nodeId < 1 || nodeId > 20) {
            return Location.INVALID;
        } else {
            return Location.values()[nodeId - 1];
        }
    }


    /**
     * Locations of interest on the field
     */
    public enum Location {
        START(1),
        GOAL(2),
        TARGET_ONE(3),
        TARGET_TWO(4),
        TARGET_THREE(5),
        TARGET_FOUR(6),
        TARGET_FIVE(7),
        TARGET_SIX(8),
        QR_CODE(9),
        NAV_1(10),
        NAV_2(11),
        NAV_3(12),
        NAV_4(13),
        NAV_5(14),
        NAV_6(15),
        NAV_7(16),
        NAV_8(17),
        NAV_9(18),
        NAV_10(19),
        NAV_11(20),
        INVALID(-1);

        public final int id;

        Location(int id) {
            this.id = id;
        }

    }
}
