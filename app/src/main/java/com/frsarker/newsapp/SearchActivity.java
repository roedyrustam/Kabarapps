package com.frsarker.newsapp;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import com.frsarker.newsapp.adapters.ListAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import io.supercharge.shimmerlayout.ShimmerLayout;

public class SearchActivity extends AppCompatActivity {

    Context context = this;
    DBHelper myDB;
    EditText editText;
    ArrayList<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>();
    NestedScrollView scrollView;
    ShimmerLayout loaderView;
    ListView listView;
    LinearLayout errorView, emptyView, initView;
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
        setContentView(R.layout.activity_search);

        myDB = new DBHelper(context);

        editText = findViewById(R.id.editText);
        scrollView = findViewById(R.id.scrollView);
        listView = findViewById(R.id.listView);
        loaderView = findViewById(R.id.shimmer_layout);
        errorView = findViewById(R.id.error);
        errorTxt = findViewById(R.id.errorTxt);
        emptyView = findViewById(R.id.empty);
        emptyTxt = findViewById(R.id.emptyTxt);
        initView = findViewById(R.id.init);

        editText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {}
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(editText.getText().toString().trim().length()>0){
                    if(Extras.isConnected(SearchActivity.this)){
                        dataList.clear();
                        pageNum = 0;
                        new fetchJsonData().execute(s.toString());
                    }else{
                        showError(getString(R.string.search_no_internet_text));
                    }
                }else{
                    showInit();
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
                                new fetchJsonData().execute(editText.getText().toString().trim());
                            }

                        }
                    }
                }
            }

        });

    }

    public void finishView(View v){
        finish();
    }

    public void showError(String msg)
    {
        errorView.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
        initView.setVisibility(View.GONE);
        errorTxt.setText(msg);
        loaderView.setVisibility(View.GONE);
        scrollView.setVisibility(View.GONE);
    }
    public void showEmpty(String msg)
    {
        emptyView.setVisibility(View.VISIBLE);
        errorView.setVisibility(View.GONE);
        initView.setVisibility(View.GONE);
        emptyTxt.setText(msg);
        loaderView.setVisibility(View.GONE);
        scrollView.setVisibility(View.GONE);
    }
    public void hideError()
    {
        errorView.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);
        initView.setVisibility(View.GONE);
        loaderView.setVisibility(View.VISIBLE);
        scrollView.setVisibility(View.GONE);
        loaderView.startShimmerAnimation();
    }
    public void showInit()
    {
        initView.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
        errorView.setVisibility(View.GONE);
        loaderView.setVisibility(View.GONE);
        scrollView.setVisibility(View.GONE);
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
            String urlParameters = "search="+ args[0];
            String response = Extras.excutePost(Config.host+"/api/search.php?page="+pageNum+"&auth_key="+secure, urlParameters);
            return response;
        }
        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jsonObj = new JSONObject(result);
                String status = jsonObj.getString("status");
                if (status.contentEquals("false")){
                    showError(getString(R.string.search_other_error_text));
                } else if (status.contentEquals("empty")) {
                    if(dataList.isEmpty()){
                        showEmpty(getString(R.string.search_nothing_found_text));
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
                    showError(getString(R.string.search_other_error_text));
                }
            }catch (Exception e){
                e.printStackTrace();
                showError(getString(R.string.search_other_error_text));
            }
        }
    }

    public void onPostProcess() {
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
