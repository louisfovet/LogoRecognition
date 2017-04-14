package fr.fovet.logorecognition;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.*;
import org.bytedeco.javacpp.opencv_features2d.*;
import org.bytedeco.javacpp.opencv_ml.*;
import org.bytedeco.javacpp.opencv_nonfree.*;

import java.io.IOException;

import static org.bytedeco.javacpp.opencv_core.cvAttrList;
import static org.bytedeco.javacpp.opencv_core.cvOpenFileStorage;
import static org.bytedeco.javacpp.opencv_core.CV_STORAGE_READ;
import static org.bytedeco.javacpp.opencv_core.cvReadByName;
import static org.bytedeco.javacpp.opencv_core.cvReleaseFileStorage;
import static org.bytedeco.javacpp.opencv_highgui.imread;

public class Compute {

    private String bestMatch;
    private String imagePathMatch;
    private String urlMatch;

    /**
     * Renvoie le nom de le marque qui a match avec la photo
     *
     * @return  String  nom de la marque
     */
    public String getBestMatch() {
        return bestMatch;
    }

    /**
     * Renvoie le chemin de l'image de référence de la marque qui a match avec la photo
     *
     * @return  String  chemin de l'image de référence d'une marque
     */
    public String getImagePathMatch() {
        return imagePathMatch;
    }

    /**
     * Renvoie l'url de l'image de référence de la marque qui a match avec la photo
     *
     * @return  String  url de l'image de référence d'une marque
     */
    public String getUrlMatch() {
        return urlMatch;
    }


    /**
     * Effectue l'analyse de la photo
     *
     * @param context Contexte de l'application
     * @param cs objet CallServer qui contient les données pour la comparaison
     * @param filePath chemin complet de la photo à analyser
     */
    public Compute(Context context, CallServer cs, String filePath) throws IOException {

        //prepare BOW descriptor extractor from the vocabulary already computed

        //final String pathToVocabulary = "vocabulary.yml" ; // to be define
        final Mat vocabulary;


        System.out.println("read vocabulary from file... ");
        Loader.load(opencv_core.class);
        String vocabularyPath = cs.getVocabularyPath();
        CvFileStorage storage = cvOpenFileStorage(vocabularyPath, null, CV_STORAGE_READ);
        Pointer p = cvReadByName(storage, null, "vocabulary", cvAttrList());
        CvMat cvMat = new CvMat(p);
        vocabulary = new Mat(cvMat);
        System.out.println("vocabulary loaded " + vocabulary.rows() + " x " + vocabulary.cols());
        cvReleaseFileStorage(storage);


        //create SIFT feature point extracter
        final SIFT detector;
        // default parameters ""opencv2/features2d/features2d.hpp""
        detector = new SIFT(0, 3, 0.04, 10, 1.6);

        //create a matcher with FlannBased Euclidien distance (possible also with BruteForce-Hamming)
        final FlannBasedMatcher matcher;
        matcher = new FlannBasedMatcher();

        //create BoF (or BoW) descriptor extractor
        final BOWImgDescriptorExtractor bowide;
        bowide = new BOWImgDescriptorExtractor(detector.asDescriptorExtractor(), matcher);

        //Set the dictionary with the vocabulary we created in the first step
        bowide.setVocabulary(vocabulary);
        System.out.println("Vocab is set");


        final CvSVM[] classifiers;
        classifiers = new CvSVM[cs.getBrands().length -1];
        for (int i = 0 ; i < cs.getBrands().length -1; i++) {
            classifiers[i] = new CvSVM();
            String classifierFile = cs.getBrands()[i].getClassifierFile();
            classifiers[i].load(classifierFile);
        }

        Mat response_hist = new Mat();
        KeyPoint keypoints = new KeyPoint();
        Mat inputDescriptors = new Mat();

        //  Mat labels = new Mat(imageFiles.length, 1, CV_32SC1);
        //  IntBuffer labelsBuf = labels.createBuffer();


        //System.out.println("path:" + im.getName());

        //String imagePath = Utils.AssetToCache(context, "TestImage/Pepsi_13.jpg", "Coca_12.jpg").getPath();
        Mat imageTest = imread(filePath, 1);
        detector.detectAndCompute(imageTest, Mat.EMPTY, keypoints, inputDescriptors);
        bowide.compute(imageTest, keypoints, response_hist);

        // Finding best match
        float minf = Float.MAX_VALUE;

        long timePrediction = System.currentTimeMillis();
        // loop for all classes
        for (int i = 0; i < cs.getBrands().length-1; i++) {
            // classifier prediction based on reconstructed histogram
            float res = classifiers[i].predict(response_hist, true);
            //System.out.println(class_names[i] + " is " + res);
            if (res < minf) {
                minf = res;
                bestMatch = cs.getBrands()[i].getBrandname();
                imagePathMatch = cs.getBrands()[i].getImagePath();
                urlMatch = cs.getBrands()[i].getUrl();
            }
        }
        timePrediction = System.currentTimeMillis() - timePrediction;
        System.out.println("Image predicted as " + bestMatch + " in " + timePrediction + " ms");

        return;
    }

}
