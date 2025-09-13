package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONObject;
import android.widget.ImageView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.squareup.picasso.Picasso; // For loading images from URL

public class BubbleTeaEquipments extends AppCompatActivity {

    private TextView bobaTitle;
    private ImageView bobaImage;

    private LinearLayout equipmentContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bubble_tea_equipments); // bind with coffee.xml

        bobaTitle = findViewById(R.id.bobaTitle);
        bobaImage = findViewById(R.id.bobaImage);
        equipmentContainer = findViewById(R.id.equipmentData);

        fetchBobaDataFromSupabase();
        fetchBobaEquipment();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.home) {
                startActivity(new Intent(BubbleTeaEquipments.this, frontpage.class));
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

    private void fetchBobaDataFromSupabase(){
        String url = SupabaseConnector.BASE_URL + "/rest/v1/drink_types?select=name,drink_type_photo&name=eq.Bubble Tea";

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

                                bobaTitle.setText(name);
                                Picasso.get().load(imageUrl).into(bobaImage); // Load image
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }
            }
        });
    }

    public void fetchBobaEquipment(){
        String url = SupabaseConnector.BASE_URL +
                "/rest/v1/equipments?select=equipment_name,equipment_description,equipment_photo,drink_types!inner(name)&drink_types.name=eq.Bubble Tea";

        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", SupabaseConnector.API_KEY)
                .addHeader("Authorization", "Bearer " + SupabaseConnector.API_KEY)
                .build();

        SupabaseConnector.client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) { e.printStackTrace(); }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful() || response.body() == null) return;

                String json = response.body().string();

                runOnUiThread(() -> {
                    try {
                        JSONArray array = new JSONArray(json);

                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            String name = obj.optString("equipment_name");
                            String desc = obj.optString("equipment_description");
                            String photo = obj.optString("equipment_photo");

                            // Create one box dynamically
                            LinearLayout box = new LinearLayout(BubbleTeaEquipments.this);
                            box.setOrientation(LinearLayout.VERTICAL);
                            box.setBackgroundColor(ContextCompat.getColor(BubbleTeaEquipments.this, R.color.bobadark));
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT
                            );
                            params.setMargins(0, 20, 0, 20);
                            box.setLayoutParams(params);
                            box.setPadding(20, 20, 20, 20);

                            // Equipment image
                            // Equipment image
                            ImageView imageView = new ImageView(BubbleTeaEquipments.this);
                            LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT
                            );
                            imageView.setLayoutParams(imageParams);
                            imageView.setAdjustViewBounds(true); // important for proportional resizing
                            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER); // show whole image
                            Picasso.get().load(photo).into(imageView);

                            // Equipment name
                            TextView nameView = new TextView(BubbleTeaEquipments.this);
                            nameView.setText(name);
                            nameView.setTextColor(ContextCompat.getColor(BubbleTeaEquipments.this, R.color.white));

                            nameView.setTextSize(30f);

                            // Equipment description
                            TextView descView = new TextView(BubbleTeaEquipments.this);
                            descView.setText(desc);
                            descView.setTextColor(ContextCompat.getColor(BubbleTeaEquipments.this, R.color.white));

                            descView.setTextSize(25f);

                            // Add views into box
                            box.addView(imageView);
                            box.addView(nameView);
                            box.addView(descView);

                            // Add box into container
                            equipmentContainer.addView(box);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        });
    }

}
