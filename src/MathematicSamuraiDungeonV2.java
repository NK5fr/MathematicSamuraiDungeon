import extensions.CSVFile;
import extensions.File;
class MathematicSamuraiDungeonV2 extends Program {
    /*










    Variables globales










    */
    final int TAILLE_SALLE = 7;
    final int TAILLE_ETAGE = 3;
    final int TAILLE_DONJON = 4;
    final int NB_ENNEMIS = 1;
    final int DMG_JOUEUR = 1;
    final int VIE_JOUEUR = 5;
    int vitesse;
    final int LONGUEUR_AFFICHAGE = 125;
    final String CHEMIN_IMAGES = "../ressources/";
    /*










    fonction utiles qui sont nécessaire en gégéral dans la programme ou pour la personnalisation et le style










    */
    // la fonction printSlow marche comme la fonction print mais il y a un délais entre l'affichage de chaque caractère ça permet une lecture plus fluide pour l'utilisateur
    void printSlow(String texte){
        hide();
        for(int i = 0; i<length(texte); i++){
            print(charAt(texte, i));
            delay(vitesse);
        }
        delay(500);
        println();
        show();
    }



    // la fonction toString permet l'affichage de la salle actuelle du joueur sans oublier d'afficher le joueur lui même
    String toString(Joueur joueur){
        String chaine = "";
        Salle salle = joueur.salle;
        for(int l = 0; l<length(salle.grille, 1); l++){
            for(int c = 0; c<length(salle.grille, 2); c++){
                if(l == joueur.l && c == joueur.c){
                    chaine = chaine + joueur.couleur + "⬢" + ANSI_RESET + " "; // emplacement du joueur
                }else if(sontCoordonneesEnnemis(l, c, salle)){
                    chaine = chaine + ANSI_RED + "◉ " + ANSI_RESET; // emplacement d'un ennemi
                }else if(salle.grille[l][c] == Case.SOL){
                    if(estFin(salle.e, l, c, salle.l, salle.c)){
                        chaine = chaine + ANSI_YELLOW + "▢" + ANSI_RESET + " "; // sols de fin de jeu
                    }else{
                        chaine = chaine + "▢ "; // sol
                    }
                }else if(salle.grille[l][c] == Case.ESCALIER && l < TAILLE_SALLE/2){
                    chaine = chaine + "▧ "; // escalier descendant
                }else if(salle.grille[l][c] == Case.ESCALIER && l > TAILLE_SALLE/2){
                    chaine = chaine + "▨ "; // escalier montant
                }else if(salle.grille[l][c] == Case.MUR){
                    chaine = chaine + "▣ "; // mur
                }
            }
            chaine = substring(chaine, 0, length(chaine)-1) + "\n";
        }
        return chaine;
    }

    void testToString(){
        Joueur joueur = new Joueur();
        joueur.l = 1;
        joueur.c = 1;
        joueur.couleur = ANSI_BLUE;
        Salle salle = new Salle();
        joueur.salle = salle;
        salle.grille = new Case[][]{{Case.MUR,Case.MUR,Case.MUR,Case.MUR,Case.MUR},
                                    {Case.MUR,Case.SOL,Case.SOL,Case.SOL,Case.MUR},
                                    {Case.MUR,Case.SOL,Case.SOL,Case.SOL,Case.MUR},
                                    {Case.MUR,Case.SOL,Case.SOL,Case.SOL,Case.MUR},
                                    {Case.MUR,Case.MUR,Case.MUR,Case.MUR,Case.MUR}};
        Ennemi ennemi = new Ennemi();
        ennemi.l = 2;
        ennemi.c = 2;                            
        salle.ennemis = new Ennemi[]{ennemi};
        assertEquals("▣ ▣ ▣ ▣ ▣\n▣ " + ANSI_BLUE + "⬢" + ANSI_RESET + " ▢ ▢ ▣\n▣ ▢ "+ANSI_RED+"◉ "+ANSI_RESET+"▢ ▣\n▣ ▢ ▢ ▢ ▣\n▣ ▣ ▣ ▣ ▣\n", toString(joueur));
    }



    // la fonction chaineVersEntier transforme une chaine de caractère en entier
    int chaineVersEntier(String texte){
        int resultat = 0;
        for(int i = 0; i<length(texte); i++){
            resultat += (charAt(texte, i)-'0')*pow(10, length(texte)-1-i);
        }
        return resultat;
    }

    void testChaineVersEntier(){
        assertEquals(4, chaineVersEntier("4"));
        assertEquals(42, chaineVersEntier("42"));
        assertEquals(425, chaineVersEntier("425"));
    }



    // la fonction aide affiche tous ce qui es nécessaire à savoir pour jouer
    void aide(Joueur joueur){
        println("Voici toutes les commandes qui sont disponibles ainsi que leur fonctionnalités :");
        println("-commande: z, permet d'avancer vers le haut");
        println("-commande: q, permet d'avancer vers la gauche");
        println("-commande: s, permet d'avancer vers le bas");
        println("-commande: d, permet d'avancer vers la droite");
        println("-commande: x, permet de quitter et sauvegarder le jeu");
        println("-commande: m, permet d'afficher la map");
        println("-commande: p, permet de changer la vitesse du texte");
        println("-commande: c, permet de changer la couleur du joueur");
        println("-commande: e, permet de changer la difficultée");
        println("-commande: o, permet d'afficher votre objectif et autres aides");
        println("\n");
        println("Voici la signification des cases présentes :");
        println("-case: ▣, c'est un mur impossible de passer à travers");
        println("-case: ▢, c'est un sol vous pouvez vous déplacer dessus");
        println("-case: ▨, c'est un escalier montant pour monter d'un étage");
        println("-case: ▧, c'est un escalier descendant pour descendre d'un étage");
        println("-case: " + joueur.couleur + "⬢" + ANSI_RESET + ", il s'agit de l'endroit où est le joueur");
        println("-case: ◉, il s'agit d'un endroit avec un ennemi");
        println("-case: " + ANSI_YELLOW + "▢" + ANSI_RESET + ", c'est la sortie du donjon");
        println("\n\n\n");
    }



    // cette fonction permet de modifier la vitesse du texte
    void choixVitesse(){
        boolean fini = false;
        while(!fini){
            println("choisissez une vitesse, tv pour très vite, v pour vite, m pour moyen, l pour lent");
            String vit = readString();
            changerVitesse(vit);
            printSlow("Est-ce que cette vitesse de texte vous convient ? (oui/non)");
            String accord = readString();
            if(equals(accord, "oui")){
                fini = true;
            }
            clearScreen();
        }
        String[][] rapid = new String[][]{{""+vitesse}};
        saveCSV(rapid, "../ressources/Vitesse.csv");
        clearScreen();
    }



    // cette fonction permet de changer la vitesse du texte en focntion d'une chaine de caractère donnée
    void changerVitesse(String vit){
        if(equals("tv", vit)){
            vitesse = 10;
        }else if(equals("v", vit)){
            vitesse = 25;
        }else if(equals("l", vit)){
            vitesse = 75;
        }else{
            vitesse = 50;
        }
    }

    void testChangerVitesse(){
        int vit = vitesse;
        changerVitesse("l");
        assertEquals(75, vitesse);
        vitesse = vit;
    }



    // cette fonction initialise la vitesse lors du lancement du code
    void initVitesse(){
        CSVFile rapid = loadCSV("../ressources/Vitesse.csv");
        vitesse = chaineVersEntier(getCell(rapid, 0, 0));
    }



    // cette fonction met à jour la puissance du joueur après chaque combat
    void majPuissance(Joueur joueur){
        joueur.vie = VIE_JOUEUR + joueur.compteurVictoire/5; // +1 vie tous les 5 victoires
        joueur.dmg = DMG_JOUEUR + joueur.compteurVictoire/7; // +1 dmg tous les 7 victoires
    }

    void testMajPuissance(){
        Joueur joueur = new Joueur();
        joueur.compteurVictoire = 35;
        majPuissance(joueur);
        assertEquals(12, joueur.vie);
        assertEquals(6, joueur.dmg);
    }



    // cette fonction demande au joueur la couleur voulue et la change
    void choixCouleur(Joueur joueur){
        println("choisissez une couleur (vert, bleu, jaune, rose)");
        String couleur = readString();
        if(equals(couleur, "vert")){
            joueur.couleur = changerCouleur(0);
        }else if(equals(couleur, "bleu")){
            joueur.couleur = changerCouleur(1);
        }else if(equals(couleur, "jaune")){
            joueur.couleur = changerCouleur(2);
        }else if(equals(couleur, "rose")){
            joueur.couleur = changerCouleur(3);
        }
        clearScreen();
    }



    // cette fonction converti un entier en couleur
    String changerCouleur(int couleur){
        if(couleur == 0){
            return ANSI_GREEN;
        }else if(couleur == 1){
            return ANSI_BLUE;
        }else if(couleur == 2){
            return ANSI_YELLOW;
        }else{
            return ANSI_PURPLE;
        }
    }

    void testChangerCouleur(){
        assertEquals(ANSI_BLUE, changerCouleur(1));
        assertEquals(ANSI_YELLOW, changerCouleur(2));
    }



    // cette fonction converti une couleur en entier
    int colorToInt(String couleur){
        if(couleur == ANSI_GREEN){
            return 0;
        }else if(couleur == ANSI_BLUE){
            return 1;
        }else if(couleur == ANSI_YELLOW){
            return 2;
        }else{
            return 3;
        }
    }

    void testColotToInt(){
        assertEquals(2, colorToInt(ANSI_YELLOW));
        assertEquals(3, colorToInt(ANSI_PURPLE));
    }



    // cette fonction retourne une chaine de caractère répétant un caractère précis n fois
    String repeat(int largeur, String s) {
        String res = "";
        for (int i=0;i<largeur;i++){
            res=res+s;
        }
        return res;
    }

    void testRepeat(){
        assertEquals("zzz", repeat(3, "z"));
        assertEquals("nnnnn", repeat(5, "n"));
    }
    


    // cette fonction permet de changer la difficultee du jeu
    void changerDifficultee(Joueur joueur){
        println("choisissez une difficultee (facile: f, moyen: m, difficile: d, très difficile: td) :");
        String diff = readString();
        if(equals("td", diff)){
            joueur.difficultee = 3;
        }else if(equals("d", diff)){
            joueur.difficultee = 2;
        }else if(equals("f", diff)){
            joueur.difficultee = 0;
        }else{
            joueur.difficultee = 1;
        }
        clearScreen();
    }


    // cette fonction permet d'aficher l'objectif du jeu
    void objectif(){
        println("votre objectif est de monter les étages et de vaincre tous les boss d'étages");
        println("Pour gagner sache que plus tu compte de victoire plus tu es puissant");
        println("Lorsque tu fait face à une divition on te demande uniquement le quotient sans virgule et sans le reste :");
        println("par exemple si tu pose la division 5/2 tu trouvera 2 en quotient et 1 en reste il te suffit donc d'écrire 2 (le quotient) en réponse");
        println("je te conseille de poser tes divisions et de ne pas te précipiter");
        readString();
        clearScreen();
    }
    /*










    fonctions qui vont permettre la validation du mouvement du joueur










    */
    // vérifie si un nom est valide
    boolean nomValide(String nom){
        return !equals("nom", nom)  && ! equals("Test", nom);
    }

