package com.example.demo.services;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.example.demo.constants.EtronPrjConstants;
import com.example.demo.models.AbonnementPlan;
import com.example.demo.models.BorneRecharge;
import com.example.demo.models.Contrat;
import com.example.demo.models.Facture;
import com.example.demo.models.Paiement;
import com.example.demo.models.Recharge;
import com.example.demo.models.User;
import com.example.demo.models.Voiture;
import com.example.demo.repositories.AbonnementPlanRepository;
import com.example.demo.repositories.BorneRechargeRepository;
import com.example.demo.repositories.ContratRepository;
import com.example.demo.repositories.PaiementRepository;
import com.example.demo.repositories.RechargeRepository;
import com.example.demo.repositories.UserRepository;
import com.example.demo.repositories.VoitureRepository;
import com.example.demo.utils.EmailUtils;
import com.example.demo.utils.EtronPrjUtils;
import com.google.common.base.Strings;
import com.example.demo.JWT.CustomerUserDetailsService;
import com.example.demo.JWT.JwtFilter;
import com.example.demo.utils.EmailUtils;
import com.example.demo.JWT.JwtUtil;


@Service
public class EtronAbonnementService {
	@Autowired
	AuthenticationManager authenticationManager;
	
	@Autowired
	CustomerUserDetailsService customerUserDetailsService;
	
	@Autowired
	JwtUtil jwtUtil;
	
	@Autowired
	JwtFilter jwtFilter;
	
	@Autowired
	EmailUtils emailUtils;
	
	@Autowired
	private UserRepository userrepos;
	
	@Autowired
	private ContratRepository contratrepos;
	
	@Autowired
	private AbonnementPlanRepository planrepos;
	
	@Autowired
	private PaiementRepository paiementrepos;
	
	@Autowired
	private VoitureRepository voiturerepos;
	
	@Autowired
	private BorneRechargeRepository bornerepos;
	
	@Autowired
	private RechargeRepository rechargerepos;
	
	/*public ResponseEntity<String> inscrireUtilisateur(String nom, String prenom, String adresse, String numeroTelephone, String email) {
        User user = new User();
        user.setName(nom);
        user.setPrenom(prenom);
        user.setAdresse(adresse);
        user.setContactnumber(numeroTelephone);
        user.setEmail(email);
        user.setDateInscription(LocalDate.now());

        return userrepos.save(user);
    }*/
	
