package cg.essengogroup.confinement.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Model implements Parcelable {

    private int type=1;
    private String image,video,description,commentaire,likes,user_id,createAt,post_id,nomUser;

    public Model() {
    }

    public Model(int type, String image, String video, String description, String commentaire, String likes, String user_id, String createAt, String post_id, String nomUser) {
        this.type = type;
        this.image = image;
        this.video = video;
        this.description = description;
        this.commentaire = commentaire;
        this.likes = likes;
        this.user_id = user_id;
        this.createAt = createAt;
        this.post_id = post_id;
        this.nomUser = nomUser;
    }

    protected Model(Parcel in) {
        type = in.readInt();
        image = in.readString();
        video = in.readString();
        description = in.readString();
        commentaire = in.readString();
        likes = in.readString();
        user_id = in.readString();
        createAt = in.readString();
        post_id = in.readString();
        nomUser = in.readString();
    }

    public static final Creator<Model> CREATOR = new Creator<Model>() {
        @Override
        public Model createFromParcel(Parcel in) {
            return new Model(in);
        }

        @Override
        public Model[] newArray(int size) {
            return new Model[size];
        }
    };

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getVideo() {
        return video;
    }

    public void setVideo(String audio) {
        this.video = audio;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public String getLikes() {
        return likes;
    }

    public void setLikes(String likes) {
        this.likes = likes;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getCreateAt() {
        return createAt;
    }

    public void setCreateAt(String createAt) {
        this.createAt = createAt;
    }

    public String getPost_id() {
        return post_id;
    }

    public void setPost_id(String post_id) {
        this.post_id = post_id;
    }

    public String getNomUser() {
        return nomUser;
    }

    public void setNomUser(String nomUser) {
        this.nomUser = nomUser;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(type);
        dest.writeString(image);
        dest.writeString(video);
        dest.writeString(description);
        dest.writeString(commentaire);
        dest.writeString(likes);
        dest.writeString(user_id);
        dest.writeString(createAt);
        dest.writeString(post_id);
        dest.writeString(nomUser);
    }
}
