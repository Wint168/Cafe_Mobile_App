package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.squareup.picasso.Picasso;

public class MatchaMenu extends AppCompatActivity{

    private TextView matchaTitle;

    private ImageView matchaImage;

    private GridLayout matchaContainer;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.matcha_menu);

        matchaTitle = findViewById(R.id.matchaTitle);
        matchaImage = findViewById(R.id.matchaImage);
        matchaContainer = findViewById(R.id.matchaContainer);

        fetchMatchaDataFromSupabase();
        fetchMatchaMenu();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.home) {
                startActivity(new Intent(MatchaMenu.this, frontpage.class));
                finish();
                return true;
            } else if (id == R.id.equipment) {
                startActivity(new Intent(MatchaMenu.this, MatchaEquipments.class));
                return true;
            } else if (id == R.id.menu) {

                return true;
            }
            return false;
        });
    }

    private void fetchMatchaDataFromSupabase(){
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

    private void fetchMatchaMenu(){
        String url = SupabaseConnector.BASE_URL +
                "/rest/v1/drink_menu?select=drink_name,drink_photo,drink_types!inner(name)&drink_types.name=eq.Matcha";

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
                if (!response.isSuccessful() || response.body() == null) return;

                String json = response.body().string();

                runOnUiThread(() -> {
                    try {
                        JSONArray array = new JSONArray(json);

                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            String name = obj.optString("drink_name");
                            String photo = obj.optString("drink_photo");
                            final String drinkName = name;

                            // Each box = LinearLayout (vertical)
                            LinearLayout box = new LinearLayout(MatchaMenu.this);
                            box.setOrientation(LinearLayout.VERTICAL);
                            box.setBackgroundColor(ContextCompat.getColor(MatchaMenu.this, R.color.matchadark));
                            box.setPadding(16, 16, 16, 16);

                            // GridLayout params for 2 per row
                            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                            params.width = 0; // divide equally
                            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
                            params.setMargins(16, 16, 16, 16);
                            box.setLayoutParams(params);

                            // Image
                            ImageView imageView = new ImageView(MatchaMenu.this);
                            LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    500
                            );
                            imageView.setLayoutParams(imageParams);
                            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            Picasso.get().load(photo).into(imageView);

                            // Name
                            TextView nameView = new TextView(MatchaMenu.this);
                            nameView.setText(name);
                            nameView.setTextColor(ContextCompat.getColor(MatchaMenu.this, R.color.white));
                            nameView.setTextSize(20f);

                            // Add into box
                            box.addView(imageView);
                            box.addView(nameView);

                            box.setOnClickListener(v -> {
                                Intent intent = new Intent(MatchaMenu.this, MatchaRecipe.class);
                                intent.putExtra("drink_name", drinkName);
                                startActivity(intent);
                            });


                            // Add box into GridLayout container
                            matchaContainer.addView(box);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        });
    }
}
