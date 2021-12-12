package com.frsarker.newsapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.widget.NestedScrollView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import io.supercharge.shimmerlayout.ShimmerLayout;

public class ArticleActivity  extends AppCompatActivity {

    Context context = this;
    String postId = "";
    DBHelper myDB;
    TextView toolbarTitle;
    WebChromeClientCustom mWebChromeClient;
    SwipeRefreshLayout swipeContainer;
    NestedScrollView scrollView;
    WebView webView;
    ShimmerLayout loaderView;
    LinearLayout errorView, emptyView;
    TextView errorTxt, emptyTxt;
    ImageButton btnBookmark;
    ImageView articleThumbnail;
    CardView thumbnailContainer;
    TextView articleTitle, articleAuthor, articleDate, articleCategory, articleTotalComments;
    FloatingActionButton fabBtn;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setContentView(R.layout.activity_article);

        myDB = new DBHelper(context);
        toolbarTitle = findViewById(R.id.toolbar_title);

        Intent intent = getIntent();
        if (intent.hasExtra("post_id")) {
            postId = intent.getStringExtra("post_id");
        }else{
            finish();
        }

        swipeContainer = findViewById(R.id.swipeContainer);
        scrollView = findViewById(R.id.scrollView);
        loaderView = findViewById(R.id.shimmer_layout);
        errorView = findViewById(R.id.error);
        errorTxt = findViewById(R.id.errorTxt);
        emptyView = findViewById(R.id.empty);
        emptyTxt = findViewById(R.id.emptyTxt);
        btnBookmark = findViewById(R.id.btn_bookmark);
        articleThumbnail = findViewById(R.id.article_thumbnail);
        thumbnailContainer = findViewById(R.id.thumbnail_container);
        articleTitle = findViewById(R.id.article_title);
        articleAuthor = findViewById(R.id.article_author);
        articleDate = findViewById(R.id.article_date);
        articleCategory = findViewById(R.id.article_category);
        articleTotalComments = findViewById(R.id.article_total_comments);
        fabBtn = findViewById(R.id.fabBtn);
        webView = findViewById(R.id.webView);

        mWebChromeClient = new WebChromeClientCustom();
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.setWebChromeClient(mWebChromeClient);

        if(Extras.isConnected(this)){
            new fetchJsonData().execute();
        }else{
            showError(getString(R.string.article_no_internet_text));
        }

