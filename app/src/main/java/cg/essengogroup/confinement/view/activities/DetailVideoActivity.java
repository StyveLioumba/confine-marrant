package cg.essengogroup.confinement.view.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import android.Manifest;
import android.app.DownloadManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import cg.essengogroup.confinement.R;
import cg.essengogroup.confinement.controller.adapters.MultiViewTypeAdapter;
import cg.essengogroup.confinement.controller.utils.PrefManager;
import cg.essengogroup.confinement.model.Model;

public class DetailVideoActivity extends AppCompatActivity {
    private static final int PERMISSION_STORAGE_CODE = 1000;

    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private FirebaseDatabase database;
    private DatabaseReference reference,refLikes,refGetLikes,refComment;

    private Intent intent;
    private Model model;

    private ImageView imageLike,imageDownload,imageComment;
    private TextView txtDescription,txtLikes,txtDownload,txtCmt;
    private ImageButton partager;

    private VideoView videoView;

    private PrefManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefManager=new PrefManager(getApplicationContext());

        if (prefManager.getDarkMode()){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES); //For night mode theme
        }else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); //For night mode theme
        }
        setContentView(R.layout.activity_detail_video);

        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar=getSupportActionBar();
        if (actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        intent=getIntent();

        if (intent!=null){
            model=intent.getParcelableExtra("model");
        }

        mAuth= FirebaseAuth.getInstance();
        firebaseUser=mAuth.getCurrentUser();
        database= FirebaseDatabase.getInstance();
        reference = database.getReference().child("confinement/posts");
        refGetLikes = reference.child(model.getPost_id()).child("likes");
        refLikes = reference.child(model.getPost_id()).child("likes").child(firebaseUser.getDisplayName());
        refComment=reference.child(model.getPost_id()+"/commentaires");

        videoView=findViewById(R.id.video);
        txtDescription=findViewById(R.id.txtDescription);

        imageLike=findViewById(R.id.imgLikes);
        partager=findViewById(R.id.partager);
        txtLikes=findViewById(R.id.txtLikes);
        txtCmt=findViewById(R.id.txtCmt);
        txtDownload=findViewById(R.id.txtDownload);

        imageDownload=findViewById(R.id.imgDownload);
        imageComment=findViewById(R.id.imgCmt);

        imageLike.setOnClickListener(v->likeOrDislike());

        imageDownload.setOnClickListener(v->setDownload());

        txtDescription.setText(model.getDescription());

        partager.setOnClickListener(v->shareVideo());

        imageComment.setOnClickListener(v->startActivity(new Intent(getApplicationContext(),CommentaireActivity.class).putExtra("model",model)));

        verifierIfIsLike();
        getNbreLikes();
        getDownload();
        getNbreLikes();
        getNbreCommentaire();

        Uri video = Uri.parse(model.getVideo());
        videoView.setVideoURI(video);
        videoView.setOnPreparedListener(PreparedListener);


        MediaController mediaController = new MediaController(this);
        videoView.setMediaController(mediaController);
        mediaController.setAnchorView(videoView);

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void likePost(){

        Map<String, Object> post=new HashMap<>();
        post.put("user_id",firebaseUser.getUid());
        post.put("createAt", ServerValue.TIMESTAMP);

        refLikes.setValue(post).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

            }
        });

    }

    private void dislikePost(){
        refLikes.removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

            }
        });
    }

    private void getNbreLikes(){
        refGetLikes.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // get total available quest
                txtLikes.setText(String.valueOf(dataSnapshot.getChildrenCount()));
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getNbreCommentaire(){
        refComment.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot!=null){
                   txtCmt.setText(String.valueOf(dataSnapshot.getChildrenCount()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void verifierIfIsLike(){
        refLikes.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    imageLike.setImageResource(R.drawable.ic_favorite_black_24dp);
                }else {
                    imageLike.setImageResource(R.drawable.ic_favorite_border_black_24dp);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void likeOrDislike(){
        refLikes.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    dislikePost();
                    imageLike.setImageResource(R.drawable.ic_favorite_black_24dp);
                }else {
                    likePost();
                    imageLike.setImageResource(R.drawable.ic_favorite_border_black_24dp);
                }
                getNbreLikes();
                verifierIfIsLike();
                getDownload();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setDownload(){

        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
                String[] permissions={
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                };

                requestPermissions(permissions,PERMISSION_STORAGE_CODE);
            }else {
                startDownloading(model);
            }
        }else {
            startDownloading(model);
        }

        Map<String,Object> data=new HashMap<>();
        data.put("createAt", ServerValue.TIMESTAMP);

        reference=database.getReference("confinement/posts/"+model.getPost_id()+"/telecharger");
        reference.child("telecharger"+System.currentTimeMillis())
                .setValue(data).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

            }
        });
    }

    private void getDownload(){
        reference=database.getReference("confinement/posts/"+model.getPost_id()+"/telecharger");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot!=null){
                    txtDownload.setText(String.valueOf(dataSnapshot.getChildrenCount()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void startDownloading(Model model){
        DownloadManager.Request request =new DownloadManager.Request(Uri.parse(model.getVideo()));

        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
        request.setTitle("Telechargement");
        request.setDescription("Telechargement du fichier "+model.getImage());

        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,model.getImage()+System.currentTimeMillis());

        DownloadManager manager= (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);
    }

    private MediaPlayer.OnPreparedListener PreparedListener = m -> {
        try {
            if (m.isPlaying()) {
                m.stop();
                m.release();
                m = new MediaPlayer();
            }
//            m.setVolume(0f, 0f);
            m.setLooping(false);
            m.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    };


    private void shareVideo(){
        Intent share = new Intent(android.content.Intent.ACTION_SEND);
        share.setType("text/plain");
        share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

        // Add data to the intent, the receiving app will decide
        // what to do with it.
        share.putExtra(Intent.EXTRA_SUBJECT, model.getDescription());
        share.putExtra(Intent.EXTRA_TEXT, model.getVideo());

        startActivity(Intent.createChooser(share, "Share link!"));
    }
}
