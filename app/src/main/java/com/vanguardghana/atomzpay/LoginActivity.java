package com.vanguardghana.atomzpay;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameEt,passwordEt;
    private String userid,password;
    private ProgressBar pb;
    public static String token;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        SharedPreferences sharedPref = LoginActivity.this.getPreferences(Context.MODE_PRIVATE);
        String defaultValue= "notset";
        token = sharedPref.getString("token", defaultValue);
        if(!token.equalsIgnoreCase("notset")){
            Intent i = new Intent(LoginActivity.this,MainActivity.class);
            startActivity(i);
            finish();
        }
        usernameEt = (EditText) findViewById(R.id.sign_in_username);
        passwordEt = (EditText) findViewById(R.id.sign_in_password);

        pb = (ProgressBar) findViewById(R.id.progress_bar_login);

    }

    public void signin(View v){

        userid = usernameEt.getText().toString();
        password = passwordEt.getText().toString();
        if(usernameEt.length()==0 || passwordEt.length()==0){
            Toast.makeText(this, "Fill All", Toast.LENGTH_SHORT).show();
        } else{
            pb.setVisibility(View.VISIBLE);
            RequestQueue queue = Volley.newRequestQueue(this);
            String url =getString(R.string.server_address)+"/login.php";
            StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>()
                    {
                        @Override
                        public void onResponse(String response) {
                            // response
                            //Toast.makeText(LoginActivity.this, response, Toast.LENGTH_SHORT).show();
                            try {
                                JSONObject json = new JSONObject(response);
                                if(json.getString("success").equalsIgnoreCase("true") && json.getString("status").equalsIgnoreCase("200")){
                                    SharedPreferences sharedPref = LoginActivity.this.getPreferences(Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPref.edit();
                                    editor.putString("token", json.getString("token"));
                                    editor.commit();
                                    token = json.getString("token");

                                    Intent i = new Intent(LoginActivity.this,MainActivity.class);
                                    startActivity(i);
                                    finish();
                                }
                                else{
                                    Toast.makeText(LoginActivity.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                Toast.makeText(LoginActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }
                            pb.setVisibility(View.GONE);
                        }
                    },
                    new Response.ErrorListener()
                    {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // error
                            Toast.makeText(LoginActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                            pb.setVisibility(View.GONE);
                        }
                    }
            ) {
                @Override
                protected Map<String, String> getParams()
                {
                    Map<String, String>  params = new HashMap<String, String>();
                    params.put("userid", userid);
                    params.put("password", password);


                    return params;
                }
            };
            queue.add(postRequest);

        }
    }
}