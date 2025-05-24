package ServerSide;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.net.Socket;

import Shared.Richiesta;
import Shared.Risposta;

public class GestoreClient implements Runnable {

    private Socket client;
    private ObjectInputStream inStream;
    private ObjectOutputStream outStream;

    public GestoreClient(Socket client) {
        this.client = client;
    }

    @Override
    public void run() {
        try {
            outStream = new ObjectOutputStream(client.getOutputStream());
            inStream = new ObjectInputStream(client.getInputStream());

            while (true) {
                Richiesta richiesta = (Richiesta) inStream.readObject();
                Risposta risposta = GestoreRichiesta.process(richiesta);
                outStream.writeObject(risposta);
                outStream.flush();
            }

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Client disconnesso: " + e.getMessage());
        } finally {
            try {
                client.close();
            } catch (IOException e) {
                System.err.println("Errore nella chiusura della connessione");
            }
        }
    }
}
