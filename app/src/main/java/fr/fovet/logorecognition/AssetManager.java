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

        float distMin = Float.MAX_VALUE;
        int idxMatch = -1;

        for(int i = 0; i < assetNames.length; i++) {
            matcher.knnMatch(descriptor, assetDescriptors[i], matches, 2);
            float distMoy = refineMatches(matches);

            if(distMoy < distMin) {
                distMin = distMoy;
                idxMatch = i;
            }
        }

        return assetNames[idxMatch];

    }

    private static float refineMatches(DMatchVectorVector oldMatches) {
        // Ratio of Distances
        double RoD = 0.6;
        DMatchVectorVector newMatches = new DMatchVectorVector();

        // Refine results 1: Accept only those matches, where best dist is < RoD
        // of 2nd best match.
        int sz = 0;
        newMatches.resize(oldMatches.size());

        double maxDist = 0.0, minDist = Double.MAX_VALUE; // infinity

        for (int i = 0; i < oldMatches.size(); i++) {
            newMatches.resize(i, 1);
            if (oldMatches.get(i, 0).distance() < RoD
                    * oldMatches.get(i, 1).distance()) {
                newMatches.put(sz, 0, oldMatches.get(i, 0));
                sz++;
                double distance = oldMatches.get(i, 0).distance();
                if (distance < minDist)
                    minDist = distance;
                if (distance > maxDist)
                    maxDist = distance;
            }
        }
        newMatches.resize(sz);

        // Refine results 2: accept only those matches which distance is no more
        // than 3x greater than best match
        sz = 0;
        DMatchVectorVector brandNewMatches = new DMatchVectorVector();
        brandNewMatches.resize(newMatches.size());
        for (int i = 0; i < newMatches.size(); i++) {
            // Since minDist may be equal to 0.0, add some non-zero value
            if (newMatches.get(i, 0).distance() <= 3 * minDist) {
                brandNewMatches.resize(sz, 1);
                brandNewMatches.put(sz, 0, newMatches.get(i, 0));
                sz++;
            }
        }
        brandNewMatches.resize(sz);

        float somme= 0;
        for(int i=0; i < brandNewMatches.size(); i++){
            somme += brandNewMatches.get(i,0).distance();
        }

        return somme / brandNewMatches.size();
    }

}
