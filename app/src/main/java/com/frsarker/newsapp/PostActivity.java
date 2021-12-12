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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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

public class PostActivity extends AppCompatActivity {

    Context context = this;
    DBHelper myDB;
    String catId = "";
    TextView toolbarTitle;
    ArrayList<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>();
    SwipeRefreshLayout swipeContainer;
    NestedScrollView scrollView;
    ShimmerLayout loaderView;
    ListView listView;
    LinearLayout errorView, emptyView;
    TextView errorTxt, emptyTxt;
    Integer pageNum = 0;
    Integer scrX = 0;
    Integer scrY = 0;
    Boolean isLoading = false;
    Boolean isLastPage = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setContentView(R.layout.activity_post);

        myDB = new DBHelper(context);
        toolbarTitle = findViewById(R.id.toolbar_title);

        Intent intent = getIntent();
        if (intent.hasExtra("cat_id")) {
            toolbarTitle.setText(intent.getStringExtra("cat_name"));
            catId = intent.getStringExtra("cat_id");
        }

        swipeContainer = findViewById(R.id.swipeContainer);
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
            showError(getString(R.string.post_no_internet_text));
        }

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (Extras.isConnected(context)) {
                    dataList.clear();
                    pageNum = 0;
                    new fetchJsonData().execute();
                } else {
                    showError(getString(R.string.post_no_internet_text));
                }
            }
        });

        scrollView.setOnScrollChangeListener( new NestedScrollView.OnScrollChangeListener(){

            @Override
            public void onScrollChange(NestedScrollView view, int scrollX,int scrollY,int oldScrollX,int oldScrollY) {
                if(Extras.isConnected(context)){
                    if (view.getChildAt(view.getChildCount() - 1) != null) {
                        if ((scrollY >= (view.getChildAt(view.getChildCount() - 1).getMeasuredHeight() - view.getMeasuredHeight())) &&
                                scrollY > oldScrollY) {
                            if ((isLoading == false) && (isLastPage == false)) {
                                new fetchJsonData().execute();
                            }

                        }
                    }
                }
            }

        });

        /* ====  Banner Ad =====*/
        if(Config.displayAdPostsScreen)
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
        swipeContainer.setRefreshing(false);
        errorView.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
        errorTxt.setText(msg);
        loaderView.setVisibility(View.GONE);
        scrollView.setVisibility(View.GONE);
    }
    public void showEmpty(String msg)
    {
        swipeContainer.setRefreshing(false);
        emptyView.setVisibility(View.VISIBLE);
        errorView.setVisibility(View.GONE);
        emptyTxt.setText(msg);
        loaderView.setVisibility(View.GONE);
        scrollView.setVisibility(View.GONE);
    }
    public void hideError()
    {
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
            if(dataList.isEmpty()){
                hideError();
            }
            isLoading = true;
            if(pageNum == 0) {
                hideError();
            }
        }
        protected String doInBackground(String... args) {
            pageNum += 1;
            String secure = Config.secureKey;
            String response = Extras.excuteGet(Config.host+"/api/category_posts.php?cat_id="+ catId +"&page="+pageNum+"&auth_key="+secure);
            return response;
        }
        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jsonObj = new JSONObject(result);
                String status = jsonObj.getString("status");
                if (status.contentEquals("false")){
                    showError(getString(R.string.post_other_error_text));
                } else if (status.contentEquals("empty")) {
                    if(dataList.isEmpty()){
                        showEmpty(getString(R.string.post_nothing_found_text));
                    }else{
                        isLastPage = true;
                    }
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
                    showError(getString(R.string.post_other_error_text));
                }
            }catch (Exception e){
                e.printStackTrace();
                showError(getString(R.string.post_other_error_text));
            }
        }
    }

    public void onPostProcess() {
        swipeContainer.setRefreshing(false);
        scrX = scrollView.getScrollX();
        scrY = scrollView.getScrollY();
        isLoading = false;

        ListAdapter adapter = new ListAdapter(context, myDB, dataList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent myIntent = new Intent(context, ArticleActivity.class);
                myIntent.putExtra("post_id", dataList.get(+position).get("post_id"));
                startActivity(myIntent);
            }
        });

        scrollView.scrollTo(scrX, scrY);
        loaderView.stopShimmerAnimation();
        loaderView.setVisibility(View.GONE);
        scrollView.setVisibility(View.VISIBLE);
    }

}
