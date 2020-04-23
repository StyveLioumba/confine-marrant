package cg.essengogroup.confinement.controller.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.borjabravo.readmoretextview.ReadMoreTextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import cg.essengogroup.confinement.R;
import cg.essengogroup.confinement.controller.utils.Methodes;
import cg.essengogroup.confinement.model.Commentaire;

public class CommentaireAdapter extends RecyclerView.Adapter<CommentaireAdapter.MyViewHolder> {
    private Context context;
    private ArrayList<Commentaire> commentaires;

    public CommentaireAdapter(Context context, ArrayList<Commentaire> commentaires) {
        this.context = context;
        this.commentaires = commentaires;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Commentaire commentaire=commentaires.get(position);

        holder.date.setText(Methodes.getDate(Long.parseLong(commentaire.getCreateAt()),"dd-MMMM-yyyy"));
        holder.pseudo.setText(commentaire.getPseudo());
        holder.txtCommentaire.setText(commentaire.getCommentaire().toLowerCase());
        holder.heure.setText(millisecToTime(Long.parseLong(commentaire.getHeure())));
    }

    @Override
    public int getItemCount() {
        return commentaires.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        LottieAnimationView userImage;
        TextView date,heure,pseudo;
        ReadMoreTextView txtCommentaire;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            userImage=itemView.findViewById(R.id.imgUser);
            date=itemView.findViewById(R.id.txtDate);
            pseudo=itemView.findViewById(R.id.pseudoUser);
            heure=itemView.findViewById(R.id.heure);
            txtCommentaire=itemView.findViewById(R.id.txtCommentaire);
        }
    }

    private String millisecToTime(long millisec) {
        DateFormat simple = new SimpleDateFormat("HH:mm:ss");

        Date result = new Date(millisec);

        return ""+simple.format(result);
    }
}
