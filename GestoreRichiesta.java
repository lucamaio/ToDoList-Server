package ServerSide;


import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

import Shared.*;


public class GestoreRichiesta {


	public static Risposta process(Richiesta richiesta) {
        String tipo = richiesta.getTipo();
        Database DB=new Database();
        String query;
        ResultSet result;
        Risposta answer=null;
        String nome=null,cognome=null, password=null,email=null,attivita=null,tipo_utente=null;
        String titolo=null,descrizione=null,statoStr=null,prioritaStr=null;
		String scadenza=null;
        int id=-1;
        switch (tipo) {
            case "SING-UP":
            	DB.avviaConnessione();
            	
            	String[] keysSingUP= {"nome","cognome","email","password","attivita","tipo utente"};
            	Object[] parametriSingUP = leggiParametri(6,keysSingUP,richiesta);    
            	            	
            	for(int i=0;i<6;i++) {
            		if(parametriSingUP[i] instanceof String){
            			switch (i) {
	            			case 0:
	        					nome=(String) parametriSingUP[i]; break;
	        				case 1:
	        					cognome=(String) parametriSingUP[i]; break;
	        				case 2:
	        					email=(String) parametriSingUP[i]; break;
	        				case 3: 
	        					password=(String) parametriSingUP[i]; break;
	        				case 4:
	        					attivita=(String) parametriSingUP[i]; break;
	        				case 5:
	        					tipo_utente=(String) parametriSingUP[i]; break;
            			}
            		}else{
            			return new Risposta("ERRORE", "Formato dati non valido.");
            		}
            	}
            	
            	// Verifico se i dati non sono nulli
            	if(nome==null ||cognome==null || email==null || password==null || attivita==null || tipo_utente==null) {
            		return new Risposta("ERRORE", "Dati mancanti!");
            	}

            	//System.out.println(tipo_utente);
                // Verfico se il tipo di utente passato è valido (empoyee o manager)
            	if(!tipo_utente.equals("employee") && !tipo_utente.equals("manager")) {
            		return new Risposta("ERRORE", "Tipo Utente non valido!");
            	}
                
                // 1. Verifico se l'utente esiste
                if(esisteUtente(email,DB)){
                    return new Risposta("ERRORE","Questo Utente è presente nel DB");
                }
                // 2. Verifico se la company esiste
                id=esisteCompany(attivita,DB);
                if(id==-2){
                    return new Risposta("ERRORE","Company non trovata.");
                }else if(id==-1){
                    return new Risposta("ERRORE","Errore nella ricerca della company");
                }
                // 3. Aggiungere controlli password ed email
                
                //  .....

                // 4. Protezione Password
                
                // ...

                // 5. Salvatagio dei dati

                if(DB.inserisciUser(nome,cognome, email, password, id, tipo_utente)) {
                    return new Risposta("OK", "Account creato");
                }else {
                    return new Risposta("ERRORE","Errore durante la creazione del utente!");
                }

            case "LOGIN":
                String[] keysLogin={"email","password"};
                Object[] parametriLogin = leggiParametri(2,keysLogin,richiesta);

                for(int i=0;i<2;i++) {
            		if(parametriLogin[i] instanceof String){
            			switch (i) {
	            			case 0:
	        					email=(String) parametriLogin[i]; break;
	        				case 1: 
	        					password=(String) parametriLogin[i]; break;
            			}
            		}else{
            			return new Risposta("ERRORE", "Formato dati non valido.");
            		}
            	}

                //1. Verifico se l'utente esiste
                DB.avviaConnessione();
                HashMap<String, Object> infoUser= VerificaCredenzialiUtente(email, password, DB);
                
                if(infoUser!=null) {
                	if((infoUser.get("esito")).equals("Errore")) {
                		return new Risposta ("ERRORE","Errore durante la ricerca del utente");
                	}
                	if((infoUser.get("esito")).equals("Password Errata")){
                		return new Risposta ("ERRORE","Errore password errata");
                	}
                	if((infoUser.get("esito")).equals("Not Found")) {
                		return new Risposta ("ERRORE","Utente non trovato");
                	}
                }else {
                	return new Risposta ("ERRORE","infoUser is null");
                }
                
                answer= new Risposta("OK","Utente trovato");
                answer.aggiungiParametro("tipo_utente", infoUser.get("tipo_utente"));
                answer.aggiungiParametro("id", infoUser.get("id"));
                answer.aggiungiParametro("id company", infoUser.get("id company"));
                return answer;

            case "LOAD ATTIVITA":
            	String[] keysLoadTask= {"tipo utente","id","id company"};
            	Object[] parametriLoadTask= leggiParametri(3,keysLoadTask,richiesta);
            	
            	tipo_utente=(String) parametriLoadTask[0];
            	id=(int) parametriLoadTask[1];
            	            	
            	ArrayList<Attivita> elencoAttivita=new ArrayList<>();
            	
            	if (tipo_utente == null || id == -1) {
            	    return new Risposta("ERRORE", "Parametri mancanti o non validi.");
            	}
            	
            	if(tipo_utente.equals("employee")) {
            		query="SELECT * FROM attivita WHERE id_employee='"+id+"' AND stato!='Completato'";
            	}else if(tipo_utente.equals("manager")) {
            		int idCompany=getIdCompany(tipo_utente,id,DB);
            		query="SELECT * FROM attivita a INNER JOIN employee e ON a.id_employee=e.id WHERE e.id_company="+idCompany;
            	}else if(tipo_utente.equals("admin")) {
            		query="SELECT * FROM attivita";
            	}else {
            		return new Risposta("ERRORE", "Tipo di utente non valido");
            	}
            	DB.avviaConnessione();
            	result=DB.eseguiQuery(query);
            	
            	 if (result != null) {
                     try {
                    	 Attivita attivita1 = null;
                    	 while (result.next()) {
                    	     id = result.getInt("id");
                    	     titolo = result.getString("titolo");
                    	     descrizione = result.getString("descrizione");
                    	     prioritaStr = result.getString("priorita");
                    	     statoStr = result.getString("stato");
                    	     scadenza = result.getString("scadenza");

                    	     int idManager = result.getInt("id_manager");
                    	     int idEmployee = result.getInt("id_employee");

                    	     // Conversione stringa -> enum
                    	     TipoPriorita priorita = TipoPriorita.valueOf(prioritaStr.toUpperCase());
                    	     StatoAttivita stato = StatoAttivita.valueOf(statoStr.toUpperCase());

                    	     // Creazione oggetto
                    	     attivita1 = new Attivita(id, titolo, descrizione, scadenza, idManager, idEmployee, priorita,stato);
                    	     attivita1.getInfo();
                    	     
                    	     elencoAttivita.add(attivita1);
                    	 }
                     }catch (SQLException e) {
                             e.printStackTrace();
                             return new Risposta("ERRORE", "Errore durante il caricamento dei dati");
                         }
            	 }  
                     if(elencoAttivita!=null) {
                     	answer=new Risposta("OK","Attività trovate con successo");
                     	answer.aggiungiParametro("Attivita", elencoAttivita);
                     	return answer;
                     }
                     //return new Risposta("OK","Nessuna Attività trovata");

            case "AGGIUNGI ATTIVITA":
            	String employee_name = null;
            	String[] keysTask={"titolo","descrizione","priorita","scadenza","employee","id_manager"};
                Object[] parameTask = leggiParametri(6,keysTask,richiesta);
                int idManager=-1,idEmployee=-1;
                //stampaParametri(parameTask,6);
                
                // 1. Salvo i dati
                for(int i=0;i<6;i++) {
            		if(parameTask[i] instanceof String){
            			switch (i) {
	            			case 0:
	        					titolo=(String) parameTask[i]; break;
	        				case 1: 
	        					descrizione=(String) parameTask[i]; break;
	        				case 2:
	        					prioritaStr=(String) parameTask[i]; break;
	        				case 3:
	        					scadenza=(String) parameTask[i]; break;
	        				case 4:
	        					employee_name=(String) parameTask[i]; break;
            			}
            		}else if(i==5) {
	        					idManager=(int) parameTask[i]; break;
            		}else{
            			return new Risposta("ERRORE", "Formato dati non valido.");
            		}
            	}
                /*
                System.out.println("Titolo: "+titolo);
                System.out.println("Descrizione: "+descrizione);
                System.out.println("Priorita: "+prioritaStr);
                System.out.println("Scadenza: "+scadenza);
                System.out.println("Nome: "+employee_name);
                System.out.println("ID manager: "+idManager);*/
                                
                //2. Verifico che i dati non sono nulli
                
                if(titolo==null || descrizione==null || prioritaStr==null || scadenza==null || employee_name==null || idManager==-1) {
                	return new Risposta("ERRORE","Dati mancanti");
                }
                
                String[] dati=employee_name.split(" ");
                //System.out.println(dati[0]+" "+dati[1]);
                cognome=dati[0];
                nome=dati[1];
                statoStr="DA_FARE";
                
                // 3. Verifico che il dipedente passato esiste e mi ricavo l'id
                DB.avviaConnessione();
                idEmployee=DB.verificaEsistenzaUtente("employee",cognome,nome);
                if(idEmployee==0) {
                	return new Risposta("ERRORE","Dipendente non trovato");
                }else if(idEmployee==-1) {
                	new Risposta("ERRORE","Errore esecuzione della query");
                }
                
                
                //4. inserico la task sul DB                
                
                if(DB.inserisciTask(titolo, descrizione, statoStr, prioritaStr, scadenza, idEmployee, idManager)) {
                	return new Risposta("OK", "Attività creata");
                }else {
                	return new Risposta("ERRORE", "Attività non creata");
                }
                
            case "GET-EMPOLOYEE":
            	DB.avviaConnessione();
            	// Logica per l'invio dei dipendenti della azienda
            	int idCompany=(int) richiesta.getParametro("id_company");
            	query="SELECT * FROM employee WHERE id_company="+idCompany;
            	
            	result=DB.eseguiQuery(query);
            	ArrayList<Employee> employeeList=new ArrayList<Employee>();
            	Employee dipendente;
            	if(result!=null) {
            		try {
            			while(result.next()) {
            				id=result.getInt("id");
            				nome=result.getString("nome");
            				cognome=result.getString("cognome");
            				dipendente=new Employee(id,nome,cognome);
            				employeeList.add(dipendente);
            			}
            			answer=new Risposta("OK", "Dipendenti Trovati!");
            			answer.aggiungiParametro("dipendenti", employeeList);
            			answer.stampaKeys();
            			return answer;
            		} catch(SQLException e) {
                        e.printStackTrace();
                        return new Risposta("ERRORE", "Errore durante la lettura dei dati o dipendenti mancanti");
                    }
            	}
            	return new Risposta("ERRORE","Non ci sono dipendenti nel DB con id_company"+idCompany);
            
            case "UPDATE-ATTIVITA":
            	DB.avviaConnessione();
            	//System.out.println("Sono dentro Upadate");
            	String tipoUtente = (String) richiesta.getParametro("tipoUtente");
            	if(tipoUtente.equals("employee")) {
            		String nuovoStato=(String) richiesta.getParametro("nuovo-stato");
            		id=(int) richiesta.getParametro("id");
            		// Controllo che i dati non siano nulli
            		if(nuovoStato==null || id==-1) {
            			return new Risposta("ERRORE","Dati Mancanti!");
            		}
            		// Eseguo la query e restituisco la risposta
            		if(!DB.updateStatoAttivita(id,nuovoStato)) {
            			return new Risposta("ERRORE","Errore durante l'aggiornamento dello stato");
            		}            		
            		return new Risposta("OK","Attivita Aggiornata con successo!");
            		
            	}else if(tipoUtente.equals("manager")) {
            		//System.out.println("Operazzioni Manager");
            		if(!richiesta.verificaKey("attivita")) {
            			return new Risposta("ERRORE","Attivita non trovata");
            		}
            		
            		Attivita task=(Attivita) richiesta.getParametro("attivita");
            		
            		// Verificho se l'attivita è null
            		
            		if(task == null) {
            			return new Risposta("ERRORE","Dati Mancanti!");
            		}
            		// Eseguo la query
            		if(!DB.updateAttivita(task)) {
            			return new Risposta ("ERRORE","Errore durante l'aggiornamento della attivita");
            		}
            		return new Risposta("OK","Attivita Aggiornata con successo!");
            		
            	}else {
            		return new Risposta("ERRORE","Autorizzazione negata!");
            	}

            default:
                return new Risposta("ERRORE", "Richiesta non riconosciuta");
        }
    }

