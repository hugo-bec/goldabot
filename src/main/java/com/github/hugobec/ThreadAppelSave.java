package com.github.hugobec;

import java.io.IOException;
import java.util.List;

public class ThreadAppelSave extends Thread {
    private int minutes;
    private List<ThreadJavacord1> listThread;
    private boolean demandeStop;

    public ThreadAppelSave(int minutes, List<ThreadJavacord1> listThread){
        this.minutes = minutes;
        this.listThread = listThread;
    }

    public void run() {
        while (true) {
            if (this.demandeStop) {
                System.out.println("ThreadAppelSave: stop");
                break;
            }

            try {
                Thread.sleep(minutes * 60000);
            } catch (InterruptedException ie) {
                ie.getMessage();
            }

            try {
                for (ThreadJavacord1 th : this.listThread) {
                    th.sauvegarder();
                    //System.out.println(th.getState());
                }
            } catch (IOException ioe) {
                System.out.println("Problème lors de la sauvegarde.");
                ioe.getMessage();
            }
        }
    }

    public void setDemandeStop(boolean b){
        this.demandeStop = b;
    }
}
