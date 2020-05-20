package com.github.hugobec;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class Test {

    public static void main(String[] args) {
        for (int i = 0; i <= 65535; i++) {
            try {
                Socket socket = new Socket("127.0.0.1", i);
                System.out.println("La machine autorise les connexions sur le port : " + i);
            }catch (UnknownHostException e){
                e.printStackTrace();
            }catch (IOException e){
                // Port fermé ou non autorisé
            }
        }
    }
}
