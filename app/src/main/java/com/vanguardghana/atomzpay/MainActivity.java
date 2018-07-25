package com.vanguardghana.atomzpay;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    
    RecyclerView verticalRecylerView;
    ArrayList<String> data;
    VerticalAdapter verticalAdapter;
    private ProgressBar pb;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent i = new Intent(MainActivity.this, PaymentActivity.class);
        startActivity(i);
        finish();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main_activity);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("UNIX Collector's App");
        
        verticalRecylerView = (RecyclerView) findViewById(R.id.recyler_view_information);
        pb = (ProgressBar) findViewById(R.id.progress_bar_main);

        data = new ArrayList<>();

        verticalAdapter=new VerticalAdapter(data);

        LinearLayoutManager verticalLayoutManagaer
                = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);

        verticalRecylerView.setLayoutManager(verticalLayoutManagaer);
        verticalRecylerView.setAdapter(verticalAdapter);

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Toast.makeText(MainActivity.this, "Clicked", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        data.clear();
        getData();
    }

    public void getData(){
        pb.setVisibility(View.VISIBLE);
        RequestQueue queue = Volley.newRequestQueue(this);
        String url =getString(R.string.server_address)+"/list_of_consumers.php?token="+LoginActivity.token;
        StringRequest postRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        // response
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

                                    data.add(consumer_id+"/.@./"+name+"/.@./"+address+"/.@./"+phone_number+"/.@./"
                                                +amount);


                                    verticalAdapter.notifyDataSetChanged();
                                }
                            }
                            else{
                                Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(MainActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                        pb.setVisibility(View.GONE);
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                return params;
            }
        };
        queue.add(postRequest);

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.dashboard, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);

        SearchManager searchManager = (SearchManager) MainActivity.this.getSystemService(Context.SEARCH_SERVICE);

        SearchView searchView = null;
        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();
        }
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(MainActivity.this.getComponentName()));

            SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener()
            {
                public boolean onQueryTextChange(String newText)
                {

                    //doFilterAsync(mSearchString);
                    Toast.makeText(MainActivity.this, "Test1", Toast.LENGTH_LONG).show();
                    return true;
                }

                public boolean onQueryTextSubmit(String query)
                {

                    //doFilterAsync(mSearchString);
                    Toast.makeText(MainActivity.this, "Test2", Toast.LENGTH_LONG).show();

                    return true;
                }
            };
        }
        return super.onCreateOptionsMenu(menu);
    }

    public class VerticalAdapter extends RecyclerView.Adapter<VerticalAdapter.MyViewHolder> {

        private List<String> horizontalList;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView name,address,phone_number,amount;
            public Button btn;

            public MyViewHolder(View view) {
                super(view);
                name = (TextView) view.findViewById(R.id.name_collect);
                address = (TextView) view.findViewById(R.id.address_collect);
                phone_number = (TextView) view.findViewById(R.id.phone_number_collect);
                amount = (TextView) view.findViewById(R.id.amount_collect);
                btn = (Button) view.findViewById(R.id.btn_collect);

            }
        }


        public VerticalAdapter(List<String> horizontalList) {
            this.horizontalList = horizontalList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.payment_collection_recyler_view, parent, false);

            return new MyViewHolder(itemView);
        }



        @Override
        public void onBindViewHolder(final MyViewHolder holder, final int position) {


            /*
             * data [0] contains counsumer_id
             * data [1] contains name
             * data [2] contains address
             * data [3] contains phone_number
             * data [4] contains amount
             */


            final String[] data = horizontalList.get(position).split( "/.@./" );
            //holder.txtView.setText(data[6]);
            holder.name.setText("Name: "+data[1]);
            holder.address.setText("Address: "+data[2]);
            holder.phone_number.setText("Phone Number :"+data[3]);
            holder.amount.setText("Amount: "+data[4]);


            holder.btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(MainActivity.this, PaymentActivity.class);
                    i.putExtra("consumer_id",data[0]);
                    startActivity(i);
                }
            });
        }

        @Override
        public int getItemCount() {
            return horizontalList.size();
        }
    }
}