        /* Bookmark Btn */
        if(myDB.isBookmarked(postId)){
            btnBookmark.setImageResource(R.drawable.ic_bookmarks);
        }else{
            btnBookmark.setImageResource(R.drawable.ic_add_bookmarks);
        }
        /* Bookmark Btn */

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (Extras.isConnected(context)) {
                    new fetchJsonData().execute();
                } else {
                    showError(getString(R.string.article_no_internet_text));
                }
            }
        });

        /* ====  Banner Ad =====*/
        if(Config.displayAdArticleScreen)
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
        Intent intent = getIntent();
        if (intent.hasExtra("from_notification")) {
            if(intent.getBooleanExtra("from_notification", false)){
                startActivity(new Intent(context, MainActivity.class));
                finish();
            }else{
                finish();
            }
        }else{
            finish();
        }
    }

    public void shareArticle(View v){
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, Config.host+"/article/325"+ postId+"772");
        startActivity(Intent.createChooser(shareIntent, "Share Article"));
    }

    public void changeBookmark(View v){
        if(myDB.isBookmarked(postId)){
            myDB.deleteBookmark(context, postId);
            Toast.makeText(context, "Removed from Favourites", Toast.LENGTH_SHORT).show();
            btnBookmark.setImageResource(R.drawable.ic_add_bookmarks);
        }else{
            myDB.insertBookmark(postId);
            Toast.makeText(context, "Added to Favourites", Toast.LENGTH_SHORT).show();
            btnBookmark.setImageResource(R.drawable.ic_bookmarks);
        }
    }

    public void fabClicked(View v){
        Intent myIntent = new Intent(context, CommentActivity.class);
        myIntent.putExtra("post_id", postId);
        startActivity(myIntent);
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
            hideError();
        }

        protected String doInBackground(String... args) {
            String secure = Config.secureKey;
            String response = Extras.excuteGet(Config.host+"/api/get_article.php?post_id="+ postId +"&auth_key="+secure);
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jsonObj = new JSONObject(result);
                String status = jsonObj.getString("status");
                if (status.contentEquals("false")){
                    showError(getString(R.string.article_other_error_text));
                } else if (status.contentEquals("true")) {

                    Picasso.get()
                            .load(Config.host+"/styles/files/uploads/"+jsonObj.getString("post_cover"))
                            .resize(500, 300)
                            .centerCrop()
                            .into(articleThumbnail, new Callback() {
                                @Override
                                public void onSuccess() {
                                    thumbnailContainer.setVisibility(View.VISIBLE);
                                }
                                @Override
                                public void onError(Exception ex) {
                                    thumbnailContainer.setVisibility(View.GONE);
                                }
                            });

                    articleTitle.setText(jsonObj.getString("post_title"));

                    if(jsonObj.getString("post_author").trim().length()> 1){
                        articleAuthor.setVisibility(View.VISIBLE);
                        articleAuthor.setText("Posted By: "+jsonObj.getString("post_author"));
                    }else{
                        articleAuthor.setVisibility(View.GONE);
                    }

                    articleDate.setText("Published on "+jsonObj.getString("post_date"));
                    articleCategory.setText(jsonObj.getString("cat_name"));

                    String comment_text = Integer.parseInt(jsonObj.getString("total_comments")) > 1 ? "Comments" : "Comment";
                    articleTotalComments.setText(jsonObj.getString("total_comments")+" "+comment_text);

                    if(jsonObj.getString("comment_status").contentEquals("true"))
                    {
                        fabBtn.show();
                    }else{
                        fabBtn.hide();
                    }

                    String post_content = jsonObj.getString("post_content");
                    webView.loadDataWithBaseURL(null, post_content, "text/html", "UTF-8", null);
                    onPostProcess();
                }else{
                    showError(getString(R.string.article_other_error_text));
                }
            }catch (Exception e){
                e.printStackTrace();
                showError(getString(R.string.article_other_error_text));
            }
        }
    }

    public void onPostProcess() {
        swipeContainer.setRefreshing(false);
        loaderView.stopShimmerAnimation();
        loaderView.setVisibility(View.GONE);
        scrollView.setVisibility(View.VISIBLE);
    }



    private class WebChromeClientCustom extends WebChromeClient {
        private static final int FULL_SCREEN_SETTING = View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_IMMERSIVE;
        private View mCustomView;
        private WebChromeClient.CustomViewCallback mCustomViewCallback;
        private int mOriginalOrientation;
        private int mOriginalSystemUiVisibility;
        public void onHideCustomView() {
            ((FrameLayout) getWindow().getDecorView()).removeView(this.mCustomView);
            this.mCustomView = null;
            getWindow().getDecorView().setSystemUiVisibility(this.mOriginalSystemUiVisibility);
            setRequestedOrientation(this.mOriginalOrientation);
            this.mCustomViewCallback.onCustomViewHidden();
            this.mCustomViewCallback = null;
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
        }
        @Override
        public void onShowCustomView(View paramView, WebChromeClient.CustomViewCallback paramCustomViewCallback) {
            if (this.mCustomView != null) {
                onHideCustomView();
                return;
            }
            this.mCustomView = paramView;
            this.mOriginalSystemUiVisibility = getWindow().getDecorView().getSystemUiVisibility();
            this.mOriginalOrientation = getRequestedOrientation();
            this.mCustomViewCallback = paramCustomViewCallback;
            ((FrameLayout) getWindow()
                    .getDecorView())
                    .addView(this.mCustomView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            ArticleActivity.this.getWindow().getDecorView().setSystemUiVisibility(FULL_SCREEN_SETTING);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
            this.mCustomView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
                @Override
                public void onSystemUiVisibilityChange(int visibility) {
                    updateControls();
                }
            });
        }
        @Override
        public Bitmap getDefaultVideoPoster() {
            return Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888);
        }
        void updateControls() {
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) this.mCustomView.getLayoutParams();
            params.bottomMargin = 0;
            params.topMargin = 0;
            params.leftMargin = 0;
            params.rightMargin = 0;
            params.height = ViewGroup.LayoutParams.MATCH_PARENT;
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            this.mCustomView.setLayoutParams(params);
            ArticleActivity.this.getWindow().getDecorView().setSystemUiVisibility(FULL_SCREEN_SETTING);
        }
    }
}
