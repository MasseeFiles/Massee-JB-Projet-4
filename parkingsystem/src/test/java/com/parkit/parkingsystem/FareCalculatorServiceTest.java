package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;

import java.lang.Boolean;    // Import pour utilisation de booleens

public class FareCalculatorServiceTest {    // declaration de la classe de test

    private static FareCalculatorService fareCalculatorService;     //declaration d'un objet fareCalculatorService - classe definie dans service
    // pourquoi instanciation ne se fait pas par un constructeur? - parce que fait dans @before
    private Ticket ticket;      //declaration d'un objet ticket - classe definie dans model

    @BeforeAll
    private static void setUp() {     //declaration method preenregistrée "setup" pour fixer environnement de depart - private : ok implementation
        fareCalculatorService = new FareCalculatorService();      // creation de l'objet farecalculatorservice (call constructeur)
    }

    @BeforeEach   //fait avant chaque test suivants - ok pour 1 ticket par cas testé
    private void setUpPerTest() {
        ticket = new Ticket();  // creation d'un objet ticket (call constructeur)
    }

    @Test
    public void calculateFareCar(){
        Date inTime = new Date();   //creation variable : date entrée du vehicule
        inTime.setTime( System.currentTimeMillis() - (  60 * 60 * 1000) );      //initialisation date entrée à (date actuelle - 1h)
        Date outTime = new Date();  //creation variable : date sortie du vehicule - par defaut, à la valeur de la date actuelle
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);      // creation de place de parking occupée
        Boolean discount = false;

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket , discount);  //calcul du prix à payer
        assertEquals(Fare.CAR_RATE_PER_HOUR, ticket.getPrice());
    }

    @Test
    public void calculateFareBike(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  60 * 60 * 1000) );
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket , false);
        assertEquals(Fare.BIKE_RATE_PER_HOUR , ticket.getPrice());
    }

    @Test
    public void calculateFareUnkownType(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  60 * 60 * 1000) );
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, null,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        assertThrows(NullPointerException.class, () -> fareCalculatorService.calculateFare(ticket , false));
    }

    @Test
    public void calculateFareBikeWithFutureInTime(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() + (  60 * 60 * 1000) );
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        assertThrows(IllegalArgumentException.class, () -> fareCalculatorService.calculateFare(ticket , false));
    }

    @Test
    public void calculateFareBikeWithLessThanOneHourParkingTime(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - ( 45 * 60 * 1000) );  //45 minutes parking time should give 3/4th parking fare
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket , false);
        assertEquals((0.75 * Fare.BIKE_RATE_PER_HOUR) , ticket.getPrice());     
    }

    @Test
    public void calculateFareCarWithLessThanOneHourParkingTime(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - ( 45 * 60 * 1000) );  //45 minutes parking time should give 3/4th parking fare
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket , false);
        assertEquals( (0.75 * Fare.CAR_RATE_PER_HOUR) , ticket.getPrice()); // Pourquoi price = 0  ?????????
    }

    @Test
    public void calculateFareCarWithMoreThanADayParkingTime(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  26 * 60 * 60 * 1000) );  //26 hours parking time should give 26 * parking fare per hour
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket , false);
        assertEquals( (26 * Fare.CAR_RATE_PER_HOUR) , ticket.getPrice());
    }

    @Test       // test stationnement gratuit - 30 minutes (voiture)
    public void calculateFareCarWithLessThan30minutesParkingTime(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  29 * 60 * 1000) );  //29 minutes parking time should give 0 * parking fare per hour (car fare)
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket , false);
        assertEquals( 0 , ticket.getPrice());
    }

    @Test       // test stationnement gratuit - 30 minutes (moto)
    public void calculateFareBikeWithLessThan30minutesParkingTime(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  29 * 60 * 1000) );  //29 minutes parking time should give 0 * parking fare per hour (bike fare)
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket , false);
        assertEquals( 0 , ticket.getPrice());
    }

    @Test       // test remise 5% client régulier - (voiture)
    public void calculateFareCarWithDiscount(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - ( 60 * 60 * 1000) );  //test sur stationnement de 1h
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        // ticket.setDiscount(discount);      // variable discount est à manipuler dans ticket 
        fareCalculatorService.calculateFare(ticket, true);
        assertEquals( 1 * Fare.CAR_RATE_PER_HOUR * 0.95, ticket.getPrice());
    }

    @Test      // test remise 5% client régulier - (moto)
    public void calculateFareBikeWithDiscount(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - ( 60 * 60 * 1000) );  //test sur stationnement de 1h
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket , true);
        assertEquals( 1 * Fare.BIKE_RATE_PER_HOUR * 0.95, ticket.getPrice());
    }
}