    void testNomValide(){
        assertTrue(nomValide("N"));
        assertFalse(nomValide("Test"));
    }



    // vérifie si un lieu est la fin du jeu
    boolean estFin(int e, int l, int c, int salleL, int salleC){
        return e == TAILLE_DONJON-1 && l > TAILLE_SALLE/2 && c > TAILLE_SALLE/2 && salleL == TAILLE_ETAGE-1 && salleC == TAILLE_ETAGE-1; // les 4 dernières cases de la dernière salle du dernier étage
    }

    void testEstFin(){
        assertTrue(estFin(3, 4, 4, 2, 2));
        assertFalse(estFin(1, 4, 4, 2, 2));
    }



    // la fonction SontMemeCoordonnees permet de verifier si deux paires de coordonnees sont identiques
    boolean SontMemeCoordonnees(int l1, int c1, int l2, int c2){
        return l1 == l2 && c1 == c2;
    }

    void testSontMemeCoordonnees(){
        assertTrue(SontMemeCoordonnees(1, 4, 1, 4));
        assertFalse(SontMemeCoordonnees(2, 4, 1, 4));
    }



    // la fonction estMur vérifie si dans la salle aux coordonnées données il y a un mur 
    boolean estMur(Salle salle, int l, int c){
        return salle.grille[l][c] == Case.MUR;
    }

    void testEstMur(){
        Salle salle = new Salle();
        salle.grille = new Case[][]{{Case.MUR, Case.MUR, Case.MUR},{Case.MUR, Case.SOL, Case.MUR},{Case.MUR, Case.MUR, Case.MUR}};
        assertFalse(estMur(salle, 1, 1));
        assertTrue(estMur(salle, 0, 0));
    }



    // la fonction estAutreSalle vérifie si avec les coordonnées données et le mouvement prévu les coordonnées vont être hors des limites de la salle
    boolean estAutreSalle(int l, int c, String d){
        if(equals(d, "z")){
            return l-1 == -1;
        }else if(equals(d, "q")){
            return c-1 == -1;
        }else if(equals(d, "s")){
            return l+1 == TAILLE_SALLE;
        }else if(equals(d, "d")){
            return c+1 == TAILLE_SALLE;
        }
        return false;
    }

    void testEstAutreSalle(){
        assertTrue(estAutreSalle(0, 5, "z"));
        assertTrue(estAutreSalle(4, 0, "q"));
        assertFalse(estAutreSalle(2, 5, "s"));
        assertFalse(estAutreSalle(0, 3, "d"));
    }



    // la fonction estEscalier vérifie si dans la salle aux coordonnées données il y a un escalier
    boolean estEscalier(Salle salle, int l, int c){
        return salle.grille[l][c] == Case.ESCALIER;
    }

    void testEstEscalier(){
        Salle salle = new Salle();
        salle.grille = new Case[][]{{Case.MUR, Case.MUR, Case.MUR},{Case.MUR, Case.ESCALIER, Case.MUR},{Case.MUR, Case.MUR, Case.MUR}};
        assertFalse(estEscalier(salle, 0, 0));
        assertTrue(estEscalier(salle, 1, 1));
    }



    // la fonction estAutreSalle vérifie si avec les coordonnées données dans une salle et le mouvement prévu le joueur va changer d'étage (présence d'escalier)
    boolean estAutreEtage(Salle salle, int l, int c, String d){
        if(equals(d, "z") && estEscalier(salle, l-1, c)){
            return true;
        }else if(equals(d, "q") && estEscalier(salle, l, c-1)){
            return true;
        }else if(equals(d, "s") && estEscalier(salle, l+1, c)){
            return true;
        }else if(equals(d, "d") && estEscalier(salle, l, c+1)){
            return true;
        }
        return false;
    }

    void testEstAutreEtage(){
        Salle salle = new Salle();
        salle.grille = new Case[][]{{Case.MUR, Case.MUR, Case.MUR, Case.MUR},{Case.MUR, Case.SOL, Case.SOL, Case.MUR},{Case.MUR, Case.SOL, Case.ESCALIER, Case.MUR},{Case.MUR, Case.MUR, Case.MUR, Case.MUR}};
        assertTrue(estAutreEtage(salle, 1, 2, "s"));
        assertTrue(estAutreEtage(salle, 2, 1, "d"));
        assertFalse(estAutreEtage(salle, 1, 1, "s"));
        assertFalse(estAutreEtage(salle, 1, 2, "q"));
    }



    // la fonction estExtremite vérifie si les coordonnées données sont celles d'une case aux extremitées de la salle
    boolean estExtremite(int l, int c){
        return l == 0 || c == 0 || l == TAILLE_SALLE-1 || c == TAILLE_SALLE-1;
    }

    void testEstExtremite(){
        assertFalse(estExtremite(1, 1));
        assertTrue(estExtremite(0,1));
    }
    /*










    fonctions qui vont permettre la gestion des différents déplacements et des actions en général










    */
    // la fonction va orienter le joueur vers ce qu'il a demandé aide, déplacement...
    void action(Joueur joueur, String d){
        if(equals("o", d)){
            objectif();
        }else if(equals("e", d)){
            changerDifficultee(joueur);
        }else if(equals("c", d)){
            choixCouleur(joueur);
        }else if(equals("p", d)){
            choixVitesse();
        }else if(equals("m", d)){
            afficherMap(joueur);
        }else{
            deplacement(joueur, d);
        }
    }



    // la fonction déplacement est la fonction principale du déplacment 
    //elle lance la vérification de ce que le déplacement va provoquer et lance l'execution du déplacement en fonction
    // elle va aussi gérer les combats et les ennemis
    void deplacement(Joueur joueur, String d){
        if(estAutreSalle(joueur.l, joueur.c, d)){ // changement de salle si le mouvement le prévoit
            changerSalle(joueur);
        }else if(estAutreEtage(joueur.salle, joueur.l, joueur.c, d)){ // changement d'étage si le mouvement le prévoit
            changerEtage(joueur, d);
        }else{
            changerPos(joueur, d); // déplacement normal sinon
        }
        if(sontCoordonneesEnnemis(joueur.l, joueur.c, joueur.salle)){ // lance un combat contre un ennemi si ce dernier est croisé
            Ennemi ennemi = recupEnnemi(joueur.l, joueur.c, joueur.salle);
            if(!combat(joueur, ennemi)){
                joueur.l = TAILLE_SALLE/2;
                joueur.c = TAILLE_SALLE/2;
            }
            joueur.salle.ennemis =initialiserSalleEnnemis(joueur.l, joueur.c, joueur.salle); // regénère un ennemi après le combat
        }else if(estFin(joueur.salle.e, joueur.l, joueur.c, joueur.salle.l, joueur.salle.c)){ // lance le combat de fin de jeu si le joueur atteind la fin du donjon
            Ennemi boss = initialiserBoss(joueur.salle.e);
            if(combat(joueur, boss)){
                joueur.fini = true;
            }else{
                clearScreen();
                printSlow("Ce boss était trop fort pour toi va t'entrainer et revient plus tard");
                joueur.l = TAILLE_SALLE/2;
                joueur.c = TAILLE_SALLE/2;
            }
        }
        gererEnnemis(joueur);
    }



    // la fonction changerPos va effectuer un simple changement de position dans la salle selon le déplacement demandé et si il n'y a pas de mur
    void changerPos(Joueur joueur, String d){
        if(equals(d, "z") && !estMur(joueur.salle, joueur.l-1, joueur.c)){
            joueur.l--;
        }else if(equals(d, "q") && !estMur(joueur.salle, joueur.l, joueur.c-1)){
            joueur.c--;
        }if(equals(d, "s") && !estMur(joueur.salle, joueur.l+1, joueur.c)){
            joueur.l++;
        }if(equals(d, "d") && !estMur(joueur.salle, joueur.l, joueur.c+1)){
            joueur.c++;
        }
    }

    void testChangerPos(){
        Joueur joueur = new Joueur();
        joueur.l = 1;
        joueur.c = 1;
        Salle salle = new Salle();
        salle.grille = new Case[][]{{Case.MUR, Case.MUR, Case.MUR, Case.MUR},{Case.MUR, Case.SOL, Case.SOL, Case.MUR},{Case.MUR, Case.SOL, Case.ESCALIER, Case.MUR},{Case.MUR, Case.MUR, Case.MUR, Case.MUR}};
        joueur.salle = salle;
        changerPos(joueur, "s");
        assertEquals(2, joueur.l);
        changerPos(joueur, "q");
        assertEquals(1, joueur.c);
    }



    // la fonction changer salle va changer la salle actuelle du joueur et sa position selon sa position actuelle selon son emplacement actuel 
    // et comme on a déjà observé sa volontée de changer de salle
    void changerSalle(Joueur joueur){
        if(joueur.l == 0){
            joueur.salle = joueur.salle.haut;
            joueur.l = TAILLE_SALLE-1;
        }else if(joueur.c == 0){
            joueur.salle = joueur.salle.gauche;
            joueur.c = TAILLE_SALLE-1;
        }else if(joueur.l == TAILLE_SALLE-1){
            joueur.salle = joueur.salle.bas;
            joueur.l = 0;
        }else if(joueur.c == TAILLE_SALLE-1){
            joueur.salle = joueur.salle.droite;
            joueur.c = 0;
        }
        if(joueur.salle.trouvee == false){
            joueur.salle.trouvee = true; // ajoute que le joueur à vu une nouvelle salle
            joueurAVuSalle(joueur, joueur.salle);
        }
    }

    void testChangerSalle(){
        Joueur joueur = new Joueur();
        joueur.l = 2;
        joueur.c = 6;
        Salle salle = new Salle();
        Salle droite = new Salle();
        salle.trouvee = true;
        droite.trouvee = true;
        salle.grille = new Case[][]{{Case.MUR, Case.MUR, Case.MUR, Case.MUR, Case.MUR, Case.MUR, Case.MUR},
                                    {Case.MUR, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.MUR},
                                    {Case.MUR, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.SOL},
                                    {Case.MUR, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.SOL},
                                    {Case.MUR, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.SOL},
                                    {Case.MUR, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.MUR},
                                    {Case.MUR, Case.MUR, Case.MUR, Case.MUR, Case.MUR, Case.MUR, Case.MUR}};
        droite.grille = new Case[][]{{Case.MUR, Case.MUR, Case.MUR, Case.MUR, Case.MUR, Case.MUR, Case.MUR},
                                     {Case.MUR, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.MUR},
                                     {Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.MUR},
                                     {Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.MUR},
                                     {Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.MUR},
                                     {Case.MUR, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.MUR},
                                     {Case.MUR, Case.MUR, Case.MUR, Case.MUR, Case.MUR, Case.MUR, Case.MUR}};
        salle.droite = droite;
        joueur.salle = salle;
        changerSalle(joueur);
        assertEquals(0, joueur.c);
        assertArrayEquals(droite.grille, joueur.salle.grille);
    }



