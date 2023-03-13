package com.parkit.parkingsystem.dao;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.DBConstants;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

public class TicketDAO {  // classe intermédiaire entre objets métier et système de stockage

    private static final Logger logger = LogManager.getLogger("TicketDAO");

    public DataBaseConfig dataBaseConfig = new DataBaseConfig();

    public boolean saveTicket(Ticket ticket){ // methode permettant d'enregistrer les données d'un ticket sur la BDD
        Connection con = null; // Objet con (classe preenregistrée "connection" ) pour acceder à base de données
        try {
            con = dataBaseConfig.getConnection(); // Modifie objet con pour chercher adresse de connection dans dataBaseConfig
            PreparedStatement ps = con.prepareStatement(DBConstants.SAVE_TICKET); // methode permettant une requete parametrés sql - voir données concernées ci-dessous 
            //ID, PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME)
            //ps.setInt(1,ticket.getId());
            ps.setInt(1,ticket.getParkingSpot().getId());  // definition des parametres entrés dans la requete sql (id, vehiclenumber...)
            ps.setString(2, ticket.getVehicleRegNumber());
            ps.setDouble(3, ticket.getPrice());
            ps.setTimestamp(4, new Timestamp(ticket.getInTime().getTime()));
            ps.setTimestamp(5, (ticket.getOutTime() == null)?null: (new Timestamp(ticket.getOutTime().getTime())) );
            return ps.execute();// execute un statement sql
        }catch (Exception ex){
            logger.error("Error fetching next available slot",ex);
        }finally {
            dataBaseConfig.closeConnection(con);
            return false; // 
        }
    }

    public Ticket getTicket(String vehicleRegNumber) { //methode pour recuperer les donnes d'un ticket à partir d'un numero de vehicule
        Connection con = null;
        Ticket ticket = null;
        try {
            con = dataBaseConfig.getConnection();
            PreparedStatement ps = con.prepareStatement(DBConstants.GET_TICKET);
            //ID, PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME)
            ps.setString(1,vehicleRegNumber);
            ResultSet rs = ps.executeQuery(); // ensemble des resultats de la requete sql definie avec le prestatement 
            if(rs.next()){// parcours du resultset (des resultats de la requete)
                ticket = new Ticket();
                ParkingSpot parkingSpot = new ParkingSpot(rs.getInt(1), ParkingType.valueOf(rs.getString(6)),false);
                ticket.setParkingSpot(parkingSpot);
                ticket.setId(rs.getInt(2));
                ticket.setVehicleRegNumber(vehicleRegNumber);
                ticket.setPrice(rs.getDouble(3));
                ticket.setInTime(rs.getTimestamp(4));
                ticket.setOutTime(rs.getTimestamp(5));
            }
            dataBaseConfig.closeResultSet(rs);
            dataBaseConfig.closePreparedStatement(ps);
        }catch (Exception ex){
            logger.error("Error fetching next available slot",ex);
        }finally {
            dataBaseConfig.closeConnection(con);
            return ticket;
        }
    }

    public boolean updateTicket(Ticket ticket) {
        Connection con = null;
        try {
            con = dataBaseConfig.getConnection();
            PreparedStatement ps = con.prepareStatement(DBConstants.UPDATE_TICKET);
            ps.setDouble(1, ticket.getPrice());
            ps.setTimestamp(2, new Timestamp(ticket.getOutTime().getTime()));
            ps.setInt(3,ticket.getId());
            ps.execute();
            return true;
        }catch (Exception ex){
            logger.error("Error saving ticket info",ex);
        }finally {
            dataBaseConfig.closeConnection(con);
        }
        return false;
    }

    /* definition d'une methode qui retourne le nombre de dates de sortie d'un vehicule identifié par son numero d'immatriculation
        doit detourner un int . S'il est > à 0, mettre le paramette de ticket discount à true poour appliquer reduction 
    */

    public boolean getNbTicket (Ticket ticket) { //methode à appeler dans ticketfarecalculator, retourne directement le boolean discount à false ou true


// Connection base sql
        Connection con = null;

return true;

//Chercher si numero de vehicule a une date de sortie - getOutTime

//si date de sortie, modifier boolean discount à true , sinon false (voir si par defaut discount est à false)

//Renvoyer discount boolean dans farecalculatorservice
    }


}

