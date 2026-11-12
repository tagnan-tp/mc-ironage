# IronAge - plugin Paper 1.21.11

Verrouille le serveur a l'age de fer : les **10 objets en diamant** (epee, hache, **lance**, pelle, houe,
pioche, casque, plastron, jambieres, bottes) n'existent qu'en **un seul exemplaire chacun**, distribue au
hasard aux joueurs par la console. Plus aucun moyen d'en obtenir d'autres.

Version courante : voir `CHANGELOG.md` (une nouvelle version a chaque modification).

## Ce que le plugin bloque, en permanence
- **Craft** des 10 objets.
- **Stockage** dans les conteneurs : coffres (et doubles), barils, shulker boxes, fours, alambics,
  distributeurs, droppers, entonnoirs, **enderchest**, **toute entite de stockage** (wagon-coffre,
  bateau-coffre, chevaux/anes/mules/lamas charges), **sac (bundle)**, **cadre-objet**, **statue**
  (aussi via distributeur). Exceptions : enclume, table d'enchantement, et le **coffre special**.
- **Loot** : coffres de structures et **vaults des Trial Chambers** (normales et ominous).
- **Entonnoirs** : ni transfert automatique, ni ramassage au sol.
- **Villageois** : un commerce qui vendrait un objet en diamant vend l'equivalent **cuivre**.
- **Despawn au sol, feu et lave** : les objets distribues ne disparaissent jamais ainsi. Un objet en
  diamant **sans le tag d'unicite** qui apparait au sol est supprime (filet de securite).

## Suivi des detenteurs (anonyme)
Aucun message ne dit qui a recu quoi. Le suivi vit dans `plugins/IronAge/` :
- `detenteur-originel.yml` : qui a recu chaque objet au tirage (fige).
- `detenteur-actuel.yml` : qui le possede maintenant (mis a jour a la connexion et toutes les 30 s).

## Commandes
| Commande | Qui | Effet |
|---|---|---|
| `/ironage` | console | distribue les 10 objets (decompte de 10 s affiche aux joueurs) |
| `/ironquick` | console | meme distribution, **sans decompte** |
| `/ironage confirm` | console | confirme la derniere action `/ironage` en attente (expire en 30 s) |
| `/ironage reset` | console | supprime TOUS les exemplaires et remet a zero |
| `/ironage reroll` | console | retire et redistribue au hasard, **enchantements conserves** |
| `/ironage chestgive <joueur>` | console | donne le coffre special (autorise a stocker les objets) |
| `/ironage give <OBJET> <joueur>` | console | (re)donne un objet precis, ex: `DIAMOND_SWORD` |
| `/diamondhoe` | joueur | reroll de la cible de la houe (1 charge) ; `/diamondhoe op` : illimite (OP) |
| `/force give [joueur] [quantite]` | OP / console | splash potion de **Force II** fonctionnelle |
| `/villagermultiplier [valeur]` | OP / console | multiplicateur du prix des livres chez les villageois |
| `/coifeur` | console | active/desactive la reponse "Feur" |
| `/irontest [numero\|reset]` | OP / console | diagnostic : applique une configuration de modules |

Toute action `/ironage` (distribution, reset, reroll, chestgive, give) doit etre confirmee par
`/ironage confirm` dans les 30 secondes.

## Decompte de distribution
`/ironage` puis `/ironage confirm` : un decompte de 10 s s'affiche a l'ecran de tous les joueurs (son de plus
en plus aigu), puis la distribution a lieu. Un titre Minecraft ne peut pas etre agrandi sans resource pack :
les chiffres grossissent par paliers (10 a 7 petits, 6 a 4 moyens, 3-2-1 tres gros avec une decoration qui
s'elargit). Option `countdown-big-digits` : `false` si des joueurs Bedrock voient des carres.

## Houe en diamant (radar)
Clic droit avec la houe : la camera se tourne vers une cible aleatoire (clignement des yeux + particules),
la cible reste la meme tant qu'elle est connectee. Si elle meurt/se deconnecte, nouveau tirage au clic suivant.
Cible invisible ou avec une tete de mob/citrouille : « Cible temporairement introuvable ». Le nombre de
**charges** (`diamondhoe-max-charges`) s'affiche en violet dans le nom de l'objet ; poser la houe + un
bloc de diamant dans une table de craft rend 1 charge (jusqu'au maximum).

## Potions de Force
La **Force II** est interdite : a l'alambic elle ressort en Force I, et toute Force II bue ou lancee est ramenee
a Force I. `/force give` donne une splash potion de Force II **fonctionnelle** (taguee, jamais modifiee).

## Masse et Wind Burst
- Une masse ne peut recevoir **que Wind Burst** (table d'enchantement et enclume).
- Les livres Wind Burst sont retires des loots (coffres, vaults) et des commerces de villageois.
- **Seul moyen d'obtenir un livre Wind Burst** : un **oeuf de dragon** dans une **tailleuse de pierre**, au
  choix **I, II, III ou IV** (l'oeuf est consomme ; le livre porte un tag, tout autre livre est inutilisable).
- **Table d'enchantement** : une masse qui a deja Wind Burst propose **les 3 autres niveaux** (parmi I a IV) ;
  niveaux d'XP requis `windburst-table-costs` (10/20/30 par defaut).
- Les masses sont stockables, **sauf celles qui portent Wind Burst** (memes regles que les objets en diamant).

## Vaults des Trial Chambers rouvrables
Un joueur peut rouvrir une vault avec une autre cle (chaque ouverture consomme une cle). Option
`trial-vaults-reopenable` (module `loot-filter`).

## Diagnostic : configurations de test (`/irontest`)
10 modules : 1 `craft-blocking`, 2 `container-blocking`, 3 `loot-filter`, 4 `holder-tracking`,
5 `item-protection`, 6 `villagers`, 7 `hoe`, 8 `feur`, 9 `strength-rule`, 10 `mace-rules`.
`/irontest <n>` applique a chaud (temporairement) une configuration ; `/irontest reset` revient a `config.yml`.

| N | Configuration |
|---|---|
| 1 | BASE : tout coupe |
| 2 | Test binaire A : modules 2, 4, 6, 8, 10 |
| 3 | Test binaire B : modules 3, 4, 7, 8 |
| 4 | Test binaire C : modules 5, 6, 7, 8 |
| 5 | Test binaire D : modules 9, 10 |
| 6 | CONTROLE : tout actif |
| 7 a 16 | Verification : un seul module actif (7 = module 1, ... 16 = module 10) |

Module fautif d'un probleme = `1 + (A ? 1 : 0) + (B ? 2 : 0) + (C ? 4 : 0) + (D ? 8 : 0)` selon les essais 2 a 5
qui reproduisent le probleme. Si l'essai 1 (tout coupe) le reproduit deja, aucun module n'est en cause. Les
recettes de l'oeuf de dragon restent actives meme si `mace-rules` est coupe (voir `windburst-recipes-enabled`).

## Compiler
Java 21 + Maven : `mvn clean package` dans ce dossier. Jar : `target/ironage-plugin-<version>.jar`, a placer
dans `plugins/` d'un serveur Paper 1.21.11 (ou via GitHub Actions, voir `.github/workflows/build.yml`).
