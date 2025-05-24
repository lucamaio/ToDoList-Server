package ServerSide;

import java.sql.*;

public class Database {

    private DatabaseMenager configurazione;
    private int porta;
    private String url,user, password;
    private Connection conn;

    public Database(){
       this.configurazione = new DatabaseMenager();
       this.url=configurazione.getDbUrl();
       this.user=configurazione.getDbUsername();
       this.porta=configurazione.getPortaServer();
       this.password=configurazione.getDbPassword();
    }


    public void avviaConnessione(){
        System.out.println(url);
        System.out.println(password);
         try{
            conn = DriverManager.getConnection(url, user, password);
            System.out.println("Connessione stabilita con il DB");
         }
         catch (SQLException e) {
            System.err.println("Errore nella connessione al database:");
            e.printStackTrace();
        }
    }

    public ResultSet eseguiQuery(String query){
        try {
            if(conn!=null || conn.isClosed()){
                avviaConnessione();
            }
            Statement stmt = conn.createStatement();
            ResultSet result=stmt.executeQuery(query);
            return result;
        } catch (Exception e) {
            System.err.println("Errore nella esecuzione della query: ");
            e.printStackTrace();
            return null;
        }
    }

    public void chiudiConnessione(){
        try{
            if(conn!=null && !conn.isClosed()){
                conn.close();
                System.out.println("Connessione con il DB chiusa");
            }
        } catch(Exception e){
            System.err.println("Errore nella chiusura del DB: ");
            e.printStackTrace();
        }
    }
}