    // la fonction changerEtage va changer l'etage actuel du joueur selon le déplacement demandé
    // calcul son nouvel emplacment selon son emplacement actuel en faisant un effet miroir
    void changerEtage(Joueur joueur, String d){
        Ennemi boss = initialiserBoss(joueur.salle.e);
        if(equals(d, "z")){
            joueur.salle = joueur.salle.descendre;
            joueur.l = TAILLE_SALLE-1-joueur.l;
            joueur.c = TAILLE_SALLE-1-joueur.c;
        }else if(equals(d, "q")){
            joueur.salle = joueur.salle.descendre;
            joueur.l = TAILLE_SALLE-1-joueur.l;
            joueur.c = TAILLE_SALLE-1-joueur.c;
        }else if(equals(d, "s")){
            joueur.salle = joueur.salle.monter;
            joueur.l = TAILLE_SALLE-1-joueur.l;
            joueur.c = TAILLE_SALLE-1-joueur.c;
        }else if(equals(d, "d")){
            joueur.salle = joueur.salle.monter;
            joueur.l = TAILLE_SALLE-1-joueur.l;
            joueur.c = TAILLE_SALLE-1-joueur.c;
        }
        if(joueur.salle.trouvee == false){
            if(combat(joueur, boss)){ // si c'est la première montée dans l'escalier un boss apparait le passage est autorisé si victoire
                joueur.salle.trouvee = true; // nouvelle salle découverte
                joueurAVuSalle(joueur, joueur.salle);
            }else{
                clearScreen();
                printSlow("Ce boss était trop fort pour toi va t'entrainer et revient plus tard");
                joueur.salle = joueur.salle.descendre;
                joueur.l = TAILLE_SALLE/2;
                joueur.c = TAILLE_SALLE/2;
            }
            
        }
    }

    void testChangerEtage(){
        Joueur joueur = new Joueur();
        joueur.l = 3;
        joueur.c = 5;
        Salle salle = new Salle();
        Salle haut = new Salle();
        salle.trouvee = true;
        haut.trouvee = true;
        salle.grille = new Case[][]{{Case.MUR, Case.MUR, Case.MUR, Case.MUR, Case.MUR, Case.MUR, Case.MUR},
                                    {Case.MUR, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.MUR},
                                    {Case.MUR, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.MUR},
                                    {Case.MUR, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.MUR},
                                    {Case.MUR, Case.SOL, Case.SOL, Case.SOL, Case.ESCALIER, Case.ESCALIER, Case.MUR},
                                    {Case.MUR, Case.SOL, Case.SOL, Case.SOL, Case.ESCALIER, Case.ESCALIER, Case.MUR},
                                    {Case.MUR, Case.MUR, Case.MUR, Case.MUR, Case.MUR, Case.MUR, Case.MUR}};
        haut.grille = new Case[][]{{Case.MUR, Case.MUR, Case.MUR, Case.MUR, Case.MUR, Case.MUR, Case.MUR},
                                     {Case.MUR, Case.ESCALIER, Case.ESCALIER, Case.SOL, Case.SOL, Case.SOL, Case.MUR},
                                     {Case.MUR, Case.ESCALIER, Case.ESCALIER, Case.SOL, Case.SOL, Case.SOL, Case.MUR},
                                     {Case.MUR, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.MUR},
                                     {Case.MUR, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.MUR},
                                     {Case.MUR, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.MUR},
                                     {Case.MUR, Case.MUR, Case.MUR, Case.MUR, Case.MUR, Case.MUR, Case.MUR}};
        salle.monter = haut;
        joueur.salle = salle;
        changerEtage(joueur, "s");
        assertEquals(3, joueur.l);
        assertEquals(1, joueur.c);
        assertArrayEquals(haut.grille, joueur.salle.grille);
    }
    /*










    fonctions permettant d'initialiser le joueur ainsi que le monde










    */
    // la fonction initialisationJoueur va initialiser un joueur avec coordonnées et salle actuelle selon les données voulues
    Joueur initialiserJoueur(int[] donnee, String nom){
        Joueur joueur = new Joueur();
        joueur.nom = nom;
        joueur.l = donnee[0];
        joueur.c = donnee[1];
        joueur.vie = VIE_JOUEUR + donnee[5]/5;
        joueur.dmg = DMG_JOUEUR + donnee[5]/7;
        joueur.compteurVictoire = donnee[5];
        joueur.vieCourant = joueur.vie;
        joueur.couleur = changerCouleur(donnee[6]);
        joueur.difficultee = donnee[7];
        Salle[][][] donjon = new Salle[TAILLE_DONJON][TAILLE_ETAGE][TAILLE_ETAGE]; // nécessaire pour la création des salles et éviter les répétitions
        joueur.salle = initialiserSalle(donnee[0], donnee[1], donnee[2], donnee[3], donnee[4], donjon);
        if(!estAncienJoueur(nom)){
            joueurAVuSalle(joueur, joueur.salle); // si le joueur est nouveau il a vu la salle 0
        }
        initialiserSalleVue(joueur); // initialise toutes les salles vues par le joueur ultérieurement
        return joueur;
    }

    void testInitialiserJoueur(){
        Salle[][][] donjon = new Salle[10][10][10];
        int[] donnee = new int[]{1, 1, 0, 0, 0, 0, 0, 0};
        String nom = "Test";
        Joueur joueur = initialiserJoueur(donnee, nom);
        Case[][] resultat = new Case[][]{{Case.MUR, Case.MUR, Case.MUR, Case.MUR, Case.MUR, Case.MUR, Case.MUR},
                                     {Case.MUR, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.MUR},
                                     {Case.MUR, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.MUR},
                                     {Case.MUR, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.MUR},
                                     {Case.MUR, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.MUR},
                                     {Case.MUR, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.MUR},
                                     {Case.MUR, Case.MUR, Case.SOL, Case.SOL, Case.SOL, Case.MUR, Case.MUR}};
        assertEquals(1, joueur.l);
        assertEquals(1, joueur.c);
        assertEquals(nom, joueur.nom);
        assertEquals(0, joueur.salle.l);
        assertEquals(0, joueur.salle.c);
        assertEquals(0, joueur.salle.e);
        assertEquals(5 , joueur.vie);
        assertEquals(1 , joueur.dmg);
        assertEquals(0 , joueur.difficultee);
        assertEquals(ANSI_GREEN, joueur.couleur);
        assertArrayEquals(resultat, joueur.salle.grille);
    }



    // la fonction initialiserSalle va initiliser une salle avec coordonnées dans l'étage et l'étage lui même en créant les salles en haut/bas/gauche/droite/au-dessus/en-dessous de manière récursive si nécessaire
    Salle initialiserSalle(int lJ, int cJ, int l, int c, int e, Salle[][][] donjon){
        Salle salle = new Salle();
        donjon[e][l][c] = salle; // valider l'initialisation de la salle
        salle.l = l;
        salle.c = c;
        salle.e = e;
        salle.grille = initialiserGrille(l, c, e);
        salle.ennemis = initialiserSalleEnnemis(lJ, cJ, salle);
        if(salle.grille[0][TAILLE_SALLE/2] == Case.SOL){ // si le passage en haut est ouvert (pas de mur)
            if(donjon[e][l-1][c] != null){ // évite les répétitions si une salle à déjà été initialisée
                salle.haut = donjon[e][l-1][c];
            }else {
                salle.haut = initialiserSalle(lJ, cJ, l-1, c, e, donjon); // initialiser la salle en haut si pas déjà fait
            }
        }
        if(salle.grille[TAILLE_SALLE-1][TAILLE_SALLE/2] == Case.SOL){ // même principe mais pour le bas
            if(donjon[e][l+1][c] != null){
                salle.bas = donjon[e][l+1][c];
            }else {
                salle.bas = initialiserSalle(lJ, cJ, l+1, c, e, donjon);
            }
        }
        if(salle.grille[TAILLE_SALLE/2][0] == Case.SOL){ // même principe mais pour la gauche
            if(donjon[e][l][c-1] != null){
                salle.gauche = donjon[e][l][c-1];
            }else {
                salle.gauche = initialiserSalle(lJ, cJ, l, c-1, e, donjon);
            }
        }
        if(salle.grille[TAILLE_SALLE/2][TAILLE_SALLE-1] == Case.SOL){ // même principe mais pour la droite
            if(donjon[e][l][c+1] != null){
                salle.droite = donjon[e][l][c+1];
            }else {
                salle.droite = initialiserSalle(lJ, cJ, l, c+1, e, donjon);
            }
        }
        if(salle.grille[1][1] == Case.ESCALIER){ // même principe mais si il y a une salle dans l'étage du dessus
            if(donjon[e-1][TAILLE_ETAGE-1][TAILLE_ETAGE-1] != null){
                salle.descendre = donjon[e-1][TAILLE_ETAGE-1][TAILLE_ETAGE-1];
            }else {
                salle.descendre = initialiserSalle(lJ, cJ, TAILLE_ETAGE-1, TAILLE_ETAGE-1, e-1, donjon);
            }
        }
        if(salle.grille[TAILLE_SALLE-2][TAILLE_SALLE-2] == Case.ESCALIER){ // même principe mais si il y a une salle dans l'étage du dessous
            if(donjon[e+1][0][0] != null){
                salle.monter = donjon[e+1][0][0];
            }else {
                salle.monter = initialiserSalle(lJ, cJ, 0, 0, e+1, donjon);
            }
        }
        return salle;
    }

    void testInitialiserSalle(){
        Salle[][][] donjon = new Salle[10][10][10];
        int lJ = 1;
        int cJ = 1;
        int l = 0;
        int c = 6;
        int e = 9;
        Salle salle = initialiserSalle(lJ, cJ, l, c, e, donjon);
        Case[][] resultat = new Case[][]{{Case.MUR, Case.MUR, Case.MUR, Case.MUR, Case.MUR, Case.MUR, Case.MUR},
                                     {Case.MUR, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.MUR},
                                     {Case.MUR, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.MUR},
                                     {Case.MUR, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.MUR},
                                     {Case.MUR, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.MUR},
                                     {Case.MUR, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.MUR},
                                     {Case.MUR, Case.MUR, Case.MUR, Case.MUR, Case.MUR, Case.MUR, Case.MUR}};
        assertEquals(l, salle.l);
        assertEquals(c, salle.c);
        assertEquals(e, salle.e);
        assertArrayEquals(resultat, salle.grille);
    }



