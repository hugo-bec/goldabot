package com.github.hugobec;
import org.javacord.api.*;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.entity.user.User;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.RoundingMode;
import java.net.URL;
import java.nio.channels.Channel;
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
 * User EX méga rare:
 * - taux de drop 10 fois inferieur que son user normal
 * - image couleur inversé
 *
 * Système d'échange
 *
 * implementer chance drop
 *
 * changer paramètres :
 * - intervalle de temps
 * - nb message actif
 *
 * */

public class ThreadJavacord1 extends Thread {

    private MessageCreateEvent event;
    private RandomAccessFile fichierSave;
    private String nomFichierSave;
    private int tempsMin, tempsMax;
    private boolean vivant;
    private int niveauActivite;
    private boolean demandeStop;

    private List<MembreCollectable> listMembre;
        //private Map<User, List<User>> inventaires;
        //private Map<User, Double> tauxDrop;
    private MembreCollectable actualGuess;

    private int nbMessage;
    private int nbTentatives;

    public ThreadJavacord1(MessageCreateEvent event, int niveauActivite, int tempsMin, int tempsMax) {
        this.event = event;
        this.tempsMin = tempsMin;
        this.tempsMax = tempsMax;
        this.niveauActivite = niveauActivite;
        this.nomFichierSave = "..\\fichiers_goldabot\\saves\\save_" + this.getServeur().getName()+ "_" + this.getServeur().getId();

        this.vivant = true;
        this.demandeStop = false;
        this.listMembre = new ArrayList<>();
        //this.tauxDrop = new HashMap<>();
        //this.inventaires = new HashMap<>();
        initListeMembre();

        try {
            //System.out.println(nomFichierSave);
            File f = new File(nomFichierSave);
            if(f.exists() && !f.isDirectory()) {
                System.out.println("le fichier existe !");
                this.fichierSave = new RandomAccessFile(nomFichierSave, "rw");

                lireTauxDrop();
                for (MembreCollectable m: this.listMembre) {
                    lireInventaires(m);
                }

                this.fichierSave.close();
            } else {
                System.out.println("le fichier n'existe pas !");
                event.getChannel().sendMessage(" thread: Creation du fichier de save");
                this.fichierSave = new RandomAccessFile(nomFichierSave, "rw");

                creerFichierSave();

                this.fichierSave.close();
            }
        } catch (IOException ioe) {
            event.getChannel().sendMessage("thread: Erreur lors de l'ouverture / lecture / ecriture du fichier de sauvegarde.");
            ioe.getMessage();
        }
    }

    public void setDemandeStop(boolean b){
        this.demandeStop = b;
    }

