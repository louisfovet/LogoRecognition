package fr.fovet.logorecognition;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;


public class Brand {

    private String brandname;
    private String url;
    private String classifierPath;
    private String classifier;
    private String classifierFile;
    private String imagesNames[];
    private Bitmap imageRef;
    private String imagePath;
    private RequestQueue queue;
    private Context context;

    /**
     * Constructeur qui va setter des attributs selon les paramètres donnés
     * puis récupération du classifier de la marque
     * ainsi que de l'image de référence
     *
     * @param brandname Nom de la marque
     * @param url URL du site web de la marque
     * @param classifierPath chemin où récupérer le classifier de la marque
     * @param imagesNames noms des images de la marque
     * @param queue queue pour effectuer des requêtes serveur
     * @param context Contexte de l'application
     */
    public Brand(String brandname, String url, String classifierPath, String imagesNames[],
                 RequestQueue queue, Context context) {
        this.brandname = brandname;
        this.url = url;
        this.classifierPath = classifierPath;
        this.imagesNames = imagesNames;
        this.queue = queue;
        this.context = context;

        getClassifierFromServer();
        getBitmapFromServer();
    }

    /**
     * Renvoie le nom de la marque
     *
     * @return  String nom de la marque
     */
    public String getBrandname() {
        return brandname;
    }

    /**
     * Renvoie le classifier associé à la marque
     *
     * @return  String classifier
     */
    public String getClassifierFile() {
        return classifierFile;
    }

    /**
     * Renvoie le chemin de l'image de référence de la marque
     *
     * @return  String chemin de l'image de référence
     */
    public String getImagePath() {
        return imagePath;
    }

    /**
     * Renvoie l'url du site web associé à la marque
     *
     * @return  String URL du site web
     */
    public String getUrl() {
        return url;
    }

    /**
     * Récupération du classifier sur le serveur
     * et mise en cache du classifier
     */
    void getClassifierFromServer() {
        String urlClassifier = Utils.SERVER_NAME + "classifiers/" + classifierPath;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, urlClassifier,

                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        classifier = response;
                        classifierFile = Utils.stringToCache(context, classifier, classifierPath).getPath();
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                }
        );

        queue.add(stringRequest);
    }

    /**
     * Récupération de l'image de référence sur le serveur
     * et mise en cache de cette image
     */
    void getBitmapFromServer() {
        String urlImage = Utils.SERVER_NAME + "train-images/" + imagesNames[0];
        ImageRequest imageRequest = new ImageRequest(urlImage,

                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap bitmap) {
                        imageRef = bitmap;
                        imagePath = Utils.BitmapToCache(context, imageRef, imagesNames[0]).getPath();
                    }
                },
                500, 500, ImageView.ScaleType.CENTER, Bitmap.Config.RGB_565,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                }
        );

        queue.add(imageRequest);
    }

}
