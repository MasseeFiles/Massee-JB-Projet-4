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

            //when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);  // pareil avec parkingSpot

            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

            System.setOut(new PrintStream(outputStreamCaptor)); 

        } catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("Failed to set up test mock objects");
        }
    }

    @Test
    public void testProcessIncomingVehicle(){

        when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(4);  
        when(inputReaderUtil.readSelection()).thenReturn(1); 
        when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);  
        when(ticketDAO.getNbTicket(anyString())).thenReturn(4);  // vehicule recurrent
       
        //WHEN
        parkingService.processIncomingVehicle();

        //THEN
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class)); //  test d'un appel de methode
        verify(ticketDAO, Mockito.times(1)).saveTicket(any(Ticket.class));
        verify(ticketDAO, Mockito.times(1)).getNbTicket(anyString());
        assertTrue(outputStreamCaptor.toString().contains("Heureux de vous revoir ! En tant qu’utilisateur régulier de notre parking, vous allez obtenir une remise de 5%")); //  test discount vehicule recurrent
    }

    @Test
    public void processExitingVehicleTest(){    // ne retourne rien donc on teste des appels de methodes

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
        verify(ticketDAO, Mockito.times(1)).getNbTicket(anyString()); 
        verify(ticketDAO, Mockito.times(1)).updateTicket(any(Ticket.class));
        verify(parkingSpotDAO, times(1)).updateParking(any(ParkingSpot.class)); // verifie que la methode updateParking est appelée une seule fois 
    }

    @Test
    public void processExitingVehicleTestUnableUpdate(){    
      /* Soit on teste affichage du message "Unable to update ticket information. Error occurred" - code dans else
        Soit on teste que parkingSpotDAO.updateParking(parkingSpot); n'est pas appelé - par defaut
      */
        
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false); 
        Ticket ticket = new Ticket();
        ticket.setInTime(new Date(System.currentTimeMillis() - (60*60*1000)));  //tests effectués pour une durée de stationnement de 1 heure
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber("ABCDEF");

        when(ticketDAO.getTicket(anyString())).thenReturn(ticket);  // si appel de BDD par methode getTicket, retourne objet ticket crée dans le test
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false);
   
        // WHEN
        parkingService.processExitingVehicle();

        // THEN
        verify(parkingSpotDAO, times(0)).updateParking(any(ParkingSpot.class)); 
        assertTrue(outputStreamCaptor.toString().contains("Unable to update ticket information. Error occurred"));
    }

    @Test
    public void testGetNextParkingNumberIfAvailable(){    

        when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);  // assignation de parkingnumber - actual
        when(inputReaderUtil.readSelection()).thenReturn(1);  //  assignation de parkingtype - actual
        ParkingSpot parkingSpotExpected = new ParkingSpot(1, ParkingType.CAR , true);
        
        // WHEN
        //parkingService.getNextParkingNumberIfAvailable();

        // THEN
        ParkingSpot parkingSpotActual = parkingService.getNextParkingNumberIfAvailable();
       //parkingspot retourné (actual doit avoir en attributs : id = 1 et IsAvailable = true;
        assertEquals( parkingSpotExpected.getId() , parkingSpotActual.getId());
        assertEquals( parkingSpotExpected.isAvailable() , parkingSpotActual.isAvailable());
    }
/*
    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberNotFound(){  
      //parkingSpotDAO.getNextAvailableSlot(parkingType) a mocker pour renvoi de valeur retour à 0 -> parkingspot reste à null

      }


*/

}
        

