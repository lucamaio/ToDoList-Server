package ServerSide;


import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

import Shared.*;


public class GestoreRichiesta {


	public static Risposta process(Richiesta richiesta) {
        String tipo = richiesta.getTipo();
        System.out.println("Nuova richiesta "+ tipo);
        Database DB=new Database();
        String query;
        ResultSet result;
        Risposta answer=null;
        String nome=null,cognome=null, password=null,email=null,attivita=null,tipo_utente=null;
        String titolo=null,descrizione=null,statoStr=null,prioritaStr=null;
		String scadenza=null;
        Utente user = null;
        Integer id, idManager, idEmployee, idDepartment,idCompany;
        ArrayList<Attivita> elencoAttivita=new ArrayList<>();
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
                String[] keysLogin = {"email", "password"};
                Object[] parametriLogin = leggiParametri(2, keysLogin, richiesta);

                for (int i = 0; i < 2; i++) {
                    if (!(parametriLogin[i] instanceof String)) {
                        return new Risposta("ERRORE", "Formato dati non valido.");
                    }
                }

                email = (String) parametriLogin[0];
                password = (String) parametriLogin[1];

                DB.avviaConnessione();
                HashMap<String, Object> infoUser = VerificaCredenzialiUtente(email, password, DB);

                if (infoUser == null) {
                    return new Risposta("ERRORE", "infoUser is null");
                }

                String esito = (String) infoUser.get("esito");
                if ("Errore".equals(esito)) {
                    return new Risposta("ERRORE", "Errore durante la ricerca dell'utente");
                }
                if ("Password Errata".equals(esito)) {
                    return new Risposta("ERRORE", "Password errata");
                }
                if ("Not Found".equals(esito)) {
                    return new Risposta("ERRORE", "Utente non trovato");
                }

                answer = new Risposta("OK", "Utente trovato");

                if (infoUser.containsKey("tipo_utente")) {
                    String tipoUtente = (String) infoUser.get("tipo_utente");
                                      
                    switch (tipoUtente) {
                        case "employee":
                            id = (Integer) infoUser.get("id");
                            nome = (String) infoUser.get("nome");
                            cognome = (String) infoUser.get("cognome");
                            idDepartment = (Integer) infoUser.get("id_department");

                            if (id == null || nome == null || cognome == null || idDepartment == null) {
                                return new Risposta("ERRORE", "Dati non caricati correttamente");
                            }

                            user = new Employee(id, nome, cognome, email, idDepartment);
                            answer.aggiungiParametro("utente", user);
                            return answer; 

                        case "manager":
                            id = (Integer) infoUser.get("id");
                            nome = (String) infoUser.get("nome");
                            cognome = (String) infoUser.get("cognome");

                            if (id == null || nome == null || cognome == null) {
                                return new Risposta("ERRORE", "Dati non caricati correttamente");
                            }

                            user = new Manager(id, nome, cognome, email);
                            answer.aggiungiParametro("utente", user);
                            return answer; 
                            
                        case "director":
                            id = (Integer) infoUser.get("id");
                            nome = (String) infoUser.get("nome");
                            cognome = (String) infoUser.get("cognome");
                            idCompany =(Integer) infoUser.get("id_company");

                            if (id == null || nome == null || cognome == null || idCompany == null) {
                                return new Risposta("ERRORE", "Dati non caricati correttamente");
                            }

                            user = new Director(id, nome, cognome, email, idCompany);
                            answer.aggiungiParametro("utente", user);
                            return answer; 
                        case "admin":
                            id = (Integer) infoUser.get("id");
                            nome = (String) infoUser.get("nome");
                            cognome = (String) infoUser.get("cognome");

                            if (id == null || nome == null || cognome == null) {
                                return new Risposta("ERRORE", "Dati non caricati correttamente");
                            }

                            user = new Admin(id, nome, cognome, email);
                            answer.aggiungiParametro("utente", user);
                            return answer; 

                        default:
                            return new Risposta("ERRORE", "Tipo utente non valido nella chiamata della funzione!");
                    }

                }else {
                	return new Risposta("ERRORE", "Tipo utente non trovato");
                }

            case "LOAD ATTIVITA":
            	
            	// 1. controllo se esiste la chiave utente
            	if(!richiesta.verificaKey("utente")) {
            		return new Risposta("ERRORE","La chiava Utente non esiste!");
            	}
            	user=(Utente) richiesta.getParametro("utente");
            	if(user.getRuolo()==null) {
            		return new Risposta("ERRORE","Il tipo utente è null");
            	}
            	
            	tipo_utente=(String) user.getRuolo();
            	            	
            	if(tipo_utente.equals("employee")) {
            		if(!richiesta.verificaKey("idDepartment")) {
            			return new Risposta("ERRORE","idDeparment mancante!");
            		}
            		idDepartment=(Integer) richiesta.getParametro("idDepartment");
            		query="SELECT * FROM attivita WHERE (id_employee="+user.getId()+" OR id_department="+idDepartment+") AND !stato='COMPLETATA'";
            	}else if(tipo_utente.equals("manager")) {
            		query="SELECT a.*, e.nome AS nome_dipendente, e.cognome AS cognome_dipendente, d.nome AS nome_dipartimento "
            				+ "FROM attivita a "
            				+ "LEFT JOIN employee e ON a.id_employee = e.id "
            				+ "LEFT JOIN department d ON e.id_department = d.id "
            				+ "WHERE (d.id_manager ="+(int) user.getId()+" OR a.id_employee IS NULL )AND !a.stato='COMPLETATA'";
            	}else if(tipo_utente.equals("dirctor")){
            		query="SELECT a.* FROM attivita a JOIN department d ON a.id_department=d.id WHERE d.id_dirctor="+user.getId()+" AND !a.stato='COMPLETATA'";
            	}else if(tipo_utente.equals("admin")) {
            		query="SELECT * FROM attivita WHERE !stato='COMPLETATA'";
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
                    		    
                    		    TipoPriorita priorita = TipoPriorita.valueOf(prioritaStr.toUpperCase());
                    		    StatoAttivita stato = StatoAttivita.valueOf(statoStr.toUpperCase());
                    		    idManager = result.getInt("id_manager");

                    		    idEmployee = result.getInt("id_employee");
                    		    if (result.wasNull()) idEmployee = null;

                    		    idDepartment = result.getInt("id_department");
                    		    if (result.wasNull()) idDepartment = null;

                    		   if (idEmployee != null || idDepartment!= null) {
                    		        attivita1 = new Attivita(
                    		            id, titolo, descrizione, scadenza,
                    		            idManager, idEmployee, idDepartment,
                    		            priorita, stato
                    		        );
                    		        elencoAttivita.add(attivita1);
                    		    } else {
                    		        return new Risposta("ERRORE", "Attività priva sia di id_employee che di id_department");
                    		    }
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

            case "AGGIUNGI ATTIVITA":
            	// 1. Verifico se il campo attivita essite
            	if(!richiesta.verificaKey("attivita")) {
            		return new Risposta("ERRORE","Il campo attivita non esiste");
            	}
            	// 2. Verifico se il campo attivita è un oggetto di classe Attivita
            	if(!(richiesta.getParametro("attivita") instanceof Attivita)) {
            		return new Risposta("ERRORE","Il campo attivita non è un'istanza della classe Attivita");
            	}
            	Attivita task=(Attivita) richiesta.getParametro("attivita");
            	
            	if(task==null) {
            		return new Risposta("ERRORE","L'attività passata è null");
            	}
            	
            	// 3. Salvo i dati della attività nelle corispetive variabili
            	
            	titolo=(String) task.getTitolo();
            	descrizione=(String) task.getDescrizione();
                scadenza=(String) task.getDataScadenza();
                prioritaStr=task.getPriorita().toString();
                String employee_name=task.getNominativoEmployee();
                String department_name=task.getdepartmentName();
                idManager=task.getIdManager();
                // 4. controllo se i dati sono nulli
                
                if(titolo==null || descrizione==null || scadenza==null || prioritaStr==null || idManager==null) {
                	return new Risposta("ERRORE","Dati Mancanti!");
                }
                
                Boolean esito1=false;
                statoStr="DA_FARE";
                DB.avviaConnessione();
                if(employee_name!=null && !employee_name.equals("*")) {
                	System.out.println(employee_name);
                	 String[] dati=employee_name.split(" ");
                     cognome=dati[0];
                     nome=dati[1];
                	// 5. controllo se l'emplyee con il nominativo passato esiste
                     idEmployee=DB.verificaEsistenzaUtente("employee",cognome,nome);
                     if(idEmployee==0) {
                     	return new Risposta("ERRORE","Dipendente non trovato");
                     }else if(idEmployee==-1) {
                     	new Risposta("ERRORE","Errore esecuzione della query");
                     }
                     esito1=DB.creaTask(titolo, descrizione, statoStr, prioritaStr, scadenza, idEmployee, idManager,"employee-task");
                     
                	
                }else if(department_name !=null && !department_name.equals("*")) {
                	// 5. Controllo se il dipartimento con il nominativo passato esiste
                	idDepartment=DB.verificaEsistenzaDipartimento(department_name);
                	if(idDepartment==0) {
                		return new Risposta("ERRORE","Dipartimento non trovato");
                	}else if(idDepartment==-1) {
                     	new Risposta("ERRORE","Errore esecuzione della query");
                     }
                	// Opzioni per l'aseegnazione della task per un dipartimento
                	esito1=DB.creaTask(titolo, descrizione, statoStr, prioritaStr, scadenza, idDepartment, idManager,"department-task");
                }else {
                	return new Risposta("ERRORE","idDepatment ed emplyee_name sono null");
                }
                
                // 6. Controllo il  risultato della operazione
                
                if(esito1) {
                	return new Risposta("OK", "Attività creata");
                }
                return new Risposta("ERRORE", "Attività non creata");
				
            case "UPDATE-ATTIVITA":
            	DB.avviaConnessione();
            	if(!richiesta.verificaKey("tipoUtente")) {
            		return new Risposta("ERRORE","Tipo Utente non trovato");
            	}
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
            		if(!richiesta.verificaKey("attivita")) {
            			return new Risposta("ERRORE","Attivita non trovata");
            		}
            		
            		task=(Attivita) richiesta.getParametro("attivita");
            		
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
            	
            case "LOAD-ARCHIVIO":
            	DB.avviaConnessione();
            	if(!richiesta.verificaKey("utente")) {
            		return new Risposta("ERRORE","La chiave utente non trovata");
            	}
            	user=(Utente) richiesta.getParametro("utente");
            	if(user.getRuolo()==null) {
            		return new Risposta("ERRORE","Il tipo utente è null");
            	}
            	
            	tipo_utente=(String) user.getRuolo();
            	            	            	
            	if(tipo_utente.equals("employee")) {
            		if(!richiesta.verificaKey("idDepartment")) {
            			return new Risposta("ERRORE","idDeparment mancante!");
            		}
            		idDepartment=(Integer) richiesta.getParametro("idDepartment");
            		query="SELECT * FROM attivita WHERE (id_employee="+user.getId()+" OR id_department="+idDepartment+") AND stato='COMPLETATA'";
            	}else if(tipo_utente.equals("manager")) {
            		query="SELECT a.*, e.nome AS nome_dipendente, e.cognome AS cognome_dipendente, d.nome AS nome_dipartimento "
            				+ "FROM attivita a "
            				+ "LEFT JOIN employee e ON a.id_employee = e.id "
            				+ "LEFT JOIN department d ON e.id_department = d.id "
            				+ "WHERE (d.id_manager ="+(int) user.getId()+" OR a.id_employee IS NULL) AND a.stato='COMPLETATA'";
            	}else if(tipo_utente.equals("dirctor")){
            		query="SELECT a.* FROM attivita a JOIN department d ON a.id_department=d.id WHERE d.id_dirctor="+user.getId()+"AND a.stato='COMPLETATA'";
            	}else if(tipo_utente.equals("admin")) {
            		query="SELECT * FROM attivita WHERE stato='COMPLETATA'";
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
                    		    
                    		    TipoPriorita priorita = TipoPriorita.valueOf(prioritaStr.toUpperCase());
                    		    StatoAttivita stato = StatoAttivita.valueOf(statoStr.toUpperCase());
                    		    idManager = result.getInt("id_manager");

                    		    idEmployee = result.getInt("id_employee");
                    		    if (result.wasNull()) idEmployee = null;

                    		    idDepartment = result.getInt("id_department");
                    		    if (result.wasNull()) idDepartment = null;

                    		   if (idEmployee != null || idDepartment!= null) {
                    		        attivita1 = new Attivita(
                    		            id, titolo, descrizione, scadenza,
                    		            idManager, idEmployee, idDepartment,
                    		            priorita, stato
                    		        );
                    		        elencoAttivita.add(attivita1);
                    		    } else {
                    		        return new Risposta("ERRORE", "Attività priva sia di id_employee che di id_department");
                    		    }
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
            	
            case "GET-DEPARTMENT-MANAGER":
            	if(!richiesta.verificaKey("manager")) {
            		return new Risposta("ERRORE","Chiave manager mancante");
            	}
            	if(!(richiesta.getParametro("manager") instanceof  Manager)) {
            		return new Risposta("ERRORE","L'utente passato non è di tipo manager");
            	}
            	Manager manager=(Manager) richiesta.getParametro("manager");
            	
            	query="SELECT * FROM department WHERE id_manager="+manager.getId();
            	DB.avviaConnessione();
            	result=DB.eseguiQuery(query);
            	if(result!=null) {
            		try {
            			ArrayList<Department> listaDipartimenti=new ArrayList<>();
            			Department dipartimento=null;
                      	 while (result.next()) {
                      		 id=result.getInt("id");
                      		 nome=result.getString("nome");
                      		 descrizione=result.getString("descrizione");
                      		 idCompany=result.getInt("id_company");
                      		 dipartimento=new Department(id,nome,descrizione,idCompany,manager.getId());
                      		 listaDipartimenti.add(dipartimento);
                      	 }
                      	 answer=new Risposta("OK","Ricerca dati completata!");
                      	 if(listaDipartimenti!=null) {
                      		 answer.aggiungiParametro("dipartimenti", listaDipartimenti);
                      	 }
                      	 return answer;
                       }catch (SQLException e) {
                               e.printStackTrace();
                               return new Risposta("ERRORE", "Errore durante il caricamento dei dati");
                           }
            	}
            	return new Risposta("OK","Nessun dipartimento è stato assegnato all'utente");
            	
            case "GET-EMPLOYEE-DEPARTMENT":
            	if(!richiesta.verificaKey("nome_dipartimento")) {
            		return new Risposta("ERRORE","Chiave nome_dipartimento mancante");
            	}
            	String nomeDipartimento=(String) richiesta.getParametro("nome_dipartimento");
            	
            	query="SELECT e.* FROM employee e INNER JOIN department d ON e.id_department=d.id WHERE d.nome='"+nomeDipartimento+"'";
            	DB.avviaConnessione();
            	result=DB.eseguiQuery(query);
            	if(result!=null) {
            		try {
            			ArrayList<Employee> listaDipendenti=new ArrayList<>();
            			//Employee dipendente=null;
                      	 while (result.next()) {
                      		 id=result.getInt("id");
                      		 nome=result.getString("nome");
                      		 cognome=result.getString("cognome");
                      		 
                      		 Employee dipendente = new Employee(id,nome,cognome);
                      		listaDipendenti.add(dipendente);
                      	 }
                      	 answer=new Risposta("OK","Ricerca dati completata!");
                      	 if(listaDipendenti!=null) {
                      		 answer.aggiungiParametro("dipendenti", listaDipendenti);
                      	 }
                      	 return answer;
                       }catch (SQLException e) {
                               e.printStackTrace();
                               return new Risposta("ERRORE", "Errore durante il caricamento dei dati");
                           }
            	}
            	
            case "GET-COMPANY-DIRECTOR":
            	// 1. Verifico se la chiave esiste in modo da poter effetuare le operazioni successive
            	if(!richiesta.verificaKey("director")) {
            		return new Risposta("ERRORE","Chiave director mancante");
            	}
            	Director director=(Director) richiesta.getParametro("director");
            	//2. controllo se l'oggetto passato è != null
            	if(director==null) {
            		return new Risposta("ERRORE","director is null");          		
            	}
            	            	
            	// 3. Avvio la connessione con il DB ed eseguo la query
            	DB.avviaConnessione();
            	query="SELECT * FROM company WHERE id="+(int) director.getIdCompany()+"";
            	result=DB.eseguiQuery(query);
            	if(result!=null) {
            		try {
                      	 while (result.next()) {
                      		 id=result.getInt("id");
                      		 nome=result.getString("nome");
                      		 descrizione=result.getString("descrizione");
                      		 email=result.getString("email");
                      		 
                      		String indirizzo=result.getString("indirizzo");
                      		String telefono=result.getString("telefono");
                      		
                      		Company azienda=new Company(id,nome,descrizione,indirizzo,email,telefono);
                      		 answer=new Risposta("OK","Ricerca dati completata!");
                          	 if(azienda!=null) {
                          		 answer.aggiungiParametro("azienda", azienda);
                          	 }
                          	 return answer;
                      	 }
                      	
                       }catch (SQLException e) {
                               e.printStackTrace();
                               return new Risposta("ERRORE", "Errore durante il caricamento dei dati");
                           }
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
    
    private static HashMap<String, Object> VerificaCredenzialiUtente(String email, String password, Database DB) {
        HashMap<String, Object> info = new HashMap<>();
        String[] tipiUtente = {"employee", "manager","director","admin"};
        String cognome=null,nome=null;
        int id_department=-1,id_company=-1;

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
                        if(tipo.equals("employee")){
                        		id_department=result.getInt("id_department");
                        }else if(tipo.equals("director")) {
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
                            
                            if(tipo.equals("employee")||tipo.equals("manager")) {
                            	info.put("id_department", id_department);
                            }else if(tipo.equals("director")) {
                            	info.put("id_company", id_company);
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
    
    /*private static int getIdCompany(String tipo_utente, int id, Database DB) {
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
    }*/
}
