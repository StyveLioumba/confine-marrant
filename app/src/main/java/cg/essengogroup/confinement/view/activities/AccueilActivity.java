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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import cg.essengogroup.confinement.R;
import cg.essengogroup.confinement.controller.adapters.MultiViewTypeAdapter;
import cg.essengogroup.confinement.controller.utils.PrefManager;
import cg.essengogroup.confinement.model.Model;

import static cg.essengogroup.confinement.controller.utils.Methodes.shareApp;

public class AccueilActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ArrayList<Model> modelArrayList;
    private MultiViewTypeAdapter adapter;

    private FloatingActionButton fab;

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseDatabase database;
    private Query reference;

    private PrefManager prefManager;

    private long backPressedTime;
    private Toast backToast;

    private LinearLayoutManager manager;

    @Override
    protected void onStart() {
        super.onStart();
        if(FirebaseAuth.getInstance().getCurrentUser()==null){
            startActivity(new Intent(AccueilActivity.this,LoginActivity.class));
            finish();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefManager=new PrefManager(AccueilActivity.this);

        if (prefManager.getDarkMode()){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES); //For night mode theme
        }else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); //For night mode theme
        }

        setContentView(R.layout.activity_accueil);

        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar=getSupportActionBar();


        mAuth=FirebaseAuth.getInstance();
        user=mAuth.getCurrentUser();

        database=FirebaseDatabase.getInstance();
        reference=database.getReference().child("confinement").child("posts").limitToLast(50);

        recyclerView=findViewById(R.id.recycle);
        manager=new LinearLayoutManager(AccueilActivity.this);
        manager.setReverseLayout(true);
        manager.setStackFromEnd(true);

        modelArrayList=new ArrayList<>();

        fab=findViewById(R.id.fab);
        fab.setOnClickListener(v->startActivity(new Intent(AccueilActivity.this,PostActivity.class)));

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 && fab.getVisibility() == View.VISIBLE) {
                    fab.hide();
                } else if (dy < 0 && fab.getVisibility() != View.VISIBLE) {
                    fab.show();
                }
            }
        });

        getData();

    }

    private void getData(){

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                modelArrayList.clear();

                for (DataSnapshot data : dataSnapshot.getChildren()){
                    Model model=new Model() ;
                    model.setUser_id(""+data.child("user_id").getValue());
                    model.setImage(""+data.child("imagePost").getValue());
                    model.setVideo(""+data.child("videoPost").getValue());
                    model.setDescription(""+data.child("description").getValue());
                    model.setType(Integer.parseInt(""+data.child("type").getValue()));
                    model.setCreateAt(String.valueOf(data.child("createAt").getValue()));
                    model.setNomUser(String.valueOf(data.child("pseudo").getValue()));
                    model.setPost_id(data.getKey());
                    modelArrayList.add(model);
                }

                adapter=new MultiViewTypeAdapter(modelArrayList,AccueilActivity.this);
                recyclerView.setLayoutManager(manager);
                recyclerView.setItemAnimator(new DefaultItemAnimator());
                recyclerView.setAdapter(adapter);

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu,menu);

        MenuItem menuItem = menu.findItem(R.id.dark);
        if (prefManager.getDarkMode()){
            menuItem.setTitle("Mode jour");
        }else {
            menuItem.setTitle("Mode nuit");
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.invite:
                shareApp(AccueilActivity.this);
                return true;
            case R.id.about:
                startActivity(new Intent(AccueilActivity.this,AboutActivity.class));
                return true;
            case R.id.dark:
                if (prefManager.getDarkMode()){
                    prefManager.setDarkMode(false);
                    startActivity(new Intent(AccueilActivity.this,AccueilActivity.class));
                    finish();
                }else {
                    prefManager.setDarkMode(true);
                    startActivity(new Intent(AccueilActivity.this,AccueilActivity.class));
                    finish();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {

        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            backToast.cancel();
            super.onBackPressed();
            return;
        } else {
            backToast = Toast.makeText(getBaseContext(), "Cliquez encore pour quitter", Toast.LENGTH_SHORT);
            backToast.show();
        }

        backPressedTime = System.currentTimeMillis();

    }
}