    // la fonction initialiserGrille va initialiser la grille d'une salle selon ses coordonnées dans l'étage et l'étage lui même en récupérant les données d'un fichier CSV
    Case[][] initialiserGrille(int l, int c, int e){
        CSVFile map = loadCSV("../ressources/Salle-"+l+"-"+c+"-"+e+".csv");
        Case[][] grille = new Case[TAILLE_SALLE][TAILLE_SALLE];
        for(int i = 0; i<TAILLE_SALLE; i++){
            for(int j = 0; j<TAILLE_SALLE; j++){
                if(equals(getCell(map, i, j), "▢")){
                    grille[i][j] = Case.SOL;
                }else if(equals(getCell(map, i, j), "▧") || equals(getCell(map, i, j), "▨")){
                    grille[i][j] = Case.ESCALIER;
                }else if(equals(getCell(map, i, j), "▣")){
                    grille[i][j] = Case.MUR;
                }
            }
        }
        return grille;
    }

    void testInitialiserGrille(){
        Case[][] grille = initialiserGrille(0, 6, 9);
        Case[][] resultat = new Case[][]{{Case.MUR, Case.MUR, Case.MUR, Case.MUR, Case.MUR, Case.MUR, Case.MUR},
                                     {Case.MUR, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.MUR},
                                     {Case.MUR, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.MUR},
                                     {Case.MUR, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.MUR},
                                     {Case.MUR, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.MUR},
                                     {Case.MUR, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.MUR},
                                     {Case.MUR, Case.MUR, Case.MUR, Case.MUR, Case.MUR, Case.MUR, Case.MUR}};
        assertArrayEquals(resultat, grille);
    }
    /*










    fonctions qui vont agir sur l'introduction du jeu et sa fin










    */
    // la fonction introduction va écrire le texte d'introduction et renvoyer les données du joueur si il est un ancien joueur ou en créer si il est nouveau
    int[] introduction(String nom){
        if(estAncienJoueur(nom)){ // vérifie si le joueur est ancien ou nouveau
            printSlow("Heureux de voir que tu es de retour " + nom + ", je vais te téléporter vers l'endroit où tu étais avant de partir");
            return recupererDonnee(nom);
        }else{
            printSlow("Bonjour nouveau joueur " + nom + " bienvenue dans ce donjon");
            printSlow("Avant que tu ne parte sache que lorsque tu vas faire face aux terribles divisions c'est le quotient sans virgule qui est demandé...");
            objectif();
            printSlow("Bonne chance Samuraï");
            return new int[]{1, 1, 0, 0, 0, 0, 0, 0}; // donnée de base
        }
    }



    // la fonction estAncienJoueur regarde si le joueur est nouveau ou non, c'est à dire si il existe dans le fichier joueurs.csv
    boolean estAncienJoueur(String nom){
        CSVFile joueurs = loadCSV("../ressources/Joueurs.csv");
        for(int i = 0; i < rowCount(joueurs); i++){
            if(equals(getCell(joueurs, i, 0), nom)){
                return true;
            }
        }
        return false;
    }

    void testEstAncienJoueur(){
        assertTrue(estAncienJoueur("Test"));
        assertFalse(estAncienJoueur("zzz"));
    }



    // la fonction recupererDonnee va récuperer et retourner les données d'un joueur dans le fichier joueurs.csv
    int[] recupererDonnee(String nom){
        int[] donnee = new int[8];
        CSVFile joueurs = loadCSV("../ressources/Joueurs.csv");
        for(int i = 0; i < rowCount(joueurs); i++){
            if(equals(getCell(joueurs, i, 0), nom)){
                for(int j = 1; j < columnCount(joueurs); j++){
                    donnee[j-1] = chaineVersEntier(getCell(joueurs, i, j));
                }
            }
        }
        return donnee;
    }

    void testRecupererDonnee(){
        int[] donnee = recupererDonnee("Test");
        assertArrayEquals(new int[]{0, 0, 0, 0, 0, 0, 0, 0}, donnee);
    }



    // la fonctions sauvegarder va ajouter ou mettre à jour les données d'un joueur dans le fichier joueurs.csv
    void sauvegarder(Joueur joueur){
        int[] donnee = new int[]{joueur.l, joueur.c, joueur.salle.l, joueur.salle.c, joueur.salle.e, joueur.compteurVictoire, colorToInt(joueur.couleur), joueur.difficultee};
        String nom = joueur.nom;
        if(estAncienJoueur(nom)){
            mettreAJour(nom, donnee); // met à jour les donnée si ancien joueur
        }else{
            ajouter(nom, donnee); // ajoute des donnée si nouveau joueur
        }
    }



    // la fonction mettreAJour va mettre a jour les données d'un joueur existant
    void mettreAJour(String nom, int[] donnee){
        CSVFile joueurs = loadCSV("../ressources/Joueurs.csv");
        String[][] fichier = new String[rowCount(joueurs)][columnCount(joueurs)];
        for(int i = 0; i < rowCount(joueurs); i++){
            if(equals(getCell(joueurs, i, 0), nom)){
                fichier[i][0] = nom;
                for(int j = 1; j < columnCount(joueurs); j++){
                    fichier[i][j] = ""+donnee[j-1]; // nouvelles données
                }
            }else{
                for(int j = 0; j < columnCount(joueurs); j++){
                    fichier[i][j] = getCell(joueurs, i, j); // donnée des autres joueurs à ne pas changer
                }
            }
        }
        saveCSV(fichier, "../ressources/Joueurs.csv");
    }



    // la focntion ajouter va ajouter les données d'un nouveau joueur au fichier joueurs.cvs
    void ajouter(String nom, int[] donnee){
        CSVFile joueurs = loadCSV("../ressources/Joueurs.csv");
        String[][] fichier = new String[rowCount(joueurs)+1][columnCount(joueurs)]; // ajoute 1 au nombre de joueur
        for(int i = 0; i < rowCount(joueurs); i++){
            for(int j = 0; j < columnCount(joueurs); j++){
                fichier[i][j] = getCell(joueurs, i, j); // donnée déjà dans le fichier
            }
        }
        fichier[length(fichier, 1)-1][0] = nom;
        for(int j = 1; j < columnCount(joueurs); j++){
                fichier[length(fichier, 1)-1][j] = ""+donnee[j-1]; // données du nouveau joueur
        }
        saveCSV(fichier, "../ressources/Joueurs.csv");
    }



    // regarde si le joueur apparait dans un fichier
    boolean joueurDansFichier(String nom, String chemin){
        CSVFile fichier = loadCSV(chemin);
        for(int i = 0; i < rowCount(fichier); i++){
            if(equals(nom, getCell(fichier, i, 0))){
                return true;
            }
        }
        return false;
    }

    void testJoueurDansFichier(){
        assertTrue(joueurDansFichier("Test", "../ressources/Joueurs.csv"));
    }


    // supprime les donnée d'un joueur dans un fichier
    void supprimerDonneeFichier(String nom, String chemin){
        CSVFile fichier = loadCSV(chemin);
        String[][] file = new String[rowCount(fichier)-1][columnCount(fichier)];
        if(joueurDansFichier(nom, chemin)){ // suppression si le joueur est dans la fichier
            int i = 0;
            int n = 0;
            while(i < rowCount(fichier)){
                if(!equals(nom, getCell(fichier, i, 0))){ // ajoute les données si il ne s'agit pas du joueur à enlever
                    for(int j = 0; j < columnCount(fichier); j++){
                        file[n][j] = getCell(fichier, i, j);
                    }
                    n++;
                }
                i++;
            }
            saveCSV(file, chemin);
        }
    }



    // supprime un joueur du jeu
    void supprimerDonnee(String nom){
        supprimerDonneeFichier(nom, "../ressources/Joueurs.csv"); // suppresion des données du joueur
        for(int i = 0; i < TAILLE_DONJON; i++){
            for(int j = 0; j < TAILLE_ETAGE; j++){
                for(int n = 0; n < TAILLE_ETAGE; n++){
                    supprimerDonneeFichier(nom, "../ressources/Salle-" + j + "-" + n + "-" + i + ".csv"); // suppression des données du joueur dans les salles vues
                }
            }
        }
    }
    /*









    fonctions qui vont gérer le fonctionnement des ennemis










    */
    // la fonction recupEnnemi permet de récupérer un ennemi précis dans la salle selon son emplacement
    Ennemi recupEnnemi(int l, int c, Salle salle){
        for(int i = 0; i<NB_ENNEMIS; i++){
            if(salle.ennemis[i].l == l && salle.ennemis[i].c == c){
                return salle.ennemis[i];
            }
        }
        return new Ennemi();
    }

    void testRecupEnnemi(){
        Salle salle = new Salle();
        Ennemi e = new Ennemi();
        e.l = 1;
        e.c = 1;
        salle.ennemis = new Ennemi[]{e};
        assertEquals(e, recupEnnemi(1, 1, salle));
    }



    // la fonction initialiserSalleEnnemis permet d'initialiser les ennemis dans une salle
    Ennemi[] initialiserSalleEnnemis(int l, int c, Salle salle){
        Ennemi[] ennemis = new Ennemi[NB_ENNEMIS];
        for(int i = 0; i<NB_ENNEMIS; i++){
            ennemis[i] = initialiserEnnemi(l, c, salle);
        }
        return ennemis;
    }

    void testInitialiserSalleEnnemis(){
        Salle salle = new Salle();
        salle.e = 0;
        salle.grille = new Case[][]{{Case.MUR, Case.MUR, Case.MUR, Case.MUR, Case.MUR, Case.MUR, Case.MUR},
                                     {Case.MUR, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.MUR},
                                     {Case.MUR, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.MUR},
                                     {Case.MUR, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.MUR},
                                     {Case.MUR, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.MUR},
                                     {Case.MUR, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.MUR},
                                     {Case.MUR, Case.MUR, Case.MUR, Case.MUR, Case.MUR, Case.MUR, Case.MUR}};
        salle.ennemis = initialiserSalleEnnemis(0, 0, salle);
        assertTrue(salle.ennemis[0].l >= 1 && salle.ennemis[0].l <= TAILLE_SALLE-2);
        assertTrue(salle.ennemis[0].c >= 1 && salle.ennemis[0].c <= TAILLE_SALLE-2);
        assertTrue(salle.ennemis[0].compteur >= 10 && salle.ennemis[0].compteur <= 20);
    }



    // la fonction initialiserSlime permet d'initialiser un slime selon les valeurs de base
    Ennemi initialiserSlime(int l, int c, Salle salle){
        Ennemi ennemi = new Ennemi();
        ennemi.l = (int) (random()*(TAILLE_SALLE-2)) + 1;
        ennemi.c = (int) (random()*(TAILLE_SALLE-2)) + 1;
        ennemi.compteur = (int) (random()*11) + 10;
        ennemi.vie = (int) (random()*6) + 5;
        ennemi.dmg = (int) (random()*3) + 1;
        ennemi.vieCourant = ennemi.vie;
        ennemi.type = Monstre.SLIME;
        while((ennemi.l == l && ennemi.c == c) || (salle.grille[ennemi.l][ennemi.c] == Case.ESCALIER) || estFin(salle.e, ennemi.l, ennemi.c, salle.l, salle.c)){ // ne peut pas être su le joueur ni sur un escalier ni sur les cases de fin
            ennemi.l = (int) (random()*(TAILLE_SALLE-2)) + 1;
            ennemi.c = (int) (random()*(TAILLE_SALLE-2)) + 1;
        }
        return ennemi;
    }

