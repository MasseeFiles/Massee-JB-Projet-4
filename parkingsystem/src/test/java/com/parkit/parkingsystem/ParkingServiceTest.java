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

@ExtendWith(MockitoExtension.class)

public class ParkingServiceTest {

    private static ParkingService parkingService;

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

            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false); 
            Ticket ticket = new Ticket();
            ticket.setInTime(new Date(System.currentTimeMillis() - (60*60*1000)));  //tests effectués pour une durée de stationnement de 1 heure
            ticket.setParkingSpot(parkingSpot); // passage dans objet ticket du parkingSpot crée ligne 41
            ticket.setVehicleRegNumber("ABCDEF");

            when(ticketDAO.getTicket(anyString())).thenReturn(ticket);  // si appel de BDD par methode getTicket, retourne objet ticket crée dans le test
            //when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true); // method update renvoie true : elle a bien bien mise a jour ticket 
            //when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);  // pareil avec parkingSpot

            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        } catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("Failed to set up test mock objects");
        }
    }
/*
    @Test
    public void testProcessIncomingVehicle(){
          //GIVEN
         // Ticket ticket = new Ticket();
         when(ticketDAO.getNbTicket(anyString())).thenReturn(3); // affichage ok de message d'accueil

        String expectedMessage = ("Heureux de vous revoir ! En tant qu’utilisateur régulier de notre parking, vous allez obtenir une remise de 5%");
        String actualMessage = tapSystemOut(() -> {
        parkingService.processIncomingVehicle(); // code affichant message a comparer
        });

          //WHEN
          parkingService.processIncomingVehicle();

          //THEN
          assertEquals(expectedMessage, actualMessage);
          //verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class)); //  test d'un appel de methode
          //verify(ticketDAO, Mockito.times(1)).getNbTicket(anyString());

          verify(ticketDAO).saveTicket(ticketCaptor.capture()); // test de valeur d'un parametre passé à une methode
          Ticket savedTicket = new Ticket();
          savedTicket = ticketCaptor.getValue();
          assertEquals(ticket.getVehicleRegNumber() , savedTicket.getVehicleRegNumber());
    }
*/
    @Test
    public void processExitingVehicleTest(){    // ne retourne rien donc on teste des appels de methodes
        // GIVEN
        Ticket ticket = new Ticket();
/*
        ticket.setInTime(new Date(System.currentTimeMillis() - (60*60*1000)));  //tests effectués pour une durée de stationnement de 1 heure
        Date outHour = new Date();
        ticket.setOutTime(outHour);
*/
        when(ticketDAO.getNbTicket(anyString())).thenReturn(3);
        when(ticketDAO.getTicket(anyString())).thenReturn(ticket);

        // WHEN
        parkingService.processExitingVehicle();

        // THEN
        verify(ticketDAO, Mockito.times(1)).getTicket(anyString());
        verify(ticketDAO, times(1)).getNbTicket(anyString()); //verifie qu'on regarde bien si un vehicule est recurrent ou pas
        verify(parkingSpotDAO, times(0)).updateParking(any(ParkingSpot.class)); // verifie que la methode updateParking est appelée une seule fois 
    }
/*
    @Test
    public void processExitingVehicleTestUnableUpdate(){    // resultat : doit verifier affichage du message "Unable to update ticket information. Error occurred"
        // GIVEN
        Ticket ticket = new Ticket();

        String expectedMessage = ("Unable to update ticket information. Error occurred") ;
        String actualMessage = tapSystemOut(() -> {
        parkingService.processExitingVehicle(); // code affichant message a comparer
        });
        Date outTime = new Date();
        ticket.setOutTime(outTime);

        when(ticketDAO.getNbTicket(anyString())).thenReturn(3);
        when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false);  //   a tester : pb avec BDD

        // WHEN
        parkingService.processExitingVehicle();

        // THEN
        assertEquals(expectedMessage, actualMessage);

        //ou assertEquals(false , parkingSpot.getIsAvailable());
    }*/
}
        

