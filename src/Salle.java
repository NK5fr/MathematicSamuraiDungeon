class Salle {
    Case[][] grille;
    Salle haut = null;
    Salle bas = null;
    Salle gauche = null;
    Salle droite = null;
    Salle monter = null;
    Salle descendre = null;
    int l;
    int c;
    int e;
    Ennemi[] ennemis;
    boolean trouvee = false;
}