    private static Object[] leggiParametri(int numberOfParametri, String[] keys, Richiesta request) {
        Object[] parametri = new Object[numberOfParametri];
        int j = 0;

        for (int i = 0; i < numberOfParametri; i++) {
            parametri[i] = request.getParametro(keys[j]);
            j++;
        }
        return parametri;
    }

    // private static void stampaParametri(final Object[] parametri, final int numberOfParametri) {
    //     int j = 0;
    //     for (int i = 0; i < numberOfParametri; i++) {
    //         System.out.println(parametri[i]);
    //     }
    // }
    
    private static HashMap<String, Object> VerificaCredenzialiUtente(String email, String password, Database DB) {
        HashMap<String, Object> info = new HashMap<>();
        String[] tipiUtente = {"employee", "manager", "admin"};
        String cognome=null,nome=null;
        int id_company=-1;

        for (String tipo : tipiUtente) {
            String query = "SELECT * FROM " + tipo + " WHERE email='" + email + "'";
            ResultSet result = DB.eseguiQuery(query);
            
            if (result != null) {
                try {
                    int count = 0;
                    int id = 0;

                    while (result.next()) {
                        id = result.getInt("id");
                        nome=result.getString("nome");
                        cognome=result.getString("cognome");
                        if(!tipo.equals("admin")) {
                        	id_company=result.getInt("id_company");
                        }                        
                        count++;
                    }

                    if (count == 1) {
                        // Verifica password
                        query = "SELECT * FROM " + tipo + " WHERE id='" + id + "' AND password='" + password + "'";
                        result = DB.eseguiQuery(query);

                        if (result != null && result.next()) {
                            info.put("esito", "OK");
                            info.put("id", id);
                            info.put("tipo_utente", tipo);
                            info.put("nome", nome);
                            info.put("cognome", cognome);
                            info.put("email", email);
                            if(!tipo.equals("admin")) {
                            	info.put("id company", id_company);
                            }
                            return info;
                        } else {
                            info.put("esito", "Password Errata");
                            return info;
                        }
                    }

                } catch (SQLException e) {
                    e.printStackTrace();
                    info.put("esito", "Errore");
                    return info;
                }
            }
        }

        info.put("esito", "Not Found");
        return info;
    }

    
    private static Boolean esisteUtente(String email,Database DB) {
    	try {
    		String query="SELECT * FROM employee WHERE email='"+email+"'";
        	ResultSet result=DB.eseguiQuery(query);
        	
        	if (result != null && result.next()) { return true; }
        	
        	query="SELECT * FROM manager WHERE email='"+email+"'";
        	result=DB.eseguiQuery(query);
        	
        	if (result != null && result.next()) { return true; }
    	}catch (SQLException e) {
            e.printStackTrace();
        }
    	return false;    	
    }

