package cg.essengogroup.confinement.model;

public class Commentaire {
    private String user_id,pseudo,image,commentaire,createAt,heure;

    public Commentaire() {
    }

    public Commentaire(String user_id, String pseudo, String image, String commentaire, String createAt, String heure) {
        this.user_id = user_id;
        this.pseudo = pseudo;
        this.image = image;
        this.commentaire = commentaire;
        this.createAt = createAt;
        this.heure = heure;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getPseudo() {
        return pseudo;
    }

    public void setPseudo(String pseudo) {
        this.pseudo = pseudo;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public String getCreateAt() {
        return createAt;
    }

    public void setCreateAt(String createAt) {
        this.createAt = createAt;
    }

    public String getHeure() {
        return heure;
    }

    public void setHeure(String heure) {
        this.heure = heure;
    }
}
