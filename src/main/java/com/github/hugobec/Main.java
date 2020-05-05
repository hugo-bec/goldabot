package com.github.hugobec;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class Main {

    static ThreadJavacord1 thread1 = null;
    static List<ThreadJavacord1> listThread = new ArrayList<>();

    public static void main(String[] args) {
        String token = "";
        try {
            RandomAccessFile tokenFile = new RandomAccessFile("..\\fichiers_goldabot\\tokengoldabot.txt", "r");
            token = tokenFile.readLine();
            tokenFile.close();
        } catch (Exception e) {
            e.getMessage();
        }
        DiscordApi api = new DiscordApiBuilder().setToken(token).login().join();

        ThreadAppelSave thas = new ThreadAppelSave(1, listThread);
        thas.start();

        // Add a listener which answers with "Pong!" if someone writes "!ping"
        api.addMessageCreateListener(event -> {
            String[] mots = stringToArray(event.getMessageContent());
            /*for (int i=0; i<mots.length; i++) {
                System.out.print(mots[i] + "+");
            }
            System.out.println();*/

            if (mots[0].equalsIgnoreCase("ping")) {
                event.getChannel().sendMessage("Pong!");
            }

            if (mots[0].equalsIgnoreCase("users")) {
                Optional<Server> oserveur = event.getServer();
                Server serveur = oserveur.get();
                Collection<User> users = serveur.getMembers();

                List<String> usersNames = new ArrayList<>();
                for (User u: users) {
                    usersNames.add(u.getName());
                }

                event.getChannel().sendMessage(usersNames.toString());
            }


            if (mots[0].equalsIgnoreCase("lancer")) {
                if (mots.length == 4) {
                    try{
                        int nbactif = Integer.parseInt(mots[1]);
                        int tempsMin = Integer.parseInt(mots[2]);
                        int tempsMax = Integer.parseInt(mots[3]);
                        lancerThread(event, nbactif, tempsMin, tempsMax);
                    } catch (NumberFormatException nfe){
                        //nfe.getMessage();
                        event.getChannel().sendMessage("Erreur: vous devez specifier 3 entiers.");
                    }
                } else {
                    event.getChannel().sendMessage("Erreur: vous devez specifier 3 entiers:\n"
                            + "Un pour le nombre de message correspondant à une activité minimum,\n"
                            + "2 autres entiers pour spécifier l'intervale de temps entre les apparitions.");
                }
            }

            if (event.getMessage().getContent().equalsIgnoreCase("stop")) {
                stopperThread(event);
            }

        });


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
            ThreadJavacord1 t1 = new ThreadJavacord1(event, nbactif, tempsMin, tempsMax);
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
                th.setDemandeStop(true);
                event.getChannel().sendMessage("Main: demande stop");
                listThread.remove(th);
                dejaStop = false;
            }
        }
        if (dejaStop) {
            event.getChannel().sendMessage("Thread deja stoppe");
        }
    }

    public static String[] stringToArray(String s){
        String[] words = s.split(" ");
        return words;
    }

}
