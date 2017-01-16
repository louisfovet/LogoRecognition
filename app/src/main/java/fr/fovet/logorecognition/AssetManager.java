package fr.fovet.logorecognition;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_features2d.*;
import org.bytedeco.javacpp.opencv_nonfree.SIFT;

import java.util.Arrays;

import static org.bytedeco.javacpp.opencv_highgui.imread;
import static org.bytedeco.javacpp.opencv_core.NORM_L2;

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

    private SIFT sift;

    private String[] assetNames;
    private Mat[] assetMat;
    private KeyPoint[] assetKeyPoints;
    private Mat[] assetDescriptors;

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

    private void loadAsset(Context context) {
        for(int i = 0; i < assetNames.length; i++) {
            String refFile = assetNames[i];
            String filePath = Utils.AssetToCache(context, "images" + "/" + refFile, refFile).getPath();
            assetMat[i] = imread(filePath);
        }
    }

    private void detectAsset() {
        for(int i = 0; i < assetNames.length; i++) {
            assetKeyPoints[i] = new KeyPoint();
            sift.detect(assetMat[i], assetKeyPoints[i]);
        }
    }

    private void computeAsset() {
        for(int i = 0; i < assetNames.length; i++) {
            assetDescriptors[i] = new Mat();
            sift.compute(assetMat[i], assetKeyPoints[i], assetDescriptors[i]);
        }
    }

    public String searchForMatch(Mat descriptor) {

        BFMatcher matcher = new BFMatcher(NORM_L2, false);
        DMatchVectorVector matches = new DMatchVectorVector();

        /*for(int i = 0; i < assetNames.length; i++) {
            matcher.match(descriptor, assetDescriptors[i], matches);
        }*/

        matcher.knnMatch(descriptor, assetDescriptors[0], matches, 2);

        return "test";
    }

    private static DMatch[] toArray(DMatchVectorVector matches) {
        assert matches.size() <= Integer.MAX_VALUE;
        // for the simplicity of the implementation we will assume that number of key points is within Int range.
        int n = (int) matches.size();

        // Convert keyPoints to Scala sequence
        DMatch[] result = new DMatch[n];
        for (int i = 0; i < n; i++) {
            result[i] = new DMatch(matches.);
        }
        return result;
    }

    private static float getScore(DMatchVectorVector matches, int numberToSelect) {
        float score = 0;

        // Convert to Scala collection, and sort
        DMatch[] sorted = toArray(matches);

        Arrays.sort(sorted, (a, b)  {
            return a.lessThan(b) ? -1 : 1;
        });

        for (int i = 0; i < numberToSelect; i ++) {
            score += sorted[i].distance();
        }

        score /= numberToSelect;
        return score;
    }

}
