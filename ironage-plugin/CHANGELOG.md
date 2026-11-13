# Historique des versions

Une **nouvelle version est attribuee a chaque modification** (MAJEUR.MINEUR.CORRECTIF). Elle apparait
dans le nom du jar (`ironage-plugin-<version>.jar`), dans le nom du zip, dans `plugin.yml` et dans la
ligne de demarrage de la console (`IronAge v<version> active`).

## 1.4.1
- Correction : un livre Wind Burst IV donnait une masse Wind Burst III (l'enclume plafonne au niveau max
  vanilla) ; le vrai niveau du livre est maintenant applique (meme niveau = +1, plafonne a IV).
- Correction : une masse DEJA enchantee Wind Burst ne pouvait pas etre utilisee dans la table d'enchantement
  (le jeu la considere non enchantable et l'evenement arrivait annule) ; il est maintenant reactive.

## 1.4.0
- Vaults des Trial Chambers (normales et ominous) rouvrables a l'infini avec une nouvelle cle
  (`trial-vaults-reopenable`, actif par defaut).
- Sources reconstruites apres une perte accidentelle des fichiers (base : copie de test du 22 octobre +
  historique des modifications) ; README reecrit.

## 1.3.1
- Annulation de la fonctionnalite "trims d'armure" de la 1.3.0 (jugee inutile) : retour au comportement de
  la 1.2.0, y compris pour la table de forge et la meule (plus d'exception de stockage).

## 1.3.0
- Trims d'armure sur les pieces en diamant : modification via la table de forge (garde-fou qui
  conserve le tag d'unicite) et retrait par clic droit sur un chaudron d'eau. (Annulee en 1.3.1.)

## 1.2.0
- Table d'enchantement : une masse Wind Burst propose 3 choix = les 3 autres niveaux (parmi I a IV),
  couts reglables (`windburst-table-costs`). Une masse sans Wind Burst reste inenchantable.

## 1.1.0
- Decompte de 10 secondes affiche aux joueurs a la distribution (`/ironage`) ; `/ironquick` = sans decompte.
- Potions de Force : Force II interdite sauf `/force give` (splash potion fonctionnelle).
- Masse : seul Wind Burst autorise ; livres Wind Burst via oeuf de dragon dans la tailleuse de pierre
  (I a IV) ; masses Wind Burst non stockables ; interrupteur `windburst-recipes-enabled`.
- `/irontest` (configurations de diagnostic), modules activables, `/villagermultiplier`,
  `/ironage reroll`, `/ironage give`, charges de la houe, coffre special, anti-despawn, vaults filtres,
  villageois diamant -> cuivre, tirage equitable, numero de version.

## 1.0.0
- Premiere version (toutes les versions precedentes n'etaient pas numerotees) : craft, stockage, loot et
  entonnoirs bloques pour les 10 objets en diamant, `/ironage` console, suivi des detenteurs, Feur.
