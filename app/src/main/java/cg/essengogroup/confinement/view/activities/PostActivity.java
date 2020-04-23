package cg.essengogroup.confinement.view.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import cg.essengogroup.confinement.R;
import cg.essengogroup.confinement.controller.utils.PrefManager;
import cg.essengogroup.confinement.view.dialogs.Dialog_loading;

public class PostActivity extends AppCompatActivity {

    private static final int CHOIX_IMAGE=101,CHOIX_VIDEO=102, STORAGE_PERMISSION_CODE = 123;

    private Uri uriPreviewImage,uriVideo;

    private MaterialButton btnPublier,btnVideo,btnImg;
    private TextInputEditText txtDescription;
    private ImageView imageView;
    private ProgressBar progressBar;

    private Intent intent;

    private StorageReference mStorageRef;
    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private FirebaseDatabase database;
    private DatabaseReference reference;

    private String lienImage,lienVideo;

    private VideoView videoView;
    private RelativeLayout relativeTop;
    private FrameLayout relativeLayout;
    private FloatingActionButton fab,fabImg;

    private PrefManager prefManager;
    private Dialog_loading dialog_loading;

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefManager=new PrefManager(getApplicationContext());

        if (prefManager.getDarkMode()){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES); //For night mode theme
        }else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); //For night mode theme
        }
        setContentView(R.layout.activity_post);

        dialog_loading=new Dialog_loading(PostActivity.this);
        dialog_loading.setCancelable(false);

        //Requesting storage permission
        requestStoragePermission();

        mAuth=FirebaseAuth.getInstance();
        firebaseUser=mAuth.getCurrentUser();
        database=FirebaseDatabase.getInstance();
        reference=database.getReference("confinement/posts");

        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar=getSupportActionBar();
        if (actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        btnPublier=findViewById(R.id.btnPublier);
        txtDescription=findViewById(R.id.txtDescription);
        imageView=findViewById(R.id.imgPreview);
        videoView=findViewById(R.id.video);
        progressBar=findViewById(R.id.progress);
        btnImg=findViewById(R.id.btnImg);
        btnVideo=findViewById(R.id.btnSon);
        relativeLayout=findViewById(R.id.relat);
        relativeTop=findViewById(R.id.relativeTop);
        fab=findViewById(R.id.fab);
        fabImg=findViewById(R.id.fab1);

        imageView.setOnClickListener(v->selectionnerImage());
        fabImg.setOnClickListener(v->selectionnerImage());

        fab.setOnClickListener(v->selectionnerVideo());
        videoView.setOnClickListener(v->selectionnerVideo());

        btnPublier.setOnClickListener(v->{
            if (relativeLayout.getVisibility()== View.VISIBLE){
                if(uriVideo!=null){
                    dialog_loading.show();
                    uploadVideoToFireBase();
                }else {
                    Toast.makeText(this, "Ajoutez une video avant de publier", Toast.LENGTH_SHORT).show();
                }
            }

            if (relativeTop.getVisibility()==View.VISIBLE){
                if (uriPreviewImage!=null){
                    dialog_loading.show();
                    uploadImageToFireBase();
                }else {
                    Toast.makeText(this, "Ajoutez une image avant de publier", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnVideo.setOnClickListener(v->{
            if (relativeTop.getVisibility()==View.VISIBLE){
                relativeTop.setVisibility(View.GONE);
                relativeLayout.setVisibility(View.VISIBLE);
            }
        });

        btnImg.setOnClickListener(v->{
            if (videoView.getVisibility()==View.VISIBLE){
                relativeLayout.setVisibility(View.GONE);
                relativeTop.setVisibility(View.VISIBLE);
            }
        });
    }

    private void selectionnerImage(){
        intent=new Intent();
        intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, CHOIX_IMAGE);
    }
    private void selectionnerVideo(){
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select a Video "), CHOIX_VIDEO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode ==CHOIX_IMAGE && resultCode == RESULT_OK && data != null ){
            uriPreviewImage = data.getData();
            try {
                //getting bitmap object from uri
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uriPreviewImage);

                //displaying selected image to imageview
                imageView.setImageBitmap(bitmap);
                fabImg.setVisibility(View.GONE);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }else if (requestCode == CHOIX_VIDEO && resultCode == RESULT_OK && data != null ){

            uriVideo = data.getData();
            fab.setVisibility(View.GONE);
            MediaController mediaController = new MediaController(this);
            mediaController.setAnchorView(videoView);
            videoView.setMediaController(mediaController);
            videoView.setVideoURI(uriVideo);
            videoView.start();

        }
    }

    private void uploadImageToFireBase(){

        mStorageRef = FirebaseStorage.getInstance()
                .getReference()
                .child("confinement")
                .child("post")
                .child("post"+System.currentTimeMillis()+".jpg");

        if (uriPreviewImage !=null){

            progressBar.setVisibility(View.VISIBLE);

            mStorageRef.putFile(uriPreviewImage).addOnSuccessListener(taskSnapshot -> {
                // Get a URL to the uploaded content
                if (taskSnapshot!=null){

                    mStorageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            lienImage=uri.toString();
                            sendPostToFireBase();
                        }
                    });
                }
                progressBar.setVisibility(View.GONE);

            }).addOnFailureListener(exception -> {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, ""+exception.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void uploadVideoToFireBase(){

        mStorageRef=FirebaseStorage.getInstance()
                .getReference()
                .child("confinement")
                .child("post")
                .child("video"+System.currentTimeMillis()+".mp3");

        if (uriVideo !=null){

            mStorageRef.putFile(uriVideo).addOnSuccessListener(taskSnapshot -> {
                // Get a URL to the uploaded content
                if (taskSnapshot!=null){

                    mStorageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            lienVideo=uri.toString();
                            sendPostToFireBase();
                        }
                    });
                }

            }).addOnFailureListener(exception -> {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, ""+exception.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void sendPostToFireBase(){

        String descriptionValue=txtDescription.getText().toString().trim();

        if (TextUtils.isEmpty(descriptionValue)){
            descriptionValue="";
        }

        Map<String,Object> postData=new HashMap<>();
        postData.put("user_id",firebaseUser.getUid());
        postData.put("pseudo",firebaseUser.getDisplayName());
        postData.put("description",descriptionValue);
        postData.put("createAt", ServerValue.TIMESTAMP);

        if (relativeLayout.getVisibility()==View.VISIBLE){
            postData.put("videoPost",lienVideo);
            postData.put("imagePost",null);
            postData.put("type",2);
        }else {
            postData.put("imagePost",lienImage);
            postData.put("sonPost",null);
            postData.put("type",1);
        }

        reference.child("post"+System.currentTimeMillis())
                .setValue(postData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        if (dialog_loading.isShowing())
                            dialog_loading.dismiss();

                        startActivity(new Intent(PostActivity.this,AccueilActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Write failed
                        // ...
                    }
                });
    }

    private void setNumPost(){
        Map<String,Object> data=new HashMap<>();
        data.put("user_id",firebaseUser.getUid());
        data.put("createAt", ServerValue.TIMESTAMP);

        reference.child("confinement")
                .child("posts")
                .child("post_numero_"+System.currentTimeMillis())
                .setValue(data).addOnSuccessListener(new OnSuccessListener<Void>() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onSuccess(Void aVoid) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //Requesting permission
    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            return;

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            //If the user has denied the permission previously your code will come to this block
            //Here you can explain why you need this permission
            //Explain here why you need this permission
        }
        //And finally ask for the permission
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
    }


    //This method will be called when the user will tap on allow or deny
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        //Checking the request code of our request
        if (requestCode == STORAGE_PERMISSION_CODE) {

            //If permission is granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Displaying a toast
                Toast.makeText(this, "Permission accordée maintenant, vous pouvez lire le stockage", Toast.LENGTH_LONG).show();
            } else {
                //Displaying another toast if permission is not granted
                Toast.makeText(this, "Oups vous venez de refuser la permission", Toast.LENGTH_LONG).show();
            }
        }
    }

}
