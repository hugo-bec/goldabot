package com.github.hugobec;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.concurrent.CompletionException;

public class Main {

    static List<ThreadJavacord1> listThread = new ArrayList<>();
    static String prefixOriginal = "g.";   //ne doit pas contenir d'espace
    static String fichierparam = "settings.txt";
    static String token = "", idadmin = "";

    public static void main(String[] args) {
        String ligneparam = "";
        String[] tabligneparam;
        try {
            RandomAccessFile tokenFile = new RandomAccessFile("../fichiers_goldabot/"+fichierparam, "r");
            ligneparam = tokenFile.readLine();
            while (ligneparam != null) {
                tabligneparam = ligneparam.split(" ");
                if (tabligneparam[0].equalsIgnoreCase("token:")) {
                    token = tabligneparam[1];
                }
                else if (tabligneparam[0].equalsIgnoreCase("idadmin:")) {
                    idadmin = tabligneparam[1];
                }
                else {
                    printOnTerminal("ERREUR: Chargement '"+fichierparam+"': paramètre '" + tabligneparam[0] + "' inconnu.", true);
                    System.exit(0);
                }
                ligneparam = tokenFile.readLine();
            }
            if (token.isEmpty()) { printOnTerminal("ERREUR: Chargement '"+fichierparam+"': token manquant", true); System.exit(0);}
            if (idadmin.isEmpty()) { printOnTerminal("ERREUR: Chargement '"+fichierparam+"': idadmin manquant", true); System.exit(0);}

            tokenFile.close();
            printOnTerminal("Token : " + token, true);
            printOnTerminal("Idadmin : " + idadmin, true);
        } catch (Exception e) {
            printOnTerminal("ERREUR: Chargement '"+fichierparam+"': impossible d'ouvrir/de lire le fichier.", true);
            e.getMessage();
            System.exit(0);
        }
        try {
        DiscordApi api = new DiscordApiBuilder().setToken(token).login().join();

        printOnTerminal("Logged in", true);

        ThreadAppelSave thas = new ThreadAppelSave(15, listThread);
        thas.start();


        api.addMessageCreateListener(event -> {
            String[] tabRequete = event.getMessageContent().split(" ");
            ThreadJavacord1 thj = getThreadLance(event.getServer().get().getId());

            if (tabRequete[0].equalsIgnoreCase(prefixOriginal + "ping")) {
                event.getChannel().sendMessage("Pong!");

            } else if (tabRequete[0].equalsIgnoreCase(prefixOriginal + "lancer")) {
                if (event.getMessageAuthor().isServerAdmin()) {
                    if (tabRequete.length == 4) {
                        try {
                            int nbactif = Integer.parseInt(tabRequete[1]);
                            int tempsMin = Integer.parseInt(tabRequete[2]);
                            int tempsMax = Integer.parseInt(tabRequete[3]);
                            if (tempsMin <= tempsMax) {
                                if (nbactif > 0) {
                                    lancerThread(event, nbactif, tempsMin, tempsMax);
                                } else {
                                    event.getChannel().sendMessage("Erreur: nbActif doit être superieur à 0.");
                                }
                            } else {
                                event.getChannel().sendMessage("Erreur: tempsMin doit être inferieur à tempsMax.");
                            }
                        } catch (NumberFormatException nfe) {
                            //nfe.getMessage();
                            event.getChannel().sendMessage("Erreur: vous devez specifier 3 entiers.");
                        }
                    } else {
                        event.getChannel().sendMessage("Erreur: vous devez specifier 3 entiers:\n"
                                + "Un pour le nombre de message correspondant à une activité minimum,\n"
                                + "2 autres entiers pour spécifier l'intervale de temps en minutes entre les apparitions.");
                    }
                } else {
                    event.getChannel().sendMessage("Vous devez être administrateur pour lancer le thread.");
                }
            } else if (tabRequete[0].equalsIgnoreCase(prefixOriginal + "stop")) {
                if (event.getMessageAuthor().isServerAdmin()) {
                    stopperThread(event);
                } else {
                    event.getChannel().sendMessage("Vous devez être administrateur pour stopper le thread.");
                }

            } else if (tabRequete[0].equalsIgnoreCase(prefixOriginal + "off")) {
                if (event.getMessageAuthor().getIdAsString().equals(idadmin)) {
                    event.getChannel().sendMessage("adieu >:c");
                    stopperProgramme(api);
                } else {
                    event.getChannel().sendMessage("Vous devez être owner pour stopper le bot");
                }

            } else if (tabRequete[0].equalsIgnoreCase(prefixOriginal + "list")) {
                if (event.getMessageAuthor().getIdAsString().equals(idadmin)) {
                    event.getChannel().sendMessage(listServeurToString());
                } else {
                    event.getChannel().sendMessage("Vous devez être owner pour voir la liste des serveurs.");
                }
            }

            else {
                if (thj != null) {
                    thj.gestionEvent(event);
                } else {
                    if (tabRequete[0].startsWith(prefixOriginal)) {
                        event.getChannel().sendMessage("Erreur: `" + tabRequete[0] + "` commande inconnu. "
                                + "Lancez le bot avec la commande `" + prefixOriginal + "lancer`.");
                    }
                }
            }

            if (thj != null) {
                thj.incrementerNbMessage();
            }

        });

        api.addServerMemberLeaveListener(event -> {
            ThreadJavacord1 thj = getThreadLance(event.getServer().getId());
            if (thj != null) {
                thj.eventUserLeave(event.getUser());
            }
        });

        api.addServerMemberJoinListener(event -> {
            ThreadJavacord1 thj = getThreadLance(event.getServer().getId());
            if (thj != null) {
                thj.eventUserJoin(event.getUser());
            }
        });


        Scanner scanner = new Scanner(System.in);
        String command;
        while(true){
            command = scanner.nextLine();
            if (!command.isEmpty()) {
                if (command.equalsIgnoreCase("off")){
                    stopperProgramme(api);

                } else if (command.equalsIgnoreCase("ls")){
                    printOnTerminal(listServeurToString(), false);

                } else {
                    printOnTerminal("Commande '" + command 
                            + "' inconnu ! Entrez 'ls' pour lister les serveurs sur lesquel est lancé le bot ou 'off' pour éteindre le bot.", true);
                }
            }
        }

        } catch (CompletionException ce) {
            printOnTerminal("ERREUR: token incorrect ou accés internet impossible.", true);
            ce.getMessage();
            System.exit(0);
        }

        // Print the invite url of your bot
        // printOnTerminal("You can invite the bot by using the following url: " + api.createBotInvite());
    }

