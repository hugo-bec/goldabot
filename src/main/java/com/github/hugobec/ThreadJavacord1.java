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
 * */

public class ThreadJavacord1 extends Thread {

    private MessageCreateEvent event;
    private RandomAccessFile fichierSave;
    private int tempsMin, tempsMax;
    private boolean vivant;
    private int niveauActivite;
    private boolean demandeStop;
    private Map<User, List<User>> inventaires;
    private Map<User, Double> tauxDrop;
    private User actualGuess;
    private int nbMessage;
    private int nbTentatives;

    public ThreadJavacord1(MessageCreateEvent event, int niveauActivite, int tempsMin, int tempsMax) {
        this.event = event;
        this.tempsMin = tempsMin;
        this.tempsMax = tempsMax;
        this.niveauActivite = niveauActivite;

        this.vivant = true;
        this.demandeStop = false;
        this.tauxDrop = new HashMap<>();
        this.inventaires = new HashMap<>();
        initInventaires();

        try {
            //System.out.println("save_" + this.getServeur().getName()+ "_" + this.getServeur().getId());
            File f = new File("save_" + this.getServeur().getName()+ "_" + this.getServeur().getId());
            if(f.exists() && !f.isDirectory()) {
                System.out.println("le fichier existe !");
                this.fichierSave = new RandomAccessFile("save_" + this.getServeur().getName()+ "_" + this.getServeur().getId(), "rw");

                lireTauxDrop();
                for (User u : this.event.getServer().get().getMembers()) {
                    lireInventaires(u);
                }

                this.fichierSave.close();
            } else {
                System.out.println("le fichier n'existe pas !");
                event.getChannel().sendMessage(" thread: Creation du fichier de save");
                this.fichierSave = new RandomAccessFile("save_" + this.getServeur().getName()+ "_" + this.getServeur().getId(), "rw");

                initTauxDrop();
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
                                if (Math.random() < getTauxDrop(this.actualGuess)) {
                                    event0.getChannel().sendMessage("Utilisateur capture !");

                                    for (Map.Entry<User, List<User>> entry : this.inventaires.entrySet()) {
                                        if (entry.getKey().getId() == event0.getMessageAuthor().getId()) {
                                            entry.getValue().add(this.actualGuess);
                                        }
                                    }
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
                    for (Map.Entry<User, List<User>> entry : this.inventaires.entrySet()) {
                        //System.out.println(entry.getKey() + " " + event0.getMessageAuthor());
                        //System.out.println(entry.getKey().getId() == event0.getMessageAuthor().getId());

                        if (entry.getKey().getId() == event0.getMessageAuthor().getId()) {
                            String messageListe;
                            int i = 0;
                            //System.out.println(entry.getValue());
                            if (entry.getValue().isEmpty()){
                                messageListe = "Votre inventaire est vide " + entry.getKey().getMentionTag() + ".";
                            } else {
                                messageListe = entry.getKey().getMentionTag() + " voici votre inventaire :\n";
                                for (User u : entry.getValue()) {
                                    messageListe += i + " | " + u.getName() + "\n";
                                    i++;
                                }
                            }
                            //System.out.println("message liste : " + messageListe);
                            event0.getChannel().sendMessage(messageListe);
                        }
                    }
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
                    this.actualGuess = getRandomUser();
                    this.nbTentatives = randInt(1, 4);
                    this.event.getChannel().sendMessage("Un nouveau membre apparait !" + "\n"
                        + this.actualGuess.getName() + "\n"
                        + "drop: " + getTauxDrop(this.actualGuess) + "\n"
                        + "nbTentatives: " + this.nbTentatives + "\n"
                        + this.actualGuess.getAvatar().getUrl().toString());

                    getImageInverse();
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

    private double getTauxDrop(User u) {
        return this.tauxDrop.get(u);
    }


    private User getRandomUser(){
        Collection<User> colMembre = this.event.getServer().get().getMembers();
        List<User> listMembre = new ArrayList(colMembre);
        int nbrMembre = listMembre.size();
        return listMembre.get(randInt(0, nbrMembre-1));
    }

    private void getImageInverse(){
        try {
            File filepdpi = new File("imagetest1.png");
            URL input = this.actualGuess.getAvatar().getUrl();
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

    private static int randInt(int min, int max) {
        Random rand = new Random();
        int randomNum = rand.nextInt((max - min) + 1) + min;
        return randomNum;
    }


// INITIALISATION

    private void initInventaires(){
        for (User u: this.event.getServer().get().getMembers()) {
            this.inventaires.put(u, new ArrayList<User>());
        }
    }

    private void initTauxDrop() {
        for (User u: this.event.getServer().get().getMembers()) {
            this.tauxDrop.put(u, Math.random());
        }
    }




// SAUVEGARDE / CHARGEMENT

    private void ecrireTauxDrop() throws IOException {
        for (Map.Entry<User, Double> entry : this.tauxDrop.entrySet()){
            this.fichierSave.write((entry.getKey().getIdAsString() + ":" + entry.getValue() + " ").getBytes());
        }
    }

    private void lireTauxDrop() throws IOException {
        this.fichierSave.seek(0);
        String ligned = this.fichierSave.readLine();
        if (ligned != null) {
            String[] tabd = this.stringToArray(ligned);
            for (int i=0; i<tabd.length; i++) {
                String[] drop = tabd[i].split(":");
                //System.out.println(drop[0] + " et " + drop[1]);
                this.tauxDrop.put(this.getServeur().getMemberById(drop[0]).get(), Double.parseDouble(drop[1]));
            }
        }
    }


    private void ecrireInventaire(User u0) throws IOException {
        this.fichierSave.write(("\n" + u0.getIdAsString()).getBytes());
        for (User u: this.inventaires.get(u0)) {
            this.fichierSave.write((" " + u.getIdAsString()).getBytes());
        }
    }

    private void lireInventaires(User u) throws IOException {
        this.fichierSave.seek(0);
        this.fichierSave.readLine();
        String ligne = this.fichierSave.readLine();

        while (ligne != null) {
            String[] tabs = stringToArray(ligne);
            if (tabs[0].equalsIgnoreCase(u.getIdAsString())) {
                //System.out.println("meme id");
                if (tabs.length > 1) {
                    for (int i=1; i<tabs.length; i++) {
                        this.inventaires.get(u).add(this.getServeur().getMemberById(tabs[i]).get());
                    }
                }
            }
            ligne = this.fichierSave.readLine();
        }
    }


    private void creerFichierSave() throws IOException {
        File f = new File("save_" + this.getServeur().getName()+ "_" + this.getServeur().getId());
        f.delete();

        this.fichierSave = new RandomAccessFile("save_" + this.getServeur().getName()+ "_" + this.getServeur().getId(), "rw");
        this.ecrireTauxDrop();
        for (User u: this.getServeur().getMembers()) {
            ecrireInventaire(u);
        }
        this.fichierSave.close();
    }

    public void sauvegarder() throws IOException {
        creerFichierSave();
    }





    private String[] stringToArray(String s){
        String[] words = s.split(" ");
        return words;
    }

}
