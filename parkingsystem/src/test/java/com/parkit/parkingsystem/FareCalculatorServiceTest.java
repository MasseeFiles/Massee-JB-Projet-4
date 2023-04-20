package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;


import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Date;

import java.lang.Boolean;    

public class FareCalculatorServiceTest {   

    private static FareCalculatorService fareCalculatorService;     
    private Ticket ticket;      

    @BeforeAll
    private static void setUp() {     
        fareCalculatorService = new FareCalculatorService();    
    }

    @BeforeEach   //fait avant chaque test suivants - ok pour 1 ticket par cas testé
    private void setUpPerTest() {
        ticket = new Ticket();  
    }

    @Test
    public void calculateFareCar(){
        Date inTime = new Date();   
        inTime.setTime( System.currentTimeMillis() - (  60 * 60 * 1000) );      //initialisation date entrée à (date actuelle - 1h)
        Date outTime = new Date(); 
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);  
        Boolean discount = false;

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket , discount);  
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
                assertThrows(NullPointerException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                fareCalculatorService.calculateFare(ticket, false);
            }
        });
        //assertThrows(NullPointerException.class, () -> fareCalculatorService.calculateFare(ticket , false));
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
        
        assertThrows(IllegalArgumentException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                fareCalculatorService.calculateFare(ticket, false);
            }
        });
        //assertThrows(IllegalArgumentException.class, () -> fareCalculatorService.calculateFare(ticket , false));
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
        assertEquals( (0.75 * Fare.CAR_RATE_PER_HOUR) , ticket.getPrice()); 
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