    void testInitialiserSlime(){
        Salle salle = new Salle();
        salle.grille = new Case[][]{{Case.MUR, Case.MUR, Case.MUR, Case.MUR, Case.MUR, Case.MUR, Case.MUR},
                                     {Case.MUR, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.MUR},
                                     {Case.MUR, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.MUR},
                                     {Case.MUR, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.MUR},
                                     {Case.MUR, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.MUR},
                                     {Case.MUR, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.MUR},
                                     {Case.MUR, Case.MUR, Case.MUR, Case.MUR, Case.MUR, Case.MUR, Case.MUR}};
        Ennemi ennemi = initialiserSlime(0, 0, salle);
        assertTrue(ennemi.l >= 1 && ennemi.l <= TAILLE_SALLE-2);
        assertTrue(ennemi.c >= 1 && ennemi.c <= TAILLE_SALLE-2);
        assertTrue(ennemi.compteur >= 10 && ennemi.compteur <= 20);
        assertTrue(ennemi.vie >= 5 && ennemi.vie <= 10);
        assertTrue(ennemi.dmg >= 1 && ennemi.dmg <= 3);
        assertTrue(ennemi.vieCourant >= 5 && ennemi.vieCourant <= 10);
        assertEquals(Monstre.SLIME, ennemi.type);
    }



    // la fonction initialiserFantome permet d'initialiser un fantome selon les valeurs de base
    Ennemi initialiserFantome(int l, int c, Salle salle){
        Ennemi ennemi = new Ennemi();
        ennemi.l = (int) (random()*(TAILLE_SALLE-2)) + 1;
        ennemi.c = (int) (random()*(TAILLE_SALLE-2)) + 1;
        ennemi.compteur = (int) (random()*11) + 10;
        ennemi.vie = (int) (random()*6) + 10;
        ennemi.dmg = (int) (random()*3) + 3;
        ennemi.vieCourant = ennemi.vie;
        ennemi.type = Monstre.GHOST;
        while((ennemi.l == l && ennemi.c == c) || (salle.grille[ennemi.l][ennemi.c] == Case.ESCALIER) || estFin(salle.e, ennemi.l, ennemi.c, salle.l, salle.c)){ // ne peut pas être su le joueur ni sur un escalier ni sur les cases de fin
            ennemi.l = (int) (random()*(TAILLE_SALLE-2)) + 1;
            ennemi.c = (int) (random()*(TAILLE_SALLE-2)) + 1;
        }
        return ennemi;
    }

    void testInitialiserFantome(){
        Salle salle = new Salle();
        salle.grille = new Case[][]{{Case.MUR, Case.MUR, Case.MUR, Case.MUR, Case.MUR, Case.MUR, Case.MUR},
                                     {Case.MUR, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.MUR},
                                     {Case.MUR, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.MUR},
                                     {Case.MUR, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.MUR},
                                     {Case.MUR, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.MUR},
                                     {Case.MUR, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.MUR},
                                     {Case.MUR, Case.MUR, Case.MUR, Case.MUR, Case.MUR, Case.MUR, Case.MUR}};
        Ennemi ennemi = initialiserFantome(0, 0, salle);
        assertTrue(ennemi.l >= 1 && ennemi.l <= TAILLE_SALLE-2);
        assertTrue(ennemi.c >= 1 && ennemi.c <= TAILLE_SALLE-2);
        assertTrue(ennemi.compteur >= 10 && ennemi.compteur <= 20);
        assertTrue(ennemi.vie >= 10 && ennemi.vie <= 15);
        assertTrue(ennemi.dmg >= 3 && ennemi.dmg <= 5);
        assertTrue(ennemi.vieCourant >= 10 && ennemi.vieCourant <= 15);
        assertEquals(Monstre.GHOST, ennemi.type);
    }



    // la fonction initialiserZombie permet d'initialiser un zombie selon les valeurs de base
    Ennemi initialiserZombie(int l, int c, Salle salle){
        Ennemi ennemi = new Ennemi();
        ennemi.l = (int) (random()*(TAILLE_SALLE-2)) + 1;
        ennemi.c = (int) (random()*(TAILLE_SALLE-2)) + 1;
        ennemi.compteur = (int) (random()*11) + 10;
        ennemi.vie = (int) (random()*6) + 15;
        ennemi.dmg = (int) (random()*3) + 5;
        ennemi.vieCourant = ennemi.vie;
        ennemi.type = Monstre.ZOMBIE;
        while((ennemi.l == l && ennemi.c == c) || (salle.grille[ennemi.l][ennemi.c] == Case.ESCALIER) || estFin(salle.e, ennemi.l, ennemi.c, salle.l, salle.c)){ // ne peut pas être su le joueur ni sur un escalier ni sur les cases de fin
            ennemi.l = (int) (random()*(TAILLE_SALLE-2)) + 1;
            ennemi.c = (int) (random()*(TAILLE_SALLE-2)) + 1;
        }
        return ennemi;
    }

    void testInitialiserZombie(){
        Salle salle = new Salle();
        salle.grille = new Case[][]{{Case.MUR, Case.MUR, Case.MUR, Case.MUR, Case.MUR, Case.MUR, Case.MUR},
                                     {Case.MUR, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.MUR},
                                     {Case.MUR, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.MUR},
                                     {Case.MUR, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.MUR},
                                     {Case.MUR, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.MUR},
                                     {Case.MUR, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.MUR},
                                     {Case.MUR, Case.MUR, Case.MUR, Case.MUR, Case.MUR, Case.MUR, Case.MUR}};
        Ennemi ennemi = initialiserZombie(0, 0, salle);
        assertTrue(ennemi.l >= 1 && ennemi.l <= TAILLE_SALLE-2);
        assertTrue(ennemi.c >= 1 && ennemi.c <= TAILLE_SALLE-2);
        assertTrue(ennemi.compteur >= 10 && ennemi.compteur <= 20);
        assertTrue(ennemi.vie >= 15 && ennemi.vie <= 20);
        assertTrue(ennemi.dmg >= 5 && ennemi.dmg <= 7);
        assertTrue(ennemi.vieCourant >= 15 && ennemi.vieCourant <= 20);
        assertEquals(Monstre.ZOMBIE, ennemi.type);
    }



    // la fonction initialiserDark permet d'initialiser un dark selon les valeurs de base
    Ennemi initialiserDark(int l, int c, Salle salle){
        Ennemi ennemi = new Ennemi();
        ennemi.l = (int) (random()*(TAILLE_SALLE-2)) + 1;
        ennemi.c = (int) (random()*(TAILLE_SALLE-2)) + 1;
        ennemi.compteur = (int) (random()*11) + 10;
        ennemi.vie = (int) (random()*6) + 20;
        ennemi.dmg = (int) (random()*3) + 7;
        ennemi.vieCourant = ennemi.vie;
        ennemi.type = Monstre.DARK;
        while((ennemi.l == l && ennemi.c == c) || (salle.grille[ennemi.l][ennemi.c] == Case.ESCALIER) || estFin(salle.e, ennemi.l, ennemi.c, salle.l, salle.c)){ // ne peut pas être su le joueur ni sur un escalier ni sur les cases de fin
            ennemi.l = (int) (random()*(TAILLE_SALLE-2)) + 1;
            ennemi.c = (int) (random()*(TAILLE_SALLE-2)) + 1;
        }
        return ennemi;
    }

    void testInitialiserDark(){
        Salle salle = new Salle();
        salle.grille = new Case[][]{{Case.MUR, Case.MUR, Case.MUR, Case.MUR, Case.MUR, Case.MUR, Case.MUR},
                                     {Case.MUR, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.MUR},
                                     {Case.MUR, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.MUR},
                                     {Case.MUR, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.MUR},
                                     {Case.MUR, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.MUR},
                                     {Case.MUR, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.MUR},
                                     {Case.MUR, Case.MUR, Case.MUR, Case.MUR, Case.MUR, Case.MUR, Case.MUR}};
        Ennemi ennemi = initialiserDark(0, 0, salle);
        assertTrue(ennemi.l >= 1 && ennemi.l <= TAILLE_SALLE-2);
        assertTrue(ennemi.c >= 1 && ennemi.c <= TAILLE_SALLE-2);
        assertTrue(ennemi.compteur >= 10 && ennemi.compteur <= 20);
        assertTrue(ennemi.vie >= 20 && ennemi.vie <= 25);
        assertTrue(ennemi.dmg >= 7 && ennemi.dmg <= 9);
        assertTrue(ennemi.vieCourant >= 20 && ennemi.vieCourant <= 25);
        assertEquals(Monstre.DARK, ennemi.type);
    }



    // la fonction initialiserBoss permet d'initialiser un boss selon les valeurs de base
    Ennemi initialiserBoss(int e){
        Ennemi ennemi = new Ennemi();
        ennemi.vie = (int) (random()*6) + (20 + e*5);
        ennemi.dmg = (int) (random()*3) + (3 + e*2);
        ennemi.vieCourant = ennemi.vie;
        if(e == 0){
            ennemi.type = Monstre.BOSS1;
        }else if(e == 1){
            ennemi.type = Monstre.BOSS2;
        }else if(e == 2){
            ennemi.type = Monstre.BOSS3;
        }else{
            ennemi.type = Monstre.BOSS4;
        }
        return ennemi;
    }

    void testInitialiserBoss(){
        Ennemi ennemi = initialiserBoss(0);
        assertTrue(ennemi.vie >= 20 && ennemi.vie <= 25);
        assertTrue(ennemi.dmg >= 3 && ennemi.dmg <= 5);
        assertTrue(ennemi.vieCourant >= 20 && ennemi.vieCourant <= 25);
        assertEquals(Monstre.BOSS1, ennemi.type);
    }



    // la fonction initialiserEnnemi permet d'initialiser un ennemi selon l'étage de la salle
    Ennemi initialiserEnnemi(int l, int c, Salle salle){
        Ennemi ennemi;
        if(salle.e == 0){
            ennemi = initialiserSlime(l, c, salle);
        }else if(salle.e == 1){
            ennemi = initialiserFantome(l, c, salle);
        }else if(salle.e == 2){
            ennemi = initialiserZombie(l, c, salle);
        }else{
            ennemi = initialiserDark(l, c, salle);
        }
        return ennemi;
    }



