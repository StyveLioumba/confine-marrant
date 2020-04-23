package cg.essengogroup.confinement.view.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import cg.essengogroup.confinement.R;
import cg.essengogroup.confinement.controller.adapters.CommentaireAdapter;
import cg.essengogroup.confinement.controller.utils.PrefManager;
import cg.essengogroup.confinement.model.Commentaire;
import cg.essengogroup.confinement.model.Model;

public class CommentaireActivity extends AppCompatActivity {

    private PrefManager prefManager;

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseDatabase database;
    private DatabaseReference reference,refCmt;

    private Intent intent;
    private Model model;

    private TextInputEditText editCmt;
    private ImageButton btnSend;
    private RecyclerView recyclerView;
    private ArrayList<Commentaire> commentaireArrayList;
    private LinearLayoutManager manager;
    private CommentaireAdapter adapter;

    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser()==null){
            startActivity(new Intent(CommentaireActivity.this,LoginActivity.class));
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefManager=new PrefManager(getApplicationContext());

        if (prefManager.getDarkMode()){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES); //For night mode theme
        }else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); //For night mode theme
        }
        setContentView(R.layout.activity_commentaire);

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

        mAuth=FirebaseAuth.getInstance();
        user=mAuth.getCurrentUser();

        database=FirebaseDatabase.getInstance();
        reference=database.getReference().child("confinement").child("posts").child(model.getPost_id()).child("commentaires");
        refCmt = database.getReference().child("confinement/posts").child(model.getPost_id()).child("commentaires");

        editCmt=findViewById(R.id.editCommentaire);
        btnSend=findViewById(R.id.btnSend);

        recyclerView=findViewById(R.id.recycleComment);
        commentaireArrayList=new ArrayList<>();
        manager=new LinearLayoutManager(CommentaireActivity.this);
        manager.setReverseLayout(true);
        manager.setStackFromEnd(true);

        btnSend.setOnClickListener(v->sendComment());
        getData();
    }

    private void getData(){
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                commentaireArrayList.clear();

                for (DataSnapshot data : dataSnapshot.getChildren()){
                    Commentaire commentaire=new Commentaire();
                    commentaire.setPseudo(String.valueOf(data.child("pseudo").getValue()));
                    commentaire.setCommentaire(String.valueOf(data.child("comment").getValue()));
                    commentaire.setCreateAt(String.valueOf(data.child("createAt").getValue()));
                    commentaire.setHeure(String.valueOf(data.child("heure").getValue()));

                    commentaireArrayList.add(commentaire);
                }
                adapter=new CommentaireAdapter(CommentaireActivity.this,commentaireArrayList);
                recyclerView.setLayoutManager(manager);
                recyclerView.setItemAnimator(new DefaultItemAnimator());
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendComment(){

        String commentaireValue=editCmt.getText().toString().trim();

        if (TextUtils.isEmpty(commentaireValue)){
            editCmt.setError("entrez un commentaire");
            editCmt.requestFocus();
            return;
        }
        Map<String, Object> data = new HashMap<>();
        data.put("pseudo",user.getDisplayName());
        data.put("comment",commentaireValue);
        data.put("heure",new Date().getTime());
        data.put("createAt", ServerValue.TIMESTAMP);

        refCmt.child("comment"+System.currentTimeMillis()).setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(CommentaireActivity.this, "Commentaire envoyé", Toast.LENGTH_SHORT).show();
                    editCmt.setText("");
                }
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
}
