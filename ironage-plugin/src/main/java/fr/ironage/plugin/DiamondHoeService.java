package fr.ironage.plugin;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Gere le "radar" de la houe en diamant : verrouille une cible aleatoire parmi
 * les joueurs en ligne, et joue un effet de pointage (clignement des yeux +
 * rotation de la camera + trainee de particules) a chaque clic droit.
 * <p>
 * Aucun resource pack n'est utilise : l'effet "clignement" est simule avec de
 * courtes impulsions de Cecite (Blindness), et la rotation de camera est une
 * teleportation sur place avec un nouvel angle de vue. Fonctionne donc a
 * l'identique sur Java et sur Bedrock (via Geyser).
 */
public final class DiamondHoeService {

    private static final long MANUAL_REROLL_COOLDOWN_WINDOW_MILLIS = 60L * 60L * 1000L; // 1 heure
    private static final int MANUAL_REROLL_MAX_PER_WINDOW = 2;

    private final IronAgePlugin plugin;
    private final HoeTargetStorage storage;

    public DiamondHoeService(IronAgePlugin plugin, HoeTargetStorage storage) {
        this.plugin = plugin;
        this.storage = storage;
    }

    /** Appele au clic droit avec la houe : verrouille une cible si aucune n'est
     * active, sinon rejoue simplement l'effet de pointage vers la cible actuelle. */
    public void onRightClick(Player user) {
        UUID currentTargetId = storage.getTarget();
        Player currentTarget = currentTargetId == null ? null : Bukkit.getPlayer(currentTargetId);

        if (currentTarget == null || !currentTarget.isOnline()) {
            currentTarget = pickRandomTarget(user);
            if (currentTarget == null) {
                user.sendMessage("\u00A7eAucune autre cible disponible en ligne pour le moment.");
                return;
            }
            storage.setTarget(currentTarget.getUniqueId());
        }

        pointTowards(user, currentTarget);
    }

    /** Reroll manuel via /diamondhoe (limite) ou /diamondhoe op (illimite). */
    public boolean manualReroll(Player user, boolean unlimited) {
        long now = System.currentTimeMillis();

        if (!unlimited) {
            List<Long> recent = storage.getRerollTimestamps().stream()
                    .filter(t -> now - t < MANUAL_REROLL_COOLDOWN_WINDOW_MILLIS)
                    .collect(Collectors.toList());
            if (recent.size() >= MANUAL_REROLL_MAX_PER_WINDOW) {
                user.sendMessage("\u00A7cLimite atteinte : 2 changements de cible maximum par heure.");
                return false;
            }
        }

        Player newTarget = pickRandomTarget(user);
        if (newTarget == null) {
            user.sendMessage("\u00A7eAucune autre cible disponible en ligne pour le moment.");
            return false;
        }

        storage.setTarget(newTarget.getUniqueId());
        storage.addRerollTimestamp(now);
        pointTowards(user, newTarget);
        return true;
    }

    /** A appeler quand un joueur se deconnecte ou meurt : si c'etait la cible
     * verrouillee, on retire le verrou pour forcer un nouveau tirage au prochain clic. */
    public void handleTargetUnavailable(UUID playerId) {
        UUID currentTarget = storage.getTarget();
        if (currentTarget != null && currentTarget.equals(playerId)) {
            storage.setTarget(null);
        }
    }

    private Player pickRandomTarget(Player user) {
        List<Player> candidates = Bukkit.getOnlinePlayers().stream()
                .filter(p -> !p.getUniqueId().equals(user.getUniqueId()))
                .collect(Collectors.toList());
        if (candidates.isEmpty()) {
            return null;
        }
        return candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
    }

    private boolean isDisguised(Player target) {
        if (target.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
            return true;
        }
        var helmet = target.getInventory().getHelmet();
        if (helmet == null) {
            return false;
        }
        Material type = helmet.getType();
        return type == Material.CARVED_PUMPKIN || type.name().endsWith("_HEAD") || type.name().endsWith("_SKULL");
    }

    private void pointTowards(Player user, Player target) {
        if (isDisguised(target)) {
            user.sendMessage("\u00A7eCible temporairement introuvable.");
            return;
        }

        // Sequence sur ~2 secondes (40 ticks) : l'oeil se ferme (cecite courte),
        // la vue tourne vers la cible, l'oeil se rouvre avec une trainee de
        // particules, puis un dernier clignement marque la fin de l'effet.
        new BukkitRunnable() {
            int tick = 0;

            @Override
            public void run() {
                if (!user.isOnline() || !target.isOnline()) {
                    cancel();
                    return;
                }

                switch (tick) {
                    case 0 -> user.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 12, 0, false, false, false));
                    case 10 -> rotateTowards(user, target);
                    case 20 -> spawnTrail(user, target);
                    case 30 -> user.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 10, 0, false, false, false));
                    default -> { }
                }

                tick++;
                if (tick > 40) {
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void rotateTowards(Player user, Player target) {
        Location eye = user.getEyeLocation();
        Location targetLoc = target.getEyeLocation();
        var direction = targetLoc.toVector().subtract(eye.toVector());
        Location newLoc = user.getLocation().clone();
        newLoc.setDirection(direction);
        user.teleport(newLoc);
    }

    private void spawnTrail(Player user, Player target) {
        Location from = user.getEyeLocation();
        Location to = target.getEyeLocation();
        var direction = to.toVector().subtract(from.toVector());
        double length = direction.length();
        if (length < 0.01) {
            return;
        }
        direction.normalize();

        double step = 0.5;
        for (double distance = 0; distance < length; distance += step) {
            Location point = from.clone().add(direction.clone().multiply(distance));
            user.spawnParticle(Particle.WITCH, point, 1, 0, 0, 0, 0);
        }
    }
}
