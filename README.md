# Goldabot

Goldabot est un bot discord qui vous permet de capturer les membres de votre serveur !  
De la même manière que [Waifu Bot](https://waifubot.net/), des membres de votre serveurs apparaitront de façon aléatoire afin de les capturer.  
Bot programmé en Java grâce à l'API [Javacord](https://javacord.org/).  


Chaque membres ont un taux de drop aléatoire, ainsi certains membres sont plus difficile à attrapé que d'autre.  
Les taux de drop sont inconnu, unique et sauvegardé en même temps que inventaires de chacun.  
Il est possible de réinitialiser la mémoire, afin de vider tout les inventaires et de reroll les taux de drop des membres (voir commande `g.resetmemoire`).  



 **Commandes :**

- `g.aide OU help` : 
  Affiche les commandes possible.

- `g.capture [nom]` : 
  Permet de capturer un membre qui est apparu, pour un membre EX il suffit d'inscrire son nom puis la marque EX précédé d'un espace pour pouvoir le capturer !
  Le bot est en mode "nom original" par défaut, il est possible de changer ce paramètre avec la commande `g.changer nomoriginal` (voir options commandes `g.changer`).

- `g.inventaire` : 
  Affiche votre inventaire.
  
- `g.echange [idInventaire] OU 'annuler' OU 'confirmer'` : 
  Permet de proposer un échange avec le membre de votre idInventaire;
  Si 2 échanges sont proposé, chacune des 2 personnes doivent confirmer l'échange avec `g.echange confirmer`;
  Chacun peut annuler l'échange avec `g.echange annuler` si il ne le trouve pas convenable;
  Si rien ne se passe les propositons seront annulé en même temps que la sauvegarde automatique.

- `g.sauvegarder` : 
  Les sauvegardes sont éffectué de manière automatique toutes les 20 à 30 minutes mais il est aussi possible de sauvegarder manuellement en cas de crash grâce à cette commande.

- `g.ping` : 
  Renvoi Pong.
  
 **Commandes admin :**
 
- `g.lancer [nbActif] [tempsMin] [tempsMax]` : 
  Lance le Thread qui permet de faire apparaitre les membres à capturer. Exemple de paramètres de lancement :

  \> `g.lancer 5 10 30` :
  Un membre du serveur va apparaître toutes les 10 à 30 minutes seulement si il y a eu 5 messages ou plus durant cet intervalle.
  Les membres vont apparaitre dans le salon où la commande a été lancé.

- `g.stop` : 
  Arrête le spawn de membre sur ce serveur.
 
- `g.changer` : 
  Permet de changer les paramètres du bot; Voir `g.changer aide OU help` pour voir les détails des différentes commandes.
  
- `g.voirconfig` : 
  Affiche les paramètres actuel.
  
- `g.resetmemoire` : 
  Réinitialise la mémoire, soit tous les inventaires et les taux de drop.


**Lien d'invitation :**  
[https://discord.com/oauth2/authorize?client_id=687418967786389524&scope=bot&permissions=281664](https://discord.com/oauth2/authorize?client_id=687418967786389524&scope=bot&permissions=281664)


