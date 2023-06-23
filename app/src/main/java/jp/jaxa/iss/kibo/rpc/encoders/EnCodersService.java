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

public class EnCodersService extends KiboRpcService {
    private final String TAG = this.getClass().getSimpleName();
//    Dictionary dictionary = Aruco.getPredefinedDictionary(Aruco.DICT_5X5_250);
//    DetectorParameters detectorParams = DetectorParameters.create();

    @Override
    protected void runPlan1(){
        api.startMission();

        // TODO: Main robot logic


        // send mission completion
    }

}
