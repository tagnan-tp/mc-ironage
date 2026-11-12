package fr.ironage.plugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;

/**
 * Decompte de 10 secondes affiche A L'ECRAN de tous les joueurs en ligne avant la distribution
 * (/ironage). Les chiffres "grossissent" par paliers, car un titre Minecraft ne peut pas etre
 * agrandi librement sans resource pack :
 * - 10 a 7 : petit (sous-titre) ;
 * - 6 a 4  : moyen (titre) ;
 * - 3, 2, 1 : tres gros (chiffres pleine largeur, ~2x plus hauts) avec une decoration qui
 *   s'elargit a chaque seconde (" 3 ", "  2  ", "   1   "), et un son de plus en plus aigu.
 * Les chiffres pleine largeur se desactivent avec "countdown-big-digits: false" (config.yml).
 */
public final class DistributionCountdown {

    private static final int START_SECONDS = 10;
    private static final Title.Times TIMES =
            Title.Times.times(Duration.ZERO, Duration.ofMillis(900), Duration.ofMillis(100));

    private final IronAgePlugin plugin;
    private volatile boolean running = false;

    public DistributionCountdown(IronAgePlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isRunning() {
        return running;
    }

    /** Lance le decompte ; onFinish (la distribution) est execute juste apres le "1". */
    public void start(Runnable onFinish) {
        if (running) {
            return;
        }
        running = true;

        new BukkitRunnable() {
            int remaining = START_SECONDS;

            @Override
            public void run() {
                if (remaining <= 0) {
                    cancel();
                    running = false;
                    announceStart();
                    onFinish.run();
                    return;
                }
                showStep(remaining);
                remaining--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void showStep(int n) {
        boolean wide = plugin.getConfig().getBoolean("countdown-big-digits", true);
        Title title;

        if (n >= 7) {
            title = Title.title(Component.empty(), bold(String.valueOf(n), NamedTextColor.GREEN), TIMES);
        } else if (n >= 4) {
            NamedTextColor color = n >= 5 ? NamedTextColor.YELLOW : NamedTextColor.GOLD;
            title = Title.title(bold(String.valueOf(n), color), Component.empty(), TIMES);
        } else {
            String digit = wide ? wideDigit(n) : String.valueOf(n);
            String left = "\u00BB".repeat(4 - n);
            String right = "\u00AB".repeat(4 - n);
            NamedTextColor color = n == 3 ? NamedTextColor.GOLD
                    : n == 2 ? NamedTextColor.RED : NamedTextColor.DARK_RED;
            title = Title.title(bold(left + " " + digit + " " + right, color), Component.empty(), TIMES);
        }

        float pitch = 0.5f + (START_SECONDS - n) * 0.15f;
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.showTitle(title);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, pitch);
        }
    }

    private void announceStart() {
        Title title = Title.title(
                bold("ARTEFACTS !", NamedTextColor.AQUA),
                Component.text("Distribution en cours...", NamedTextColor.GRAY),
                Title.Times.times(Duration.ZERO, Duration.ofSeconds(2), Duration.ofMillis(500)));
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.showTitle(title);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        }
    }

    private Component bold(String text, NamedTextColor color) {
        return Component.text(text, color, TextDecoration.BOLD);
    }

    /** Chiffre pleine largeur (U+FF10 + n) : dessine en police 16 px, donc ~2x plus grand. */
    private String wideDigit(int n) {
        return String.valueOf((char) ('\uFF10' + n));
    }
}
