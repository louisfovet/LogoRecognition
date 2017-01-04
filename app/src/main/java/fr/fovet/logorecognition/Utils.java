package fr.fovet.logorecognition;


import android.graphics.Bitmap;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.opencv_calib3d;
import org.bytedeco.javacpp.opencv_core.*;
import org.bytedeco.javacpp.opencv_features2d.*;
import org.bytedeco.javacpp.opencv_nonfree.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import static org.bytedeco.javacpp.opencv_highgui.imread;

public class Utils {

    public static Mat load(File file, int flags) throws IOException {
        if(!file.exists()) {
            throw new FileNotFoundException("Image file does not exist: " + file.getAbsolutePath());
        }
        Mat image = imread(file.getAbsolutePath(), flags);
        if(image == null || image.empty()) {
            throw new IOException("Couldn't load image: " + file.getAbsolutePath());
        }
        return image;
    }

    public static void callOpenCV(String photoPath) throws IOException {
        int nFeatures = 0;
        int nOctaveLayers = 3;
        double contrastThreshold = 0.03;
        double edgeThreshold = 10;
        double sigma = 1.6;

        Mat photo = Utils.load(new File(photoPath), 1);

        if(photo.empty()) {
            throw new RuntimeException("Cannot find img " + photoPath);
        }

        SIFT sift = new SIFT(nFeatures, nOctaveLayers, contrastThreshold, edgeThreshold, sigma);
        KeyPoint keyPoint = new KeyPoint();
        Mat descriptor = new Mat();

        Loader.load(opencv_calib3d.class);
        sift.detect(photo, keyPoint);
        //sift.compute(photo, keyPoint, descriptor);

    }

    public static Bitmap scaleBitmapDown(Bitmap bitmap, int maxDimension) {

        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int resizedWidth = maxDimension;
        int resizedHeight = maxDimension;

        if (originalHeight > originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
        } else if (originalWidth > originalHeight) {
            resizedWidth = maxDimension;
            resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
        } else if (originalHeight == originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = maxDimension;
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
    }
}
