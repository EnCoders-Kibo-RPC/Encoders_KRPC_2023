package jp.jaxa.iss.kibo.rpc.encoder;

import jp.jaxa.iss.kibo.rpc.api.KiboRpcService;

import java.util.List;
import gov.nasa.arc.astrobee.Result;
import gov.nasa.arc.astrobee.android.gs.MessageType;
import gov.nasa.arc.astrobee.types.Point;
import gov.nasa.arc.astrobee.types.Quaternion;

import org.opencv.core.Mat;
import org.opencv.objdetect.QRCodeDetector;

/**
 * Class meant to handle commands from the Ground Data System and execute them in Astrobee
 */

public class YourService extends KiboRpcService {
    @Override
    protected void runPlan1(){
        api.startMission();
        int loop_counter = 0;

        while (true){
            // get the list of active target id
            List<Integer> list = api.getActiveTargets();

            // move to a point
            Point point = new Point(10.4d, -10.1d, 4.47d);
            Quaternion quaternion = new Quaternion(0f, 0f, 0f, 1f);
            Result result = api.moveTo(point, quaternion, false);

            // get a camera image
            Mat image = api.getMatNavCam();

            // irradiate the laser
            api.laserControl(true);

            // take active target snapshots
            int target_id = 1;
            api.takeTargetSnapshot(target_id);

            /* ************************************************ */
            /* write your own code and repair the ammonia leak! */
            /* ************************************************ */

            // get remaining active time and mission time
            List<Long> timeRemaining = api.getTimeRemaining();

            // check the remaining milliseconds of mission time
            if (timeRemaining.get(1) < 60000){
                break;
            }

            loop_counter++;
            if (loop_counter == 2){
                break;
            }
        }
        // turn on the front flash light
        api.flashlightControlFront(0.05f);

        // get QR code content
        Mat image = api.getMatNavCam();
        String mQrContent = yourMethod(image);

        // turn off the front flash light
        api.flashlightControlFront(0.00f);

        // notify that astrobee is heading to the goal
        api.notifyGoingToGoal();

        /* ********************************************************** */
        /* write your own code to move Astrobee to the goal positiion */
        /* ********************************************************** */
        Point goalPos = new Point(11.143d, -6.7607d, 4.9654d);
        Quaternion goalQuat = new Quaternion(0f, 0f, -0.707f, 0.707f);
        api.moveTo(goalPos, goalQuat, false);

        // send mission completion
        api.reportMissionCompletion(mQrContent);
    }

    @Override
    protected void runPlan2(){
        // write your plan 2 here
    }

    @Override
    protected void runPlan3(){
        // write your plan 3 here
    }

    // You can add your method
    private String yourMethod(Mat img){
        QRCodeDetector detector = new QRCodeDetector();
        String detected = detector.detectAndDecode(img);
        return detected;
    }
}
