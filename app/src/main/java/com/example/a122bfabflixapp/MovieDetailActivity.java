package com.example.a122bfabflixapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class MovieDetailActivity extends AppCompatActivity {

    private String movieId;
    private String gemail;
    private String sessionID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);
        Intent movieDetail = getIntent();
        movieId= movieDetail.getStringExtra("MOVIEID");
        sessionID = movieDetail.getStringExtra("SESSIONID");
        gemail = movieDetail.getStringExtra("EMAIL");

        System.out.println(movieId);
        HTTPrequestGetMovieId();
        configureBackSearch();
    }

    public void configureBackSearch(){
        Button BackSearhButton = (Button) findViewById(R.id.BackToSearchButton);

        BackSearhButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


    }

    public void HTTPrequestGetMovieId(){
        try{

            RequestQueue requestQueue = Volley.newRequestQueue(this);


            String URL = "http://10.0.2.2:4979/api/movies/get/"+ movieId;

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, URL,null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    System.out.println("VOLLEY "+  response);
                    try{
                        if (response.getInt("resultCode") == 210) {
                            Toast.makeText(getApplicationContext(),response.getString("message"), Toast.LENGTH_LONG).show();
                            System.out.println(response);
                            fillImage(response.getJSONObject("movie"));
                            fillGeneral(response.getJSONObject("movie"));
                            fillGenres(response.getJSONObject("movie").getJSONArray("genres"));
                            fillStars(response.getJSONObject("movie").getJSONArray("stars"));

                        }
                        else
                            Toast.makeText(getApplicationContext(),response.getString("message"), Toast.LENGTH_LONG).show();
                    }
                    catch(JSONException e){
                        System.out.println("Error getting data from 200 response");
                    }
                }
            },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            String body;
                            JSONObject jsonObject;
                            //get status code here
                            String statusCode = String.valueOf(error.networkResponse.statusCode);
                            //get response body and parse with appropriate encoding
                            System.out.println(statusCode);
                            if(error.networkResponse.data!=null) {
                                try {
                                    body = new String(error.networkResponse.data,"UTF-8");
                                    System.out.println("this is the body: " +body);

                                    try {
                                        jsonObject = new JSONObject(body);
                                        System.out.println("this is the json obj: " +jsonObject);


                                    }catch (JSONException err){
                                        System.out.println("Error " + err.toString());
                                    }

                                } catch (UnsupportedEncodingException e) {
                                    System.out.println("there was a body error ");
                                    e.printStackTrace();
                                }
                                System.out.println("VOLLEY " +  error.toString());
                            }
                        }
                    }) {

                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            Map<String, String>  params = new HashMap<String, String>();
                            TextView temp = (TextView) findViewById(R.id.LoginEmail);
                            params.put("email", gemail);
                            params.put("sessionID", sessionID);

                            return params;
                        }
            };

            requestQueue.add(jsonObjectRequest);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void fillImage(JSONObject jsonObject){
        try{
            ImageView imageView = (ImageView) findViewById(R.id.MDposterPath);
            Uri absPathUri= Uri.parse("https://image.tmdb.org/t/p/w500"+jsonObject.getString("poster_path"));
            Picasso.get().load(absPathUri).into(imageView);
        }
        catch (Exception e){
            System.out.println("Error loading image");
        }
    }

    public void fillGeneral(JSONObject jsonObject){
        try{
            TextView movieTitle = (TextView) findViewById(R.id.MDmovieTitle);
            movieTitle.setText(jsonObject.getString("title"));

            TextView overview = (TextView) findViewById(R.id.MDoverview);
            if (!jsonObject.isNull("overview"))
                overview.setText(jsonObject.getString("overview"));


            TextView director = (TextView) findViewById(R.id.MDdirector);
            if (!jsonObject.isNull("director"))
                director.setText(jsonObject.getString("director"));


            TextView year = (TextView) findViewById(R.id.MDyear);
            if (!jsonObject.isNull("year"))
                year.setText(jsonObject.get("year").toString());

            TextView rating = (TextView) findViewById(R.id.MDrating);
            rating.setText(jsonObject.get("rating").toString());
            TextView votes = (TextView) findViewById(R.id.MDvotes);
            votes.setText(jsonObject.get("numVotes").toString());

        }
        catch (JSONException e){
            System.out.println("Error getting values from fill general");
        }

    }

    public void fillGenres(JSONArray jsonArray){
        StringBuilder genres = new StringBuilder();
        for (int i = 0; i < jsonArray.length(); ++i){
            try {
                JSONObject temp = jsonArray.getJSONObject(i);
                genres.append(temp.getString("name"));
                genres.append(", ");
            }
            catch (JSONException e){
                System.out.println("error trying to build genre string");
            }
        }
        TextView genresAll = (TextView) findViewById(R.id.MDgenres);
        genresAll.setText(genres.substring(0,genres.length()-2));
    }

    public void fillStars(JSONArray jsonArray){
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < jsonArray.length(); ++i){
            try {
                JSONObject temp = jsonArray.getJSONObject(i);
                stars.append(temp.getString("name"));
                stars.append(", ");
            }
            catch (JSONException e){
                System.out.println("error trying to build star string");
            }
        }
        TextView genres = (TextView) findViewById(R.id.MDstar);
        genres.setText(stars.substring(0,stars.length()-2));

    }
}
