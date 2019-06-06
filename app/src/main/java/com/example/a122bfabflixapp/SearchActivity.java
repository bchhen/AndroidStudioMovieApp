package com.example.a122bfabflixapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SearchActivity extends AppCompatActivity {

    //private JSONArray movies;
    private ArrayList<JSONObject> movies;
    private String sessionID;
    private String gemail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Intent searchIntent = getIntent();
        sessionID = searchIntent.getStringExtra("SESSIONID");
        gemail = searchIntent.getStringExtra("EMAIL");

        configureSearchButton();
    }

    public void configureSearchButton(){
        Button searchButton = (Button) findViewById(R.id.SearchButton);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HTTPrequestSearch();
            }
        });

    }
    class CustomAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return movies.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            view = getLayoutInflater().inflate(R.layout.custom_movie_layout, null);

            TextView title = (TextView) view.findViewById(R.id.MovieTitle);
            TextView director = (TextView) view.findViewById(R.id.director);
            TextView rating = (TextView) view.findViewById(R.id.rating);
            TextView votes  = (TextView) view.findViewById(R.id.votes);

            try{
                JSONObject temp = movies.get(i);
                System.out.println(temp+"\n");
                Integer tempint = temp.getInt("numVotes");
                String tempvotes = "Votes:"+tempint.toString();


                title.setText(temp.getString("title"));
                director.setText(temp.getString("director"));
                rating.setText(temp.get("rating").toString());
                votes.setText(tempvotes);
            }
            catch (Exception e){
                //e.printStackTrace();
                System.out.println("Cannot get info from jsonObj (movie list)");
            }

            return view;
        }
    }


    public void HTTPrequestSearch(){
        try{
            if (movies != null)
                movies.clear();
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            String URL2  = "http://www.mocky.io/v2/5cf759bf3000009629a37cbc?fbclid=IwAR2FrbA86LU8lceG_abd9izI2Y787NpaHJupHreQwGpiTLHkleiC_djMQxk";

            TextView searchTitle = (TextView) findViewById(R.id.SearchTitle);

            String URL = "http://10.0.2.2:4979/api/movies/search?title="+ searchTitle.getText().toString();

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, URL,null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    System.out.println("VOLLEY "+  response);
                    try{
                        if (response.getInt("resultCode") == 210) {
                            Toast.makeText(getApplicationContext(),response.getString("message"), Toast.LENGTH_LONG).show();
                            System.out.println(response);
                            movies = convertJSONArrToArrList(response.getJSONArray("movies"));
                            ListView listView = (ListView) findViewById(R.id.MovieList);
                            CustomAdapter customAdapter = new CustomAdapter();
                            listView.setAdapter(customAdapter);

                            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                    Intent movieDetail = new Intent(SearchActivity.this, MovieDetailActivity.class);
                                    movieDetail.putExtra("EMAIL",gemail);
                                    movieDetail.putExtra("SESSIONID",sessionID);
                                    try {
                                        movieDetail.putExtra("MOVIEID", movies.get(i).getString("movieId"));
                                    }catch (JSONException err){
                                        System.out.println("Unable to get movieid from json object");
                                        System.out.println("Error " + err.toString());
                                    }
                                    startActivity(movieDetail);
                                }
                            });

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


    private ArrayList<JSONObject> convertJSONArrToArrList(JSONArray movies){
        ArrayList<JSONObject> movieArrList = new ArrayList<>();

        for (int i = 0; i < movies.length(); ++i) {
            try{
                movieArrList.add(movies.getJSONObject(i));
            }
            catch (JSONException e){
                System.out.println("There was a problem with ");
            }
        }
        return movieArrList;
    }
}
