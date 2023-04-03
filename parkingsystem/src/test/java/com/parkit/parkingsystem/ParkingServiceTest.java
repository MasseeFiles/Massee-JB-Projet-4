package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Captor;
import org.mockito.ArgumentCaptor;  
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import org.slf4j.Logger;    // logger
//import nl.altindag.log.LogCaptor;
import org.apache.logging.log4j.Logger;

import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
import org.slf4j.LoggerFactory;     //logger

import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;

import static org.mockito.Mockito.*;

import java.io.*;

@ExtendWith(MockitoExtension.class)

public class ParkingServiceTest {

    private static ParkingService parkingService;

    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    //private static final Logger logger = LoggerFactory.getLogger(ParkingServiceTest.class);   //Logger

    
    @Mock
    private static InputReaderUtil inputReaderUtil;

    @Mock
    private static ParkingSpotDAO parkingSpotDAO;
    
    @Mock
    private static TicketDAO ticketDAO;

    @Captor
    private static ArgumentCaptor<Ticket> ticketCaptor;
    //private static LogCaptor<ParkingService> logCaptor;

    @BeforeEach
    private void setUpPerTest() {

        try {
            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
            System.setOut(new PrintStream(outputStreamCaptor)); 

        } catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("Failed to set up test mock objects");
        }
    }

    @Test
    public void testProcessIncomingVehicle() throws Exception {

        when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(4);  
        when(inputReaderUtil.readSelection()).thenReturn(1); 
        when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);  
        when(ticketDAO.getNbTicket(anyString())).thenReturn(4);  // vehicule recurrent
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF"); 
       
        //WHEN
        parkingService.processIncomingVehicle();

        //THEN
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class)); //  test d'un appel de methode
        verify(ticketDAO, Mockito.times(1)).saveTicket(any(Ticket.class));
        verify(ticketDAO, Mockito.times(1)).getNbTicket(anyString());
        assertTrue(outputStreamCaptor.toString().contains("Heureux de vous revoir ! En tant qu’utilisateur régulier de notre parking, vous allez obtenir une remise de 5%")); //  test discount vehicule recurrent
    }

    @Test
    public void processExitingVehicleTest() throws Exception {    // ne retourne rien donc on teste des appels de methodes

        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false); 
        Ticket ticket = new Ticket();
        ticket.setInTime(new Date(System.currentTimeMillis() - (60*60*1000)));  //tests effectués pour une durée de stationnement de 1 heure
        ticket.setParkingSpot(parkingSpot); // passage dans objet ticket du parkingSpot crée ligne 41
        ticket.setVehicleRegNumber("ABCDEF");
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF"); 
        when(ticketDAO.getTicket(anyString())).thenReturn(ticket);  // si appel de BDD par methode getTicket, retourne objet ticket crée dans le test
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true); 

        // WHEN
        parkingService.processExitingVehicle();

        // THEN
        verify(ticketDAO, Mockito.times(1)).getTicket(anyString());
        verify(ticketDAO, Mockito.times(1)).getNbTicket(anyString()); 
        verify(ticketDAO, Mockito.times(1)).updateTicket(any(Ticket.class));
        verify(parkingSpotDAO, times(1)).updateParking(any(ParkingSpot.class)); // verifie que la methode updateParking est appelée une seule fois 
    }

    @Test
    public void processExitingVehicleTestUnableUpdate() throws Exception {    

    /* Soit on teste affichage du message "Unable to update ticket information. Error occurred" - code dans else
    Soit on teste que parkingSpotDAO.updateParking(parkingSpot); n'est pas appelé - par defaut
    */

        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false); 
        Ticket ticket = new Ticket();
        ticket.setInTime(new Date(System.currentTimeMillis() - (60*60*1000)));  //tests effectués pour une durée de stationnement de 1 heure
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber("ABCDEF");
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF"); 

        when(ticketDAO.getTicket(anyString())).thenReturn(ticket);  // si appel de BDD par methode getTicket, retourne objet ticket crée dans le test
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false);
   
        // WHEN
        parkingService.processExitingVehicle();

        // THEN
        verify(parkingSpotDAO, times(0)).updateParking(any(ParkingSpot.class)); 
        assertTrue(outputStreamCaptor.toString().contains("Unable to update ticket information. Error occurred"));
    }

    @Test
    public void testGetNextParkingNumberIfAvailable(){    //  retourne un parkingSpot

        when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);  // assignation de parkingnumber - actual
        when(inputReaderUtil.readSelection()).thenReturn(1);  //  assignation de parkingtype - actual
        ParkingSpot parkingSpotExpected = new ParkingSpot(1, ParkingType.CAR , true);

        /* THEN
        parkingspot retourné (actual) doit avoir en attributs : id = 1 et IsAvailable = true; */

        ParkingSpot parkingSpotActual = parkingService.getNextParkingNumberIfAvailable();
        assertEquals( parkingSpotExpected.getId() , parkingSpotActual.getId());
        assertEquals( parkingSpotExpected.isAvailable() , parkingSpotActual.isAvailable());
    }


    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberNotFound() {    //controle du message renvoyé par l'exception levée

        //parkingSpotDAO.getNextAvailableSlot(parkingType) a mocker pour renvoi de valeur retour à 0 -> parkingspot reste à null
        when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(-1);
        when(inputReaderUtil.readSelection()).thenReturn(2);
/*
// pas possible d'utiliser l'expression lambda
        Exception ex = assertThrows(Exception.class, () -> {       
        parkingService.getNextParkingNumberIfAvailable();
        });

        String expectedMessage = ("Error fetching parking number from DB. Parking slots might be full");
        String actualMessage = ex.getMessage();
        
        assertEquals( expectedMessage , actualMessage);
    }

    */

//essai avec recuperation d'exception - try/catch        
        Exception ex = new Exception();
        
        try {
            parkingService.getNextParkingNumberIfAvailable();
            } catch (Exception e) {
            ex = e ;             
            };

        String expectedMessage = ("Error fetching parking number from DB. Parking slots might be full");
        String actualMessage = ex.getMessage();

        assertEquals( expectedMessage , actualMessage);
        }

/*
// essai avec class anonyme

          Exception ex = assertThrows(Exception, new Executable() {
              @Override
              public void codeThrowingException() throws Throwable {
                parkingService.getNextParkingNumberIfAvailable();
              // throw new Exception("Error fetching parking number from DB. Parking slots might be full");
              }
          });

        String expectedMessage = ("Error fetching parking number from DB. Parking slots might be full");
        String actualMessage = ex.getMessage();
        
        assertEquals( expectedMessage , actualMessage);
    }
*/

    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberWrongArgument() { 
        when(inputReaderUtil.readSelection()).thenReturn(3);  //  lié à getVehicleType appelé dans getNextParkingNumberIfAvailable - 3 laisse parkingspot à null 

        //WHEN
        parkingService.getNextParkingNumberIfAvailable();
        assertTrue(outputStreamCaptor.toString().contains("Incorrect input provided"));

/*
//logcaptor

        assertTrue(logCaptor.getErrorLogs().contains("Error fetching next available parking slot"));

        OU

        assertThrows(IllegalArgumentException.class, () -> {
        parkingService.getNextParkingNumberIfAvailable();
        });*/
    }
}       

