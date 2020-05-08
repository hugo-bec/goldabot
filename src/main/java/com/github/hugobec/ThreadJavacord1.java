package com.github.hugobec;
import org.javacord.api.*;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.Event;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.entity.user.User;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.RoundingMode;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Time;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

/**A Faire :
 *
 * Système d'échange
 *
 * Faire une commande help pour admin et non admin
 * commande pour voir l'état des configs
 *
 * fonction resettant les demandes (echange + resetmemoire)
 *
 * commande pour reset la mémoire (?)
 *
 *
 *
 * */

public class ThreadJavacord1 extends Thread {

    private MessageCreateEvent event;
    private RandomAccessFile fichierSave;
    private String nomFichierSave;
    private int tempsMin, tempsMax;
    private double tauxEx;
    private boolean nomOriginaux;
    private int niveauActivite;
    private String prefix;

    private boolean demandeStop;
    private boolean demandeReset;

    private List<MembreCollectable> listMembre;
    private MembreCollectable actualGuess;

    private int nbMessage;
    private int nbTentatives;


    public ThreadJavacord1(MessageCreateEvent event, String prefix, int niveauActivite, int tempsMin, int tempsMax) {
        this.event = event;
        this.tempsMin = tempsMin;
        this.tempsMax = tempsMax;
        this.niveauActivite = niveauActivite;
        this.tauxEx = 0.1;
        this.nomOriginaux = true;
        this.nomFichierSave = "..\\fichiers_goldabot\\saves\\save_" + this.getServeur().getName()+ "_" + this.getServeur().getId();
        this.prefix = prefix;

        this.demandeStop = false;
        this.demandeReset = false;
        this.listMembre = new ArrayList<>();
        initListeMembre();

        try {
            File f = new File(nomFichierSave);
            if(f.exists() && !f.isDirectory())      //Le fichier de sauvegarde existe déjà
            {
                this.fichierSave = new RandomAccessFile(nomFichierSave, "rw");
                lireTauxDrop();
                for (MembreCollectable m: this.listMembre) {
                    lireInventaires(m);
                }
                this.fichierSave.close();
            }
            else {    //Le fichier de sauvegarde n'existe pas encore
                event.getChannel().sendMessage(" thread: Creation du fichier de save");
                this.fichierSave = new RandomAccessFile(nomFichierSave, "rw");
                creerFichierSave();
                this.fichierSave.close();
            }
        } catch (IOException ioe) {
            event.getChannel().sendMessage("thread: Erreur lors de l'ouverture / lecture / ecriture du fichier de sauvegarde.");
            ioe.getMessage();
        }
        event.getChannel().sendMessage("thread: thread configure");
    }


    public void run(){
        event.getChannel().sendMessage("thread: thread start");

        while(true){
            try {
                boolean activity = false;
                Thread.sleep(randInt(tempsMin, tempsMax) * 100); //*60000
                if (this.demandeStop) {
                    this.event.getChannel().sendMessage("thread stoppe");
                    break;
                }

                if (this.nbMessage >= this.niveauActivite) { // && this.actualGuess == null) {
                    if (this.actualGuess != null){
                        this.event.getChannel().sendMessage("Le membre s'est enfui !");
                    }
                    this.actualGuess = dropMembreCollectable();
                    this.nbTentatives = randInt(1, 4);
                    String messageSpawn = ">>> Un membre ";
                    if (this.actualGuess.isEX()) { messageSpawn += "**EX** "; }
                    messageSpawn += "vient d'apparaitre !" + "\n"
                        //+ this.actualGuess.getName() + "\n"
                        + "drop: " + this.actualGuess.getTauxDrop() + "\n"
                        + "nbTentatives: " + this.nbTentatives + "\n";

                    try {
                        if (this.actualGuess.isEX()) {
                            this.event.getChannel().sendMessage(messageSpawn, getImageInverse(this.actualGuess));
                        } else {
                            this.event.getChannel().sendMessage(messageSpawn, getImageOriginale(this.actualGuess));
                        }
                    } catch (Exception e){
                        e.getMessage();
                    }
                }
                this.nbMessage = 0;

            } catch (InterruptedException ie) {
                ie.getMessage();
            }
        }
    }

    public Server getServeur(){
        return this.event.getServer().get();
    }

    private MembreCollectable getRandomMembre(){
        int nbrMembre = listMembre.size();
        return listMembre.get(randInt(0, nbrMembre-1));
    }

