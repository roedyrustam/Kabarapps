package com.frsarker.newsapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.shobhitpuri.custombuttons.GoogleSignInButton;

public class StartActivity extends AppCompatActivity {

    Context context = this;
    SharedPreferences sharedpreferences;
    SharedPreferences.Editor editor;
    private static final int RC_SIGN_IN = 9001;
    GoogleSignInButton signInButton;
    Button skipBtn;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    Boolean isFromCommentScreen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setContentView(R.layout.activity_start);

        sharedpreferences = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
        editor = sharedpreferences.edit();

        /* Handle Notification */
        if (getIntent().getExtras() != null) {
            for (String key : getIntent().getExtras().keySet()) {
                String value = getIntent().getExtras().getString(key);
                if(key.contentEquals("news_id"))
                {
                    Intent myIntent = new Intent(context, ArticleActivity.class);
                    myIntent.putExtra("from_notification", true);
                    myIntent.putExtra("post_id", value);
                    context.startActivity(myIntent);
                    finish();
                }
            }
        }

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        skipBtn = findViewById(R.id.skipBtn);
        signInButton = findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        Intent intent = getIntent();
        if (intent.hasExtra("isFromCommentScreen")) {
            isFromCommentScreen = intent.getBooleanExtra("isFromCommentScreen", false);
            if(isFromCommentScreen) {
                skipBtn.setVisibility(View.GONE);
                editor.putInt("skipped", 1);
                editor.apply();
            }
        }

    }

    public void setAsSplashScreen()
    {
        signInButton.setVisibility(View.GONE);
        skipBtn.setVisibility(View.GONE);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(context, MainActivity.class));
                finish();
            }
        }, Config.splashTimeout);
    }

    public void skip(View v)
    {
        editor.putInt("skipped", 1);
        editor.apply();
        startActivity(new Intent(context, MainActivity.class));
        finish();
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(context, "Sign in failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            if(isFromCommentScreen) {
                                finish();
                            }else{
                                startActivity(new Intent(context, MainActivity.class));
                                finish();
                            }
                        }else{
                            signOut();
                            Toast.makeText(context, "Something wrong.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void signOut(){
        mAuth.signOut();
        mGoogleSignInClient.signOut();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(!isFromCommentScreen) {
            if(sharedpreferences.getInt("skipped", 0) == 1)
            {
                setAsSplashScreen();
            }else if(currentUser != null){
                setAsSplashScreen();
            }
        }
    }

}