package ServerSide;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import Shared.Richiesta;
import Shared.Risposta;

public class GestoreRichiesta {

    public static Risposta process(Richiesta richiesta) {
        String tipo = richiesta.getTipo();

        switch (tipo) {
            case "SING-UP":
                // Logica registrazione utente
                return new Risposta("OK", "Account creato");

            case "LOGIN":
                Database DB = new Database();
                DB.avviaConnessione();

                Object parametro1 = richiesta.getParametro("username");
                Object parametro2 = richiesta.getParametro("password");

                if (parametro1 != null) {
                    System.out.println("Tipo parametro1: " + parametro1.getClass().getName());
                }
                if (parametro2 != null) {
                    System.out.println("Tipo parametro2: " + parametro2.getClass().getName());
                }

                String username = null, password = null;
                if (parametro1 instanceof String && parametro2 instanceof String) {
                    username = (String) parametro1;
                    password = (String) parametro2;

                    if (username == null || password == null) {
                        return new Risposta("ERRORE", "Dati mancanti!");
                    }
                } else {
                    return new Risposta("ERRORE", "Formato dati non valido.");
                }

                String query = "SELECT * FROM Employees WHERE username='" + username + "' AND password='" + password
                        + "'";
                ResultSet result = DB.eseguiQuery(query);

                if (result != null) {
                    int count = 0;
                    int id;

                    try {
                        while (result.next()) {
                            id = result.getInt("id");
                            count++;
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                        return new Risposta("ERRORE", "Errore nella lettura del risultato.");
                    }

                    if (count == 1) {
                        return new Risposta("OK", "Accesso riuscito");
                    } else {
                        return new Risposta("ERRORE", "Utente non trovato o credenziali errate");
                    }
                } else {
                    return new Risposta("ERRORE", "Errore durante l'accesso al database.");
                }

            case "LOAD ATTIVITA":
                // logica creazione attività
                return new Risposta("OK", "Attività caricate");

            case "AGGIUNGI ATTIVITA":
                return new Risposta("OK", "Attività creata");

            case "LOGOUT":
                // logica gestione logout;
                return new Risposta("OK", "Utente disconesso");

            default:
                return new Risposta("ERRORE", "Richiesta non riconosciuta");
        }
    }
}
