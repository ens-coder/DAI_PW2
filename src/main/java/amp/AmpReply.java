package amp;

/**
 * Réponses du protocole AMP (Auction Message Protocol) possibles
 * encapsulée dans une classe enum
 *
 * Pour plus d'informations concernant le protocole voir docs/protocol.md.
 */
public enum AmpReply {
    OK("OK", "requête acceptée"),
    UNKNOWN("ERROR -1", "commande inconnue ou paramètre invalide"),
    NOT_JOINED("ERROR -2", "vous devez d'abord rejoindre la vente (JOIN <nom>)"),
    NAME_TAKEN("ERROR 1", "ce nom est déjà utilisé"),
    ALREADY_JOINED("ERROR 2", "vous avez déjà rejoint la vente"),
    NOT_A_PLAYER("ERROR 3", "ce nom ne figure pas dans la liste des joueurs"),
    NO_ROUND("ERROR 4", "aucun objet n'est en vente pour le moment"),
    BID_TOO_LOW("ERROR 5", "offre inférieure au prix de base");

    private final String line;
    private final String description;

    AmpReply(String line, String description) {
        this.line = line;
        this.description = description;
    }

    /** @return la ligne envoyée au client, sans fin de ligne */
    public String line() {
        return line;
    }

    /** @return l'explication à afficher à l'utilisateur */
    public String description() {
        return description;
    }

    /**
     * Retrouve la réponse correspondant à une ligne reçue
     *
     * @param line ligne reçue du serveur
     * @return la réponse, ou null si la ligne n'en est pas une
     */
    public static AmpReply fromLine(String line) {
        for (AmpReply reply : values()) {
            if (reply.line.equals(line)) {
                return reply;
            }
        }
        return null;
    }
}