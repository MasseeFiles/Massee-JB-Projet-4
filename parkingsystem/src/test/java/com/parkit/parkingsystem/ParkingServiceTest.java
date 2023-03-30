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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;

import static org.mockito.Mockito.*;

import java.io.*;

@ExtendWith(MockitoExtension.class)

public class ParkingServiceTest {

    private static ParkingService parkingService;

    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @Mock
    private static ParkingSpotDAO parkingSpotDAO;
    
    @Mock
    private static TicketDAO ticketDAO;

    @Captor
    private static ArgumentCaptor<Ticket> ticketCaptor;

    @BeforeEach
    private void setUpPerTest() {
        try {
          
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");  // pour appel de methode getVehicleRegNumber ex : processIncomingVehicle
/*
            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false); 
            Ticket ticket = new Ticket();
            ticket.setInTime(new Date(System.currentTimeMillis() - (60*60*1000)));  //tests effectués pour une durée de stationnement de 1 heure
            ticket.setParkingSpot(parkingSpot); // passage dans objet ticket du parkingSpot crée ligne 41
            ticket.setVehicleRegNumber("ABCDEF");

            when(ticketDAO.getTicket(anyString())).thenReturn(ticket);  // si appel de BDD par methode getTicket, retourne objet ticket crée dans le test
*/

            //when(ticketDAO.getNbTicket(anyString())).thenReturn(3); //test pour un client recurrent
            when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);  // pareil avec parkingSpot

            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

            System.setOut(new PrintStream(outputStreamCaptor)); 

        } catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("Failed to set up test mock objects");
        }
    }
  /*  
    @AfterEach
        private void tearDown() {
        System.setOut(standardOut);
    }
*/
    @Test
    public void testProcessIncomingVehicle(){
 // 2 conditions : (parkingSpot !=null && parkingSpot.getId() > 0)

        when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(4);  // 2 conditions : (parkingSpot !=null && parkingSpot.getId() > 0)
        when(inputReaderUtil.readSelection()).thenReturn(1); 
       
        String expectedMessage = ("Heureux de vous revoir ! En tant qu’utilisateur régulier de notre parking, vous allez obtenir une remise de 5%");

        //WHEN
        parkingService.processIncomingVehicle();

        //THEN
        
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class)); //  test d'un appel de methode
        verify(ticketDAO, Mockito.times(1)).saveTicket(any(Ticket.class));
        verify(ticketDAO, Mockito.times(1)).getNbTicket(anyString());

        //assertEquals(expectedMessage, outputStreamCaptor.toString());
        
/*
        verify(ticketDAO).saveTicket(ticketCaptor.capture()); // test de valeur d'un parametre passé à une methode
        Ticket savedTicket = new Ticket();
        savedTicket = ticketCaptor.getValue();
        assertEquals(("ABCDEF") , savedTicket.getVehicleRegNumber());
*/
    }

    @Test
    public void processExitingVehicleTest(){    // ne retourne rien donc on teste des appels de methodes



            //when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");  // pour appel de methode getVehicleRegNumber ex : processIncomingVehicle

            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false); 
            Ticket ticket = new Ticket();
            ticket.setInTime(new Date(System.currentTimeMillis() - (60*60*1000)));  //tests effectués pour une durée de stationnement de 1 heure
            ticket.setParkingSpot(parkingSpot); // passage dans objet ticket du parkingSpot crée ligne 41
            ticket.setVehicleRegNumber("ABCDEF");

            when(ticketDAO.getTicket(anyString())).thenReturn(ticket);  // si appel de BDD par methode getTicket, retourne objet ticket crée dans le test









        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true); 
        // WHEN
        parkingService.processExitingVehicle();

        // THEN
        verify(ticketDAO, Mockito.times(1)).getTicket(anyString());
        verify(ticketDAO, Mockito.times(1)).getNbTicket(anyString()); //verifie qu'on regarde bien si un vehicule est recurrent ou pas
        verify(ticketDAO, Mockito.times(1)).updateTicket(any(Ticket.class));
        verify(parkingSpotDAO, times(1)).updateParking(any(ParkingSpot.class)); // verifie que la methode updateParking est appelée une seule fois 
    }
/*
    @Test
    public void processExitingVehicleTestUnableUpdate(){    // resultat : doit verifier affichage du message "Unable to update ticket information. Error occurred"
        when(ticketDAO.updateTicket(any(Ticket.class)).thenReturn(false);
        // GIVEN
        String expectedMessage = ("Unable to update ticket information. Error occurred") ;
   
        // WHEN
        parkingService.processExitingVehicle();

        // THEN
        assertEquals(expectedMessage, outputStreamCaptor.toString());
    }
*/
}
        