    private File getImageInverse(MembreCollectable m) throws Exception{
        File filepdpi = new File("..\\fichiers_goldabot\\pdpinverse1.png");
        URL input = m.getAvatarUrl();
        BufferedImage image = ImageIO.read(input);
        int rgb, alpha, red, green, blue, rgbi;

        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                rgb = image.getRGB(x, y);
                alpha = (rgb >> 24) & 0xFF;
                red =   (rgb >> 16) & 0xFF;
                green = (rgb >>  8) & 0xFF;
                blue =  (rgb      ) & 0xFF;

                rgbi = (255-blue) + (255-green)*256 + (255-red)*256*256 + alpha*256*256*256;
                image.setRGB(x, y, rgbi);
            }
        }

        ImageIO.write(image, "png", filepdpi);
        //this.event.getChannel().sendMessage(filepdpi);

        return filepdpi;
    }

    private File getImageOriginale(MembreCollectable m) throws Exception{
        File filepdp = new File("..\\fichiers_goldabot\\pdporiginale1.png");
        URL input = m.getAvatarUrl();
        BufferedImage image = ImageIO.read(input);
        ImageIO.write(image, "png", filepdp);

        return filepdp;
    }

    private MembreCollectable dropMembreCollectable(){
        if (Math.random() < this.tauxEx) {
            MembreCollectable m = new MembreCollectable(getRandomMembre());
            m.setEX(true);
            return m;
        }
        return getRandomMembre();
    }



// SETTER

    public void setNiveauActivite(int niveauActivite) {
        this.niveauActivite = niveauActivite;
    }

    public void setDemandeStop(boolean b){
        this.demandeStop = b;
    }