    // la fonction gererEnnemis permet de diminuer le compteur des ennemis de 1 et de le réinitialiser si il attend 0
    void gererEnnemis(Joueur joueur){
        for(int i = 0; i<NB_ENNEMIS; i++){
            joueur.salle.ennemis[i].compteur--;
            if(joueur.salle.ennemis[i].compteur == 0){
                joueur.salle.ennemis[i] = initialiserEnnemi(joueur.l, joueur.c, joueur.salle);
            }
        }
    }

    void testGererEnnemis(){
        Salle salle = new Salle();
        salle.grille = new Case[][]{{Case.MUR, Case.MUR, Case.MUR, Case.MUR, Case.MUR, Case.MUR, Case.MUR},
                                     {Case.MUR, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.MUR},
                                     {Case.MUR, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.MUR},
                                     {Case.MUR, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.MUR},
                                     {Case.MUR, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.MUR},
                                     {Case.MUR, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.SOL, Case.MUR},
                                     {Case.MUR, Case.MUR, Case.MUR, Case.MUR, Case.MUR, Case.MUR, Case.MUR}};
        salle.ennemis = initialiserSalleEnnemis(0, 0, salle);
        int temps = salle.ennemis[0].compteur;
        Joueur joueur = new Joueur();
        joueur.l = 0;
        joueur.c = 0;
        joueur.salle = salle;
        gererEnnemis(joueur);
        assertEquals(temps-1, salle.ennemis[0].compteur);
    }



    // la fonction sontCoordonneesEnnemis vérifie si les coordonnées sont celle d'un ennemi dans une salle
    boolean sontCoordonneesEnnemis(int l, int c, Salle salle){
        for(int i = 0; i<NB_ENNEMIS; i++){
            if(l == salle.ennemis[i].l && c == salle.ennemis[i].c){
                return true;
            }
        }
        return false;
    }

    void testSontCoordonneesEnnemis(){
        Salle salle = new Salle();
        Ennemi ennemi = new Ennemi();
        ennemi.l = 1;
        ennemi.c = 1;
        salle.ennemis = new Ennemi[]{ennemi};
        assertTrue(sontCoordonneesEnnemis(1, 1, salle));
        assertFalse(sontCoordonneesEnnemis(3, 3, salle));
    }
    /*












    fonctions pour afficher la map











    */
    // cette fonction permet, lors de l'initialisation du joueur, d'initialiser les salles déjà vues par ce joueur
    void initialiserSalleVue(Joueur joueur){
        boolean[][][] donjon = new boolean[TAILLE_DONJON][TAILLE_ETAGE][TAILLE_ETAGE]; // utile pour éviter qu'on répète les traitements sur les mêmes salles
        initialiserSalleVue(joueur.salle, joueur, donjon);
    }



    // cette fonction permet d'initialiser une salle qui a déjà été visitée par un joueur ou non
    void initialiserSalleVue(Salle salle, Joueur joueur, boolean[][][] donjon){
        CSVFile map = loadCSV("../ressources/Salle-"+salle.l+"-"+salle.c+"-"+salle.e+".csv");
        donjon[salle.e][salle.l][salle.c] = true; // validation salle parcourue
        for(int i = TAILLE_SALLE; i < rowCount(map); i++){
            if(equals(joueur.nom, getCell(map, i, 0))){
                salle.trouvee = true; // si le nom du joueur apparait dans le fichier alors la salle est vue
            }
        }
        if(salle.trouvee == true && salle.haut != null && donjon[salle.e][salle.l-1][salle.c] == false){ // si la salle est vue et que la salle du haut existe et n'a pas été parcourue
            initialiserSalleVue(salle.haut, joueur, donjon);
        }
        if(salle.trouvee == true && salle.gauche != null && donjon[salle.e][salle.l][salle.c-1] == false){ // pareil pour la gauche
            initialiserSalleVue(salle.gauche, joueur, donjon);
        }
        if(salle.trouvee == true && salle.bas != null && donjon[salle.e][salle.l+1][salle.c] == false){ // pareil pour le bas
            initialiserSalleVue(salle.bas, joueur, donjon);
        }
        if(salle.trouvee == true && salle.droite != null && donjon[salle.e][salle.l][salle.c+1] == false){ // pareil pour la droite
            initialiserSalleVue(salle.droite, joueur, donjon);
        }
        if(salle.trouvee == true && salle.monter != null && donjon[salle.e+1][0][0] == false){ // pareil pour la salle de l'étage du dessus
            initialiserSalleVue(salle.monter, joueur, donjon);
        }
        if(salle.trouvee == true && salle.descendre != null && donjon[salle.e-1][TAILLE_ETAGE-1][TAILLE_ETAGE-1] == false){ // pareil pour la salle de l'étage du dessous
            initialiserSalleVue(salle.descendre, joueur, donjon);
        }
    }

    void testSalleVue(){
        boolean[][][] donjon = new boolean[10][10][10];
        Joueur joueur = new Joueur();
        joueur.nom = "test";
        Salle salle = new Salle();
        salle.e = 7;
        salle.l = 7;
        salle.c = 7;
        Salle gauche = new Salle();
        gauche.e = 7;
        gauche.l = 7;
        gauche.c = 6;
        salle.gauche = gauche;
        gauche.droite = salle;
        joueur.salle = salle;
        initialiserSalleVue(salle, joueur, donjon);
        assertTrue(joueur.salle.trouvee);
        assertTrue(joueur.salle.gauche.trouvee);
    }



    // cette fonction ajoute un joueur parmis ceux qui ont exploré la salle
    void joueurAVuSalle(Joueur joueur, Salle salle){
        CSVFile map = loadCSV("../ressources/Salle-"+salle.l+"-"+salle.c+"-"+salle.e+".csv");
        String[][] tab = new String[rowCount(map)+1][columnCount(map)]; // augmente le nombre de joueur ayant vu la salle de 1
        for(int i = 0; i < rowCount(map); i++){
            for(int j = 0; j < columnCount(map); j++){
                tab[i][j] = getCell(map, i, j); // fichier de base
            }
        }
        tab[length(tab, 1)-1][0] = joueur.nom; // ajoute du nom du nouveau joueur
        for(int n = 1; n < columnCount(map); n++){
            tab[length(tab, 1)-1][n] = "x"; // on complète la ligne avec de x pour avoir la même taille que le fichier
        }
        saveCSV(tab, "../ressources/Salle-"+salle.l+"-"+salle.c+"-"+salle.e+".csv");
    }



    // cette fonction est la focntion générale pour afficher la map
    void afficherMap(Joueur joueur){
        CSVFile map = loadCSV("../ressources/Map.csv");
        String[][] carte = new String[rowCount(map)][columnCount(map)]; // va contenir l'image de la map
        for(int i = 0; i < rowCount(map); i++){
            for(int j = 0; j < columnCount(map); j++){
                carte[i][j] = getCell(map, i, j);
            }
        }
        boolean[][][] donjon = new boolean[TAILLE_DONJON][TAILLE_ETAGE][TAILLE_ETAGE]; // éviter les répétitions
        salleVue(carte, donjon, joueur.salle);
        println("Etage 0"+repeat(TAILLE_ETAGE-1, " ")+"Etage 1"+repeat(TAILLE_ETAGE-1, " ")+"Etage 2"+repeat(TAILLE_ETAGE-1, " ")+"Etage 3");
        println(toStringMap(carte, joueur));
        println("\n▣ est une salle explorée");
        println(ANSI_RED + "▣" + ANSI_RESET +" est la salle actuelle");
        println("▢ est un endroit inexploré");
        print("appuyez pour quittez :");
        readString();
        clearScreen();
    }



    // cette fonction permet de changer la map si une salle n'a pas été vue
    void salleVue(String[][] carte, boolean[][][] donjon, Salle salle){
        donjon[salle.e][salle.l][salle.c] = true; // salle analysée
        if(salle.trouvee == false){
            carte[salle.l][(TAILLE_ETAGE+1)*salle.e+salle.c] = "▢"; // si elle n'a pas été trouvée elle devient une case vide
        }
        if(salle.descendre != null && salle.trouvee == false){ // enlève les barres si l'étage du dessus n'a pas été vu
            carte[0][(TAILLE_ETAGE+1)*salle.e-1] = "   ";
            carte[1][(TAILLE_ETAGE+1)*salle.e-1] = "   ";
            carte[2][(TAILLE_ETAGE+1)*salle.e-1] = "   ";
        }
        if(salle.haut != null && donjon[salle.e][salle.l-1][salle.c] == false){ // répète l'action sur la salle du haut si elle existe et pas déjà analysée
            salleVue(carte, donjon, salle.haut);
        }
        if(salle.gauche != null && donjon[salle.e][salle.l][salle.c-1] == false){ // pareil sur la gauche
            salleVue(carte, donjon, salle.gauche);
        }
        if(salle.bas != null && donjon[salle.e][salle.l+1][salle.c] == false){ // pareil sur le bas
            salleVue(carte, donjon, salle.bas);
        }
        if(salle.droite != null && donjon[salle.e][salle.l][salle.c+1] == false){ // pareil sur la droite
            salleVue(carte, donjon, salle.droite);
        }
        if(salle.monter != null && donjon[salle.e+1][0][0] == false){ // pareil sur la salle de l'étage du dessus
            salleVue(carte, donjon, salle.monter);
        }
        if(salle.descendre != null && donjon[salle.e-1][TAILLE_ETAGE-1][TAILLE_ETAGE-1] == false){ // pareil sur la salle de l'étage du dessous
            salleVue(carte, donjon, salle.descendre);
        }
    }

    void testSalleVue2(){
        boolean[][][] donjon = new boolean[TAILLE_DONJON][TAILLE_ETAGE][TAILLE_ETAGE];
        String[][] carte = new String[(TAILLE_ETAGE+1)*4-1][3];
        Salle salle = new Salle();
        salle.trouvee = false;
        salle.e = 0;
        salle.l = 0;
        salle.c = 0;
        salleVue(carte, donjon, salle);
        assertEquals("▢", carte[0][0]);
    }



    // cette focnction permet de convertir la map en chaine de caractère
    String toStringMap(String[][] carte, Joueur joueur){
        String map = "";
        for(int i = 0; i < length(carte, 1); i++){
            for(int j = 0; j < length(carte, 2); j++){
                if(i == joueur.salle.l && j == (TAILLE_ETAGE+1)*joueur.salle.e+joueur.salle.c){
                    map += ANSI_RED + carte[i][j] + " " + ANSI_RESET; // salle actuel
                }else if(length(carte[i][j]) > 1){
                    map += carte[i][j]; // entre deux étages
                }else{
                    map += carte[i][j] + " "; // caractère de base
                }
            }
            map += "\n";
        }
        return map;
    }

