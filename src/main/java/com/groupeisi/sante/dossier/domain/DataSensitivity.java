package com.groupeisi.sante.dossier.domain;

/**
 * Niveaux de sensibilité (classification des données de santé, aligné CDP / moindre privilège).
 */
public enum DataSensitivity {
    /** Identité légère (nom affichage agenda). */
    IDENTITE_RESTREINTE,
    /** Données d'identification et couverture (NIR, mutuelle). */
    IDENTIFIANTS_COUVERTURE,
    /** Données médicales (diagnostics, notes cliniques). */
    DONNEES_MEDICALES
}
