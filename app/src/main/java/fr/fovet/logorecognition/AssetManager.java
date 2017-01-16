package fr.fovet.logorecognition;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_features2d.KeyPoint;
import org.bytedeco.javacpp.opencv_nonfree.SIFT;

import static org.bytedeco.javacpp.opencv_highgui.imread;

/**
 * Created by Louis on 16/01/2017.
 */
public class AssetManager {

    // SIFT keypoint features
    private static final int N_FEATURES = 0;
    private static final int N_OCTAVE_LAYERS = 3;
    private static final double CONTRAST_THRESHOLD = 0.04;
    private static final double EDGE_THRESHOLD = 10;
    private static final double SIGMA = 1.6;

    SIFT sift;

    String[] assetNames;
    Mat[] assetMat;
    KeyPoint[] assetKeyPoints;
    Mat[] assetDescriptors;

    public AssetManager(Context context) {

        assetNames = new String[]{"apple0.png","burgerking0.png","facebook0.png","google0.png",
            "hp0.png","kfc0.png","leffe0.png","logitech0.png","orange0.png","starbucks0.png",
            "telecomlille0.png","twitter0.png"};

        assetMat = new Mat[assetNames.length];
        assetKeyPoints = new KeyPoint[assetNames.length];
        assetDescriptors = new Mat[assetNames.length];

        sift = new SIFT(N_FEATURES, N_OCTAVE_LAYERS, CONTRAST_THRESHOLD, EDGE_THRESHOLD, SIGMA);

        loadAsset(context);
        detectAsset();
        computeAsset();
    }

    void loadAsset(Context context) {
        for(int i = 0; i < assetNames.length; i++) {
            String refFile = assetNames[i];
            String filePath = Utils.AssetToCache(context, "images" + "/" + refFile, refFile).getPath();
            assetMat[i] = imread(filePath);
        }
    }

    void detectAsset() {
        for(int i = 0; i < assetNames.length; i++) {
            assetKeyPoints[i] = new KeyPoint();
            sift.detect(assetMat[i], assetKeyPoints[i]);
        }
    }

    void computeAsset() {
        for(int i = 0; i < assetNames.length; i++) {
            assetDescriptors[i] = new Mat();
            sift.compute(assetMat[i], assetKeyPoints[i], assetDescriptors[i]);
        }
    }

    void
}
