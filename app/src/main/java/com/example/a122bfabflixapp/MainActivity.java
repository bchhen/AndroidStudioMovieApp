package com.example.a122bfabflixapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        configureLoginButton();
        configureRegisterLink();

    }

    public void configureLoginButton(){
        Button loginButton = (Button) findViewById(R.id.LoginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HTTPrequestLogin();
                //startActivity(new Intent(MainActivity.this, SearchActivity.class));
            }
        });

    }

    public void configureRegisterLink(){
        TextView registerlink = (TextView) findViewById(R.id.RegisterLink);
        registerlink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,RegisterActivity.class));
            }
        });

    }

    public void HTTPrequestLogin(){
        try{
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            String URL = "http://10.0.2.2:3979/api/idm/login";
            final JSONObject jsonBody = new JSONObject();
            TextView email = (TextView) findViewById(R.id.LoginEmail);
            final String UserEmail = email.getText().toString();
            TextView pass = (TextView) findViewById(R.id.LoginPass);
            System.out.println(pass.getText());
            //char[] Password = pass.getText().toString().toCharArray();
            jsonBody.accumulate("email", UserEmail);
            jsonBody.accumulate("password",pass.getText());
//            jsonBody.put("email", UserEmail);
//            jsonBody.put("password",Password);
            final String jsonString = jsonBody.toString();

            System.out.println(jsonBody.toString());

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URL,jsonBody, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    System.out.println("VOLLEY "+  response);
                    try{
                        if (response.getInt("resultCode") == 120) {
                            Toast.makeText(getApplicationContext(),response.getString("message"), Toast.LENGTH_LONG).show();
                            Intent goToSearch = new Intent(MainActivity.this,SearchActivity.class);
                            goToSearch.putExtra("EMAIL",UserEmail);
                            goToSearch.putExtra("SESSIONID",response.getString("sessionID"));
                            System.out.println("Im finishing");
                            finish();
                            System.out.println("Starting Search");

                            startActivity(goToSearch);

                        }
                        else
                            Toast.makeText(getApplicationContext(),response.getString("message"), Toast.LENGTH_LONG).show();
                    }
                    catch(JSONException e){
                        System.out.println("Error getting data from 200 login response");
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
                                        switch(jsonObject.getInt("resultCode")){
                                            case -12:
                                            case -11:
                                            case -10:
                                                Toast.makeText(getApplicationContext(),jsonObject.getString("message"), Toast.LENGTH_LONG).show();
                                                break;
                                            default:
                                                Toast.makeText(getApplicationContext(),"Bad Input. Please try again.", Toast.LENGTH_LONG).show();
                                                break;
                                        }
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
                    });

            requestQueue.add(jsonObjectRequest);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
