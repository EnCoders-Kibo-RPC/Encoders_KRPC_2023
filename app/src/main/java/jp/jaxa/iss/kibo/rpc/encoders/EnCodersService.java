package jp.jaxa.iss.kibo.rpc.encoders;

import android.graphics.Bitmap;

import org.opencv.core.Mat;

import jp.jaxa.iss.kibo.rpc.api.KiboRpcService;
import jp.jaxa.iss.kibo.rpc.encoders.PathPlanning.Node;
import jp.jaxa.iss.kibo.rpc.encoders.PathPlanning.NodePath;
import jp.jaxa.iss.kibo.rpc.encoders.PathPlanning.PathSolver;
import jp.jaxa.iss.kibo.rpc.encoders.PathPlanning.PathSolver.Location;

import static jp.jaxa.iss.kibo.rpc.encoders.PathPlanning.PathSolver.Location.START;
import static jp.jaxa.iss.kibo.rpc.encoders.PathPlanning.PathSolver.Location.TARGET_ONE;

import android.util.Log;

//import org.opencv.aruco.*;

/**
 * Class meant to handle commands from the Ground Data System and execute them in Astrobee
 */

public class EnCodersService extends KiboRpcService {
    private final String TAG = this.getClass().getSimpleName();
//    Dictionary dictionary = Aruco.getPredefinedDictionary(Aruco.DICT_5X5_250);
//    DetectorParameters detectorParams = DetectorParameters.create();

    @Override
    protected void runPlan1(){
        api.startMission();

        // TODO: Main robot logic
        PathSolver pathSolver = new PathSolver();

        NodePath path = pathSolver.getPaths(START, new Location[] {TARGET_ONE,})[0];
        String result = "";
        for(Node n : path.getNodes()) {
            result += ("n.getId() = " + n.getId()) + "\n";
        }

        Log.d(TAG, result);
        api.reportMissionCompletion("this simulation sucks");

        // send mission completion
    }

}
