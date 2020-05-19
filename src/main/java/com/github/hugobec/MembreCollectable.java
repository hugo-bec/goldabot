package com.github.hugobec;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.entity.channel.Channel;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MembreCollectable {

    private User membre;
    private boolean isEX;
    private double tauxDrop;
    private List<MembreCollectable> inventaire;
    private boolean peutCapturer;

    public MembreCollectable(User membre, boolean isEX) {
        this.membre = membre;
        this.isEX = isEX;
        this.tauxDrop = Math.random();
        this.inventaire = new ArrayList<>();
        this.peutCapturer = true;
    }

    public MembreCollectable(MembreCollectable m) {
        this.membre = m.membre;
        this.isEX = m.isEX;
        this.tauxDrop = m.tauxDrop;
        this.inventaire = m.inventaire;
    }

    public boolean isEX() {
        return isEX;
    }

    public String getNom(boolean nomOriginal, Server serveur){
        String sreturn = "";
        if (nomOriginal)
            { sreturn += this.membre.getName(); }
        else
            { sreturn += this.membre.getNickname(serveur).get(); }
        if (this.isEX) { sreturn += " EX"; }
        return sreturn;
    }

    public String getId(){
        return this.membre.getIdAsString();
    }
    public double getTauxDrop() {
        return tauxDrop;
    }
    public List<MembreCollectable> getInventaire() {
        return inventaire;
    }
    public URL getAvatarUrl(){
        return this.membre.getAvatar().getUrl();
    }
    public String getMentionTag(){
        return this.membre.getMentionTag();
    }

    public void setTauxDrop(double tauxDrop) {
        this.tauxDrop = tauxDrop;
    }
    public void setEX(boolean EX) {
        isEX = EX;
    }

    public boolean peutCapturer() { return peutCapturer; }
    public void setPeutCapturer(boolean peutCapturer) { this.peutCapturer = peutCapturer; }

    public void ajouterInventaire(MembreCollectable m){
        this.inventaire.add(m);
    }


    public String getInventaireToString(boolean nomOriginaux, Server serveur){
        String stringInventaire = ">>> ";
        int i = 0;
        if (this.getInventaire().isEmpty()){
            stringInventaire += "Votre inventaire est vide " + this.membre.getMentionTag() + ".";
        } else {
            stringInventaire += this.membre.getMentionTag() + " voici votre inventaire :";
            for (MembreCollectable m: this.inventaire) {
                stringInventaire += "\n" + i + " | " + m.getNom(nomOriginaux, serveur).replace("EX", "**EX**");;
                i++;
            }
            //stringInventaire.replace("EX", "**EX**");
            System.out.println(stringInventaire);
            //System.out.println("test EX salut a a a EX".replace("EX", "**EX**"));
        }
        return stringInventaire;
    }
}
