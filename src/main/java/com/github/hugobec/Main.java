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

public class Main {

    static List<ThreadJavacord1> listThread = new ArrayList<>();
    static String prefixOriginal = "g.";   //ne doit pas contenir d'espace

    public static void main(String[] args) {
        String token = "";
        try {
            RandomAccessFile tokenFile = new RandomAccessFile("../fichiers_goldabot/tokengoldabot.txt", "r");
            token = tokenFile.readLine();
            tokenFile.close();
            //System.out.println("Token : " + token);
        } catch (Exception e) {
            e.getMessage();
        }
        DiscordApi api = new DiscordApiBuilder().setToken(token).login().join();
        System.out.println("Logged in");

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
                    System.out.print(serveurToString());

                } else {
                    System.out.println("Commande '" + command + "' inconnu ! Entrez 'ls' pour lister les serveurs sur lesquel est lancé le bot ou 'off' pour éteindre le bot.");
                }
            }
        }

        // Print the invite url of your bot
        // System.out.println("You can invite the bot by using the following url: " + api.createBotInvite());
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
        System.out.println("api logged out, fin du programme");
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

    public static String serveurToString(){
        String slistServeur = "";
        if (listThread.isEmpty()){
            slistServeur = "Le bot n'est lancé sur aucun serveur.";
        } else {
            for (ThreadJavacord1 thj: listThread){
                slistServeur += "Serveur: " + thj.getServeur().getName() + ", Salon: " + thj.getNomSalon() + ", nbMembre: "
                        + thj.getServeur().getMembers().size() + "\n";
            }
        }
        return slistServeur;
    }

}
