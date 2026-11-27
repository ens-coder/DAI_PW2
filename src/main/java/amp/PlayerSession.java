package amp;

/**
 * Mémorise et traite une session
 * avec un client et une salle en outre elle
 * découpe chaque ligne reçue, vérifie l'ordre des messages (JOIN avant
 * BID) et
 * appelle la salle des ventes
 *
 * (une instance par connexion)
 *
 * Comportement du protocole amp : voir docs/protocol.md
 */
public class PlayerSession {
    private final TradeRoom room;
    private String name;

    /**
     * @param room la salle des ventes, partagée par toutes les sessions
     * @throws IllegalArgumentException si room est null
     */
    public PlayerSession(TradeRoom room) {
        if (room == null) {
            throw new IllegalArgumentException("Salle des ventes absente");
        }
        this.room = room;
    }

    /**
     * Traite une ligne reçue du client
     *
     * @param line la ligne, sans le caractère de fin de ligne
     * @return la ligne de réponse à renvoyer au client
     */
    public String handle(String line) {
        return process(line).line();
    }

    /** @return true si le client a rejoint la vente */
    public boolean hasJoined() {
        return name != null;
    }

    /** @return le nom du client, ou null s'il n'a pas rejoint */
    public String name() {
        return name;
    }

    // traite les commandes selon le protocole amp
    private AmpReply process(String line) {
        if (line == null) {
            return AmpReply.UNKNOWN;
        }

        //on tokenize
        String[] parts = line.split(" ");

        // nombre de token incorrecte
        if (parts.length != 2) {
            return AmpReply.UNKNOWN;
        }
        String param = parts[1];

        //on vérifie et traite les tokens
        switch (parts[0]) {
            case "JOIN":
                if (name != null) {
                    return AmpReply.ALREADY_JOINED;
                }

                //récupère réponse de la commande JOIN
                AmpReply reply = room.join(param);
                if (reply == AmpReply.OK) {
                    name = param;
                }
                return reply;

            //similaire à join mais avec montant
            case "BID":
                long amount;
                try {
                    amount = Long.parseLong(param);
                } catch (NumberFormatException e) {
                    return AmpReply.UNKNOWN;
                }
                if (amount < 0) {
                    return AmpReply.UNKNOWN;
                }
                if (name == null) {
                    return AmpReply.NOT_JOINED;
                }
                return room.bid(name, amount);

            default:
                return AmpReply.UNKNOWN;
        }
    }
}