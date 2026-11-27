package ch.heigvd;

import amp.Client;
import amp.Srv;
import amp.TradeRoom;
import java.util.List;

/**
 TEMPORAIRE
 A IMPLEMENTER AVEC PICOCLI
 */
public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length == 1 && args[0].equals("server")) {
            TradeRoom room =
                    new TradeRoom(
                            new String[] {"PS5", "chemise à carreaux", "orange"},
                            new long[] {300, 20, 1},
                            List.of("alice", "bob", "carol"));
            Srv server = new Srv(Srv.DEFAULT_PORT, room);
            server.run();
        } else if (args.length == 1 && args[0].equals("client")) {
            Client client = new Client("localhost", Srv.DEFAULT_PORT);
            client.run();
        } else {
            System.out.println("Usage : java Main server | java Main client");
        }
    }
}