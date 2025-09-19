package com.example.myapplication;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
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

public class BubbleTeaRecipe extends AppCompatActivity{
    private TextView bobaTitle;

    private ImageView bobaImage;

    private LinearLayout bobaContainer;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bubble_tea_recipe);

        bobaTitle = findViewById(R.id.bobaTitle);
        bobaImage = findViewById(R.id.bobaImage);
        bobaContainer = findViewById(R.id.bobaIngredient);

        fetchBobaDataFromSupabase();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.home) {
                startActivity(new Intent(BubbleTeaRecipe.this, frontpage.class));
                finish();
                return true;
            } else if (id == R.id.equipment) {
                startActivity(new Intent(BubbleTeaRecipe.this, BubbleTeaEquipments.class));
                return true;
            } else if (id == R.id.menu) {

                return true;
            }
            return false;
        });
        String drinkName = getIntent().getStringExtra("drink_name");
        if (drinkName != null) {
            fetchBobaName(drinkName);

        }
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
            public void onFailure(Call call, IOException e) {e.printStackTrace();}

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

    private void fetchBobaName(String drinkName){
        String url = SupabaseConnector.BASE_URL +
                "/rest/v1/drink_menu?select=drink_name,drink_photo&drink_name=eq." + drinkName;

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
                            // Box container
                            LinearLayout box = new LinearLayout(BubbleTeaRecipe.this);
                            box.setOrientation(LinearLayout.VERTICAL);
                            box.setBackgroundColor(ContextCompat.getColor(BubbleTeaRecipe.this, R.color.bobadark));
                            box.setPadding(10, 10, 10, 10);

// Fixed box size
                            int boxSizeInDp = 250;
                            float scale = getResources().getDisplayMetrics().density;
                            int boxSizeInPx = (int) (boxSizeInDp * scale);

                            LinearLayout.LayoutParams boxParams = new LinearLayout.LayoutParams(
                                    boxSizeInPx,
                                    ViewGroup.LayoutParams.WRAP_CONTENT
                            );
                            boxParams.gravity = Gravity.CENTER; // <-- Center horizontally
                            box.setLayoutParams(boxParams);

// ImageView (square inside box)
                            ImageView imageView = new ImageView(BubbleTeaRecipe.this);
                            LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    boxSizeInPx
                            );
                            imageView.setLayoutParams(imageParams);
                            imageView.setAdjustViewBounds(true);
                            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                            imageView.setCropToPadding(false);

                            Picasso.get().load(photo).into(imageView);

// Name
                            TextView nameView = new TextView(BubbleTeaRecipe.this);
                            nameView.setText(name);
                            nameView.setTextColor(ContextCompat.getColor(BubbleTeaRecipe.this, R.color.white));
                            nameView.setTextSize(30f);
                            nameView.setGravity(Gravity.CENTER);

// Add into box
                            box.addView(imageView);
                            box.addView(nameView);

// Add box into parent
                            bobaContainer.setGravity(Gravity.CENTER); // <-- Center child in parent
                            bobaContainer.addView(box);
                            fetchIngredients(drinkName);

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        });

    }
    private void fetchIngredients(String drinkName) {
        String url = SupabaseConnector.BASE_URL +
                "/rest/v1/ingredients?select=ingredient_name,drink_ingredients!inner(drink_menu!inner(drink_name))&drink_ingredients.drink_menu.drink_name=eq." + drinkName;

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
                        // 🔹 Create one box (LinearLayout) to hold all ingredients
                        LinearLayout ingredientBox = new LinearLayout(BubbleTeaRecipe.this);
                        ingredientBox.setOrientation(LinearLayout.VERTICAL);
                        ingredientBox.setBackgroundColor(ContextCompat.getColor(BubbleTeaRecipe.this, R.color.bobadark));
                        ingredientBox.setPadding(30, 30, 30, 30);

                        LinearLayout.LayoutParams boxParams = new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                        );
                        boxParams.setMargins(40, 40, 40, 40); // margin around the box
                        ingredientBox.setLayoutParams(boxParams);

                        TextView titleView = new TextView(BubbleTeaRecipe.this);
                        titleView.setText("Ingredients");
                        titleView.setTextSize(28f);
                        titleView.setTextColor(ContextCompat.getColor(BubbleTeaRecipe.this, R.color.white));
                        titleView.setPadding(0, 0, 0, 20);
                        titleView.setTypeface(null, Typeface.BOLD);
                        ingredientBox.addView(titleView);
                        // 🔹 Add all ingredients into the one box
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            String ingredient = obj.getString("ingredient_name");

                            TextView ingredientView = new TextView(BubbleTeaRecipe.this);
                            ingredientView.setText("• " + ingredient);
                            ingredientView.setTextSize(25f);
                            ingredientView.setTextColor(ContextCompat.getColor(BubbleTeaRecipe.this, R.color.white));
                            ingredientView.setPadding(0, 10, 0, 10);

                            ingredientBox.addView(ingredientView);
                        }

                        // 🔹 Finally, add the box to the main layout
                        bobaContainer.addView(ingredientBox);
                        fetchRecipe(drinkName);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        });
    }
    private void fetchRecipe(String drinkName){
        String url = SupabaseConnector.BASE_URL +
                "/rest/v1/drink_menu?select=recipe&drink_name=eq." + drinkName;

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

                        // 🔹 Create one box (LinearLayout) to hold all ingredients
                        LinearLayout ingredientBox = new LinearLayout(BubbleTeaRecipe.this);
                        ingredientBox.setOrientation(LinearLayout.VERTICAL);
                        ingredientBox.setBackgroundColor(ContextCompat.getColor(BubbleTeaRecipe.this, R.color.bobadark));
                        ingredientBox.setPadding(30, 30, 30, 30);

                        LinearLayout.LayoutParams boxParams = new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                        );
                        boxParams.setMargins(40, 40, 40, 40); // margin around the box
                        ingredientBox.setLayoutParams(boxParams);

                        TextView titleView = new TextView(BubbleTeaRecipe.this);
                        titleView.setText("Recipe");
                        titleView.setTextSize(28f);
                        titleView.setTextColor(ContextCompat.getColor(BubbleTeaRecipe.this, R.color.white));
                        titleView.setPadding(0, 0, 0, 20);
                        titleView.setTypeface(null, Typeface.BOLD); // 🔹 Bold text
                        ingredientBox.addView(titleView);


                        // 🔹 Add all ingredients into the one box
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            String recipe = obj.getString("recipe");

                            TextView ingredientView = new TextView(BubbleTeaRecipe.this);
                            ingredientView.setText(recipe);
                            ingredientView.setTextSize(25f);
                            ingredientView.setTextColor(ContextCompat.getColor(BubbleTeaRecipe.this, R.color.white));
                            ingredientView.setPadding(0, 10, 0, 10);

                            ingredientBox.addView(ingredientView);
                        }

                        // 🔹 Finally, add the box to the main layout
                        bobaContainer.addView(ingredientBox);


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        });
    }
}