    void testToStringMap(){
        String[][] carte = new String[][]{{"▣","▣","▢"},{"▣","▢","▢"},{"▢","▢","▢"}};
        Joueur joueur = new Joueur();
        Salle salle = new Salle();
        salle.l = 0;
        salle.c = 0;
        salle.e = 0;
        joueur.salle = salle;
        assertEquals(ANSI_RED + "▣ " + ANSI_RESET + "▣ ▢ \n▣ ▢ ▢ \n▢ ▢ ▢ \n", toStringMap(carte, joueur));
        joueur.salle.c += 1;
        assertEquals("▣ " + ANSI_RED + "▣ " + ANSI_RESET + "▢ \n▣ ▢ ▢ \n▢ ▢ ▢ \n", toStringMap(carte, joueur));
    }
/*
    /*












    code lié aux combats












    */
    // enlève des points de vie au joueur
    void subirDmg(Joueur joueur, int dmg) {
        if (joueur.vieCourant <= dmg) {
            joueur.vieCourant = 0; // peut pas être négatif
        } else {
        joueur.vieCourant = joueur.vieCourant - dmg;
        }
    }
    
    void testSubirDmg() {
        Joueur j = new Joueur();
        j.vieCourant = 5;
        subirDmg(j,5);
        assertEquals(0, j.vieCourant);
        j.vieCourant = 8;
        subirDmg(j,5);
        assertEquals(3, j.vieCourant);
    }




    //Verifie qu'un joueur a un nombre non nul de point de vie
    boolean estMort(Joueur joueur) {
        return (joueur.vieCourant <= 0);
    }

    void testEstMort() {
        Joueur j = new Joueur();
        j.vieCourant = 5;
        assertFalse(estMort(j));
        j.vieCourant = 0;
        assertTrue(estMort(j));
    }



    // enlève des points de vie à l'ennemi
    void subirDmg(Ennemi ennemi, int dmg) {
        if (ennemi.vieCourant <= dmg) { // peut pas être négatif
            ennemi.vieCourant = 0;
        } else {
            ennemi.vieCourant = ennemi.vieCourant - dmg;
        }
    }

    void testSubirDmgEnnemi() {
        Ennemi e = new Ennemi();
        e.vieCourant = 5;
        subirDmg(e,5);
        assertEquals(0, e.vieCourant);
        e.vieCourant = 8;
        subirDmg(e,5);
        assertEquals(3, e.vieCourant);
    }



    //Verifie qu'un ennemi a un nombre non nul de point de vie
    boolean estMort(Ennemi e) {
        return (e.vieCourant <= 0);
    }

    void testEstMortEnnemi() {
        Ennemi e = new Ennemi();
        e.vieCourant = 5;
        assertFalse(estMort(e));
        e.vieCourant = 0;
        assertTrue(estMort(e));
    }



    //Créer une nouvelle question
    Question newQuestion(int chiffreA, int chiffreB, char operateur, int reponse) {
        Question q = new Question();
        q.chiffreA = chiffreA;
        q.chiffreB = chiffreB;
        q.operateur = operateur;
        q.reponse = reponse;
        return q;
    }

    void testNewQuestion() {
        Question q = newQuestion(1,9,'+',10);
        assertEquals(1, q.chiffreA);
        assertEquals(9, q.chiffreB);
        assertEquals('+', q.operateur);
        assertEquals(10, q.reponse);
    }



    //Fonction qui créer les arguments neccessaires pour la fonction newQuestion de maniere aléatoire
    Question generateQuestion(int difficultee) {
        char operateur;
        int a;
        int b;
        int operateurAleatoire = (int)(random()*4);
        if(operateurAleatoire == 1){
            operateur = '-';
            a = generateChiffreAddSous(difficultee);
            b = generateChiffreAddSous(difficultee);
            while(a-b < 0){ // pas de nombre négatif
                a = generateChiffreAddSous(difficultee);
                b = generateChiffreAddSous(difficultee);
            }
        }else if(operateurAleatoire == 2){
            operateur = '*';
            a = generateChiffreMultDiv(difficultee);
            b = generateChiffreMultDiv(difficultee);
        }else if(operateurAleatoire == 3){
            operateur = '/';
            a = generateChiffreMultDiv(difficultee);
            b = generateChiffreMultDiv(difficultee);
            if(b == 0){
                b = 1;
            }
        }else{
            operateur = '+';
            a = generateChiffreAddSous(difficultee);
            b = generateChiffreAddSous(difficultee);
        }
        int reponse = calculer(a, b, operateur); // calcul la réponse
        return newQuestion(a, b, operateur, reponse); // génère la question et la retourne
    }



    // Fonction permettant de générer un chiffre aléatoire en fonction de la difficulté pour les additions/soustraction
    int generateChiffreAddSous(int difficulte) {
        switch (difficulte) {
            case 0:
                return (int)(random()*31);
            case 1:
                return (int)(random()*51);
            case 2:
                return (int)(random()*151);
            case 3:
                return (int)(random()*201);
            default:
                return (int)(random()*51);
        }
    }



    // Fonction permettant de générer un chiffre aléatoire en fonction de la difficulté pour les multiplications/divisions
    int generateChiffreMultDiv(int difficulte) {
        switch (difficulte) {
            case 0:
                return (int)(random()*4);
            case 1:
                return (int)(random()*6);
            case 2:
                return (int)(random()*11);
            case 3:
                return (int)(random()*13);
            default:
                return (int)(random()*6);
        }
    }



    //Fonction servant a calculer la reponse d'une question
    int calculer(int a, int b, char operateur) {
        int reponse = 0;
        switch (operateur) {
            case '+':
                reponse = a + b;
                break;
            case '-':
                reponse = a - b;
                break;
            case '*':
                reponse = a * b;
                break;
            case '/':
                reponse = a / b;
                break;
            default:
                break;
        }
        return reponse;
    }

    void testCalculer() {
        assertEquals(10, calculer(5,5,'+'));
        assertEquals(0, calculer(5,5,'-'));
        assertEquals(25, calculer(5,5,'*'));
        assertEquals(1, calculer(5,5,'/'));
    }



    // cette fonction valide ou non une réponse
    boolean bonneReponse(Question q, String reponse) {
        return equals(""+q.reponse, reponse);
    }

    void testBonneReponse() {
        Question q = newQuestion(5,5,'+',10); 
        assertEquals(""+q.reponse, "10");
        q = newQuestion(5,5,'+',90); 
        assertEquals(""+q.reponse, "90");
        q = newQuestion(5,5,'+',2); 
        assertEquals(""+q.reponse, "2");
    }



    //Fonction servant a afficher une question
    String toString(Question q) {
        return "" + q.chiffreA + " " + q.operateur + " " + q.chiffreB;
    }

    void testToStringQ() {
        Question q = newQuestion(1,9,'+',10);
        assertEquals("1 + 9", toString(q));
        q = newQuestion(1,9,'*',10);
        assertEquals("1 * 9", toString(q));
        q = newQuestion(99,999,'/',10);
        assertEquals("99 / 999", toString(q));
        q = newQuestion(0,0,'-',10);
        assertEquals("0 - 0", toString(q));
    }



    //Fonction servant a afficher le joueur et l'ennemi sur la même ligne dans un string de longueur donnée
    // affiche les noms du montre et joueur puis les points vie restant d'un couleur et les points de vie perdu en blanc pour l'ennemi et joueur
    String toString(Joueur joueur, Ennemi ennemi, int longueur) {
        return joueur.nom + repeat(longueur - (length(joueur.nom) + length(monstreToString(ennemi.type))), " ") + monstreToString(ennemi.type) + '\n' +
        joueur.couleur + repeat(joueur.vieCourant, "█") + ANSI_RESET + repeat(joueur.vie - joueur.vieCourant, "█") + repeat(longueur - (ennemi.vie + joueur.vie), " ") + repeat(ennemi.vie - ennemi.vieCourant, "█") + ANSI_RED + repeat(ennemi.vieCourant, "█") + ANSI_RESET; 
    }



    // cette fonction converti un type de monstre en chaine de caractère
    String monstreToString(Monstre type) {
        if(type == Monstre.SLIME){
            return "Slime";
        }else if (type == Monstre.GHOST){
            return "Ghost";
        }else if (type == Monstre.ZOMBIE){
            return "Zombie";
        }else if (type == Monstre.DARK){
            return "Shinobi";
        }else if (type == Monstre.BOSS1){
            return "Gnomes";
        }else if (type == Monstre.BOSS2){
            return "Amongi";
        }else if (type == Monstre.BOSS3){
            return "Louis \"Maitre Fantôme\" XVI";
        }else if (type == Monstre.BOSS4){
            return "Henri \"Maitre des Chevalier\" IV";
        } else {
            return "Slime";
        }
    } 



    //Fonction servant a centrer une question dans un string de longueur donnée
    String toString(Question q, int longueur) {
        int moitieLongueur = (longueur - length(toString(q))) / 2;
        return repeat(moitieLongueur, " ") + toString(q) + repeat(moitieLongueur, " ");
    }

    void testToStringQLongueur() {
        int longueur = 10;
        Question q = newQuestion(1,9,'+',10);
        assertEquals("  1 + 9  ", toString(q, longueur));
        q = newQuestion(1,9,'*',10);
        assertEquals("       1 * 9       ", toString(q, longueur*2));
        q = newQuestion(99,999,'/',10);
        assertEquals(" 99 / 999 ", toString(q, longueur));
        q = newQuestion(0,0,'-',10);
        assertEquals("  0 - 0  ", toString(q, longueur));
    }



    //Fonction servant a centrer un string s sur une longueur donnée
    void printCentre(String s, int longueur) {
        int moitieLongueur = (longueur - length(s)) / 2;
        println(repeat(moitieLongueur, " ") + s + repeat(moitieLongueur, " "));
    }



    //Fonction servant a afficher les bonnes couleurs selon les caracteres ASCII du fichier texte (1 chiffre = 1 couleur)
    String afficherCaractere(char car) {
        String res = "█";
        switch (car) {
            case '0':
                res = ANSI_RED + res + ANSI_RESET;
                break;
            case '1':
                res = ANSI_GREEN + res + ANSI_RESET;
                break;
            case '2':
                res = ANSI_YELLOW + res + ANSI_RESET;
                break;
            case '3':
                res = ANSI_BLUE + res + ANSI_RESET;
                break;
            case '4':
                res = ANSI_PURPLE + res + ANSI_RESET;
                break;
            case '5':
                res = ANSI_CYAN + res + ANSI_RESET;
                break;
            case '6':
                res = ANSI_WHITE + res + ANSI_RESET;
                break;
            case '7':
                res = ANSI_BLACK + res + ANSI_RESET;
                break;
            default:
                res = " " + ANSI_RESET;
                break;
        }
        return res;
    }



    // Fonction servant a afficher une ligne d'un fichier texte avec les bonnes couleurs
    String ligneAfficher(String ligne) {
        String res = "";
        for (int i = 0; i < length(ligne); i++) {
            res = res + afficherCaractere(charAt(ligne, i));
        }
        return res;
    }


    // retourne le nombre de ligne d'un fichier texte
    int nombreDeLigne(String filename) {
        File f = newFile(filename);
        int res = 0;
        while(ready(f)) {
            readLine(f);
            res++;
        }
        return res;
    }



