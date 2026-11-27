package amp;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Salle des ventes :
 * joueurs présents et
 * vente de plusieurs objets, un par round
 * Utilisable par plusieurs threads
 *
 * Comportement détaillé :
 * voir docs/protocol.md.
 */
public class TradeRoom {
    public static final String RESULT_NONE = "RESULT NONE";
    public static final String END = "END";

    private final String[] items;
    private final long[] basePrices;

    private final List<String> players;
    private final Set<String> present = ConcurrentHashMap.newKeySet();
    private final ConcurrentHashMap<String, Long> bids = new ConcurrentHashMap<>();
    private final AtomicBoolean open = new AtomicBoolean(false);
    private final AtomicInteger current = new AtomicInteger(-1);

    /**
     * @param items objets à vendre, dans l'ordre des rounds
     * @param basePrices prix de base, même indice que l'objet
     * @param players joueurs autorisés, dans l'ordre qui départage les égalités
     * @throws IllegalArgumentException si un paramètre est invalide
     */
    public TradeRoom(String[] items, long[] basePrices, List<String> players) {
        if (items == null || basePrices == null || items.length == 0
                || items.length != basePrices.length) {
            throw new IllegalArgumentException("Objets et prix invalides");
        }
        if (players == null || players.isEmpty()) {
            throw new IllegalArgumentException("Aucun joueur");
        }
        this.items = items.clone();
        this.basePrices = basePrices.clone();
        this.players = List.copyOf(players);
    }

    /**
     * Fait entrer un joueur dans la salle
     *
     * @param name nom du joueur
     * @return OK, NOT_A_PLAYER ou NAME_TAKEN
     */
    public AmpReply join(String name) {
        if (!players.contains(name)) {
            return AmpReply.NOT_A_PLAYER;
        }
        if (!present.add(name)) {
            return AmpReply.NAME_TAKEN;
        }
        return AmpReply.OK;
    }

    /** @return le nombre de joueurs de la liste */
    public int playerCount() {
        return players.size();
    }

    /** @return true si tous les joueurs de la liste ont rejoint */
    public boolean allPresent() {
        return present.size() == players.size();
    }

    /** @return true si chaque joueur a fait au moins une offre dans le round en cours */
    public boolean allBid() {
        return bids.size() == players.size();
    }

    /** @return true s'il reste un objet à mettre en vente */
    public boolean hasNextItem() {
        return current.get() + 1 < items.length;
    }

    /**
     * Ouvre le round de l'objet suivant
     *
     * @return la ligne "ROUND <basePrice> <item>" à diffuser, ou null s'il n'y a plus d'objet
     */
    public String nextRound() {
        if (!hasNextItem()) {
            return null;
        }
        bids.clear();
        current.incrementAndGet();
        open.set(true);
        return currentRound();
    }

    /** @return la ligne "ROUND" du round en cours, ou null si aucun round n'est ouvert */
    public String currentRound() {
        int i = current.get();
        return open.get() ? "ROUND " + basePrices[i] + " " + items[i] : null;
    }

    /**
     * Enregistre une offre
     * remarque: seule offre la plus haute de chaque joueur est gardée
     *
     * @param name nom du joueur
     * @param amount montant proposé
     * @return OK, NO_ROUND ou BID_TOO_LOW
     */
    public AmpReply bid(String name, long amount) {
        if (!open.get()) {
            return AmpReply.NO_ROUND;
        }
        if (amount < basePrices[current.get()]) {
            return AmpReply.BID_TOO_LOW;
        }
        // met le montant associé au nom utilisateur (unique)
        Long previous = bids.get(name);
        if (previous == null || amount > previous) {
            bids.put(name, amount);
        }
        return AmpReply.OK;
    }

    /**
     * Ferme le round en cours, avec ou sans offre ; en cas d'égalité, le premier de la liste des
     * joueurs gagne
     *
     * @return "RESULT <winner> <amount>", ou "RESULT NONE" si aucune offre
     */
    public String closeRound() {
        open.set(false);
        String winner = null;
        long best = -1;
        for (String player : players) {
            Long amount = bids.get(player);
            if (amount != null && amount > best) {
                winner = player;
                best = amount;
            }
        }
        return winner == null ? RESULT_NONE : "RESULT " + winner + " " + best;
    }
}