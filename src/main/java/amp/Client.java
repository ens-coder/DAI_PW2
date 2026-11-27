package amp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Client AMP :
 * REPL au clavier
 * et un second thread qui affiche ce que le serveur envoie
 *
 * (voir docs/protocol.md)
 */
public class Client {
    private final String host;
    private final int port;

    /**
     * @param host nom ou adresse du serveur
     * @param port port du serveur
     */
    public Client(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * Se connecte, puis envoie chaque commande tapée jusqu'à QUIT
     *
     * @throws IOException si la connexion échoue ou est perdue
     */
    public void run() throws IOException {

        try (
                ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
                Socket socket = new Socket(host, port);

                BufferedReader in = new BufferedReader( new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
                BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8))) {

            System.out.println("Connexion validée ! Commandes : JOIN <nom>, BID <montant>, QUIT");

            //permet de séparer le thread principale (ici lecture clavier) de ce qu'on reçoit du serveur (méthode listen())
            executor.submit(() -> listen(in));

            String command;

            //si on déconnecte l'entrée standard ou que entrée clavier = QUIT on sort sinon on envoi entrée clavier au serveur
            while ((command = keyboard.readLine()) != null && !command.equals("QUIT")) {
                out.write(command + "\n");
                out.flush();
            }
        }
    }

    /** Affiche chaque ligne reçue du serveur, jusqu'à la fermeture de la connexion */
    private static void listen(BufferedReader in) {
        try {
            String line;
            AmpReply reply;
            while ((line = in.readLine()) != null) {
                reply = AmpReply.fromLine(line);

                if (reply != null && reply != AmpReply.OK) {
                    System.out.println("Erreur : " + reply.description());
                } else {
                    System.out.println(line);
                }
            }
        } catch (IOException e) {
            // connexion fermée
            System.out.println("Erreur client connexion");
        }
        // car readLine est null donc indique fin de connexion
        System.out.println("Connexion terminée !");
    }
}