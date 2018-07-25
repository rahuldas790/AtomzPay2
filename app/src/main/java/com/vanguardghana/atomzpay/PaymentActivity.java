package com.vanguardghana.atomzpay;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.telpo.tps550.api.demo.printer.UsbPrinterActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static com.vanguardghana.atomzpay.LoginActivity.token;

public class PaymentActivity extends AppCompatActivity {

    private EditText consumerIdEt,amountEt,receiptEt;
    private TextInputLayout consumerIdTIL;
    private String consumerId,amount_collected;
    private ProgressBar pb;
    private Button btn, submit_payment;
    private TextView nameTv,addressTv,phone_numerTv,amountTv,consumerIdTv;
    private PrintReceipt printReceipt;
    String receiptInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_payment_activity);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("UNIX Collector's App");

        consumerIdEt = (EditText) findViewById(R.id.conumser_id_payment);
        amountEt = (EditText) findViewById(R.id.amount_paid_payment);
        receiptEt = (EditText) findViewById(R.id.receipt_number_payment);

        consumerIdTIL = (TextInputLayout) findViewById(R.id.username_payment_activity);

        btn = (Button) findViewById(R.id.submit_consumer_id);
        submit_payment = (Button) findViewById(R.id.collect_payment);

        nameTv = (TextView) findViewById(R.id.name_payment);
        addressTv = (TextView) findViewById(R.id.address_payment);
        phone_numerTv = (TextView) findViewById(R.id.phone_number_payment);
        amountTv = (TextView) findViewById(R.id.amount_payment);
        consumerIdTv = (TextView) findViewById(R.id.consumer_id_payment);

        pb = (ProgressBar) findViewById(R.id.progress_bar_payment);

        submit_payment.setVisibility(View.GONE);
        nameTv.setVisibility(View.GONE);
        addressTv.setVisibility(View.GONE);
        phone_numerTv.setVisibility(View.GONE);
        amountTv.setVisibility(View.GONE);
        consumerIdTv.setVisibility(View.GONE);
        receiptEt.setVisibility(View.GONE);
        amountEt.setVisibility(View.GONE);

        final ProgressDialog pdLoading = new ProgressDialog(PaymentActivity.this);

        submit_payment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(amountEt.length()==0 || receiptEt.length()==0){
                    Toast.makeText(PaymentActivity.this, "Fill All", Toast.LENGTH_SHORT).show();
                }
                else{
                    pdLoading.setMessage("\tLoading...");
                    pdLoading.setCancelable(false);
                    pdLoading.show();
                    pb.setVisibility(View.VISIBLE);
                    consumerId = consumerIdEt.getText().toString();
                    amount_collected = amountEt.getText().toString();
                    final String receipt_number = receiptEt.getText().toString();


                    RequestQueue queue = Volley.newRequestQueue(PaymentActivity.this);
                    String url =getString(R.string.server_address)+"/payment.php";
                    StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                            new Response.Listener<String>()
                            {
                                @Override
                                public void onResponse(String response) {
                                    // response
                                    //Toast.makeText(PaymentActivity.this, response, Toast.LENGTH_SHORT).show();
                                    pb.setVisibility(View.GONE);
                                    pdLoading.dismiss();
                                    AlertDialog.Builder builder = new AlertDialog.Builder(PaymentActivity.this);
                                    builder.setMessage("Data was successfully submitted to the server.!!!")
                                            .setCancelable(false)
                                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    finish();
                                                    Intent  i = new Intent(getApplicationContext(),PaymentActivity.class);
                                                    startActivity(i);
                                                }
                                            });
                                    AlertDialog alert = builder.create();
                                    alert.show();

                                    setInfoAndPrintReceipt();
                                }
                            },
                            new Response.ErrorListener()
                            {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    // error
                                    pdLoading.dismiss();
                                    Toast.makeText(PaymentActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                                    pb.setVisibility(View.GONE);
                                }
                            }
                    ) {
                        @Override
                        protected Map<String, String> getParams()
                        {
                            Map<String, String>  params = new HashMap<String, String>();
                            params.put("token", "hello");
                            params.put("consumer_id", consumerId);
                            params.put("receipt_number",receipt_number);
                            params.put("amount_collected",amount_collected);
                            return params;
                        }
                    };
                    queue.add(postRequest);

                }

            }
        });
        printReceipt = new PrintReceipt(PaymentActivity.this);
    }
    public void setInfoAndPrintReceipt() {

        Calendar c = Calendar.getInstance();

        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String formattedDate = df.format(c.getTime());

        //Receipt info.
        String receipt_number;
        receiptInfo = "\n---------------------------\n" +
                "     TOLL RECEIPT.     \n" +
                "AGENT CODE: " + consumerId + " \n" +
                "LOCATION: " + token + " \n" +
                "TOLL TYPE: " + consumerId + " \n" +
                "AMOUNT CHARGED: \u20B5" + amount_collected + " \n" +
                //    "PAYMENT MODE: " + receipt_number + " \n" +
                "DATE: " + formattedDate + " \n" +
                "Please Retain As Receipt\n \n" +
                "---------------------------\n" +
                "             Copyright \u00a9 \n" +
                "     Some Technology Ltd.";


        UsbPrinterActivity.printContent = receiptInfo;
        Intent intent = new Intent(this, UsbPrinterActivity.class);
        intent.putExtra("data", receiptInfo);
        startActivity(intent);
    }

    public void submit(View v){
        consumerId = consumerIdEt.getText().toString();
        amount_collected = amountEt.getText().toString();

        if(consumerIdEt.length()==0){
            Toast.makeText(this, "Fill Consumer ID", Toast.LENGTH_SHORT).show();
        } else{
            pb.setVisibility(View.VISIBLE);
            RequestQueue queue = Volley.newRequestQueue(this);
            String url =getString(R.string.server_address)+"/consumer_information.php";
            StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>()
                    {
                        @Override
                        public void onResponse(String response) {
                            // response
                            //Toast.makeText(PaymentActivity.this, response, Toast.LENGTH_SHORT).show();
                            try {
                                JSONObject json = new JSONObject(response);
                                if(json.getString("success").equalsIgnoreCase("true") &&
                                        json.getString("code").equalsIgnoreCase("200")){

                                    JSONArray jsonArray = json.getJSONArray("details");
                                    for(int i=0;i<jsonArray.length();i++){
                                        JSONObject jsonData = jsonArray.getJSONObject(i);
                                        String consumer_id = jsonData.getString("user_id");
                                        String name = jsonData.getString("name");
                                        String address = jsonData.getString("address");
                                        String phone_number = jsonData.getString("phone_number");
                                        String amount = jsonData.getString("amount_to_be_collected");

                                        submit_payment.setVisibility(View.VISIBLE);
                                        nameTv.setVisibility(View.VISIBLE);
                                        addressTv.setVisibility(View.VISIBLE);
                                        phone_numerTv.setVisibility(View.VISIBLE);
                                        consumerIdTv.setVisibility(View.VISIBLE);
                                        amountTv.setVisibility(View.VISIBLE);
                                        receiptEt.setVisibility(View.VISIBLE);
                                        amountEt.setVisibility(View.VISIBLE);

                                        btn.setVisibility(View.GONE);
                                        consumerIdEt.setVisibility(View.GONE);
                                        consumerIdTIL.setVisibility(View.GONE);

                                        consumerIdTv.setText("Consumer ID: "+consumer_id);
                                        nameTv.setText("Name: "+name);
                                        addressTv.setText("Address: "+address);
                                        phone_numerTv.setText("Phone Number: "+phone_number);
                                        amountTv.setText("Bill: Rs."+amount);
                                    }
                                }
                                else if(json.getString("success").equalsIgnoreCase("false") &&
                                        json.getString("code").equalsIgnoreCase("400") &&
                                        json.getString("message").equalsIgnoreCase("Invalid Consumer ID")){
                                    Toast.makeText(PaymentActivity.this, "Invalid Consumer ID", Toast.LENGTH_SHORT).show();
                                }
                                else{
                                    Toast.makeText(PaymentActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                Toast.makeText(PaymentActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(PaymentActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                            pb.setVisibility(View.GONE);
                        }
                    }
            ) {
                @Override
                protected Map<String, String> getParams()
                {
                    Map<String, String>  params = new HashMap<String, String>();
                    params.put("token", "hello");
                    params.put("consumer_id", consumerId);
                    return params;
                }
            };
            queue.add(postRequest);


        }

    }
}
