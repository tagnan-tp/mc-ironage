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
- **Anonymat total** : aucun message en tchat ou en console n'associe un nom de
  joueur à un objet précis. Le suivi se fait uniquement via deux fichiers YAML
  dans le dossier du plugin (`plugins/IronAge/`) :
  - `detenteur-originel.yml` : qui a reçu quoi lors du tirage `/ironage` (figé).
  - `detenteur-actuel.yml` : qui possède quoi en ce moment (mis à jour
    automatiquement toutes les 30 secondes, plus à chaque connexion/déconnexion,
    via un tag invisible posé sur chaque objet).
- **Message de blocage configurable** : la clé `deny-storage-message` dans
  `config.yml` permet de personnaliser le message envoyé au joueur qui essaie
  de stocker un objet dans un conteneur. Codes couleur `&` acceptés, et le
  placeholder `%item%` est remplacé par le nom de l'objet (ex: `Diamond Sword`).
- **Failles de contournement fermées** :
  - **Villageois** : les commerces d'Armurier/Forgeron d'armes qui vendraient un
    objet en diamant contrôlé sont automatiquement transformés pour vendre
    l'équivalent en **cuivre** à la place (même prix, même niveau, même XP).
    Seul ce commerce précis est modifié, le reste du villageois reste intact.
  - **Sac (bundle)** : impossible d'y ranger un objet en diamant contrôlé.
  - **Toute entité de stockage** (wagon-coffre, bateau-coffre,
    cheval/âne/mule/lama chargé, et toute future entité de stockage ajoutée
    par Mojang) : même blocage que les coffres classiques — la règle est
    générique (toute entité avec un inventaire de stockage, sauf le joueur
    lui-même), donc pas besoin de lister chaque cas un par un.
  - **Statue (armor stand)** : impossible d'y équiper une armure en diamant
    contrôlée.
  - **Enderchest** : bloqué comme les autres conteneurs (son inventaire est
    rattaché au joueur, pas au bloc, d'où un traitement séparé).

## Fonctionnalité bonus : "Feur"

Quand un joueur écrit une phrase se terminant par le mot `quoi` (la
ponctuation finale comme `?`, `!` ou `,` est ignorée — ex: "tu fais quoi ?",
"il a dit quoi", "on y va ou quoi"), le serveur répond `Feur !` à tout le
monde. Activable/désactivable avec `/coifeur` (réservé à la console).
Réglable dans `config.yml` :
- `feur-enabled` : true/false (état par défaut au démarrage).
- `feur-message` : texte envoyé (codes couleur `&` acceptés).

## Administration avancée

- **`/ironage reset`** : supprime définitivement tous les exemplaires des
  objets en diamant actuellement en jeu (inventaires des joueurs en ligne,
  objets au sol, équipement des mobs qui en auraient ramassé, conteneurs des
  chunks chargés — y compris le coffre spécial), remet le plugin en état
  "aucune distribution", **et efface aussi la cible verrouillée par la houe**
  (sinon une future houe redistribuée aurait hérité d'une cible obsolète). Les
  joueurs hors-ligne porteurs d'un objet sont nettoyés automatiquement à leur
  prochaine connexion.
- **`/ironage confirm`** : toute action de `/ironage` (distribution, reset,
  chestgive) doit être confirmée par cette commande dans les 30 secondes,
  sinon elle expire sans effet.
- **`/ironage reroll`** : retire les exemplaires existants (inventaires en ligne,
  sol, mobs, conteneurs chargés) et les **redistribue au hasard** entre les
  joueurs en ligne, de façon équitable, en **conservant leurs enchantements**
  (ainsi que leur durabilité et les charges de la houe). Le détenteur
  originel n'est pas modifié (historique du premier tirage). Les objets
  introuvables (joueur hors-ligne, chunk non chargé, objet détruit) sont
  listés en console et restent où ils sont : utilise `/ironage give` au besoin.
- **`/ironage chestgive <joueur>`** : donne un coffre spécial ("Coffre de
  l'Age de Fer") au joueur indiqué — c'est le seul conteneur du serveur
  explicitement autorisé à stocker les objets en diamant uniques.
- **`/ironage give <OBJET> <joueur>`** : donne/réassigne manuellement un objet
  précis (nom Bukkit exact, ex: `DIAMOND_SWORD`, `DIAMOND_HOE`) à un joueur.
  Tout exemplaire existant de cet objet est retiré au préalable (inventaires
  en ligne, sol, mobs, conteneurs chargés) pour garantir l'unicité — utile en
  cas de bug, de perte injuste, ou pour rattraper un objet mal distribué.
- **Villageois, sac, wagon-coffre, animaux, enderchest, statue, distributeur,
  cadre-objet** : toutes ces failles de contournement sont fermées (voir
  détail plus haut).
- **Loot des Trial Chambers** : les vaults (et vaults ominous) ne peuvent plus
  distribuer d'objet en diamant contrôlé (ce loot ne passe pas par le même
  événement que les coffres de structures, d'où un listener dédié). En plus,
  un filet de sécurité global annule tout objet en diamant contrôlé qui
  apparaît au sol sans être un vrai exemplaire de `/ironage` (ex: mob spawné
  avec une armure diamant qui la lâcherait à sa mort).
- **Anti-despawn / anti-combustion** : les objets en diamant uniques ne
  disparaissent jamais au sol (durée de vie réinitialisée en continu) et ne
  peuvent pas être détruits par le feu ou la lave.

## `/diamondhoe` — radar de la houe en diamant

Le détenteur de la houe en diamant unique peut, en clic droit, verrouiller un
joueur en ligne aléatoire comme cible, puis se faire pointer vers elle : sa
caméra tourne vers la cible, avec un effet de clignement des yeux (impulsions
de Cécité très courtes) et une traînée de particules. Aucun resource pack
n'est utilisé, donc l'effet est identique sur Java et sur Bedrock (via
Geyser).

- La cible reste verrouillée tant qu'elle ne se déconnecte pas et que le
  joueur ne demande pas un nouveau tirage.
- Si la cible se déconnecte ou meurt, un nouveau tirage a lieu automatiquement
  au prochain clic droit.
- Si la cible est invisible ou porte une tête de mob / citrouille sculptée,
  le pointage échoue temporairement ("Cible temporairement introuvable"),
  sans reroll.
- `/diamondhoe` : consomme **1 charge** de la houe (2 par défaut, réglable
  dans `config.yml` via `diamondhoe-max-charges`), doit tenir la houe en main
  (utilisable par n'importe quel joueur).
- `/diamondhoe op` : reroll manuel illimité, réservé aux joueurs OP, doit
  aussi tenir la houe en main, et ne consomme jamais de charge.
- **Système de charges** : le nombre de charges restantes est affiché
  directement dans le nom de l'objet, **en violet** (ex: "Houe en Diamant
  Unique (2/2 charges)"). Poser la houe **et** un bloc de diamant dans une
  table de craft (ou la grille 2x2) lui rend 1 charge en consommant 1 bloc de
  diamant — et s'arrête tout seul dès que la houe est pleine : poser toute une
  pile de blocs de diamant ne gaspille jamais plus que le strict nécessaire.

**Note importante** : `/diamondhoe` et `/diamondhoe op` sont les deux seules
commandes du plugin utilisables par un joueur (pas la console) — c'est
inévitable puisqu'elles nécessitent de tenir la houe en main, ce qu'une
console ne peut pas faire. Toutes les autres commandes (`/ironage`,
`/coifeur`) restent strictement réservées à la console.

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

- `/ironage` et `/coifeur` sont restreintes à `ConsoleCommandSender` : un
  joueur, même OP, ne peut pas les exécuter en jeu (uniquement depuis la
  console du serveur, un fichier `.txt` de commandes planifiées, ou un
  plugin tiers agissant en tant que console).
- Le nettoyage des conteneurs lors d'un `/ironage reset` ne couvre que les
  **chunks actuellement chargés** — un coffre dans une zone non chargée au
  moment du reset ne peut pas être atteint techniquement à cet instant (mais
  ne devrait de toute façon jamais contenir d'objet diamant, puisque le
  stockage est bloqué en permanence, sauf coffre spécial).

## `/villagermultiplier` — prix des livres enchantés

Commande réservée aux joueurs OP (et à la console). Règle un multiplicateur
appliqué au prix des **livres enchantés** vendus par les villageois. L'auto-
complétion propose `1`, `2`, `0.5` et `0.25`, mais n'importe quel nombre
positif (jusqu'à 100) est accepté (`x2` et `0,5` fonctionnent aussi). Sans
argument, la commande affiche la valeur actuelle. La valeur est sauvegardée
dans `config.yml` (`villager-book-price-multiplier`) et s'applique dès la
prochaine ouverture d'un menu de commerce, y compris pour les villageois
existants. Le prix reste borné entre 1 et 64 émeraudes.

## Décompte de distribution (`/ironage` et `/ironquick`)

`/ironage` (puis `/ironage confirm`) lance d'abord un **décompte de 10 secondes affiché à
l'écran de tous les joueurs en ligne**, avec un son de plus en plus aigu, puis la
distribution a lieu juste après le « 1 » (message « ARTEFACTS ! »). Le décompte ne dit
jamais qui reçoit quoi.

Un titre Minecraft ne peut pas être agrandi à volonté sans resource pack (Java seulement),
donc les chiffres « grossissent » par paliers : **10 à 7** petits (sous-titre), **6 à 4**
moyens (titre), **3, 2, 1** très gros (chiffres pleine largeur, environ 2x plus hauts) avec
une décoration qui s'élargit à chaque seconde (`» 3 «`, `»» 2 ««`, `»»» 1 «««`).
Option `countdown-big-digits` dans `config.yml` : à mettre sur `false` si des joueurs
Bedrock voient des carrés à la place des chiffres larges.

**`/ironquick`** (console, à confirmer aussi via `/ironage confirm`) fait la même
distribution **sans décompte**. Un seul décompte à la fois est possible. `/ironage reroll`
n'a pas de décompte.

## Potions de Force (`/force give`)

La **Force II** est interdite pour les joueurs : une potion de Force II fabriquée à
l'alambic (Force + poudre de glowstone) ressort en **Force I** (le glowstone est
perdu), et par sécurité toute potion, splash, lingering ou flèche de Force II qui
serait bue ou lancée est ramenée à Force I. La **Force I** (normale ou longue)
n'est pas touchée.

**`/force give [joueur] [quantité]`** (OP ou console) donne une **splash potion de
Force II fonctionnelle** (taguée, jamais modifiée). En jeu, sans joueur indiqué, elle
est donnée à celui qui tape la commande ; depuis la console, le joueur est
obligatoire. Quantité de 1 à 16 (une potion par emplacement). Cette potion ne peut
pas être transformée (par exemple en lingering) : le résultat serait ramené à Force I.

## Masse et Wind Burst

- Une **masse** ne peut plus être enchantée à la **table d'enchantement**, et à
  l'**enclume** elle n'accepte que Wind Burst (tout autre enchantement est refusé).
- Les **livres Wind Burst** sont retirés des loots (coffres de structures, vaults des
  Trial Chambers) et des commerces de villageois. Les masses trouvées en loot sont
  débarrassées de leurs enchantements autres que Wind Burst. La table d'enchantement
  ne produit jamais de Wind Burst.
- **Seul moyen d'obtenir un livre Wind Burst** : poser un **œuf de dragon** dans une
  **tailleuse de pierre** (stonecutter), qui propose au choix **Wind Burst I, II, III
  ou IV** (l'œuf est consommé). Ces livres portent un tag invisible : tout autre livre
  Wind Burst (créatif, ancien loot...) est inutilisable à l'enclume.
- Les **masses** peuvent être stockées normalement (coffres, etc.), **sauf celles qui
  portent Wind Burst**, qui sont soumises aux mêmes restrictions de stockage que les
  objets en diamant (conteneurs, sac, cadre, entonnoir...). Le coffre spécial de
  `/ironage chestgive` les accepte.

Les recettes de l'œuf de dragon restent actives même si le module `mace-rules` est coupé
via `/irontest`.

## Diagnostic : configurations de test (`/irontest`)

Les fonctions du plugin sont regroupées en 10 modules numérotés :
1 `craft-blocking`, 2 `container-blocking`, 3 `loot-filter`, 4 `holder-tracking`,
5 `item-protection`, 6 `villagers`, 7 `hoe`, 8 `feur`, 9 `strength-rule`, 10 `mace-rules`.

**`/irontest`** (alias `/itest`, OP ou console) applique **instantanément, à chaud**,
une configuration de modules prédéfinie, pour tester sans réfléchir (nommée ainsi
car Minecraft a déjà un `/test` vanilla depuis la 1.21.5) :
- `/irontest` : liste les configurations numérotées.
- `/irontest <n>` : applique la configuration n.
- `/irontest reset` : revient à la section `modules` de `config.yml`.

| N° | Configuration |
|----|---------------|
| 1 | BASE : tout coupé |
| 2 | Test binaire A : modules 2, 4, 6, 8, 10 |
| 3 | Test binaire B : modules 3, 4, 7, 8 |
| 4 | Test binaire C : modules 5, 6, 7, 8 |
| 5 | Test binaire D : modules 9, 10 |
| 6 | CONTRÔLE : tout actif (le problème doit réapparaître) |
| 7 à 16 | Vérification : un seul module actif (7 = module 1, … 16 = module 10) |

Lecture des résultats des essais 1 à 5 (plante = oui/non) : le module fautif a pour
numéro `1 + (A ? 1 : 0) + (B ? 2 : 0) + (C ? 4 : 0) + (D ? 8 : 0)`. Si l'essai 1 (tout
coupé) plante déjà, le problème ne vient d'aucun module. Si seul l'essai 6 plante,
c'est le module 1 ou une combinaison de modules : les essais 7 à 16 tranchent.

Les changements sont **temporaires** (un redémarrage revient à `config.yml`). Un module
coupé n'applique plus ses protections : à utiliser pour tester, puis `/irontest reset`.
Les 6 commandes du plugin restent toujours enregistrées.