    // retourne la première occurence d'un caractère dans une chaine 
    int premiereOccurence(String texte, char c) {
        int res = 0;
        boolean trouvé = charAt(texte,0) == c;
        while (res < length(texte)-1 && !trouvé) {
            res+=1;
            trouvé = (charAt(texte, res) == c);
        }
        return res;
    }

    void testPremiereOccurence() {
        assertEquals(0, premiereOccurence("azertyuiop", 'a'));
        assertEquals(10, premiereOccurence("azertyuiop\n", '\n'));
    }



    // calcul le nombre d'occurence d'un caractère dans une chaine
    int nombreOccurences(String texte, char c) {
        int res = 0;
        int i = 0;
        while (i < length(texte)) {
            if (charAt(texte,i) == c) {
                res++;
            }
            i++;
        }
        return res;
    }

    void testNombreOccurences() {
        assertEquals(0, nombreOccurences("azertyuiop", 'm'));
        assertEquals(4, nombreOccurences("The quick brown fox jumps over a river", 'r'));
    }



    // Fonction servant a isoler le texte qu'on a dans une chaine de charactère avant la première occurence de \n (on récupère la première ligne)
    String getFirstLigne(String sprite) {
        boolean trouvé = false;
        String res = substring(sprite,0,premiereOccurence(sprite,'\n'));
        return res;
    }

    void testGetFirstLine() {
        assertEquals("AZERTY", getFirstLigne("AZERTY\nQSDFGH\nWXCVBN"));
        assertEquals("", getFirstLigne("\nAAAAA"));
    }



    // Cette fonction enlève tout ce qu'il y a avant la premiere occurence de \n dans un texte. On enlève le \n (on supprime la première ligne)
    String removeFirstLine(String sprite) {
        return substring(sprite,premiereOccurence(sprite,'\n')+1, length(sprite));
    }

    void testRemoveFirstline() {
        String texte = "AZERTY\nQSDFGH\nWXCVBN";
        texte = removeFirstLine(texte);
        assertEquals(texte,"QSDFGH\nWXCVBN");
        texte = removeFirstLine(texte);
        assertEquals(texte,"WXCVBN");
    }



    // Fonction servant a afficher un sprite mit dans le dossier ressources sous forme de fichier texte
    String afficherSprite(String filename) {
        File file = newFile(filename); 
        String retour = "";
        while(ready(file)) {
            String ligne = readLine(file);
            retour += ligneAfficher(ligne);
            retour += "\n";
        }
        return retour;
    }



    //Fonction affichant deux sprites
    String afficher2Sprite(String cheminfichier1, String cheminfichier2) {
        String reponse = "";
        String sprite1 = afficherSprite(cheminfichier1);
        String sprite2 = afficherSprite(cheminfichier2);
        int nbLignes = nombreOccurences(sprite1, '\n'); // On part du principe que tous les sprite ont un nombre de lignes égaux
        for (int i = 0; i < nbLignes ; i++) {
            reponse = reponse + repeat(LONGUEUR_AFFICHAGE/5, " ")+  getFirstLigne(sprite1)+ repeat(2*(LONGUEUR_AFFICHAGE/5), " ") + getFirstLigne(sprite2) + repeat(LONGUEUR_AFFICHAGE/5, " ") + '\n';
            sprite1 = removeFirstLine(sprite1);
            sprite2 = removeFirstLine(sprite2);
        }
        reponse = reponse + sprite1 + repeat(3*(LONGUEUR_AFFICHAGE/5)-(length(sprite1)+length(sprite2)), " ") + sprite2;
        return reponse;
        
    }



    // retourne le préfixe d'un ennemi selon son type pour récupérer plus tard l'image
    String prefixeEnnemi(Ennemi ennemi) {
        if(ennemi.type == Monstre.SLIME){
            return "SL";
        }else if (ennemi.type == Monstre.GHOST){
            return "G";
        }else if (ennemi.type == Monstre.ZOMBIE){
            return "Z";
        }else if (ennemi.type == Monstre.DARK){
            return "D";
        }else if (ennemi.type == Monstre.BOSS1){
            return "B1";
        }else if (ennemi.type == Monstre.BOSS2){
            return "B2";
        }else if (ennemi.type == Monstre.BOSS3){
            return "B3";
        }else if (ennemi.type == Monstre.BOSS4){
            return "B4";
        } else {
            return "SL";
        }
    }



    // Fonction servant a faire l'affichage et la reduction de dégat quand le joueur a faux a la question q posée
    void joueurAFaux(Joueur joueur, Ennemi ennemi, Question q) {
        print(ANSI_RED);
        printCentre(("Mauvaise reponse!"), LONGUEUR_AFFICHAGE);
        println();
        printCentre(("Vous perdez " + ennemi.dmg + " pv.."), LONGUEUR_AFFICHAGE);
        println();
        printCentre(("La bonne reponse etait " + q.reponse), LONGUEUR_AFFICHAGE);
        print(ANSI_RESET);
        subirDmg(joueur, ennemi.dmg);
        println();
    }



    // Fonction servant a faire l'affichage et la reduction de dégat quand le joueur a donné la bonne réponse a la question posée
    void joueurABon(Joueur joueur, Ennemi ennemi) {
        print(ANSI_GREEN); 
        printCentre(("Bonne reponse!"), LONGUEUR_AFFICHAGE);
        println();
        printCentre(("Vous infligez " + joueur.dmg + " degats!"), LONGUEUR_AFFICHAGE);
        print(ANSI_RESET);
        subirDmg(ennemi, joueur.dmg);
        println();
    }



    //Fonction servant a Monstrer le "bas de page" du combat
    void affichageCombat(Joueur joueur, Ennemi ennemi, int tour, Question q) {
        println(repeat(LONGUEUR_AFFICHAGE, "-"));
        printCentre(("Tour " + tour), LONGUEUR_AFFICHAGE);
        println(toString(joueur, ennemi, LONGUEUR_AFFICHAGE));
        println(toString(q, LONGUEUR_AFFICHAGE));
        print(">>>   ");
    }



    // Fonction servant a réaliser un tour du combat
    void tourDeCombat(Joueur joueur, Ennemi ennemi, int tour) {
        Question q = generateQuestion(joueur.difficultee); // générer question
        affichageCombat(joueur,ennemi,tour,q);
        String reponse = readString();
        if (bonneReponse(q, reponse)) { // lire si la réponse est bonne puis afficher annimation en conséquence
            joueurABon(joueur,ennemi);
            clearScreen();
            println('\n'+afficher2Sprite(CHEMIN_IMAGES+"S3.img", CHEMIN_IMAGES+prefixeEnnemi(ennemi)+"2.img"));
            affichageCombat(joueur,ennemi,tour,q);
            delay(500);
            clearScreen();
            println('\n'+afficher2Sprite(CHEMIN_IMAGES+"S1.img", CHEMIN_IMAGES+prefixeEnnemi(ennemi)+"1.img"));
        } else {
            joueurAFaux(joueur,ennemi,q);
            clearScreen();
            println('\n'+afficher2Sprite(CHEMIN_IMAGES+"S2.img", CHEMIN_IMAGES+prefixeEnnemi(ennemi)+"3.img"));
            affichageCombat(joueur,ennemi,tour,q);
            delay(500);
            clearScreen();
            println('\n'+afficher2Sprite(CHEMIN_IMAGES+"S1.img", CHEMIN_IMAGES+prefixeEnnemi(ennemi)+"1.img"));
        }
    }



    // Fonction servant a faire les affichages du résultat du combat
    boolean resolutionJeu(Joueur joueur, Ennemi ennemi, int tour) {
        println(repeat(LONGUEUR_AFFICHAGE, "-"));
        if (estMort(joueur)) {
            print(ANSI_RED);
            printCentre("Vous avez perdu en " + tour + " tours...", LONGUEUR_AFFICHAGE);
            print(ANSI_RESET);
            println('\n'+afficher2Sprite(CHEMIN_IMAGES+"S4.img", CHEMIN_IMAGES+prefixeEnnemi(ennemi)+"1.img"));
            return false;
        } else {
            print(ANSI_GREEN);
            printCentre("Vous avez gagne en " + tour + " tours !", LONGUEUR_AFFICHAGE);
            print(ANSI_RESET);
            println('\n'+afficher2Sprite(CHEMIN_IMAGES+"S1.img", CHEMIN_IMAGES+prefixeEnnemi(ennemi)+"4.img"));
            joueur.compteurVictoire += 1;
            return true;
        }
    }


    
    //cette fonction emule le combat entre deux joueurs
    boolean combat(Joueur joueur, Ennemi ennemi) {
        int v = joueur.vie;
        int d = joueur.dmg;
        int tour = 0;
        boolean victoire;
        println('\n'+afficher2Sprite(CHEMIN_IMAGES+"S1.img", CHEMIN_IMAGES+prefixeEnnemi(ennemi)+"1.img"));
        while (!estMort(joueur) && !estMort(ennemi)) {
            tour++;
            tourDeCombat(joueur,ennemi,tour);
        }
        victoire = resolutionJeu(joueur,ennemi,tour);
        majPuissance(joueur); // met à jour la puissance du joueur selon son nombre de victoire
        joueur.vieCourant = joueur.vie;
        clearScreen();
        if(v != joueur.vie){
            printSlow("Bien joué, après ce combat ta vie à augmentée d'une unité"); // notif si le joueur augmente sa stat de vie
        }
        if(d != joueur.dmg){
            printSlow("Bien joué, après ce combat tes dégats ont augmentés d'une unité"); // notif si le joueur augmente sa stat de dmg
        }
        clearScreen();
        return victoire;
    }

    /*










    algorithm principal du jeu










    */
    void algorithm(){
        clearScreen();
        initVitesse();
        printSlow("Bonjour samuraï des maths, donne moi ton nom pour entrer dans le donjon");
        String nom = readString(); // récupération du nom
        while(!nomValide(nom)){ // validation du nom
            printSlow("nom interdit, quel est votre nom ?");
            nom = readString();
        }
        int[] donnee = introduction(nom);
        Joueur joueur = initialiserJoueur(donnee, nom); // initialisation du joueur
        String d = "";
        while(!equals(d, "x") && !joueur.fini){ // tant que le joueur n'a pas voulu quitter ou a gagné le jeu
            clearScreen();
            action(joueur, d); // action du joueur
            if(!joueur.fini){
                aide(joueur); // affichage de l'aide
                println(toString(joueur)); // affichage de la map
                d = toLowerCase(readString()); // récupération de la commande
            }
        }
        if(joueur.fini){ // message de victoire et suppression des données
            printSlow("Tu as vaincu le dernier boss, ton niveau en mathématiques est impressionant");
            printSlow("Bien joué samouraï, j'espère te revoir dans un donjon plus difficile");
            supprimerDonnee(joueur.nom);
        }else{ // sauvegarde des données
            printSlow("Bonne chance, j'ai hâte de te revoir");
            sauvegarder(joueur);
            clearScreen();
        }
    }
}