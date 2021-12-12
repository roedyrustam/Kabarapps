package com.frsarker.newsapp;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import com.frsarker.newsapp.adapters.ListAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import io.supercharge.shimmerlayout.ShimmerLayout;

public class BookmarksActivity extends AppCompatActivity {

    private static BookmarksActivity instance;
    Context context = this;
    DBHelper myDB;
    ArrayList<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>();
    NestedScrollView scrollView;
    ShimmerLayout loaderView;
    ListView listView;
    LinearLayout errorView, emptyView;
    TextView errorTxt, emptyTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setContentView(R.layout.activity_bookmarks);

        instance = this;
        myDB = new DBHelper(context);
        scrollView = findViewById(R.id.scrollView);
        listView = findViewById(R.id.listView);
        loaderView = findViewById(R.id.shimmer_layout);
        errorView = findViewById(R.id.error);
        errorTxt = findViewById(R.id.errorTxt);
        emptyView = findViewById(R.id.empty);
        emptyTxt = findViewById(R.id.emptyTxt);
        if(Extras.isConnected(this)){
            new fetchJsonData().execute();
        }else{
            showError(getString(R.string.bookmarks_no_internet_text));
        }

        /* ====  Banner Ad =====*/
        if(Config.displayAdBookmarksScreen)
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

    public void finishView(View v){
        finish();
    }

    public void showError(String msg)
    {
        errorView.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
        errorTxt.setText(msg);
        loaderView.setVisibility(View.GONE);
        scrollView.setVisibility(View.GONE);
    }
    public void showEmpty(String msg)
    {
        emptyView.setVisibility(View.VISIBLE);
        errorView.setVisibility(View.GONE);
        emptyTxt.setText(msg);
        loaderView.setVisibility(View.GONE);
        scrollView.setVisibility(View.GONE);
    }
    public void hideError()
    {
        errorView.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);
        loaderView.setVisibility(View.VISIBLE);
        scrollView.setVisibility(View.GONE);
        loaderView.startShimmerAnimation();
    }

    public static BookmarksActivity getInstance() {
        return instance;
    }

    public void reloadListView()
    {
        if(Extras.isConnected(this)){
            new fetchJsonData().execute();
        }else{
            showError(getString(R.string.bookmarks_no_internet_text));
        }
    }

    class fetchJsonData extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dataList.clear();
            hideError();
        }
        protected String doInBackground(String... args) {
            String secure = Config.secureKey;
            String urlParameters = "post_ids="+ myDB.getAllBookmarks();
            String response = Extras.excutePost(Config.host+"/api/bookmark_posts.php?auth_key="+secure, urlParameters);
            return response;
        }
        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jsonObj = new JSONObject(result);
                String status = jsonObj.getString("status");
                if (status.contentEquals("false")){
                    showError(getString(R.string.bookmarks_other_error_text));
                } else if (status.contentEquals("empty")) {
                        showEmpty(getString(R.string.bookmarks_nothing_found_text));
                } else if (status.contentEquals("true")) {

                    JSONArray posts = jsonObj.getJSONArray("posts");
                    for (int i = 0; i < posts.length(); i++) {
                        JSONObject jsonObject = posts.getJSONObject(i);

                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put("post_id", jsonObject.optString("post_id"));
                        map.put("cat_name", jsonObject.optString("cat_name"));
                        map.put("post_cat", jsonObject.optString("post_cat"));
                        map.put("post_title", jsonObject.optString("post_title"));
                        map.put("post_cover", jsonObject.optString("post_cover"));
                        map.put("post_date", jsonObject.optString("post_date"));
                        map.put("comment_status", jsonObject.optString("comment_status"));
                        dataList.add(map);
                    }
                    onPostProcess();
                }else{
                    showError(getString(R.string.bookmarks_other_error_text));
                }
            }catch (Exception e){
                e.printStackTrace();
                showError(getString(R.string.bookmarks_other_error_text));
            }
        }
    }

    public void onPostProcess() {
        ListAdapter adapter = new ListAdapter(context, myDB, dataList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Intent i = new Intent(context, ArticleActivity.class);
                            i.putExtra("post_id", dataList.get(+position).get("post_id"));
                            startActivity(i);
            }
        });

        loaderView.stopShimmerAnimation();
        loaderView.setVisibility(View.GONE);
        scrollView.setVisibility(View.VISIBLE);
    }

}