package cg.essengogroup.confinement.controller.adapters;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import cg.essengogroup.confinement.R;
import cg.essengogroup.confinement.controller.utils.Methodes;
import cg.essengogroup.confinement.model.Model;
import cg.essengogroup.confinement.view.activities.CommentaireActivity;
import cg.essengogroup.confinement.view.activities.DetailImageActivity;
import cg.essengogroup.confinement.view.activities.DetailVideoActivity;

import static cg.essengogroup.confinement.controller.utils.Constantes.*;

public class MultiViewTypeAdapter extends RecyclerView.Adapter {

    private ArrayList<Model> dataModel;

    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private FirebaseDatabase database;
    private DatabaseReference reference,referenceUser;

    private Activity activity;

    public MultiViewTypeAdapter(ArrayList<Model>data,Activity activity) {
        this.dataModel = data;
        this.activity=activity;
        mAuth=FirebaseAuth.getInstance();
        firebaseUser=mAuth.getCurrentUser();
        database=FirebaseDatabase.getInstance();
        reference = database.getReference().child("confinement/posts");
        referenceUser=database.getReference().child("confinement/users");
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case IMAGE_TYPE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
                return new ImageTypeViewHolder(view);
            case VIDEO_TYPE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video, parent, false);
                return new VideoTypeViewHolder(view);
        }
        return null;
    }

    @Override
    public int getItemViewType(int position) {

        switch (dataModel.get(position).getType()) {
            case 1:
                return IMAGE_TYPE;
            case 2:
                return VIDEO_TYPE;
            default:
                return -1;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int listPosition) {

        Model model = dataModel.get(listPosition);
        if (model != null) {
            switch (model.getType()) {
                case IMAGE_TYPE:
                    ((ImageTypeViewHolder) holder).txtDesignation.setText(model.getDescription());
                    ((ImageTypeViewHolder) holder).txtDate.setText(Methodes.getDate(Long.parseLong(model.getCreateAt()),"dd-MMMM-yyyy"));
                    Glide.with(activity.getApplicationContext())
                            .load(model.getImage())
                            .placeholder( R.drawable.default_user)
                            .centerCrop()
                            .into(((ImageTypeViewHolder) holder).imagePoster);

                    /**
                     * l'image back pour me permettre d'envoyer l'image en entier
                     */
                    Glide.with(activity.getApplicationContext())
                            .load(model.getImage())
                            .placeholder( R.drawable.default_user)
                            .into(((ImageTypeViewHolder) holder).imageBack);

                    referenceUser.child(model.getNomUser()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            if (dataSnapshot!=null){
                                Glide.with(activity.getApplicationContext())
                                        .load(String.valueOf(dataSnapshot.child("image").getValue()))
                                        .placeholder( R.drawable.default_user)
                                        .circleCrop()
                                        .into(((ImageTypeViewHolder) holder).imageUser);

                                ((ImageTypeViewHolder) holder).nomUser.setText(String.valueOf(dataSnapshot.child("pseudo").getValue()));
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    ((ImageTypeViewHolder) holder).partager.setOnClickListener(v->shareContent( ((ImageTypeViewHolder) holder).imageBack));

                    reference.child(model.getPost_id())
                            .child("likes")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    // get total available quest
                                    ((ImageTypeViewHolder) holder).txtLikes.setText(""+dataSnapshot.getChildrenCount());
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                    reference.child(model.getPost_id())
                            .child("likes").child(firebaseUser.getDisplayName())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        ((ImageTypeViewHolder) holder).imageLike.setImageResource(R.drawable.ic_favorite_black_24dp);
                                    }else {
                                        ((ImageTypeViewHolder) holder).imageLike.setImageResource(R.drawable.ic_favorite_border_black_24dp);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                    ((ImageTypeViewHolder) holder).imageLike.setOnClickListener(v->{

                        reference.child(model.getPost_id())
                                .child("likes")
                                .child(firebaseUser.getDisplayName())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            dislikePost(model.getPost_id());
                                            ((ImageTypeViewHolder) holder).imageLike.setImageResource(R.drawable.ic_favorite_black_24dp);
                                        }else {
                                            likePost(model.getPost_id());
                                            ((ImageTypeViewHolder) holder).imageLike.setImageResource(R.drawable.ic_favorite_border_black_24dp);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                    });

                    /**
                     * recuperer le nombre de telechargement
                     */
                    reference.child(model.getPost_id()+"/telecharger").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot!=null){
                                ((ImageTypeViewHolder) holder).txtDownload.setText(String.valueOf(dataSnapshot.getChildrenCount()));
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    ((ImageTypeViewHolder) holder).cardView.setOnClickListener(v->{
                        activity.startActivity(new Intent(activity, DetailImageActivity.class).putExtra("model",model));
                    });

                    /**
                     * recuperer le nombre de commentaire
                     */

                    reference.child(model.getPost_id()).child("commentaires").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot!=null){
                                ((ImageTypeViewHolder) holder).txtCmt.setText(String.valueOf(dataSnapshot.getChildrenCount()));
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    ((ImageTypeViewHolder) holder).cardView.setOnClickListener(v->{
                        activity.startActivity(new Intent(activity, DetailImageActivity.class).putExtra("model",model));
                    });

                    ((ImageTypeViewHolder) holder).imageCmt.setOnClickListener(v->{
                        activity.startActivity(new Intent(activity, CommentaireActivity.class).putExtra("model",model));
                    });



                    break;
                case VIDEO_TYPE:
                    ((VideoTypeViewHolder) holder).txtDesignation.setText(model.getDescription());
                    ((VideoTypeViewHolder) holder).txtDate.setText(Methodes.getDate(Long.parseLong(model.getCreateAt()),"dd-MMMM-yyyy"));

                    Uri video = Uri.parse(model.getVideo());
                    ((VideoTypeViewHolder) holder).videoPoster.setVideoURI(video);
                    ((VideoTypeViewHolder) holder).videoPoster.setOnPreparedListener(PreparedListener);

                    ((VideoTypeViewHolder) holder).partager.setOnClickListener(v->shareVideo(model));

                    ((VideoTypeViewHolder) holder).cardView.setOnClickListener(v-> activity.startActivity(new Intent(activity, DetailVideoActivity.class).putExtra("model",model)));

                    ((VideoTypeViewHolder) holder).imageCmt.setOnClickListener(v-> activity.startActivity(new Intent(activity, CommentaireActivity.class).putExtra("model",model)));



                    /**
                     * recuperer l'identité du posteur.
                     */

                    referenceUser.child(model.getNomUser()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            if (dataSnapshot!=null){
                                Glide.with(activity.getApplicationContext())
                                        .load(String.valueOf(dataSnapshot.child("image").getValue()))
                                        .placeholder( R.drawable.default_user)
                                        .circleCrop()
                                        .into(((VideoTypeViewHolder) holder).imageUser);

                                ((VideoTypeViewHolder) holder).nomUser.setText(String.valueOf(dataSnapshot.child("pseudo").getValue()));
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    /**
                     * systeme de likes.
                     * recuperer le nombre de likes
                     */
                    reference.child(model.getPost_id())
                            .child("likes")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    // get total available quest
                                    ((VideoTypeViewHolder) holder).txtLikes.setText(""+dataSnapshot.getChildrenCount());
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                    /**
                     * systeme de likes.
                     * verifier si le post est liker
                     */
                    reference.child(model.getPost_id())
                            .child("likes").child(firebaseUser.getDisplayName())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        ((VideoTypeViewHolder) holder).imageLike.setImageResource(R.drawable.ic_favorite_black_24dp);
                                    }else {
                                        ((VideoTypeViewHolder) holder).imageLike.setImageResource(R.drawable.ic_favorite_border_black_24dp);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                    ((VideoTypeViewHolder) holder).imageLike.setOnClickListener(v->{
                        /**
                         * systeme de likes.
                         * ajouter l'utilisateur qui like ou supprimer
                         */
                        reference.child(model.getPost_id())
                                .child("likes")
                                .child(firebaseUser.getDisplayName())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            dislikePost(model.getPost_id());
                                            ((VideoTypeViewHolder) holder).imageLike.setImageResource(R.drawable.ic_favorite_black_24dp);
                                        }else {
                                            likePost(model.getPost_id());
                                            ((VideoTypeViewHolder) holder).imageLike.setImageResource(R.drawable.ic_favorite_border_black_24dp);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                    });

                    /**
                     * recuperer le nombre de telechargement
                     */
                    reference.child(model.getPost_id()+"/telecharger").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot!=null){
                                ((VideoTypeViewHolder) holder).txtDownload.setText(String.valueOf(dataSnapshot.getChildrenCount()));
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    /**
                     * recuperer le nombre de commentaire
                     */

                    reference.child(model.getPost_id()).child("commentaires").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot!=null){
                                ((VideoTypeViewHolder) holder).txtCmt.setText(String.valueOf(dataSnapshot.getChildrenCount()));
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return dataModel.size();
    }


    public static class ImageTypeViewHolder extends RecyclerView.ViewHolder {

        TextView txtDesignation,txtLikes,nomUser,txtDate,txtDownload,txtCmt;
        CardView cardView;
        ImageView imageUser,imageLike,imagePoster,imageDownload,imageBack,imageCmt;
        ImageButton partager;

        public ImageTypeViewHolder(View itemView) {
            super(itemView);

            txtDesignation=itemView.findViewById(R.id.txt);
            txtLikes=itemView.findViewById(R.id.txtLikes);
            txtDate=itemView.findViewById(R.id.datePoster);
            nomUser=itemView.findViewById(R.id.pseudoUser);
            imageUser=itemView.findViewById(R.id.imgUser);
            imageLike=itemView.findViewById(R.id.imgLikes);
            imageDownload=itemView.findViewById(R.id.imgDownload);
            partager=itemView.findViewById(R.id.partager);
            txtDownload=itemView.findViewById(R.id.txtDownload);
            imageBack=itemView.findViewById(R.id.imgBack);
            imageCmt=itemView.findViewById(R.id.imgCmt);
            txtCmt=itemView.findViewById(R.id.txtCmt);

            imagePoster=itemView.findViewById(R.id.img);

            cardView=itemView.findViewById(R.id.cardImg);
        }
    }

    public static class VideoTypeViewHolder extends RecyclerView.ViewHolder {

        TextView txtDesignation,txtLikes,nomUser,txtDate,txtDownload,txtCmt;
        CardView cardView;
        VideoView videoPoster;
        ImageView imageUser,imageLike,imageDownload,imageCmt;
        ImageButton partager;

        public VideoTypeViewHolder(View itemView) {
            super(itemView);

            txtDesignation=itemView.findViewById(R.id.txt);
            txtLikes=itemView.findViewById(R.id.txtLikes);
            txtDate=itemView.findViewById(R.id.datePoster);
            nomUser=itemView.findViewById(R.id.pseudoUser);
            imageUser=itemView.findViewById(R.id.imgUser);
            imageLike=itemView.findViewById(R.id.imgLikes);
            imageDownload=itemView.findViewById(R.id.imgDownload);
            partager=itemView.findViewById(R.id.partager);
            txtDownload=itemView.findViewById(R.id.txtDownload);
            imageCmt=itemView.findViewById(R.id.imgCmt);
            txtCmt=itemView.findViewById(R.id.txtCmt);

            videoPoster=itemView.findViewById(R.id.video);

            cardView=itemView.findViewById(R.id.cardVideo);
        }
    }

    private MediaPlayer.OnPreparedListener PreparedListener = m -> {
        try {
            if (m.isPlaying()) {
                m.stop();
                m.release();
                m = new MediaPlayer();
            }
            m.setVolume(0f, 0f);
            m.setLooping(true);
            m.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    };

    private void likePost(String post_id){

        Map<String, Object> post=new HashMap<>();
        post.put("user_id",firebaseUser.getUid());
        post.put("createAt", ServerValue.TIMESTAMP);

        reference.child(post_id).child("likes").child(firebaseUser.getDisplayName()).setValue(post).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
            }
        });

    }

    private void dislikePost(String post_id){
        reference.child(post_id)
                .child("likes")
                .child(firebaseUser.getDisplayName()).removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

            }
        });
    }

    private void shareContent(ImageView imageView){

        Bitmap bitmap =getBitmapFromView(imageView);
        try {
            File file = new File(activity.getExternalCacheDir(),activity.getResources().getString(R.string.app_name)+System.currentTimeMillis()+".png");
            FileOutputStream fOut = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();
            file.setReadable(true, false);
            final Intent intent = new Intent(android.content.Intent.ACTION_SEND);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Intent.EXTRA_TEXT, activity.getResources().getString(R.string.app_name));
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
            intent.setType("image/png");
            activity.startActivity(Intent.createChooser(intent, "Share image via"));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private Bitmap getBitmapFromView(View view) {
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        Drawable bgDrawable =view.getBackground();
        if (bgDrawable!=null) {
            bgDrawable.draw(canvas);
        }   else{
            canvas.drawColor(Color.WHITE);
        }
        view.draw(canvas);
        return returnedBitmap;
    }

    private void shareVideo(Model model){
        Intent share = new Intent(android.content.Intent.ACTION_SEND);
        share.setType("text/plain");
        share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

        // Add data to the intent, the receiving app will decide
        // what to do with it.
        share.putExtra(Intent.EXTRA_SUBJECT, model.getDescription());
        share.putExtra(Intent.EXTRA_TEXT, model.getVideo());

        activity.startActivity(Intent.createChooser(share, "Share link!"));
    }
}
