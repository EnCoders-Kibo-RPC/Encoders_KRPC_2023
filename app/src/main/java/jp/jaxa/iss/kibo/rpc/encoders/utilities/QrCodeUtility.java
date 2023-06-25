package jp.jaxa.iss.kibo.rpc.encoders.utilities;

import android.graphics.Bitmap;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.jaxa.iss.kibo.rpc.api.KiboRpcApi;

public class QrCodeUtility {
    private final String TAG = this.getClass().getSimpleName();

    private static final int MAX_READ_ATTEMPTS = 5;
    private static final float flashBrightness = 0.33f;

    private final KiboRpcApi api;
    private QRCodeReader reader;

    private Map<DecodeHintType, Object> hints; // Hints to pass to the QR Reader
    private Map<String, String> qrMessageMap;

    /**
     * Construct a QrCodeUtility
     *
     * @param apiReference A reference to the KiboRpcApi from the KiboRPCService
     */
    public QrCodeUtility(KiboRpcApi apiReference) {
        this.api = apiReference;

        reader = new QRCodeReader();

        List<BarcodeFormat> possibleFormats = new ArrayList<>();
        possibleFormats.add(BarcodeFormat.QR_CODE);

        hints = new HashMap<>();
        hints.put(DecodeHintType.CHARACTER_SET, "utf-8");
        hints.put(DecodeHintType.POSSIBLE_FORMATS, possibleFormats);

        qrMessageMap = new HashMap<>();
        qrMessageMap.put("JEM", "STAY_AT_JEM");
        qrMessageMap.put("COLUMBUS", "GO_TO_COLUMBUS");
        qrMessageMap.put("RACK1", "CHECK_RACK_1");
        qrMessageMap.put("ASTROBEE", "I_AM_HERE");
        qrMessageMap.put("INTBALL", "LOOKING_FORWARD_TO_SEE_YOU");
        qrMessageMap.put("BLANK", "NO_PROBLEM");
    }

    /**
     * Attempt to read text from a QR Code using the Astrobee's NavCam
     *
     * @return The text contained in the QR_Code. A blank string will be returned if the read fails.
     */
    public String readQrMessage() {
        for(int attempt = 0; attempt < MAX_READ_ATTEMPTS; attempt++) {
            // Adjust the brightness each attempt
            api.flashlightControlFront(((attempt + 1) % 3) * flashBrightness);

            BinaryBitmap image = matToBinaryBitmap(api.getMatNavCam());

            try {
                Result result = reader.decode(image, hints);
                String data = result.getText();
                if(data != null) {
                    Log.wtf(TAG, "QR Data: " + data);
                    return data;
                }
            } catch(Exception e) {
                Log.wtf(TAG, "ERROR Reading QR Code...");
                e.printStackTrace();
            }
        }

        Log.wtf(TAG, "FAILED to read QR Code. Aborting...");
        return "BLANK";
    }

    /**
     * Convert a Mat image to a BinaryBitmap image
     *
     * @param mat The matImage
     * @return The converted image
     */
    private BinaryBitmap matToBinaryBitmap(Mat mat) {
        Bitmap bitmap = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, bitmap);

        int[] pixels = new int[bitmap.getWidth() * bitmap.getHeight()];
        bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        LuminanceSource source = new RGBLuminanceSource(bitmap.getWidth(), bitmap.getHeight(), pixels);
        return new BinaryBitmap(new HybridBinarizer(source));
    }

    /**
     * Convert a QR message to a Report Message
     *
     * @param message
     * @return
     */
    public String decodeQrMessage(String message) {
        return qrMessageMap.getOrDefault(message, "");
    }

}
