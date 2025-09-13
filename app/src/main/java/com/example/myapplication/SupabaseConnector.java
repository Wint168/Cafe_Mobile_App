package com.example.myapplication;

import okhttp3.OkHttpClient;

public class SupabaseConnector {

    public static final String BASE_URL = "https://zlroizpanpbygkmzkdzo.supabase.co";
    public static final String API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Inpscm9penBhbnBieWdrbXprZHpvIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTQ4OTQzNjMsImV4cCI6MjA3MDQ3MDM2M30.XVfV8pfoHFtTLmHXu-Z2-MgiSChOGWceJxGblz11xT0";

    public static OkHttpClient client = new OkHttpClient();

    // This class just holds the connection info.
    // Queries will be made in other Java files using this client and BASE_URL.
}
