package amp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Serveur AMP :
 *
 * accepte un client par joueur (un thread du pool chacun),
 * puis déroule la vente
 * sur le thread principal (délai des rounds, diffusion de ROUND, RESULT et END)
 *
 *
 * Comportement protocole : voir
 * docs/protocol.md.
 */
public class Srv {
    public static final int DEFAULT_PORT = 6436;
    public static final long ROUND_DURATION_MS = 30_000;
    private static final long CHECK_INTERVAL_MS = 500;

    private final int port;
    private final TradeRoom room;
    private final Set<BufferedWriter> joined = ConcurrentHashMap.newKeySet();
    private final Set<Socket> sockets = ConcurrentHashMap.newKeySet();

    /**
     * @param port port d'écoute, entre 1024 et 65535
     * @param room la salle des ventes
     * @throws IllegalArgumentException si un paramètre est invalide
     */
    public Srv(int port, TradeRoom room) {
        if (port < 1024 || port > 65535) {
            throw new IllegalArgumentException("Port invalide : " + port);
        }
        if (room == null) {
            throw new IllegalArgumentException("Salle des ventes absente");
        }
        this.port = port;
        this.room = room;
    }

    /**
     * Démarre le serveur et déroule toute la vente, puis ferme les connexions
     *
     * @throws IOException si le port ne peut pas être ouvert
     * @throws InterruptedException si le thread principal est interrompu pendant une attente
     */
    public void run() throws IOException, InterruptedException {
        try (
                //nombre de joueurs fixe et connu à l'avance -> un thread pour chacun
                ExecutorService executor = Executors.newFixedThreadPool(room.playerCount());
                ServerSocket serverSocket = new ServerSocket(port)
        ) {
            System.out.println("Le serveur d'hôtel des ventes écoute sur le port " + port);
            for (int i = 0; i < room.playerCount(); i++) {
                Socket socket = serverSocket.accept();
                sockets.add(socket);
                //réservation d'un socket par tâche qui s'occupe d'un client
                executor.submit(new ClientHandler(socket));
            }
            runSale();
            for (Socket socket : sockets) {
                socket.close();
            }
        }
        System.out.println("Le serveur d'hôtel des ventes est arrêté");
    }

    /**
     * Démarre la vente :
     * attend tous les joueurs, puis un round par objet.
     * Un round se termine quand tous les joueurs ont fait une offre,
     *  ou au plus tard après ROUND_DURATION_MS
     */
    private void runSale() throws InterruptedException {
        //tant que tout les clients n'ont pas rejoint on attends
        while (!room.allPresent()) {
            //permet de temporiser et ne pas monopoliser le CPU
            Thread.sleep(CHECK_INTERVAL_MS);
        }
        long deadline;
        while (room.hasNextItem()) {
            broadcast(room.nextRound());
            deadline = System.currentTimeMillis() + ROUND_DURATION_MS;
            // on attends tant que tout le monde n'a pas miser ou que le délai n'est pas écoulé
            while (!room.allBid() && System.currentTimeMillis() < deadline) {
                Thread.sleep(CHECK_INTERVAL_MS);
            }
            broadcast(room.closeRound());
        }
        broadcast(TradeRoom.END);
    }

    /** Tâche qui s'occupe d'un client (une instance par client) */
    private class ClientHandler implements Runnable {
        private final Socket socket;

        ClientHandler(Socket socket) {
            this.socket = socket;
        }

        /** Lit chaque ligne du client, la passe à sa session, renvoie la réponse, puis ferme */
        @Override
        public void run() {
            String address = socket.getRemoteSocketAddress().toString();
            System.out.println("Le serveur d'hôtel des ventes: connexion de " + address);

            //création de session (mémoire action/état par participant) qui permet de lier une session ouverte à la structure room
            PlayerSession session = new PlayerSession(room);
            BufferedWriter out = null;
            try (
                    socket;
                    // ce qui arrive du client
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                    // ce qu'on envoi au client
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))
            ) {
                // permet de récupérer la référence du writer qui sera accessible dans le scope du finally
                out = writer;
                String line;

                boolean wasJoined;
                //gère les input du participant et les réponses associées relativement au protocole
                while ((line = in.readLine()) != null) {
                    wasJoined = session.hasJoined();
                    send(out, session.handle(line));

                    // juste après le JOIN réussi : le participant reçoit désormais les diffusions via broadcast et structure de données joined
                    if (!wasJoined && session.hasJoined()) {
                        joined.add(out);
                    }
                }
            } catch (IOException e) {
                System.out.println("Le serveur d'hôtel des ventes: connexion perdue avec " + address + " : " + e.getMessage());
            } finally {
                joined.remove(out);
                sockets.remove(socket);
                System.out.println("Le serveur d'hôtel des ventes: déconnexion de " + address);
            }
        }
    }

    /** Envoie une ligne à tous les clients qui ont rejoint */
    private void broadcast(String line) {
        System.out.println("Le serveur d'hôtel des ventes vous notifie de: " + line);
        for (BufferedWriter out : joined) {
            try {
                send(out, line);
            } catch (IOException e) {
                joined.remove(out);
            }
        }
    }

    private static void send(BufferedWriter out, String line) throws IOException {
        out.write(line + "\n");
        out.flush();
    }
}