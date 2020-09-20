# Goldabot

Goldabot est un bot discord qui vous permet de capturer les membres de votre serveur !  
De la même manière que [Waifu Bot](https://waifubot.net/), des membres de votre serveurs apparaitront de façon aléatoire afin de les capturer.  
Bot programmé en Java grâce à l'API [Javacord](https://javacord.org/).  

Le bot fait apparaître des membres seulement si il détecte une certaine activité qui consiste en un certain nombre de message envoyé sur le serveur. 
Ce nombre de message est décrit par le premier paramètre de la commande `g.lancer` et est modifiable grâce à la commande `g.changer messageactif [n]` (voir commande `g.changer`).

Chaque membres ont un taux de drop aléatoire, ainsi certains membres sont plus difficile à attrapé que d'autre. 
Les taux de drop sont inconnu, unique et sauvegardé en même temps que inventaires de chacun. 
Il est possible de réinitialiser la mémoire, afin de vider tout les inventaires et de reroll les taux de drop des membres (voir commande `g.resetmemoire`). 

Parfois des membres EX peuvent apparaître. Ce sont des versions rare des membres qui sont représenté par une version "négative" des images de profil habituelle. Pour les attraper il suffit d'ajouter "EX" à la suite de son pseudonyme. Par défaut ce pourcentage de chance d'apparition des EX est de 0.1, mais il est possible de la modifier grâce à la commande `g.changer tauxex [taux]` (voir commande `g.changer`). 

Chaque membre a un certain nombre de tentative possible pour être capturer; si vous échouez la capture d'un membre (mauvais nom OU échec de chance de capture), le nombre de tentative diminu de 1. Si un membre n'a plus de nbTentatives, il s'enfuit ! par défaut ce nombre est entre 1 et 4 mais il est modifiable grâce à la commande `g.changer tentative [nbMin] [nbMax]` (voir commande `g.changer`).
Chaque personne n'a qu'une seule tentatives de capture mais plusieurs personnes peuvent tenter de capturer un membre.

Note: Il est possible de changer le préfix du bot (`g.`) sur votre serveur grâce à la commande `g.changer prefix [nouveau préfix]` (voir commande `g.changer`).

 **Commandes :**

- `g.aide OU help` : 
  Affiche les commandes possible.

- `g.capture [nom]` : 
  Permet de capturer un membre qui est apparu, pour capturer un membre vous devez écrire son pseudonyme précédé de cette commande. Pour un membre EX il suffit d'inscrire son nom puis la marque EX précédé d'un espace pour pouvoir le capturer !
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

**Paramètres commande 'changer' (admin)**
`g.changer [param] ([option] ([option]))` permet de modifier les paramètres du bot.
Il est possible d'afficher la config actuelle grâce à la commande `g.voirconfig`.

Liste des paramètres disponible  :  
- `tauxex [taux]` : Permet de changer le taux de spawn des membres EX (défaut: 0.1).
- `messageactif [nbMessage]` : Permet de changer le nombre de message minimal entre deux spawn permettant de considérer une activité.
- `nomoriginal [vrai OU faux]` : Permet de mettre le bot en mode 'nom originaux des membres' ou en mode 'pseudo sur le serveur'; Ceci changera également les pseudos à deviner durant la capture (défaut: vrai). 
- `intervalle [minutesMin] [minutesMax]` : Permet de changer la fourchette de temps possible entre les spawns.
- `tentative [nbMin] [nbMax]` : Permet de changer la fourchette de nombre de tentatives possible pour la capture d'un membre (défaut: 1 à 4).
- `prefix [prefix]` : Permet de changer le prefix du bot.



**Lien d'invitation :**  
[https://discord.com/oauth2/authorize?client_id=687418967786389524&scope=bot&permissions=281664](https://discord.com/oauth2/authorize?client_id=687418967786389524&scope=bot&permissions=281664)


