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
            if(conn==null || conn.isClosed()){
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
    
    public Boolean InserisciUser(String nome,String cognome, String email,String password,int id_company,String tipo_utente) {
    	try {
    		 if(conn!=null || conn.isClosed()){
                 avviaConnessione();
             }
    		 String query=null;
    		 System.out.println("Iserisci User | Tipo Utente: "+tipo_utente);
             if(tipo_utente.equals("employee")){
                query="INSERT INTO employees (nome,cognome,email,password,id_company) VALUES (?,?,?,?,?)";
             }else{
                query="INSERT INTO manager (nome,cognome,email,password,id_company) VALUES (?,?,?,?,?)";
             }
             
    		 PreparedStatement pstmt = conn.prepareStatement(query);
    		 pstmt.setString(1, nome);
    		 pstmt.setString(2, cognome);
    		 pstmt.setString(3, email);
    		 pstmt.setString(4,password);
    		 pstmt.setInt(5, id_company);
    		 int numRows = pstmt.executeUpdate();
    		 return numRows>0;

    		}catch (Exception e) {
    	            System.err.println("Errore nella esecuzione della query: ");
    	            e.printStackTrace();
    	            return false;
    	        }
    	}
    
    public Boolean InserisciTask(String titolo,String descrizione,String stato, String priorita,String scadenza, int idEmployee, int idManager) {
    	try {
    		 if(conn!=null || conn.isClosed()){
                 avviaConnessione();
             }
    		 String query=null;
             query="INSERT INTO attivita (titolo,descrizione,stato,priorita,scadenza,id_employee,id_manager) VALUES (?,?,?,?,?,?,?)";
            
             
    		 PreparedStatement pstmt = conn.prepareStatement(query);
    		 pstmt.setString(1, titolo);
    		 pstmt.setString(2, descrizione);
    		 pstmt.setString(3,stato);
    		 pstmt.setString(4, priorita);
    		 pstmt.setString(5, scadenza);
    		 pstmt.setInt(6, idEmployee);
    		 pstmt.setInt(7, idManager);
    		 
    		 int numRows = pstmt.executeUpdate();
    		 return numRows>0;

    		}catch (Exception e) {
    	            System.err.println("Errore nella esecuzione della query: ");
    	            e.printStackTrace();
    	            return false;
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
