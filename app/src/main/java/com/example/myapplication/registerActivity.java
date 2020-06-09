package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;

import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.appevents.suggestedevents.ViewOnClickListener;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;


import java.text.CollationElementIterator;
import java.util.Arrays;

public class registerActivity extends AppCompatActivity {
    EditText mFullname;
    EditText mEmail;
    EditText mPassword;
    TextView mSignup;
    TextView textViewUser;
    FirebaseAuth mAuth;
    Button register;
    Button google;
    ImageView mLogo;
    Button facebooklogin;
    private AccessTokenTracker accessTokenTracker;
    private FirebaseAuth.AuthStateListener authStateListener;
    private static final String TAG= "Facebook authentication";
    CallbackManager mcallbackmanager;
    private GoogleSignInClient mGoogleSignInClient;
    private final static int RC_SIGN_IN=123;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_activity);
        mAuth= FirebaseAuth.getInstance();
        FacebookSdk.sdkInitialize(getApplicationContext());

        mFullname= (EditText)findViewById(R.id.fullname);
        mEmail= (EditText)findViewById(R.id.email);

        mPassword= (EditText)findViewById(R.id.password);
        mSignup=(TextView)findViewById(R.id.sign_in);
        register= (Button)findViewById(R.id.register);


        facebooklogin=findViewById(R.id.facebook);

        mLogo=(ImageView) findViewById(R.id.imageView);
        mcallbackmanager= CallbackManager.Factory.create();
        facebooklogin.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                LoginManager.getInstance().logInWithReadPermissions(registerActivity.this, Arrays.asList("email","public_profile"));
                LoginManager.getInstance().registerCallback(mcallbackmanager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Log.d(TAG, "ON SUCCESS" + loginResult );
                        handleFacebookToken(loginResult.getAccessToken());

                    }

                    @Override
                    public void onCancel() {
                        Log.d(TAG, "onCancel");
                    }

                    @Override
                    public void onError(FacebookException error) {
                        Log.d(TAG, "onError" + error);
                    }
                });
            }
        });


        createRequest();
        google=(Button)findViewById(R.id.google);
        google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
         if(mAuth.getCurrentUser()!=null){
             startActivity(new Intent(getApplicationContext(),Mainpage.class));
             finish();
         }
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name= mFullname.getText().toString().trim();
                String email=mEmail.getText().toString().trim();
                String password= mPassword.getText().toString().trim();

                if(TextUtils.isEmpty(email)){
                    mEmail.setError("Email is required");
                    return;
                }
                if(TextUtils.isEmpty(password)){
                    mPassword.setError("Password is required");
                    return;
                }
                if(password.length()<0){
                    mPassword.setError("Password must be => 6 characters");
                    return;
                }

             mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                 @Override
                 public void onComplete(@NonNull Task<AuthResult> task) {
                   if(task.isSuccessful()){
                       Toast.makeText(registerActivity.this,"user created",Toast.LENGTH_SHORT).show();
                       startActivity(new Intent(getApplicationContext(),Mainpage.class));
                   }else {
                       Toast.makeText(registerActivity.this, "Error!" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                   }
                   }
             });


            }
        });
        mSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent (getApplicationContext(),loginActivity.class));
            }
        });
    }

   private void handleFacebookToken(AccessToken token){
      Log.d(TAG, "handleFacebookToken" +token);

      AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
      mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
          @Override
          public void onComplete(@NonNull Task<AuthResult> task) {
              if(task.isSuccessful()){
                  Log.d(TAG, "sign in with credential :  successful ");
                  FirebaseUser user= mAuth.getCurrentUser();
                  updateUI();
              }else{
                  Log.d(TAG, "sign in with credential :  failure ", task.getException());
                  Toast.makeText(registerActivity.this,"Authentication failed",Toast.LENGTH_SHORT).show();
                  updateUI();
              }
          }
      });
   }





    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser user= mAuth.getCurrentUser();
        if(user!=null){
            updateUI();
            Intent intent= new Intent(getApplicationContext(),Mainpage.class);
            startActivity(intent);
        }
    }
    private void updateUI(){
       Toast.makeText(registerActivity.this, "you're logged in ", Toast.LENGTH_SHORT).show();
       Intent fblogin= new Intent(registerActivity.this, Mainpage.class);
       startActivity(fblogin);
       finish();
    }


    private void createRequest(){
        //configure google sign in
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

    }

    public void signIn(){
        Intent signInIntent= mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent,RC_SIGN_IN);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        mcallbackmanager.onActivityResult(requestCode,resultCode,data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);

                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                // ...
                Toast.makeText(this,e.getMessage(),Toast.LENGTH_SHORT).show();
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
                            FirebaseUser user = mAuth.getCurrentUser();
                            Intent intent= new Intent(getApplicationContext(),Mainpage.class);
                            startActivity(intent);
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(registerActivity.this,"sorry auth failed", Toast.LENGTH_SHORT ).show();

                        }

                        // ...
                    }
                });
    }


}