    public void run(){
        event.getChannel().sendMessage("thread: thread start");

        this.event.getApi().addMessageCreateListener(event0 -> {
            if (event0.getServer().get().getId() == this.event.getServer().get().getId() && this.vivant) {
                //System.out.println(event0.getServer().get());
                this.nbMessage++;

                String[] mots = stringToArray(event0.getMessageContent());

                if (mots[0].equalsIgnoreCase("capture")) {
                    if (this.actualGuess != null) {
                        if (mots.length > 1) {
                            mots[1] = event0.getMessageContent().substring("capture ".length());

                            System.out.println(this.actualGuess.getName());
                            System.out.println(mots[1]);

                            if (mots[1].equalsIgnoreCase(this.actualGuess.getName())) {
                                if (Math.random() < this.actualGuess.getTauxDrop()) {
                                    event0.getChannel().sendMessage("Utilisateur capture !");

                                    getMembreById(event0.getMessageAuthor().getIdAsString()).ajouterInventaire(this.actualGuess);
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
                                event0.getChannel().sendMessage("Incorrect, ce n'est pas son nom !");
                                this.nbTentatives--;
                                if (this.nbTentatives <= 0) {
                                    this.actualGuess = null;
                                    this.event.getChannel().sendMessage("L'utilisateur s'est enfui !");
                                }
                            }
                        } else {
                            event0.getChannel().sendMessage(
                                    "Veuillez donner le nom de l'utilisateur");
                        }
                    } else {
                        event0.getChannel().sendMessage(
                                "L'utilisateur a deja ete capture ! Attendez qu'un nouvel utilisateur apparaisse.");
                    }
                }

                if (mots[0].equalsIgnoreCase("inventaire")) {
                    MembreCollectable mAuteur = getMembreById(event0.getMessageAuthor().getIdAsString());

                    event0.getChannel().sendMessage(mAuteur.getInventaireToString());
                }

                if (mots[0].equalsIgnoreCase("sauvegarder")) {
                    try {
                        creerFichierSave();
                    } catch (IOException ioe) {
                        event.getChannel().sendMessage("thread: Erreur lors de la creation du fichier de sauvegarde.");
                        ioe.getMessage();
                    }
                }
            }
        });

        event.getChannel().sendMessage("thread: thread configure");

        while(true){
            try {
                boolean activity = false;
                Thread.sleep(randInt(tempsMin, tempsMax) * 100); //*60000
                if (this.demandeStop) {
                    this.vivant = false;
                    this.event.getChannel().sendMessage("thread stoppe");
                    break;
                }

                if (this.nbMessage >= this.niveauActivite) { // && this.actualGuess == null) {
                    if (this.actualGuess != null){
                        this.event.getChannel().sendMessage("L'utilisateur s'est enfui !");
                    }
                    this.actualGuess = getRandomMembre();
                    this.nbTentatives = randInt(1, 4);
                    this.event.getChannel().sendMessage("Un nouveau membre apparait !" + "\n"
                        + this.actualGuess.getName() + "\n"
                        + "drop: " + this.actualGuess.getTauxDrop() + "\n"
                        + "nbTentatives: " + this.nbTentatives + "\n"
                        + this.actualGuess.getAvatarUrl().toString());

                    sendImageInverse(this.actualGuess);
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

    private void sendImageInverse(MembreCollectable m){
        try {
            File filepdpi = new File("..\\fichiers_goldabot\\imagetest1.png");
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

            /*
            int rgb = image.getRGB(0, 0);
            int alpha = (rgb >> 24) & 0xFF;
            int red =   (rgb >> 16) & 0xFF;
            int green = (rgb >>  8) & 0xFF;
            int blue =  (rgb      ) & 0xFF;

            int rgbi = (255-blue) + (255-green)*256 + (255-red)*256*256 + alpha*256*256*256;
            int alphai = (rgbi >> 24) & 0xFF;
            int redi =   (rgbi >> 16) & 0xFF;
            int greeni = (rgbi >>  8) & 0xFF;
            int bluei =  (rgbi      ) & 0xFF;

            System.out.println("couleur originale: alpha: " + alpha + ", rouge:" + red + ", vert:" + green + ", bleu:" + blue);
            System.out.println("resultat obtenu: alpha: " + alphai + ", rouge:" + redi + ", vert:" + greeni + ", bleu:" + bluei);
            System.out.println("resultat attendu: alpha: " + alpha + ", rouge:" + (255-red) + ", vert:" + (255-green) + ", bleu:" + (255-blue));
            */

            ImageIO.write(image, "png", filepdpi);
            this.event.getChannel().sendMessage(filepdpi);

        } catch (Exception e){
            e.getMessage();
        }
    }

    /*private User dropUser(){
        User u;
        if (Math.random() < 0.1) {

        }
        return u;
    }*/

    private int randInt(int min, int max) {
        Random rand = new Random();
        int randomNum = rand.nextInt((max - min) + 1) + min;
        return randomNum;
    }


// INITIALISATION

    private void initListeMembre(){
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
            this.fichierSave.write((" " + m.getId()).getBytes());
        }
    }


    private void lireTauxDrop() throws IOException {
        this.fichierSave.seek(0);
        String ligned = this.fichierSave.readLine();
        if (ligned != null) {
            String[] tabd = this.stringToArray(ligned);

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
            String[] tabs = stringToArray(ligne);
            if (tabs[0].equalsIgnoreCase(m.getId())) {
                if (tabs.length > 1) {
                    for (int i=1; i<tabs.length; i++) {
                        minv = getMembreById(tabs[i]);

                        if (minv != null) { m.ajouterInventaire(minv); }
                    }
                }
            }
            ligne = this.fichierSave.readLine();
        }
    }


    private void creerFichierSave() throws IOException {
        File f = new File(nomFichierSave);
        f.delete();

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



    private MembreCollectable getMembreById(String id){
        for (MembreCollectable m: this.listMembre) {
            if (m.getId().equalsIgnoreCase(id)){
                return m;
            }
        }
        return null;
    }

    private String[] stringToArray(String s){
        String[] words = s.split(" ");
        return words;
    }

}
