package ServerSide;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import Shared.Richiesta;
import Shared.Risposta;

public class GestoreRichiesta {

    public static Risposta process(Richiesta richiesta) {
        String tipo = richiesta.getTipo();
        Database DB=new Database();

        switch (tipo) {
            case "SING-UP":
            	DB.avviaConnessione();
            	
            	String[] keys= {"username","email","password","attivita","tipo utente"};
            	Object[] parametri = leggiParametri(5,keys,richiesta);    
            	            	
            	String username=null, password=null,email=null,attivita=null,tipo_utente=null;
            	
            	for(int i=0;i<5;i++) {
            		if(parametri[i] instanceof String){
            			switch (i) {
	            			case 0:
	        					username=(String) parametri[i];
	        					break;
	        				case 1:
	        					email=(String) parametri[i];
	        					break;
	        				case 2: 
	        					password=(String) parametri[i];
	        					break;
	        				case 3:
	        					attivita=(String) parametri[i];
	        					break;
	        				case 4:
	        					tipo_utente=(String) parametri[i];
	        					break;
            			}
            		}else{
            			return new Risposta("ERRORE", "Formato dati non valido.");
            		}
            	}
            	
            	if(username==null || email==null || password==null || attivita==null || tipo_utente==null) {
            		return new Risposta("ERRORE", "Dati mancanti!");
            	}
            	System.out.println(tipo_utente);
            	if(!tipo_utente.equals("employee") && !tipo_utente.equals("manager")) {
            		return new Risposta("ERRORE", "Tipo Utente non valido!");
            	}
            	// Controllo se l'attività esiste è mi prendo l'id
            	
            	String query="SELECT * FROM company WHERE nome='"+attivita+"'";
            	ResultSet result = DB.eseguiQuery(query);

                if (result != null) {
                    int count = 0;
                    int id= 0;
                    try {
                        while (result.next()) {
                            id = (int) result.getInt("id");
                            count++;
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                        return new Risposta("ERRORE", "Errore nella lettura del risultato.");
                    }

                    if (count == 1) {
                    	
                    	// Controllo se l'utente è già presente nel db
                    	
                    	switch (tipo_utente){
                    		case "employee":
                    			query="SELECT * FROM employees WHERE email='"+email"' OR username='"+username"'";
                    			break;
                    		case "manager":
                    			query="SELECT * FROM employees WHERE email='"+email"' OR username='"+username"'";
                    			break;
                    	}
                    	result=DB.eseguiQuery(query);
            			
           			 	if (result != null) {
                            count = 0;
                            try {
                                while (result.next()) {
                                    count++;
                                }
                                System.out.println(count);
                            } catch (SQLException e) {
                                e.printStackTrace();
                                return new Risposta("ERRORE", "Errore nella lettura del risultato.");
                            }
                    	/*if(DB.InserisciUser(username, email, password, id, tipo_utente)) {
                    		return new Risposta("OK", "Account creato");
                        }else {
                        	return new Risposta("ERRORE","Errore durante la creazione del utente!");
                        }*/                   	
                    } else {
                        return new Risposta("ERRORE", "Attivita non trovata");
                    }
                } else {
                    return new Risposta("ERRORE", "Errore durante l'accesso al database.");
                }
            	

            case "LOGIN":
            	//System.out.println("LOGIN");
                /*DB.avviaConnessione();

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
                }*/

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
    
    private static Object[] leggiParametri(int numberOfParametri,String[] keys,Richiesta request) {
    	Object[] parametri = new Object[numberOfParametri];
    	int j=0;
    	
    	for(int i=0;i<numberOfParametri;i++) {
    		parametri[i]=request.getParametro(keys[j]);
    		j++;
    	}
    	return parametri;
    }
    
    private static void stampaParametri(final Object[] parametri, final int numberOfParametri) {
    	int j=0;
    	for(int i=0;i<numberOfParametri;i++) {
    		System.out.println(parametri[i]);
    	}    	
    }
}