    private static int esisteCompany(String attivita,Database DB){
        String query="SELECT id FROM company WHERE nome='"+attivita+"'";
        ResultSet result=DB.eseguiQuery(query);

        if(result==null){
            return -2;
        }

        int count = 0;
        int id= 0;
        try {
            while (result.next()) {
                id = (int) result.getInt("id");
                count++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }

        if (count != 1) {
            return -1;
        }  
        return id;        
    }
    
    private static int getIdCompany(String tipo_utente, int id, Database DB) {
    	String query=null;
    	if(tipo_utente.equals("employee")){
    		query="SELECT id_company FROM employee WHERE id='"+id+"'";
    	}else if(tipo_utente.equals("manager")) {
    		query="SELECT id_company FROM manager WHERE id='"+id+"'";
    	}else {
    		return -1;
    	}
    	
    	ResultSet result=DB.eseguiQuery(query);
    	
    	 if (result != null) {
             try {
                 int count = 0;
                 int IDCompany = 0;

                 while (result.next()) {
                	 IDCompany = result.getInt("id_company");
                     count++;
                 }
                 if (count == 1) {
                	 return IDCompany;
                 }
                 return 0;
                 }catch (SQLException e) {
                	 e.printStackTrace();
                	 return 0;
                 }
    	 }
		return -1;
    }
}
