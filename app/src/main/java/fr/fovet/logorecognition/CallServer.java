package fr.fovet.logorecognition;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class CallServer {

    private RequestQueue queue;
    private Context context;
    private Brand brands[];
    private String vocabulary;
    private String vocabularyPath;

    /**
     * Constructeur qui créé une queue pour les appels serveurs
     * Création d'un objet Json
     *
     * @param context Contexte de l'application
     */
    public CallServer(Context context) {

        this.context = context;
        this.queue = Volley.newRequestQueue(this.context);
        new Json();
    }

    /**
     * Renvoie le chemin du fichier vocabulary
     *
     * @return  String  chemin du fichier vocabulary
     */
    public String getVocabularyPath() {
        return vocabularyPath;
    }

    /**
     * Renvoie la liste des objets de type Brand récupérées sur le serveur
     *
     * @return  Brand[] liste des marques
     * @see Brand
     */
    public Brand[] getBrands() {
        return brands;
    }

    public class Json {

        /**
         * Constructeur qui effectue une requête JSON pour récupérer l'index
         * puis parsing du JSON récupéré
         */
        public Json() {
            String urlJson = Utils.SERVER_NAME + "index.json";
            JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, urlJson, null,

                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject json) {
                        try {
                            parseJson(json);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                }
            );

            queue.add(jsonRequest);
        }

        /**
         * Parse le JSON de l'index récupéré du serveur
         * lance la récupération du vocabulary
         *
         * @param  json  JSONObject correspondant au retour du serveur
         */
        void parseJson(JSONObject json) throws JSONException {
            getVocabularyFromServer(json.getString("vocabulary"));

            JSONArray arrayBrands = json.getJSONArray("brands");
            brands = new Brand[arrayBrands.length() + 1];

            for(int i = 0 ; i < arrayBrands.length(); i++){
                JSONObject arrayBrandsObject = arrayBrands.getJSONObject(i);

                JSONArray arrayImages = arrayBrandsObject.getJSONArray("images");
                String imageNames[] = new String[arrayImages.length() + 1];

                for(int j = 0; j < arrayImages.length(); j++) {
                    imageNames[j] = new String(arrayImages.getString(j));
                }

                brands[i] = new Brand(
                        arrayBrandsObject.getString("brandname"),
                        arrayBrandsObject.getString("url"),
                        arrayBrandsObject.getString("classifier"),
                        imageNames,
                        queue,
                        context
                );
            }
        }

        /**
         * Récupération du fichier vocabulary sur le serveur
         *
         * @param  vocabularyFile  chemin du fichier vocabulary récupéré après parsing du json
         */
        void getVocabularyFromServer(final String vocabularyFile) {
            String urlClassifier = Utils.SERVER_NAME + vocabularyFile;
            StringRequest stringRequest = new StringRequest(Request.Method.GET, urlClassifier,

                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            vocabulary = response;
                            vocabularyPath = Utils.stringToCache(context, vocabulary, vocabularyFile).getPath();
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

    }

}
