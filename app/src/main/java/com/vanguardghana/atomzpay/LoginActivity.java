package com.vanguardghana.atomzpay;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import org.json.JSONObject;


public class LoginActivity extends AppCompatActivity {




    ProgressDialog progressDialog;
    EditText usernameEditText, passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);

        //hide editText focus
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);


    }


    public void submitDetails(View view) {

        String username = usernameEditText.getText().toString();
        String password = passwordEditText.getText().toString();


        if (!validate(username, password)) {
            Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();
            return;
        }

        progressDialog = new ProgressDialog(LoginActivity.this, R.style.Theme_AppCompat_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();

        postSignInData(username, password);


    }



    public boolean validate(String username, String password) {
        boolean valid = true;

        if (username.isEmpty()) {
            usernameEditText.setError("enter a valid ID");
            valid = false;
        } else {
            usernameEditText.setError(null);
        }

        if (password.isEmpty()) {
            passwordEditText.setError("Enter password");
            valid = false;
        } else {
            passwordEditText.setError(null);
        }

        return valid;
    }

    public void showDialog(String setMessage, String setPositiveButton){

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Theme_AppCompat_Dialog);
        builder.setMessage(setMessage)
                .setCancelable(true)
                .setPositiveButton(setPositiveButton, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button
                    }
                });

        AlertDialog createDialog = builder.create();
        createDialog.show();
    }

    //function to Store data from web app in shared preferences
    public static void setLogInData(String key, String value, Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.apply();

    }

    // method to send log-in details via ION
    private void postSignInData(String username, String password) {

        ConnectionDetector cd = new ConnectionDetector();
        if (cd.isConnectingToInternet(this)) {

            Ion.with(this)
                    .load("POST", "http://192.168.3.41/rsms_tolls/api/login" )
                    .setHeader("Accept", "application/json")
                    .setBodyParameter("collector_code", username)
                    .setBodyParameter("password", password)
                    .asString()
                    .withResponse()
                    .setCallback(new FutureCallback<Response<String>>() {
                        @Override
                        public void onCompleted(Exception e, Response<String> response) {


                            try {

                                if (e != null) {
                                    e.printStackTrace();
                                    progressDialog.dismiss();
                                    showDialog("Oops!! Something went wrong. Server cannot be reached \nTry Again", "Close");
                                    System.out.println("---------------------------------------------------------------- error");
                                }else {

                                    //Extract response Status code
                                    int responseCode = response.getHeaders().code();

                                    //Extract response Results
                                    String result = response.getResult();

                                    if (!result.contentEquals("null")) {
                                        System.out.println("------------------------------------------------------@@@@@@@@@@@@@@@@@@ user login Data is " + result);

                                        JSONObject json = new JSONObject(result);
                                        String status = json.getString("status");



                                        if(responseCode== 200 && status.equals("success")){

                                            String userData = json.getString("data");
                                            JSONObject convertDataToJson = new JSONObject(userData);
                                            String deAgentID= convertDataToJson.getString("collector_code");
                                            String deAgentName= convertDataToJson.getString("name");
                                            String deApiToken = convertDataToJson.getString("api_token");

                                            String deLocationObject= convertDataToJson.getString("assigned_location");
                                            String deLocation = new JSONObject(deLocationObject).getString("name");
                                            String deLocationID = new JSONObject(deLocationObject).getString("location_code");

                                            String deTollTypeObject= convertDataToJson.getString("assigned_toll_type");
                                            String deTollType = new JSONObject(deTollTypeObject).getString("name");
                                            String deTollTypeID = new JSONObject(deTollTypeObject).getString("toll_type_code");
                                            String deNormalAmount2Pay= new JSONObject(deTollTypeObject).getString("amount");

                                            //Saving LogIn Data to Shared prefs
                                            setLogInData("apiToken", deApiToken, getApplicationContext());

                                            setLogInData("agentID", deAgentID, getApplicationContext());
                                            setLogInData("agentName", deAgentName, getApplicationContext());
                                            setLogInData("location", deLocation, getApplicationContext());
                                            setLogInData("locationID", deLocationID, getApplicationContext());
                                            setLogInData("tollType", deTollType, getApplicationContext());
                                            setLogInData("tollTypeID", deTollTypeID, getApplicationContext());
                                            setLogInData("normalAmount2Pay", deNormalAmount2Pay, getApplicationContext());

                                            progressDialog.dismiss();

                                            Intent intent = new Intent(LoginActivity.this, PaymentCollectionActivity.class);
                                            startActivity(intent);
                                            finish();

                                        }


                                        if (responseCode== 200 && status.equals("error")) {
                                            showDialog("Invalid Agent ID or Password", "Close");
                                            System.out.println("--------------------------@@@@@@@@@@- wrong input info.");
                                            progressDialog.dismiss();

                                        }


                                    }
                                }

                            } catch (Exception ex) {
                                ex.printStackTrace();

                                    showDialog("Unable to Sign in: \nServer response error", "Close");
                                    System.out.println("--------------------------------@@@@@@@@@@@@@@@@@@ Server response error " );
                                    progressDialog.dismiss();


                            }
                        }
                    }) ;


        } else {

            showDialog("No internet connection. Check your connection and try again", "Close");
            System.out.println("-----No internet connection. Check your connection and try again @##### " );
            progressDialog.dismiss();



        }



    }


}
