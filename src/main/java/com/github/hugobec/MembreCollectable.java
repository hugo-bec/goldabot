package com.github.hugobec;
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

    public MembreCollectable(User membre, boolean isEX) {
        this.membre = membre;
        this.isEX = isEX;
        this.tauxDrop = Math.random();
        this.inventaire = new ArrayList<>();
    }

    public boolean isEX() {
        return isEX;
    }

    public String getName(){
        return this.membre.getName();
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

    public void setTauxDrop(double tauxDrop) {
        this.tauxDrop = tauxDrop;
    }
    public void setEX(boolean EX) {
        isEX = EX;
    }

    public void ajouterInventaire(MembreCollectable m){
        this.inventaire.add(m);
    }


    public String getInventaireToString(){
        String stringInventaire = "";
        int i = 0;
        if (this.getInventaire().isEmpty()){
            stringInventaire += "Votre inventaire est vide " + this.membre.getMentionTag() + ".";
        } else {
            stringInventaire += this.membre.getMentionTag() + " voici votre inventaire :";
            for (MembreCollectable m: this.inventaire) {
                stringInventaire += "\n" + i + " | " + m.membre.getName();
                if (m.isEX()) {
                    stringInventaire += "EX";
                }
                i++;
            }
        }
        return stringInventaire;
    }
}
