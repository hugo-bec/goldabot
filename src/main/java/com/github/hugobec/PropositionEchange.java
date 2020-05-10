package com.github.hugobec;

import java.util.List;

public class PropositionEchange {

    private MembreCollectable membre;
    private int id;
    private boolean confirm;

    public PropositionEchange(MembreCollectable membre, int id) {
        this.membre = membre;
        this.id = id;
        this.confirm = false;
    }


    public void confirmer() {
        this.confirm = true;
    }
    public boolean estConfirme() {
        return confirm;
    }


    public int getId() {
        return id;
    }

    public MembreCollectable getMembreAEchanger() {
        return this.membre.getInventaire().get(this.id);
    }
    public MembreCollectable getMembreProposition(){
        return this.membre;
    }

    public void changerMembre(MembreCollectable m){
        this.membre.getInventaire().set(this.id, m);
    }

}
