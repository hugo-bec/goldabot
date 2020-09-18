package com.github.hugobec;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ThreadAppelSave extends Thread {
    private int minutes;
    private List<ThreadJavacord1> listThread;
    private boolean demandeStop;

    public ThreadAppelSave(int minutes, List<ThreadJavacord1> listThread){
        this.minutes = minutes;
        this.listThread = listThread;
    }

    public void run() {

        /*
        try {
            final RandomAccessFile logFile = new RandomAccessFile("../fichiers_goldabot/logs.txt", "rw");
            for (int i=0; i<1000; i++) {
                printOnLog(logFile ,"Compteur"+minutes+":"+i+"\n");
            }
            logFile.close();
        } catch (IOException ioe){
            ioe.getMessage();
            System.out.println("ERREUR: Impossible d'ouvrir / d'écrire dans logs.txt");
        }

        System.out.println(minutes+": Test fait");
         */

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
                    th.resetDemandeEchange();
                    th.resetDemandeMemoire();
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

    /*
    private void printOnLog(RandomAccessFile logFile, String message) {
        boolean done = false;
        while(!done) {
            done = false;
            //synchronized (logFile) {
                try {
                    logFile.write(message.getBytes());
                    done = true;
                } catch (IOException ioe){
                    ioe.getMessage();
                }
            //}
        }

        Lock l = new ReentrantLock();
        l.lock();
        try {
            logFile.write(message.getBytes());
        } catch (Exception e) {
            e.getStackTrace();
            e.getMessage();
        } finally {
            l.unlock();
        }
    }
    */
}
