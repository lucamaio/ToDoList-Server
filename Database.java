package ServerSide;

import java.sql.*;
import Shared.*;

public class Database {

    private DatabaseMenager configurazione;
    private int porta;
    private String url, user, password;
    private Connection conn;

    public Database() {
        this.configurazione = new DatabaseMenager();
        this.url = configurazione.getDbUrl();
        this.user = configurazione.getDbUsername();
        this.porta = configurazione.getPortaServer();
        this.password = configurazione.getDbPassword();
    }

    public void avviaConnessione() {
        try {
            conn = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            System.err.println("Errore nella connessione al database:");
            e.printStackTrace();
        }
    }

    public ResultSet eseguiQuery(String query) {
        try {
            if (conn == null || conn.isClosed()) {
                avviaConnessione();
            }
            Statement stmt = conn.createStatement();
            return stmt.executeQuery(query);
        } catch (Exception e) {
            System.err.println("Errore nell'esecuzione della query:");
            e.printStackTrace();
            return null;
        }
    }

    public Boolean inserisciUser(String nome, String cognome, String email, String password, int id_department, String tipo_utente) {
        PreparedStatement pstmt = null;
        try {
            if (conn == null || conn.isClosed()) {
                avviaConnessione();
            }
            String query;
            if ("employee".equals(tipo_utente)) {
                query = "INSERT INTO employee (nome, cognome, email, password, id_department) VALUES (?,?,?,?,?)";
            } else {
                query = "INSERT INTO manager (nome, cognome, email, password, id_department) VALUES (?,?,?,?,?)";
            }

            pstmt = conn.prepareStatement(query);
            pstmt.setString(1, nome);
            pstmt.setString(2, cognome);
            pstmt.setString(3, email);
            pstmt.setString(4, password);
            pstmt.setInt(5, id_department);

            int numRows = pstmt.executeUpdate();
            return numRows > 0;
        } catch (Exception e) {
            System.err.println("Errore nell'esecuzione della query:");
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public int verificaEsistenzaUtente(String tipoUtente, String cognome, String nome) {
        PreparedStatement stmt = null;
        ResultSet result = null;

        try {
            if (conn == null || conn.isClosed()) {
                avviaConnessione();
            }

            if (!tipoUtente.equals("employee") && !tipoUtente.equals("manager") && !tipoUtente.equals("admin")) {
                throw new IllegalArgumentException("Tipo utente non valido");
            }

            String query = "SELECT id FROM " + tipoUtente + " WHERE cognome = ? AND nome = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, cognome);
            stmt.setString(2, nome);

            result = stmt.executeQuery();

            if (result.next()) {
                return result.getInt("id");
            } else {
                return 0;
            }
        } catch (SQLException e) {
            System.err.println("Errore nell'esecuzione della query:");
            e.printStackTrace();
            return -1;
        } finally {
            try {
                if (result != null) result.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    public int verificaEsistenzaDipartimento(String nomeDipartimento) {
    	PreparedStatement stmt = null;
		ResultSet result = null;
		try {
            if (conn == null || conn.isClosed()) {
                avviaConnessione();
            }

            if (nomeDipartimento == null) {
                throw new IllegalArgumentException("Errore: il nome del dipartimento è null.");
            }

            String query = "SELECT id FROM department WHERE nome = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, nomeDipartimento);

            result = stmt.executeQuery();

            if (result.next()) {
                return result.getInt("id");
            } else {
                return 0;
            }
        } catch (SQLException e) {
            System.err.println("Errore nell'esecuzione della query:");
            e.printStackTrace();
            return -1;
        } finally {
            try {
                if (result != null) result.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    public int verificaPasswordDipartimento(String passw) {
    	PreparedStatement stmt = null;
		ResultSet result = null;
		try {
            if (conn == null || conn.isClosed()) {
                avviaConnessione();
            }

            if (passw == null) {
                throw new IllegalArgumentException("Errore: il nome del dipartimento è null.");
            }

            String query = "SELECT id FROM department WHERE password = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, passw);

            result = stmt.executeQuery();

            if (result.next()) {
                return result.getInt("id");
            } else {
                return 0;
            }
        } catch (SQLException e) {
            System.err.println("Errore nell'esecuzione della query:");
            e.printStackTrace();
            return -1;
        } finally {
            try {
                if (result != null) result.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public Boolean creaTask(String titolo, String descrizione, String stato, String priorita, String scadenza, Integer idGeneric, Integer idManager,String tipo) {
        PreparedStatement pstmt = null;
        try {
            if (conn == null || conn.isClosed()) {
                avviaConnessione();
            }
            String query=null;
           switch(tipo) {
            	case "employee-task":
	            	query="INSERT INTO attivita (titolo, descrizione, stato, priorita, scadenza, id_employee, id_manager) VALUES (?,?,?,?,?,?,?)";break;
            	case "department-task":
            		query="INSERT INTO attivita (titolo, descrizione, stato, priorita, scadenza, id_department, id_manager) VALUES (?,?,?,?,?,?,?)";break;
            	default:
            		System.err.println("Tipo attività non riconosciuto: " + tipo);
            		return false;
            }
            pstmt = conn.prepareStatement(query);
            pstmt.setString(1, titolo);
        	pstmt.setString(2, descrizione);
        	pstmt.setString(3, stato);
        	pstmt.setString(4, priorita);
        	pstmt.setString(5, scadenza);
        	pstmt.setInt(6, idGeneric);
        	pstmt.setInt(7, idManager);
        	int numRows = pstmt.executeUpdate();
            
        	return numRows > 0;
          
        } catch (Exception e) {
            System.err.println("Errore nell'esecuzione della query:");
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public Boolean updateStatoAttivita(int id, String stato) {
        PreparedStatement pstmt = null;
        try {
            if (conn == null || conn.isClosed()) {
                avviaConnessione();
            }
            String query = "UPDATE attivita SET stato=? WHERE id=?";
            pstmt = conn.prepareStatement(query);
            pstmt.setString(1, stato);
            pstmt.setInt(2, id);

            int righeModificate = pstmt.executeUpdate();
            return righeModificate > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (pstmt != null) pstmt.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public Boolean updateAttivita(Attivita attivita) {
        PreparedStatement pstmt = null;
        String query=null;
        try {
            if (conn == null || conn.isClosed()) {
                avviaConnessione();
            }
            if(attivita.getIdEmployee()!=null) {
            	 query = "UPDATE attivita SET titolo=?, descrizione=?, scadenza=?, stato=?, priorita=?, id_employee=? WHERE id=?";
            	 pstmt = conn.prepareStatement(query);
                 pstmt.setString(1, attivita.getTitolo());
                 pstmt.setString(2, attivita.getDescrizione());
                 pstmt.setString(3, attivita.getDataScadenza());
                 pstmt.setString(4, attivita.getStato().toString());
                 pstmt.setString(5, attivita.getPriorita().toString());
                 pstmt.setInt(6, attivita.getIdEmployee());
                 pstmt.setInt(7, attivita.getId());
            }else if(attivita.getIdDepartment()!=null) {
            	query = "UPDATE attivita SET titolo=?, descrizione=?, scadenza=?, stato=?, priorita=?, id_department=? WHERE id=?";
           	 	pstmt = conn.prepareStatement(query);
                pstmt.setString(1, attivita.getTitolo());
                pstmt.setString(2, attivita.getDescrizione());
                pstmt.setString(3, attivita.getDataScadenza());
                pstmt.setString(4, attivita.getStato().toString());
                pstmt.setString(5, attivita.getPriorita().toString());
                pstmt.setInt(6, attivita.getIdDepartment());
                pstmt.setInt(7, attivita.getId());
            }else {
            	throw new Exception("ID Emplyee e ID Department sono null");
            }
                      
            int righeModificate = pstmt.executeUpdate();
            return righeModificate > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (pstmt != null) pstmt.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    public Boolean updateAzienda(Company azienda) {
        PreparedStatement pstmt = null;
        try {
            if (conn == null || conn.isClosed()) {
                avviaConnessione();
            }
            String query = "UPDATE company SET nome=?, descrizione=?, indirizzo=?, telefono=?, email=? WHERE id=?";
            pstmt = conn.prepareStatement(query);
            pstmt.setString(1, azienda.getNome());
            pstmt.setString(2, azienda.getDescrizione());
            pstmt.setString(3, azienda.getIndirizzo());
            pstmt.setString(4, azienda.getTelefono());
            pstmt.setString(5, azienda.getEmail());
            pstmt.setInt(6, azienda.getId());

            int righeModificate = pstmt.executeUpdate();
            return righeModificate > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (pstmt != null) pstmt.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void chiudiConnessione() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
                System.out.println("Connessione con il DB chiusa");
            }
        } catch (Exception e) {
            System.err.println("Errore nella chiusura del DB:");
            e.printStackTrace();
        }
    }
} 