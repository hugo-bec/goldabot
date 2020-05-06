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

        api.addMessageCreateListener(event -> {
            String[] tabRequete = stringToArray(event.getMessageContent());

            if (tabRequete[0].equalsIgnoreCase("ping")) {
                event.getChannel().sendMessage("Pong!");
            }

            else if (tabRequete[0].equalsIgnoreCase("lancer")) {
                if (event.getMessageAuthor().isServerAdmin()) {
                    if (tabRequete.length == 4) {
                        try {
                            int nbactif = Integer.parseInt(tabRequete[1]);
                            int tempsMin = Integer.parseInt(tabRequete[2]);
                            int tempsMax = Integer.parseInt(tabRequete[3]);
                            if (tempsMin <= tempsMax) {
                                lancerThread(event, nbactif, tempsMin, tempsMax);
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
                                + "2 autres entiers pour spécifier l'intervale de temps entre les apparitions.");
                    }
                } else {
                    event.getChannel().sendMessage("Vous devez être administrateur pour lancer.");
                }
            }

            else if (tabRequete[0].equalsIgnoreCase("stop")) {
                stopperThread(event);
            }


            else {
                ThreadJavacord1 thj = getThreadLance(event.getServer().get().getId());
                if (thj != null) {
                    if (tabRequete[0].equalsIgnoreCase("capture")) {
                        thj.eventCapture(event.getMessageContent(), event);
                    }
                    else if (tabRequete[0].equalsIgnoreCase("inventaire")) {
                        thj.eventInventaire(event);
                    }
                    else if (tabRequete[0].equalsIgnoreCase("sauvegarder")) {
                        thj.eventSauvegarder(event);
                    }
                    else if (tabRequete[0].equalsIgnoreCase("changer")) {
                        if (event.getMessageAuthor().isServerAdmin()) {
                            if (tabRequete.length == 3) {
                                if (tabRequete[1].equalsIgnoreCase("tauxex")) {
                                    double taux = Double.parseDouble(tabRequete[2])
                                    if (taux > 0 && taux < 1) {
                                        thj.setTauxEx(taux);
                                    } else {
                                        event.getChannel().sendMessage("Erreur: Le taux doit être compris entre 0 et 1.");
                                    }
                                    event.getChannel().sendMessage("Taux d'apparition Ex change !");

                                } else if (tabRequete[1].equalsIgnoreCase("messageactif")) {
                                    thj.setNiveauActivite(Integer.parseInt(tabRequete[2]));
                                    event.getChannel().sendMessage("Nombre de message minimum pour considérer une activite change !");

                                } else {
                                    event.getChannel().sendMessage("Erreur: \"" + tabRequete[1] + "\" inconnu.");
                                }
                            } else if (tabRequete.length == 4) {
                                if (tabRequete[1].equalsIgnoreCase("intervalle")) {
                                    int tempsMin = Integer.parseInt(tabRequete[2]);
                                    int tempsMax = Integer.parseInt(tabRequete[3]);
                                    if (tempsMin <= tempsMax) {
                                        thj.setIntervalleTemps(tempsMin, tempsMax);
                                        event.getChannel().sendMessage("Intervalle entre les apparitions change !");
                                    } else {
                                        event.getChannel().sendMessage("Erreur: tempsMin doit être inferieur à tempsMax.");
                                    }
                                }
                            } else {
                                event.getChannel().sendMessage("Veuillez donner le nom du paramètre et la/les variable(s) associée(s).");
                            }
                        } else {
                            event.getChannel().sendMessage("Vous devez être administrateur pour changer les paramètres.");
                        }
                    }
                    else if(tabRequete[0].equalsIgnoreCase("echanger")){

                    }
                    
                    thj.incrementerNbMessage();
                }
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

    public static ThreadJavacord1 getThreadLance(long id) {
        for (ThreadJavacord1 thj: listThread){
            if (thj.getServeur().getId() == id){
                return thj;
            }
        }
        return null;
    }

}
