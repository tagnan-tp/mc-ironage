# IronAge — plugin Paper 1.21.11

Verrouille le serveur à l'âge de fer : tous les objets en diamant (armes, outils,
armure) sont bannis du craft, du stockage en conteneur, du loot de structures et
des entonnoirs. Le seul moyen d'en obtenir un exemplaire est la commande console
`/ironage`, qui distribue aléatoirement **un seul exemplaire de chacun des 10 objets**
entre les joueurs en ligne.

## Objets concernés
`DIAMOND_SWORD`, `DIAMOND_AXE`, `DIAMOND_SPEAR`, `DIAMOND_SHOVEL`, `DIAMOND_HOE`,
`DIAMOND_PICKAXE`, `DIAMOND_HELMET`, `DIAMOND_CHESTPLATE`, `DIAMOND_LEGGINGS`,
`DIAMOND_BOOTS`.

(`DIAMOND_SPEAR` est l'arme ajoutée par la mise à jour 1.21.11 "Mounts of Mayhem".)

## Ce que fait le plugin

- **Craft bloqué en permanence** : le résultat de toute recette (établi ou grille
  2x2) impliquant un de ces objets est annulé (`PrepareItemCraftEvent` +
  sécurité `CraftItemEvent`).
- **Stockage bloqué dans les "block entities"** : impossible de déposer un de ces
  objets dans un coffre, coffre double, baril, shulker box, four/fourneau/fumoir,
  chaudron à potions, distributeur, dropper ou entonnoir (clic, shift-clic, drag).
  **Exception explicite** : enclume et table d'enchantement restent utilisables
  normalement (réparation, enchantement).
- **Retiré du loot généré** : ces objets sont filtrés de tout loot généré par les
  loot tables vanilla (`LootGenerateEvent`), donc absents des coffres de
  structures (forteresse, bastion, etc.).
- **Bloqué dans les entonnoirs** : un entonnoir ne peut ni transférer
  automatiquement un de ces objets (entonnoir/distributeur/dropper) ni le
  ramasser au sol.
- **`/ironage`** (console uniquement) : distribue aléatoirement un exemplaire de
  chacun des 10 objets entre les joueurs en ligne. Ne peut être exécutée
  qu'une seule fois (flag sauvegardé dans `config.yml`), pour garantir l'unicité
  des objets. `/ironage reset` (console) réinitialise ce flag si tu veux
  volontairement relancer une distribution.

## Compiler le plugin

Prérequis : **Java 21** et **Maven** installés sur ta machine.

```bash
cd ironage-plugin
mvn clean package
```

Le `.jar` sera généré dans `target/ironage-plugin-1.0.0.jar`. Il ne te reste
qu'à le déposer dans le dossier `plugins/` de ton serveur Paper 1.21.11 et à
redémarrer le serveur.

## Notes

- La commande est restreinte à `ConsoleCommandSender` : un joueur, même OP, ne
  peut pas l'exécuter en jeu (uniquement depuis la console du serveur, un
  fichier `.txt` de commandes planifiées, ou un plugin tiers agissant en tant
  que console).
- Si un objet en diamant est détruit (mort en lave, etc.), il est perdu pour de
  bon tant que `/ironage reset` n'est pas utilisé — c'est voulu, ça fait partie
  du twist de rareté.
