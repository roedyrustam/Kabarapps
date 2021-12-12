package com.frsarker.newsapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.core.widget.NestedScrollView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.frsarker.newsapp.adapters.CarouselAdapter;
import com.frsarker.newsapp.adapters.CarouselCatAdapter;
import com.frsarker.newsapp.adapters.ListAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import io.supercharge.shimmerlayout.ShimmerLayout;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    Context context = this;
    DBHelper myDB;
    SharedPreferences sharedpreferences;
    SharedPreferences.Editor editor;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    SwipeRefreshLayout swipeContainer;
    NestedScrollView scrollView;
    ShimmerLayout loaderView;
    LinearLayout componentContainer, errorView, emptyView;
    TextView errorTxt, emptyTxt;
    Toolbar toolbar;
    DrawerLayout drawerLayout;
    NavigationView navView;
    View navHeaderLayout;
    LinearLayout defaultNavView, loginNavView;
    ImageView userImage;
    TextView userName, userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setContentView(R.layout.activity_main);

        myDB = new DBHelper(context);

        sharedpreferences = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
        editor = sharedpreferences.edit();

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        /*Navigation Drawer*/
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        navView = findViewById(R.id.nav_view);
        navHeaderLayout = navView.getHeaderView(0);
        defaultNavView = navHeaderLayout.findViewById(R.id.defaultNavView);
        loginNavView = navHeaderLayout.findViewById(R.id.loginNavView);
        userImage = navHeaderLayout.findViewById(R.id.userImage);
        userName = navHeaderLayout.findViewById(R.id.userName);
        userEmail = navHeaderLayout.findViewById(R.id.userEmail);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, 0, 0);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navView.setNavigationItemSelectedListener(this);
        /*Navigation Drawer*/

        swipeContainer = findViewById(R.id.swipeContainer);
        scrollView = findViewById(R.id.scrollView);
        componentContainer = findViewById(R.id.componentContainer);
        loaderView = findViewById(R.id.shimmer_layout);
        errorView = findViewById(R.id.error);
        errorTxt = findViewById(R.id.errorTxt);
        emptyView = findViewById(R.id.empty);
        emptyTxt = findViewById(R.id.emptyTxt);

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (Extras.isConnected(context)) {
                    new fetchJsonData().execute();
                } else {
                    showError(getString(R.string.dashboard_no_internet_text));
                }
            }
        });

        if (Extras.isConnected(this)) {
            new fetchJsonData().execute();
        } else {
            showError(getString(R.string.dashboard_no_internet_text));
        }

        /* ====  Banner Ad =====*/
        if(Config.displayAdDashboardScreen)
        {
            LinearLayout adContainer = findViewById(R.id.adContainer);
            MobileAds.initialize(this, new OnInitializationCompleteListener() {
                @Override
                public void onInitializationComplete(InitializationStatus initializationStatus) {
                }
            });
            AdView adView = new AdView(this);
            adView.setAdSize(AdSize.BANNER);
            adView.setAdUnitId(Config.bannerAdUnitId);
            adContainer.addView(adView);

            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);
        }
        /* ====  Banner Ad =====*/
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            defaultNavView.setVisibility(View.GONE);
            loginNavView.setVisibility(View.VISIBLE);
            userName.setText(currentUser.getDisplayName());
            userEmail.setText(currentUser.getEmail());

            String photoUrl = currentUser.getPhotoUrl().toString();
            for (UserInfo profile : currentUser.getProviderData()) {
                if (profile.getProviderId().equals("facebook.com")) {
                    String facebookUserId = profile.getUid();
                    photoUrl = "https://graph.facebook.com/" + facebookUserId + "/picture?height=500";
                } else if (profile.getProviderId().equals("google.com")) { }
            }
            try {
                Picasso.get()
                        .load(photoUrl)
                        .resize(300, 200)
                        .centerCrop()
                        .error(R.drawable.no_image)
                        .into(userImage);
            } catch (Exception e) { }
        }
    }

    public void signIn(View v)
    {
        Intent i = new Intent(context, StartActivity.class);
        i.putExtra("isFromCommentScreen", true);
        startActivity(i);
    }
    public void signOut(View v)
    {
        mAuth.signOut();
        // Google sign out
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        editor.putInt("skipped", 0);
                        editor.apply();
                        startActivity(new Intent(context, StartActivity.class));
                        finish();
                    }
                });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();
        switch (id) {
            case R.id.nav_categories:
                startActivity(new Intent(context, CategoriesActivity.class));
                break;
            case R.id.nav_bookmarks:
                startActivity(new Intent(context, BookmarksActivity.class));
                break;
            default:
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    public void searchView(View v){
        startActivity(new Intent(context, SearchActivity.class));
    }

    public void showError(String msg) {
        swipeContainer.setRefreshing(false);
        errorView.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
        errorTxt.setText(msg);
        loaderView.setVisibility(View.GONE);
        scrollView.setVisibility(View.GONE);
    }

    public void showEmpty(String msg) {
        swipeContainer.setRefreshing(false);
        emptyView.setVisibility(View.VISIBLE);
        errorView.setVisibility(View.GONE);
        emptyTxt.setText(msg);
        loaderView.setVisibility(View.GONE);
        scrollView.setVisibility(View.GONE);
    }

    public void hideError() {
        swipeContainer.setRefreshing(false);
        errorView.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);
        loaderView.setVisibility(View.VISIBLE);
        scrollView.setVisibility(View.GONE);
        loaderView.startShimmerAnimation();
    }

    class fetchJsonData extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            navView.getMenu().clear();
            componentContainer.removeAllViews();
            hideError();
        }
        protected String doInBackground(String... args) {
            String secure = Config.secureKey;
            String response = Extras.excuteGet(Config.host + "/api/dashboard.php?auth_key=" + secure);
            return response;
        }
        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jsonObj = new JSONObject(result);
                String status = jsonObj.getString("status");
                if (status.contentEquals("false")) {
                    showError(getString(R.string.dashboard_other_error_text));
                } else if (status.contentEquals("empty")) {
                    showEmpty(getString(R.string.dashboard_nothing_found_text));
                } else if (status.contentEquals("true")) {
                    JSONArray pages = jsonObj.getJSONArray("pages");
                    populatePagesIntoMenu(pages);
                    JSONArray components = jsonObj.getJSONArray("components");
                    for (int i = 0; i < components.length(); i++) {
                        JSONObject componentObject = components.getJSONObject(i);
                        ArrayList dataList = new ArrayList<HashMap<String, String>>();
                        if (componentObject.optString("data_type").contentEquals("carousel") || componentObject.optString("data_type").contentEquals("list")) {
                            JSONArray postsData = componentObject.getJSONArray("data");
                            for (int j = 0; j < postsData.length(); j++) {
                                JSONObject postObject = postsData.getJSONObject(j);
                                HashMap<String, String> map = new HashMap<String, String>();
                                map.put("post_id", postObject.optString("post_id"));
                                map.put("cat_name", postObject.optString("cat_name"));
                                map.put("post_cat", postObject.optString("post_cat"));
                                map.put("post_title", postObject.optString("post_title"));
                                map.put("post_cover", postObject.optString("post_cover"));
                                map.put("post_date", postObject.optString("post_date"));
                                map.put("comment_status", postObject.optString("comment_status"));
                                dataList.add(map);
                            }
                        } else if (componentObject.optString("data_type").contentEquals("cat")) {
                            JSONArray catData = componentObject.getJSONArray("data");

                            int color_pos = 0;
                            for (int j = 0; j < catData.length(); j++) {
                                JSONObject catObject = catData.getJSONObject(j);
                                HashMap<String, String> map = new HashMap<String, String>();
                                map.put("color_pos", color_pos+"");
                                map.put("cat_id", catObject.optString("cat_id"));
                                map.put("cat_name", catObject.optString("cat_name"));
                                map.put("post_count", catObject.optString("post_count"));
                                dataList.add(map);
                                color_pos++;
                                if(color_pos > 5)
                                {
                                    color_pos = 0;
                                }
                            }
                        } else if (componentObject.optString("data_type").contentEquals("customhtml")) {
                            String htmldata = componentObject.optString("data").toString();
                            processCustomHTML(htmldata, componentObject.optString("data_headline").toString());
                        }

                        if (componentObject.optString("data_type").contentEquals("carousel")) {
                            processCarousel(dataList, componentObject.optString("data_headline").toString());
                        } else if (componentObject.optString("data_type").contentEquals("list")) {
                            processList(dataList, componentObject.optString("data_headline").toString());
                        } else if (componentObject.optString("data_type").contentEquals("cat")) {
                            processCat(dataList, componentObject.optString("data_headline").toString());
                        }
                    }

                } else {
                    showError(getString(R.string.dashboard_other_error_text));
                }
            } catch (Exception e) {
                e.printStackTrace();
                showError(getString(R.string.dashboard_other_error_text));
            }
        }
    }

    public void populatePagesIntoMenu(JSONArray pages)
    {
        Menu menu = navView.getMenu();
        menu.add(0, 1, 1, "News Categories")
                .setIcon(R.drawable.ic_category)
                .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        startActivity(new Intent(context, CategoriesActivity.class));
                        return false;
                    }
                });
        menu.add(0, 2, 2, "Bookmarks")
                .setIcon(R.drawable.ic_bookmarks)
                .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        startActivity(new Intent(context, BookmarksActivity.class));
                        return false;
                    }
                });
        try {
            for (int i = 0; i < pages.length(); i++) {
                final JSONObject pageObject = pages.getJSONObject(i);
                menu.add(1, i+3, i+3, pageObject.optString("page_title"))
                        .setIcon(R.drawable.ic_pages)
                        .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                Intent myIntent = new Intent(context, PageActivity.class);
                                myIntent.putExtra("page_id", pageObject.optString("page_id"));
                                myIntent.putExtra("page_title", pageObject.optString("page_title"));
                                startActivity(myIntent);
                                return false;
                            }
                        });
            }
        }catch (Exception e){}
    }

    public void processCarousel(ArrayList<HashMap<String, String>> data, String headline) {
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.component_carousel, null);
        TextView headlineTextView = view.findViewById(R.id.headline);
        if (headline.trim().isEmpty()) {
            headlineTextView.setVisibility(View.GONE);
        } else {
            headlineTextView.setText(headline);
        }
        RecyclerView listView = view.findViewById(R.id.listView);
        componentContainer.addView(view);

        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        listView.setLayoutManager(layoutManager);
        CarouselAdapter adapter = new CarouselAdapter(context, myDB, data);
        listView.setAdapter(adapter);
        onPostProcess();
    }

    public void processCat(ArrayList<HashMap<String, String>> data, String headline) {
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.component_cat, null);
        TextView headlineTextView = view.findViewById(R.id.headline);
        if (headline.trim().isEmpty()) {
            headlineTextView.setVisibility(View.GONE);
        } else {
            headlineTextView.setText(headline);
        }
        RecyclerView listView = view.findViewById(R.id.listView);
        componentContainer.addView(view);

        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        listView.setLayoutManager(layoutManager);
        CarouselCatAdapter adapter = new CarouselCatAdapter(context, data);
        listView.setAdapter(adapter);
        onPostProcess();
    }

    public void processCustomHTML(String data, String headline) {
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.component_customhtml, null);
        TextView headlineTextView = view.findViewById(R.id.headline);
        if (headline.trim().isEmpty()) {
            headlineTextView.setVisibility(View.GONE);
        } else {
            headlineTextView.setText(headline);
        }
        WebView webView = view.findViewById(R.id.webView);
        componentContainer.addView(view);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setBackgroundColor(Color.TRANSPARENT);
        webView.setVerticalScrollBarEnabled(false);
        webView.loadDataWithBaseURL(null, data, "text/html", "UTF-8", null);
        onPostProcess();
    }

    public void processList(final ArrayList<HashMap<String, String>> data, String headline) {
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.component_list, null);
        TextView headlineTextView = view.findViewById(R.id.headline);
        if (headline.trim().isEmpty()) {
            headlineTextView.setVisibility(View.GONE);
        } else {
            headlineTextView.setText(headline);
        }
        ListView listView = view.findViewById(R.id.listView);
        componentContainer.addView(view);

        ListAdapter adapter = new ListAdapter(context, myDB, data);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent myIntent = new Intent(context, ArticleActivity.class);
                myIntent.putExtra("post_id", data.get(+position).get("post_id"));
                startActivity(myIntent);
            }
        });
        onPostProcess();
    }

    public void onPostProcess() {
        swipeContainer.setRefreshing(false);
        loaderView.stopShimmerAnimation();
        loaderView.setVisibility(View.GONE);
        scrollView.setVisibility(View.VISIBLE);
        registerToken();
    }

    public void registerToken()
    {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            return;
                        }
                        // Get new Instance ID token
                        String token = task.getResult().getToken();
                        if (Extras.isConnected(context)) {
                            new sendTokenData().execute(token);
                        }
                    }
                });
    }

    class sendTokenData extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        protected String doInBackground(String... args) {
            String secure = Config.secureKey;
            String urlParameters = "token="+args[0];
            String response = Extras.excutePost(Config.host+"/api/register_token.php?auth_key="+secure, urlParameters);
            return response;
        }
        @Override
        protected void onPostExecute(String result) {}
    }
}