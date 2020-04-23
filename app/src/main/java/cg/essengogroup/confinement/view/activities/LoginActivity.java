package cg.essengogroup.confinement.view.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import com.facebook.FacebookSdk;


import java.util.HashMap;
import java.util.Map;

import cg.essengogroup.confinement.R;
import cg.essengogroup.confinement.controller.utils.PrefManager;
import cg.essengogroup.confinement.view.dialogs.Dialog_loading;

public class LoginActivity extends AppCompatActivity {

    private CallbackManager callbackManager;
    private LoginButton btnFbCnx;

    private FirebaseAuth mAuth;

    private FirebaseDatabase database;
    private DatabaseReference mReference;
    private FirebaseAuth.AuthStateListener authStateListener;
    private AccessTokenTracker accessTokenTracker;

    private PrefManager prefManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefManager=new PrefManager(getApplicationContext());

        if (prefManager.getDarkMode()){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        setContentView(R.layout.activity_login);

        mAuth=FirebaseAuth.getInstance();
        database=FirebaseDatabase.getInstance();

        btnFbCnx=findViewById(R.id.login_button);

        btnFbCnx.setReadPermissions("email","public_profile");
        FacebookSdk.sdkInitialize(LoginActivity.this);
        callbackManager=CallbackManager.Factory.create();

        btnFbCnx.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFbTokenAccesToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });

        authStateListener=new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user=firebaseAuth.getCurrentUser();
                if (user!=null){
                    startActivity(new Intent(getApplicationContext(),AccueilActivity.class));
                    finish();
                }
            }
        };

        accessTokenTracker=new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                if (currentAccessToken==null){
                    mAuth.signOut();
                }
            }
        };

       /* btnContinuer.setOnClickListener(v->{
            pseudoValue=editPseudo.getText().toString().trim();

            if (TextUtils.isEmpty(pseudoValue)){
                editPseudo.setError("entrez votre pseudo");
                editPseudo.requestFocus();
                return;
            }

            dialog_loading.show();
            singInAnonyme(pseudoValue);
        });

        editPseudo.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    inputLayout.setHint("Pseudo");
                } else {
                    inputLayout.setHint("ex : essengogroup123");
                }
            }
        });*/
    }

    private void handleFbTokenAccesToken(AccessToken token){
        AuthCredential credential= FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    FirebaseUser user=mAuth.getCurrentUser();
                    saveUserInfoToDatabase(user);
                }
            }
        });
    }

    private void saveUserInfoToDatabase(FirebaseUser user){

        mReference=database.getReference().child("confinement").child("users").child(user.getDisplayName());

        Map<String, Object> data = new HashMap<>();
        data.put("user_id", user.getUid());
        data.put("pseudo", user.getDisplayName().toLowerCase());
        data.put("image", String.valueOf(user.getPhotoUrl()));
        data.put("create_at", ServerValue.TIMESTAMP);

        mReference.setValue(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        startActivity(new Intent(getApplicationContext(),AccueilActivity.class));
                        finish();
                    }
                });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        callbackManager.onActivityResult(requestCode,resultCode,data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (authStateListener!=null){
            mAuth.removeAuthStateListener(authStateListener);
        }
    }


}
