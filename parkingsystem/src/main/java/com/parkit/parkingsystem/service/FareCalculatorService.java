 package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket, Boolean discount){
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){    // exception sur cohérence date entree - date de sortie
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }

        long inHour = ticket.getInTime().getTime() ;        //result in milliseconds
        long outHour = ticket.getOutTime().getTime();       //result in milliseconds
        long duration = (outHour - inHour);    //result in milliseconds
        ticket.setPrice (0);

        if (discount == true) {
            duration =  duration - ( (5 * duration) / 100); //reduction de 5% si client regulier - result in millisecond - ok
        }

        if (duration > 1800000) {  
            switch (ticket.getParkingSpot().getParkingType()){
                case CAR: {
                    ticket.setPrice( ( duration / 3600000.00 ) * Fare.CAR_RATE_PER_HOUR );// perte de precision 
                    break;
                }
                case BIKE: {
                    ticket.setPrice( (duration / 3600000.00 ) * Fare.BIKE_RATE_PER_HOUR );
                    break;
                }
                default: throw new IllegalArgumentException("Unkown Parking Type");
            }
        } else {    // reduction si durée de stationnement est inférieure à 30 minutes 
            duration = 0;
        }
    }
}