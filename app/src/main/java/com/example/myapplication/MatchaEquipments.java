package com.example.myapplication;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONObject;
import android.widget.ImageView;
import com.squareup.picasso.Picasso;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.content.Intent;
// For loading images from URL

public class MatchaEquipments extends AppCompatActivity {

    private TextView matchaTitle;
    private ImageView matchaImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.matcha_equipments);

        matchaTitle = findViewById(R.id.matchaTitle);
        matchaImage= findViewById(R.id.matchaImage);

        fetchMatchaDataFromSupabase();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.home) {
                startActivity(new Intent(MatchaEquipments.this, frontpage.class));
                finish();
                return true;
            } else if (id == R.id.equipment) {
                // already here
                return true;
            } else if (id == R.id.menu) {
                // navigate to menu activity
                return true;
            }
            return false;
        });


    }

    private void fetchMatchaDataFromSupabase() {
        String url = SupabaseConnector.BASE_URL + "/rest/v1/drink_types?select=name,drink_type_photo&name=eq.Matcha";

        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", SupabaseConnector.API_KEY)
                .addHeader("Authorization", "Bearer " + SupabaseConnector.API_KEY)
                .build();

        SupabaseConnector.client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String json = response.body().string();

                    runOnUiThread(() -> {
                        try {
                            JSONArray array = new JSONArray(json);
                            if (array.length() > 0) {
                                JSONObject obj = array.getJSONObject(0);
                                String name = obj.getString("name");
                                String imageUrl = obj.getString("drink_type_photo"); // must be a public URL

                                matchaTitle.setText(name);
                                Picasso.get().load(imageUrl).into(matchaImage); // Load image
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }
            }
        });
    }
}