// EVENT

    public void gestionEvent(MessageCreateEvent eventReq) {
        String[] tabRequete = eventReq.getMessageContent().split(" ");

        if (tabRequete[0].equalsIgnoreCase(this.prefix+"capture")) {
            eventCapture(eventReq);
        }
        else if (tabRequete[0].equalsIgnoreCase(this.prefix+"inventaire")) {
            eventInventaire(eventReq);
        }
        else if (tabRequete[0].equalsIgnoreCase(this.prefix+"sauvegarder")) {
            eventSauvegarder(eventReq);
            eventReq.getChannel().sendMessage("Sauvegarde faite !");
        }
        else if (tabRequete[0].equalsIgnoreCase(this.prefix+"changer")) {
            if (eventReq.getMessageAuthor().isServerAdmin()) {
                if (tabRequete.length >= 3) {
                    if (tabRequete[1].equalsIgnoreCase("tauxex")) {
                        try {
                            double taux = Double.parseDouble(tabRequete[2]);
                            if (taux > 0 && taux < 1) {
                                this.tauxEx = taux;
                                eventReq.getChannel().sendMessage("Taux d'apparition Ex change à " + taux + " !");
                            } else {
                                eventReq.getChannel().sendMessage("Erreur: Le taux doit être compris entre 0 et 1.");
                            }
                        } catch (NumberFormatException nfe) {
                            nfe.getMessage();
                            eventReq.getChannel().sendMessage(
                                    "Erreur: Veuillez spécifier un nombre.");
                        }

                    } else if (tabRequete[1].equalsIgnoreCase("messageactif")) {
                        try {
                            int nbMessageActif = Integer.parseInt(tabRequete[2]);
                            setNiveauActivite(nbMessageActif);
                            eventReq.getChannel().sendMessage(
                                    "Nombre de message minimum pour considérer une activite change à " + nbMessageActif + " !");
                        } catch (NumberFormatException nfe) {
                            nfe.getMessage();
                            eventReq.getChannel().sendMessage(
                                    "Erreur: Veuillez spécifier un entier.");
                        }

                    } else if (tabRequete[1].equalsIgnoreCase("nomoriginal")) {
                        if (tabRequete[2].equalsIgnoreCase("vrai")) {
                            this.nomOriginaux = true;
                            eventReq.getChannel().sendMessage("Le bot est maintenant en mode \"pseudo original\".");
                        } else if (tabRequete[2].equalsIgnoreCase("faux")) {
                            this.nomOriginaux = false;
                            eventReq.getChannel().sendMessage("Le bot est maintenant en mode \"pseudo du serveur\".");
                        } else {
                            eventReq.getChannel().sendMessage("Erreur: Veuillez choisir entre \'vrai\' ou \'faux\'.");
                        }
                    }

                    else if (tabRequete[1].equalsIgnoreCase("intervalle")) {
                        if (tabRequete.length >= 4) {
                            try {
                                int ptempsMin = Integer.parseInt(tabRequete[2]);
                                int ptempsMax = Integer.parseInt(tabRequete[3]);
                                if (ptempsMin <= ptempsMax) {
                                    this.tempsMin = ptempsMin;
                                    this.tempsMax = ptempsMax;
                                    eventReq.getChannel().sendMessage(
                                            "Intervalle entre les apparitions change " + this.tempsMin + " et " + this.tempsMax + " !");
                                } else {
                                    eventReq.getChannel().sendMessage("Erreur: tempsMin doit être inferieur à tempsMax.");
                                }
                            } catch (NumberFormatException nfe) {
                                nfe.getMessage();
                                eventReq.getChannel().sendMessage(
                                        "Erreur: Veuillez spécifier deux entiers.");
                            }

                        } else { eventReq.getChannel().sendMessage("Erreur: Vous devez spécifier deux entiers."); }
                    }

                    else { eventReq.getChannel().sendMessage("Erreur: \"" + tabRequete[1] + "\" inconnu."); }

                } else { eventReq.getChannel().sendMessage("Veuillez donner le nom du paramètre et la/les variable(s) associée(s)."); }

            } else { eventReq.getChannel().sendMessage("Vous devez être administrateur pour changer les paramètres."); }
        }

        else if (tabRequete[0].equalsIgnoreCase(this.prefix+"resetmemoire")){
            if (eventReq.getMessageAuthor().isServerAdmin()) {
                eventResetMemoire(eventReq);
                System.out.println("resetmemoire event");
            } else { eventReq.getChannel().sendMessage("Vous devez être administrateur pour changer les paramètres."); }
        }

        else if (tabRequete[0].equalsIgnoreCase(this.prefix+"echanger")){

        }



        this.nbMessage++;
    }

    private void eventCapture(MessageCreateEvent eventReq){
        if (this.actualGuess != null) {
            String[] tabRequete = eventReq.getMessageContent().split(" ");
            if (tabRequete.length > 1) {
                String contenuReq = eventReq.getMessageContent().substring("capture ".length());
                String nomCorrect;
                if (this.nomOriginaux) {
                    nomCorrect = this.actualGuess.getNomOriginal();
                } else {
                    nomCorrect = this.actualGuess.getNomServeur(eventReq.getServer().get());
                }

                //System.out.println(this.actualGuess.getName());
                //System.out.println(contenuReq);

                if (contenuReq.equalsIgnoreCase(nomCorrect)) {
                    if (Math.random() < this.actualGuess.getTauxDrop()) {
                        eventReq.getChannel().sendMessage("Utilisateur capture !");

                        getMembreById(eventReq.getMessageAuthor().getIdAsString()).ajouterInventaire(this.actualGuess);
                    } else {
                        this.event.getChannel().sendMessage("Vous n'avais pas reussi a capturer l'utilisateur !");
                        this.nbTentatives--;
                        if (this.nbTentatives <= 0) {
                            this.actualGuess = null;
                            this.event.getChannel().sendMessage("L'utilisateur s'est enfui !");
                        }
                    }

                    //System.out.println(this.inventaires);
                    //this.actualGuess = null;
                } else {
                    eventReq.getChannel().sendMessage("Incorrect, ce n'est pas son nom !");
                    this.nbTentatives--;
                    if (this.nbTentatives <= 0) {
                        this.actualGuess = null;
                        this.event.getChannel().sendMessage("L'utilisateur s'est enfui !");
                    }
                }
            } else {
                eventReq.getChannel().sendMessage(
                        "Veuillez donner le nom de l'utilisateur");
            }
        } else {
            eventReq.getChannel().sendMessage(
                    "L'utilisateur a deja ete capture ! Attendez qu'un nouvel utilisateur apparaisse.");
        }
    }


    private void eventInventaire(MessageCreateEvent eventReq){
        MembreCollectable mAuteur = getMembreById(eventReq.getMessageAuthor().getIdAsString());

        eventReq.getChannel().sendMessage(mAuteur.getInventaireToString(this.nomOriginaux, eventReq.getServer().get()));
    }


    private void eventSauvegarder(MessageCreateEvent eventReq){
        try {
            creerFichierSave();
        } catch (IOException ioe) {
            eventReq.getChannel().sendMessage("thread: Erreur lors de la creation du fichier de sauvegarde.");
            ioe.getMessage();
        }
    }


    private void eventResetMemoire(MessageCreateEvent eventReq){
        String[] tabRequete = eventReq.getMessageContent().split(" ");
        if (!demandeReset) {
            this.demandeReset = true;
            eventReq.getChannel().sendMessage(">>> Etes-vous sûr(e) de vouloir effacer la mémoire ?????\n"
                    + "En effaçant la mémoire tout le monde perdra son inventaire et les taux de drop des membres se réinitialiseront.\n"
                    + "Pour annuler la demande de reset memoire faites \'"+this.prefix+"resetmemoire annuler\',\n"
                    + "Pour confirmer la demande de reset memoire faites \'"+this.prefix+"resetmemoire confirmer\'.\n"
                    + "Si vous ne faites rien la demande sera automatiquement annulé après un petit moment.");
        } else {
            if (tabRequete.length == 2) {
                if (tabRequete[1].equalsIgnoreCase("annuler")) {
                    this.demandeReset = false;
                    eventReq.getChannel().sendMessage("La demande de reset mémoire a été annuler.");
                } else if (tabRequete[1].equalsIgnoreCase("confirmer")) {
                    initListeMembre();
                    eventSauvegarder(eventReq);
                    this.demandeReset = false;
                    eventReq.getChannel().sendMessage("Mémoire réinitialisé !");
                } else {
                    eventReq.getChannel().sendMessage("Erreur: Veuillez spécifier \'confirmer\' ou \'annuler\'.");
                }
            } else {
                eventReq.getChannel().sendMessage("Erreur: Veuillez spécifier \'confirmer\' ou \'annuler\'.");
            }
        }
    }