	public ResponseEntity<String> inscrireUtilisateur(Map<String, String> requestMap) {
        try {
            if (validateSignUpMap(requestMap)) {
                User user = userrepos.findByEmail(requestMap.get("email"));
                if (Objects.isNull(user)) {
                	userrepos.save(getUserFromMap(requestMap));
                    return EtronPrjUtils.getResponseEntity("Successfully Registered", HttpStatus.OK);
                } else {
                    return EtronPrjUtils.getResponseEntity("Email already exists", HttpStatus.BAD_REQUEST);
                }
            } else {
                return EtronPrjUtils.getResponseEntity(EtronPrjConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return EtronPrjUtils.getResponseEntity(EtronPrjConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    private boolean validateSignUpMap(Map<String, String> requestMap) {
        if(requestMap.containsKey("name") && requestMap.containsKey("contactnumber")
                && requestMap.containsKey("email") && requestMap.containsKey("password"))
        {
            return true;
        }
        return false;
    }

    private User getUserFromMap(Map<String, String> requestMap) {
        User user = new User();
        user.setName(requestMap.get("name"));
        user.setPrenom(requestMap.get("prenom"));
        user.setContactnumber(requestMap.get("contactnumber"));
        user.setEmail(requestMap.get("email"));
        user.setPassword(requestMap.get("password"));
        user.setAdresse("adresse");
        user.setDateInscription(LocalDate.now());
        return user;
    }
    
    public ResponseEntity<String> login(Map<String, String> requestMap) {
		System.out.println("Inside login");
		try {
			Authentication auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(requestMap.get("email"), requestMap.get("password")));
			
			if(auth.isAuthenticated()) {
				return new ResponseEntity<String>("{\"token\":\"" + jwtUtil.generateToken(customerUserDetailsService.getUserDetails().getEmail(), customerUserDetailsService.getUserDetails().getRole()) + "\"}", HttpStatus.OK);
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return new ResponseEntity<String>("{\"message\":\""+"Bad Credentials."+"\"}", HttpStatus.BAD_REQUEST);
	}
    
    public ResponseEntity<String> checkToken() {
		return EtronPrjUtils.getResponseEntity("true", HttpStatus.OK);
	}

	public ResponseEntity<String> changePassword(Map<String, String> requestMap) {
		try {
			User userObj = userrepos.findByEmail(jwtFilter.getCurrentuser());
			if(!userObj.equals(null)) {
				if(userObj.getPassword().equals(requestMap.get("oldPassword"))) {
					userObj.setPassword(requestMap.get("newPassword"));
					userrepos.save(userObj);
					emailUtils.sendSimpleMessage(jwtFilter.getCurrentuser(), "Password changed", "Account password is changed.", null);
					return EtronPrjUtils.getResponseEntity("Password Updated Successfully", HttpStatus.OK);
				}
				return EtronPrjUtils.getResponseEntity("Incoreect Old Password", HttpStatus.BAD_REQUEST);
			}
			return EtronPrjUtils.getResponseEntity(EtronPrjConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return EtronPrjUtils.getResponseEntity(EtronPrjConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	public ResponseEntity<String> forgotPassword(Map<String, String> requestMap) {
		try {
			User userObj = userrepos.findByEmail(requestMap.get("email"));
			if(!Objects.isNull(userObj) && !Strings.isNullOrEmpty(userObj.getEmail())) {
				emailUtils.forgotPasswordMail(userObj.getEmail(), "Your Login Credentials for Etron Management System", userObj.getPassword());
			}
			return EtronPrjUtils.getResponseEntity("Check your mail box...", HttpStatus.OK);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return EtronPrjUtils.getResponseEntity(EtronPrjConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
	}
    
	
    
	//Map<String, String> requestMap
    
	public ResponseEntity<String> choisirFormuleAbonnement(int idUser, String typeAbonnement) {
		try {
		User user = userrepos.findById(idUser).get();
		boolean eligibleAbonnementGratuit = estEligibleAbonnementGratuit(user);

        AbonnementPlan abonnement = planrepos.findByType(typeAbonnement);

        double fraisMensuels = abonnement.getFraismois(); 

        if (eligibleAbonnementGratuit && typeAbonnement.equals("pro")) {
            fraisMensuels = 0.0; // Abonnement gratuit la première année pour les propriétaires de l'e-tron.
        }

        Contrat contrat = new Contrat();
        contrat.setUser(user);
        contrat.setDateDebut(LocalDate.now());
        contrat.setFraisMois(fraisMensuels);
        contrat.setPlan(abonnement);
        
        contratrepos.save(contrat);
        
        return EtronPrjUtils.getResponseEntity("Successfully Registered", HttpStatus.OK);
        
		} catch(Exception e) {
			e.printStackTrace();
		}
		return EtronPrjUtils.getResponseEntity(EtronPrjConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
        		
    }
	
	// Méthode pour effectuer un paiement mensuel
	public ResponseEntity<String> effectuerPaiementAvecCode(int idUser, String type) {
		try {
			User user = userrepos.findById(idUser).get();
			AbonnementPlan abonnement = user.getContrat().getPlan();
			
			if (abonnement != null && abonnement.getType().equals(type)) {
	            Paiement paiement = new Paiement();
	            paiement.setUser(user);
	            paiement.setDatePaiement(LocalDate.now());
	            paiement.setMontant(abonnement.getFraismois());

	            paiementrepos.save(paiement);


	            return EtronPrjUtils.getResponseEntity("Successfully Registered", HttpStatus.OK); // Paiement réussi
	        }
		} catch(Exception e) {
			e.printStackTrace();
		}
		

        return EtronPrjUtils.getResponseEntity(EtronPrjConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR); // Code invalide

	        
	}
	 
	 // Connaitre tous les bornes qui se trouvent autour d'un utilisateur d'un rayon max de 5 KM
	public ResponseEntity<List<BorneRecharge>> ListerBornesRechargeUser(double userLatitude , double userLongitude , double rayonDistanceMax ){
		try {
			List<BorneRecharge> bornes = bornerepos.findAll();
			List<BorneRecharge> bornesProches = new ArrayList<BorneRecharge>();
			
			for (BorneRecharge borne : bornes) {
			    double distance = calculerDistance(userLatitude, userLongitude, borne.getLatitude(), borne.getLongitude());
			    
			    if (distance <= rayonDistanceMax) {
			        bornesProches.add(borne);
			    }
			}
			return new ResponseEntity<>(bornesProches, HttpStatus.OK);
		} catch(Exception e) {
			e.printStackTrace();
		}
		return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
		
		
	}
	
	public ResponseEntity<Facture> calculerFactureMensuelle(int idUser, int mois, int annee) {
        // Obtenez les données de recharge de l'utilisateur pour le mois et l'année donnés.
		try {
			User user = userrepos.findById(idUser).get();
	        List<Recharge> recharges = rechargerepos.findByUserAndMoisAndAnnee(user, mois, annee);

	        double montantTotal = 0.0;

	        for (Recharge recharge : recharges) {
	            double quantiteEnergie = recharge.getQuantiteEnergie(); // en kWh
	            String typeCharge = recharge.getTypeCharge(); // AC, DC/HPC, haute puissance
	            int dureeRechargeEnMinutes = recharge.getDureeRecharge();
	            
	            double coutRecharge = calculerCoutRecharge(quantiteEnergie, typeCharge, user.getContrat().getPlan().getType(), dureeRechargeEnMinutes);
	            montantTotal += coutRecharge;
	        }

	        LocalDate dateActuelle = LocalDate.now();
	        int jourActuel = dateActuelle.getDayOfMonth();
	        LocalDate dateDemandeFacture = LocalDate.of(annee, mois, jourActuel);
	        Facture facture = new Facture();
	        facture.setContrat(user.getContrat());
	        facture.setDateFacturation(dateDemandeFacture);
	        facture.setMontantTotal(montantTotal);
	        
	        return new ResponseEntity<>(facture, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ResponseEntity<>(new Facture(), HttpStatus.INTERNAL_SERVER_ERROR);
		
    }

	private double calculerCoutRecharge(double quantiteEnergie, String typeCharge, String typeAbonnement, int dureeRechargeEnMinutes) {
	    double cout = 0.0;

	    if (typeAbonnement.equals("basic")) {
	        if (typeCharge.equals("AC")) {
	            cout = quantiteEnergie * 0.50;
	            if (dureeRechargeEnMinutes > 240) {
	                cout += (dureeRechargeEnMinutes - 240) * 0.05;
	            }
	        } else if (typeCharge.equals("DC/HPC")) {
	            cout = quantiteEnergie * 0.74;
	            if (dureeRechargeEnMinutes > 90) {
	                cout += (dureeRechargeEnMinutes - 90) * 0.15;
	            }
	        } else if (typeCharge.equals("haute puissance")) {
	            cout = quantiteEnergie * 0.59;
	        }
	    } else if (typeAbonnement.equals("pro")) {
	        if (typeCharge.equals("AC")) {
	            cout = quantiteEnergie * 0.42;
	            if (dureeRechargeEnMinutes > 240) {
	                cout += (dureeRechargeEnMinutes - 240) * 0.05;
	            }
	        } else if (typeCharge.equals("DC/HPC")) {
	            cout = quantiteEnergie * 0.66;
	            if (dureeRechargeEnMinutes > 90) {
	                cout += (dureeRechargeEnMinutes - 90) * 0.15;
	            }
	        } else if (typeCharge.equals("haute puissance")) {
	            cout = quantiteEnergie * 0.59;
	        }
	    } else {
	        if (typeCharge.equals("AC")) {
	            cout = quantiteEnergie * 0.35;
	            if (dureeRechargeEnMinutes > 240) {
	                cout += (dureeRechargeEnMinutes - 240) * 0.05;
	            }
	        } else if (typeCharge.equals("DC/HPC")) {
	            cout = quantiteEnergie * 0.63;
	            if (dureeRechargeEnMinutes > 90) {
	                cout += (dureeRechargeEnMinutes - 90) * 0.15;
	            }
	        } else if (typeCharge.equals("haute puissance")) {
	            cout = quantiteEnergie * 0.50;
	        }
	    }

	    return cout;
	}


    // Méthode pour vérifier l'éligibilité à l'abonnement gratuit la première année
    public boolean estEligibleAbonnementGratuit(User user) {
    	
        // Vérifiez si l'utilisateur est propriétaire de la nouvelle voiture e-tron.
    	Voiture v = voiturerepos.findVoitureByUser(user);

        LocalDate dateAchatEtron = user.getDateInscription();
        LocalDate dateExpirationAbonnementGratuit = dateAchatEtron.plusYears(1);

        // Si la date actuelle est antérieure à la date d'expiration de l'abonnement gratuit, l'utilisateur est éligible.
        return LocalDate.now().isBefore(dateExpirationAbonnementGratuit) && v.getModele() == voiturerepos.getDernierModele();
    }
    
    public double calculerDistance(double lat1, double lon1, double lat2, double lon2) {
        // Rayon de la Terre en kilomètres
        double rayonTerre = 6371.0;

        // Conversion des coordonnées de degrés en radians
        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);

        // Différence de latitudes et de longitudes
        double deltaLat = lat2Rad - lat1Rad;
        double deltaLon = lon2Rad - lon1Rad;

        // Calcul de la distance en utilisant la formule haversine
        double a = Math.pow(Math.sin(deltaLat / 2), 2) + Math.cos(lat1Rad) * Math.cos(lat2Rad) * Math.pow(Math.sin(deltaLon / 2), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = rayonTerre * c;

        return distance;
    }
    
    //Ajout Recharge d'un User
    public Recharge getRechargeById(long id){
        return rechargerepos.findById(id).orElse(null);
    }

    public Recharge addRecharge(Recharge recharge){
        return rechargerepos.save(recharge);
    }
    
    public List<Recharge> getRechargeByUserAndMoisAndAnnee(int idUser , int mois, int annee){
    	User user = userrepos.findById(idUser).get();
    	return rechargerepos.findByUserAndMoisAndAnnee(user, mois, annee);
    }
    
    //Ajout Un Abonnement 
    public AbonnementPlan addAbonnement(AbonnementPlan plan){
        return planrepos.save(plan);
    }
    
    //Ajout de Voiture AUDI
    public Voiture addAbonnement(Voiture voiture){
        return voiturerepos.save(voiture);
    }

}
