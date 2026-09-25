package fr.ironage.plugin;

/**
 * Contient au maximum une action "en attente de confirmation" a la fois.
 * Utilise par /ironage (distribution, reset, chestgive) qui doivent tous
 * etre confirmes via /ironage confirm dans un delai donne.
 */
public final class PendingAction {

    private Runnable action;
    private String description;
    private long expiresAtMillis;

    public synchronized void set(Runnable action, String description, long durationMillis) {
        this.action = action;
        this.description = description;
        this.expiresAtMillis = System.currentTimeMillis() + durationMillis;
    }

    public synchronized boolean hasPending() {
        return action != null && System.currentTimeMillis() < expiresAtMillis;
    }

    public synchronized String getDescription() {
        return description;
    }

    public synchronized void confirmAndClear() {
        if (!hasPending()) {
            return;
        }
        Runnable toRun = action;
        clear();
        toRun.run();
    }

    public synchronized void clear() {
        action = null;
        description = null;
    }
}