// INITIALISATION

    private void initListeMembre(){
        this.listMembre = new ArrayList<>();
        for (User u: this.event.getServer().get().getMembers()) {
            this.listMembre.add(new MembreCollectable(u, false));
        }
    }



// SAUVEGARDE / CHARGEMENT

    private void ecrireTauxDrop() throws IOException {
        for (MembreCollectable m: this.listMembre){
            //this.fichierSave.write((entry.getKey().getIdAsString() + ":" + entry.getValue() + " ").getBytes());
            this.fichierSave.write((m.getId() + ":" + m.getTauxDrop() + " ").getBytes());
        }
    }

    private void ecrireInventaire(MembreCollectable m0) throws IOException {
        this.fichierSave.write(("\n" + m0.getId()).getBytes());
        for (MembreCollectable m: m0.getInventaire()) {
            if (m.isEX()) {
                this.fichierSave.write((" +" + m.getId()).getBytes());
            } else {
                this.fichierSave.write((" " + m.getId()).getBytes());
            }
        }
    }


    private void lireTauxDrop() throws IOException {
        this.fichierSave.seek(0);
        String ligned = this.fichierSave.readLine();
        if (ligned != null) {
            String[] tabd = ligned.split(" ");

            for (int i=0; i<tabd.length; i++) {
                String[] drop = tabd[i].split(":");
                getMembreById(drop[0]).setTauxDrop(Double.parseDouble(drop[1]));
            }
        }
    }


    private void lireInventaires(MembreCollectable m) throws IOException {
        MembreCollectable minv = null;
        this.fichierSave.seek(0);
        this.fichierSave.readLine();    //on saute la première ligne de drop
        String ligne = this.fichierSave.readLine();
        while (ligne != null) {
            String[] tabs = ligne.split(" ");
            if (tabs[0].equalsIgnoreCase(m.getId())) {
                if (tabs.length > 1) {
                    for (int i=1; i<tabs.length; i++) {
                        if (tabs[i].charAt(0) == '+') {
                            MembreCollectable minv0 = getMembreById(tabs[i].substring("+".length()));
                            if (minv0 != null) {
                                minv = new MembreCollectable(minv0);
                                minv.setEX(true);
                            }
                        } else {
                            minv = getMembreById(tabs[i]);
                        }

                        if (minv != null) { m.ajouterInventaire(minv); }
                    }
                }
            }
            ligne = this.fichierSave.readLine();
        }
    }


    private void creerFichierSave() throws IOException {
        deleteFichierSave();

        this.fichierSave = new RandomAccessFile(nomFichierSave, "rw");
        this.ecrireTauxDrop();
        for (MembreCollectable m: this.listMembre) {
            ecrireInventaire(m);
        }
        this.fichierSave.close();
    }

    public void sauvegarder() throws IOException {
        creerFichierSave();
    }

    private void deleteFichierSave(){
        File f = new File(nomFichierSave);
        f.delete();
    }



// OUTILS

    private MembreCollectable getMembreById(String id){
        for (MembreCollectable m: this.listMembre) {
            if (m.getId().equalsIgnoreCase(id)){
                return m;
            }
        }
        return null;
    }

    private int randInt(int min, int max) {
        Random rand = new Random();
        int randomNum = rand.nextInt((max - min) + 1) + min;
        return randomNum;
    }

}
