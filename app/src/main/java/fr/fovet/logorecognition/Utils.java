package fr.fovet.logorecognition;


import android.content.Context;
import android.content.CursorLoader;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.opencv_calib3d;
import org.bytedeco.javacpp.opencv_core.*;
import org.bytedeco.javacpp.opencv_features2d.*;
import org.bytedeco.javacpp.opencv_nonfree.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.bytedeco.javacpp.opencv_highgui.imread;

public class Utils {

    public static final String SERVER_NAME="http://www-rech.telecom-lille.fr/nonfreesift/";
    //public static final String SERVER_NAME="http://www-rech.telecom-lille.fr/freeorb/";

    // SIFT keypoint features
    private static final int N_FEATURES = 0;
    private static final int N_OCTAVE_LAYERS = 3;
    private static final double CONTRAST_THRESHOLD = 0.04;
    private static final double EDGE_THRESHOLD = 10;
    private static final double SIGMA = 1.6;

    /**
     * Récupère le descripteur d'une image donnée (v2 de l'application)
     *
     * @param  context  Contexte de l'application
     * @param  photoPath  Chemin complet de la photo
     * @return  Mat  the image descriptor
     */
    public static Mat getDescriptor(Context context, String photoPath) throws IOException {

        Mat img = imread(photoPath);

        if(img.empty()) {
            throw new RuntimeException("Cannot find img " + photoPath);
        }

        SIFT sift = new SIFT(N_FEATURES, N_OCTAVE_LAYERS, CONTRAST_THRESHOLD, EDGE_THRESHOLD, SIGMA);
        KeyPoint keyPoints = new KeyPoint();
        Mat descriptor = new Mat();

        sift.detect(img, keyPoints);
        sift.compute(img, keyPoints, descriptor);

        Toast.makeText(context, "Nb of detected keypoints:" + keyPoints.capacity(), Toast.LENGTH_LONG).show();

        return descriptor;
    }

    /**
     * Stocke une asset en cache (v2 de l'application)
     *
     * @param  context  Contexte de l'application
     * @param  Path  Chemin où l'on trouve l'asset
     * @param  fileName  Nom que l'on donne à l'image mise en cache
     * @return  File  renvoie l'image mise en cache
     */
    public static File AssetToCache(Context context, String Path, String fileName) {
        InputStream input;
        FileOutputStream output;
        byte[] buffer;
        String filePath = context.getCacheDir() + "/" + fileName;
        File file = new File(filePath);
        AssetManager assetManager = context.getAssets();

        try {
            input = assetManager.open(Path);
            buffer = new byte[input.available()];
            input.read(buffer);
            input.close();

            output = new FileOutputStream(filePath);
            output.write(buffer);
            output.close();
            return file;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Stocke un bitmap en cache
     *
     * @param  context  Contexte de l'application
     * @param  bitmap  image à mettre en cache
     * @param  fileName  Nom que l'on donne à l'image mise en cache
     * @return  File  renvoie l'image mise en cache
     */
    public static File BitmapToCache(Context context, Bitmap bitmap, String fileName) {
        File file = new File(context.getCacheDir(), fileName);

        try {
            file.createNewFile();

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bos);
            byte[] bitmapData = bos.toByteArray();

            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bitmapData);

            fos.flush();
            fos.close();

            return file;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Stocke une string en cache
     *
     * @param  context  Contexte de l'application
     * @param  string  string à mettre en cache
     * @param  fileName  Nom que l'on donne à l'image mise en cache
     * @return  File  renvoie l'image mise en cache
     */
    public static File stringToCache(Context context, String string, String fileName) {
        File file = new File(context.getCacheDir(), fileName);

        try {
            file.createNewFile();

            byte[] stringData = string.getBytes();

            FileOutputStream fos = new FileOutputStream(file);
            fos.write(stringData);

            fos.flush();
            fos.close();

            return file;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Utilisée pour réduire l'image (sinon problèmes de mémoire)
     *
     * @param  bitmap  image à réduire
     * @param  maxDimension  dimension max de l'image
     * @return  Bitmap  renvoie l'image réduite
     */
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

    /**
     * Renvoie le site web associé à une marque
     *
     * @param  match  string correspondant au nom de l'image
     * @return  String  renvoie le site web correspond à la marque
     */
    public static String getWebsite(String match) {
        String website = "";

        switch (match) {
            case "apple0.png": website = "https://www.apple.com";
                break;
            case "burgerking0.png": website = "https://www.burgerking.fr";
                break;
            case "facebook0.png": website = "https://www.facebook.com";
                break;
            case "google0.png": website = "https://www.google.fr";
                break;
            case "hp0.png": website = "http://www8.hp.com/fr/fr/home.html";
                break;
            case "kfc0.png": website = "https://www.kfc.fr";
                break;
            case "leffe0.png": website = "http://www.leffe.com";
                break;
            case "logitech0.png": website = "http://www.logitech.fr/fr-fr";
                break;
            case "orange0.png": website = "http://www.orange.fr";
                break;
            case "starbucks0.png": website = "https://www.starbucks.fr/café";
                break;
            case "telecomlille0.png": website = "http://www.telecom-lille.fr";
                break;
            case "twitter0.png": website = "https://twitter.com";
                break;
            default: website = "http://www.google.fr";
                break;
        }

        return website;
    }

}
