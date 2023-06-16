package jp.jaxa.iss.kibo.rpc.encoders;

import android.util.Log;

import jp.jaxa.iss.kibo.rpc.api.KiboRpcService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gov.nasa.arc.astrobee.Result;
import gov.nasa.arc.astrobee.types.Point;
import gov.nasa.arc.astrobee.types.Quaternion;

import org.opencv.objdetect.QRCodeDetector;
import org.opencv.core.*;
import org.opencv.utils.Converters;
//import org.opencv.aruco.*;

/**
 * Class meant to handle commands from the Ground Data System and execute them in Astrobee
 */

public class YourService extends KiboRpcService {
    private final String TAG = this.getClass().getSimpleName();
//    Dictionary dictionary = Aruco.getPredefinedDictionary(Aruco.DICT_5X5_250);
//    DetectorParameters detectorParams = DetectorParameters.create();

    @Override
    protected void runPlan1(){
        api.startMission();
        int loop_counter = 0;

        while (true){
            // get the list of active target id
            List<Integer> list = api.getActiveTargets();
            Log.i(TAG, "targets: " + list);
            Log.i(TAG, "Starting Point: " + api.getRobotKinematics().getPosition());

            //move to intermediate point to avoid KOZ1
            Point intermediatePoint = new Point(10.35d, -9.794d, 5.4d);
            Quaternion intQuaternion = new Quaternion(0f, 0f, 0f, 1f);
            Log.i(TAG, "1st move inside KIZ1?: " + insideKIZ1(intermediatePoint));
            api.moveTo(intermediatePoint, intQuaternion, false);

            //Move to point 1
            Point point = new Point(11.2d, -10.0d, 5.44d);
            Quaternion quaternion = new Quaternion(0f, 0f, -0.707f, 0.707f);
            Log.i(TAG, "2nd move inside KIZ1?: " + insideKIZ1(point));

            Result result = api.moveTo(point, quaternion, false);

            final int LOOP_MAX = 5;
            int loopCounter = 0;
            while(!result.hasSucceeded() && loopCounter < LOOP_MAX) {
                result = api.moveTo(point, quaternion, true);
                ++loopCounter;
            }

            // get a camera image
            Mat image = api.getMatNavCam();
//            List<Mat> corners = new ArrayList<Mat>();
//            Mat ids = new Mat();
//            Aruco.detectMarkers(image, dictionary, corners, ids, detectorParams);
//            Log.i(TAG, "Detected id's: " + ids.dump());
            Mat imageCopy = new Mat();
//            image.copyTo(imageCopy);
//            Aruco.drawDetectedMarkers(imageCopy, corners);
            api.saveMatImage(image, "target1.png");
            api.saveMatImage(imageCopy, "detectedAruco.png");

            // irradiate the laser
            api.laserControl(true);

            // take active target snapshots
            api.takeTargetSnapshot(1);

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
            if (loop_counter == 1){
                break;
            }
        }
        // turn on the front flash light
        api.flashlightControlFront(0.05f);

        // get QR code content
        Point qrPoint = new Point(11.381944d, -8.566172d, 4.4f);
        Log.i(TAG, "QR point in KIZ1?: " + insideKIZ1(qrPoint));
        Quaternion qrQuat = new Quaternion(0f, 0.707f, 0f, 0.707f);
        api.moveTo(qrPoint, qrQuat, false);

        Mat image = api.getMatNavCam();
        api.saveMatImage(image, "qr.png");
        String mQrContent = yourMethod(image);

        // turn off the front flash light
        api.flashlightControlFront(0.00f);

        //move below KOZ3
        Point intermediatePoint = new Point(11.5d, -9d, 5.5d);
        Quaternion quaternion = new Quaternion(0f, 0f, 0f, 1f);
        api.moveTo(intermediatePoint, quaternion, false);

        //move past KOZ4
        Point intPoint = new Point(11.5, -7.75d, 5.5d);
        api.moveTo(intPoint, quaternion, true);

        Point finalPoint = new Point(11d, -7.5d, 5.5d);
        api.moveTo(finalPoint, quaternion, true);

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

    // You can add your method
    private String yourMethod(Mat img){
        QRCodeDetector detector = new QRCodeDetector();
        String detected = detector.detectAndDecode(img);
        Log.i(TAG, detected + " was detected");
        Map<String, String> qrMap = new HashMap<>();
        qrMap.put("JEM", "STAY_AT_JEM");
        qrMap.put("COLUMBUS", "GO_TO_COLUMBUS");
        qrMap.put("RACK1", "CHECK_RACK_1");
        qrMap.put("ASTROBEE", "I_AM_HERE");
        qrMap.put("INTBALL", "LOOKING_FORWARD_TO_SEE_YOU");
        qrMap.put("BLANK", "NO_PROBLEM");
        return qrMap.get(detected);
    }

    private boolean insideKIZ1(Point point) {
        if (point.getX() > 10.3f && point.getX() < 11.55f && point.getY() > -10.2f && point.getY() < -6.0f && point.getZ() > 4.32f && point.getZ() < 5.57f) {
            return true;
        } else {
            return false;
        }
    }



    private boolean passesThroughKOZ1(Point currentPos, Point goalPos) {
        return false;
    }
}
