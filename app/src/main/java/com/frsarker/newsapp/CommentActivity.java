package com.frsarker.newsapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.frsarker.newsapp.adapters.CommentAdapter;
import com.frsarker.newsapp.helper.InputValidatorHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import io.supercharge.shimmerlayout.ShimmerLayout;

public class CommentActivity extends AppCompatActivity {

    Context context = this;
    private FirebaseAuth mAuth;
    String postId = "";
    ArrayList<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>();
    SwipeRefreshLayout swipeContainer;
    NestedScrollView scrollView;
    ShimmerLayout loaderView;
    ListView listView;
    FloatingActionButton fabBtn;
    LinearLayout errorView, emptyView;
    TextView errorTxt, emptyTxt;
    Integer pageNum = 0;
    Integer scrX = 0;
    Integer scrY = 0;
    Boolean isLoading = false;
    Boolean isLastPage = false;
    Boolean login_only_comment = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setContentView(R.layout.activity_comment);

        Intent intent = getIntent();
        if (intent.hasExtra("post_id")) {
            postId = intent.getStringExtra("post_id");
        }else{
            finish();
        }

        swipeContainer = findViewById(R.id.swipeContainer);
        scrollView = findViewById(R.id.scrollView);
        listView = findViewById(R.id.listView);
        fabBtn = findViewById(R.id.fabBtn);
        loaderView = findViewById(R.id.shimmer_layout);
        errorView = findViewById(R.id.error);
        errorTxt = findViewById(R.id.errorTxt);
        emptyView = findViewById(R.id.empty);
        emptyTxt = findViewById(R.id.emptyTxt);

        if(Extras.isConnected(this)){
            new fetchJsonData().execute();
        }else{
            showError(getString(R.string.comment_no_internet_text));
        }

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (Extras.isConnected(context)) {
                    dataList.clear();
                    pageNum = 0;
                    new fetchJsonData().execute();
                } else {
                    showError(getString(R.string.dashboard_no_internet_text));
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
    }

    public void finishView(View v){
        finish();
    }