    public static void lancerThread(MessageCreateEvent event, int nbactif, int tempsMin, int tempsMax){
        boolean threadDejaLance = false;
        for (ThreadJavacord1 t: listThread){
            if (t.getServeur().getId() == event.getServer().get().getId()){
                threadDejaLance = true;
            }
        }
        if (!threadDejaLance) {
            ThreadJavacord1 t1 = new ThreadJavacord1(event, prefixOriginal, nbactif, tempsMin, tempsMax);
            t1.start();
            listThread.add(t1);
            event.getChannel().sendMessage("main: thread lance");
        } else {
            event.getChannel().sendMessage("Thread deja lance sur ce serveur.");
        }
    }

    public static void stopperThread(MessageCreateEvent event){
        boolean dejaStop = true;
        for (ThreadJavacord1 th: listThread) {
            if (th.getServeur().equals(event.getServer().get())){           // A TESTER
                th.stopperThread();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    ie.getMessage();
                }
                listThread.remove(th);
                dejaStop = false;
            }
        }
        if (dejaStop) {
            event.getChannel().sendMessage("Thread deja stoppe");
        }
    }

    public static void stopperProgramme(DiscordApi api){
        for (ThreadJavacord1 th: listThread) {
            th.interrupt();
        }
        api.disconnect();
        printOnTerminal("api logged out, fin du programme", true);
        System.exit(0);
    }


    public static ThreadJavacord1 getThreadLance(long id) {
        for (ThreadJavacord1 thj: listThread){
            if (thj.getServeur().getId() == id){
                return thj;
            }
        }
        return null;
    }

    public static String listServeurToString(){
        String slistServeur = "";
        if (listThread.isEmpty()){
            slistServeur = "Le bot n'est lancé sur aucun serveur.\n";
        } else {
            for (ThreadJavacord1 thj: listThread){
                slistServeur += "Serveur: " + thj.getServeur().getName() + ", Salon: " + thj.getNomSalon() + ", nbMembre: "
                        + thj.getServeur().getMembers().size() + "\n";
            }
        }
        return slistServeur;
    }

    public static void printOnTerminal(String message, boolean ln){
        System.out.print("Goldabot: " + message);
        if (ln) System.out.println("");
    }

}
