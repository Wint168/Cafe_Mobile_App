package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.animation.AlphaAnimation;
import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONObject;


public class frontpage extends AppCompatActivity {

    private TextView frontLine;
    private LinearLayout drinkType;
    private ImageView drinkImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        frontLine = findViewById(R.id.frontLine);
        drinkType = findViewById(R.id.drinkType);
        drinkImage = findViewById(R.id.drinkImage);

        String[] lines = {
                "Welcome to addiction by Wint",
                "where you can find recipes for"
        };

        // Show first line
        frontLine.postDelayed(() -> frontLine.setText(lines[0]), 1000);

        // Show second line with fade, then fetch drinks
        frontLine.postDelayed(() -> {
            frontLine.append("\n" + lines[1]);
            AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
            fadeIn.setDuration(1000);
            frontLine.startAnimation(fadeIn);

            // Fetch drinks after animation
            fetchDrinkTypesFromSupabase();
        }, 4000);
    }

    private void fetchDrinkTypesFromSupabase() {
        String url = SupabaseConnector.BASE_URL + "/rest/v1/drink_types?select=name,drink_type_photo";

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

                    try {
                        JSONArray array = new JSONArray(json);

                        runOnUiThread(() -> {
                            for (int i = 0; i < array.length(); i++) {
                                try {
                                    JSONObject obj = array.getJSONObject(i);
                                    String name = obj.optString("name");
                                    String image = obj.optString("drink_type_photo");

                                    // Inflate drink_item.xml for each drink
                                    // Inflate drink_item.xml for each drink
                                    View drinkItem = getLayoutInflater().inflate(R.layout.drink_type, drinkType, false);

// Find inner views
                                    TextView drinkName = drinkItem.findViewById(R.id.drinkType); // TextView inside drink_type.xml
                                    ImageView drinkImg = drinkItem.findViewById(R.id.drinkImage); // ImageView inside drink_type.xml

// Set text and image
                                    drinkName.setText(name);
                                    Picasso.get().load(image).into(drinkImg);

// Set click listener
                                    drinkName.setOnClickListener(v -> {
                                        switch (name.toLowerCase()) {
                                            case "coffee":
                                                startActivity(new Intent(frontpage.this, CoffeeEquipments.class));
                                                break;
                                            case "matcha":
                                                startActivity(new Intent(frontpage.this, MatchaEquipments.class));
                                                break;
                                            case "bubble tea":
                                                startActivity(new Intent(frontpage.this, BubbleTeaEquipments.class));
                                                break;
                                        }
                                    });

// Add to parent layout
                                    drinkType.addView(drinkItem);

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