    public void fabClicked(View view){
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            String userName = currentUser.getDisplayName();
            String userEmail = currentUser.getEmail();
            String photoUrl = currentUser.getPhotoUrl().toString();
            for (UserInfo profile : currentUser.getProviderData()) {
                if (profile.getProviderId().equals("facebook.com")) {
                    String facebookUserId = profile.getUid();
                    photoUrl = "https://graph.facebook.com/" + facebookUserId + "/picture?height=500";
                } else if (profile.getProviderId().equals("google.com")) { }
            }
            fabClickedForNotLogged(userName, userEmail, photoUrl);
        }else{
            if(login_only_comment){
                Intent i = new Intent(context, StartActivity.class);
                i.putExtra("isFromCommentScreen", true);
                startActivity(i);
            }else{
                fabClickedForNotLogged("", "", "");
            }
        }

    }

    public void fabClickedForNotLogged(final String userName, final String userEmail, final String userPhoto) {
        final View dialogView = View.inflate(this, R.layout.dialog_comment, null);
        final EditText user_name_box = dialogView.findViewById(R.id.user_name);
        final EditText user_email_box = dialogView.findViewById(R.id.user_email);
        final EditText comment_box = dialogView.findViewById(R.id.comment);

        if(userName.length() > 0 && userEmail.length() > 0){
            user_name_box.setText(userName);
            user_email_box.setText(userEmail);
            user_name_box.setVisibility(View.GONE);
            user_email_box.setVisibility(View.GONE);
        }

        final AlertDialog dialog = new AlertDialog.Builder(this)
                                    .setView(dialogView)
                                    .setTitle("Post Comment")
                                    .setNegativeButton("Cancel", null)
                                    .setPositiveButton("Post", null)
                                    .create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        // TODO Do something

                        InputValidatorHelper inputValidatorHelper = new InputValidatorHelper();
                        boolean allowSave = true;
                        String user_name = user_name_box.getText().toString();
                        String user_email = user_email_box.getText().toString();
                        String comment = comment_box.getText().toString();

                        if (!inputValidatorHelper.isValidEmail(user_email)  || inputValidatorHelper.isNullOrEmpty(user_email)) {
                            user_email_box.setError("Invalid email address");
                            allowSave = false;
                        }
                        if (!inputValidatorHelper.isValidName(user_name) || inputValidatorHelper.isNullOrEmpty(user_name)) {
                            user_name_box.setError("Invalid full name");
                            allowSave = false;
                        }

                        if (inputValidatorHelper.isNullOrEmpty(comment)) {
                            comment_box.setError("Comment should not be empty.");
                            allowSave = false;
                        }

                        if(allowSave)
                        {
                            if(Extras.isConnected(context)){
                                new postCommentData().execute(user_name, user_email, userPhoto, comment);
                            }else{
                                Toast.makeText(context, getString(R.string.comment_post_error_text), Toast.LENGTH_LONG).show();
                            }
                            dialog.dismiss();
                        }
                    }
                });
            }
        });
        dialog.show();
    }

    class postCommentData extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(context, "Please wait...", Toast.LENGTH_SHORT).show();
        }
        protected String doInBackground(String... args) {
            String secure = Config.secureKey;
            String urlParameters = "post_id="+ postId +"&user_name="+args[0]+"&user_email="+args[1]+"&user_photo="+args[2]+"&comment="+args[3];
            String response = Extras.excutePost(Config.host+"/api/comment_post.php?auth_key="+secure, urlParameters);
            return response;
        }
        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jsonObj = new JSONObject(result);
                String status = jsonObj.getString("status");
                String msg = jsonObj.getString("msg");
                if (status.contentEquals("false")){
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                } else if (status.contentEquals("true")) {
                    Toast.makeText(context, getString(R.string.comment_post_success_text), Toast.LENGTH_LONG).show();
                }else{
                    showError(getString(R.string.comment_post_error_text));
                }
            }catch (Exception e){
                e.printStackTrace();
                showError(getString(R.string.comment_post_error_text));
            }
        }
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
            String response = Extras.excuteGet(Config.host+"/api/get_comments.php?post_id="+ postId +"&page="+pageNum+"&auth_key="+secure);
            return response;
        }
        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jsonObj = new JSONObject(result);
                String status = jsonObj.getString("status");
                if (status.contentEquals("false")){
                    showError(getString(R.string.comment_other_error_text));
                } else if (status.contentEquals("empty")) {
                    login_only_comment = jsonObj.getBoolean("login_only_comment");
                    if(dataList.isEmpty()){
                        showEmpty(getString(R.string.comment_nothing_found_text));
                        fabBtn.show();
                    }else{
                        isLastPage = true;
                    }
                } else if (status.contentEquals("true")) {
                    login_only_comment = jsonObj.getBoolean("login_only_comment");
                    JSONArray posts = jsonObj.getJSONArray("comments");
                    for (int i = 0; i < posts.length(); i++) {
                        JSONObject jsonObject = posts.getJSONObject(i);
                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put("comment_id", jsonObject.optString("comment_id"));
                        map.put("user_name", jsonObject.optString("user_name"));
                        map.put("user_photo", jsonObject.optString("user_photo"));
                        map.put("comment", jsonObject.optString("comment"));
                        map.put("commented_at", jsonObject.optString("commented_at"));
                        dataList.add(map);
                    }
                    onPostProcess();
                    fabBtn.show();
                }else{
                    showError(getString(R.string.comment_other_error_text));
                }
            }catch (Exception e){
                e.printStackTrace();
                showError(getString(R.string.comment_other_error_text));
            }
        }
    }

    public void onPostProcess() {
        swipeContainer.setRefreshing(false);
        scrX = scrollView.getScrollX();
        scrY = scrollView.getScrollY();
        isLoading = false;

        CommentAdapter adapter = new CommentAdapter(context, dataList);
        listView.setAdapter(adapter);
        scrollView.scrollTo(scrX, scrY);
        loaderView.stopShimmerAnimation();
        loaderView.setVisibility(View.GONE);
        scrollView.setVisibility(View.VISIBLE);
    }
}
