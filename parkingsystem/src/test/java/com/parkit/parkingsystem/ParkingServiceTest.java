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
import org.mockito.ArgumentCaptor;  //importation de ArgumentCaptor
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
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");

            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);  //place occupée est Car, 1
            Ticket ticket = new Ticket();
            ticket.setInTime(new Date(System.currentTimeMillis() - (60*60*1000)));  //tests effectués pour une durée de stationnement de 1 heure
            ticket.setParkingSpot(parkingSpot); // passage dans objet ticket du parkingSpot crée ligne 41
            ticket.setVehicleRegNumber("ABCDEF");

            when(ticketDAO.getTicket(anyString())).thenReturn(ticket);  // si appel de BDD par methode getTicket, retourne objet ticket crée dans le test
            when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true); // method update renvoie true : elle a bien bien mise a jour ticket 
            when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);  // pareil avec parkingSpot

            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        } catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("Failed to set up test mock objects");
        }
    }

    @Test
    public void testProcessIncomingVehicle(){

      //  test avec ArgumentCaptor
          //GIVEN
          ticketCaptor = ArgumentCaptor.forClass(Ticket.class); // meme chose que dans @Captor?
          when(ticketDAO.saveTicket(any(Ticket.class))).thenReturn(false);  //mock du resultat de l'appel de fonction - QUESTION : pourquoi method saveTicket() renvoie dans tous les cas false???
          when(ticketDAO.getNbTicket(anyString())).thenReturn(3); // on choisit le cas d'un vehicule recurrent avec 3 passages enregistrés - doit modifier discount pour FareCalculatorService

          //WHEN
          parkingService.processIncomingVehicle();

          //THEN
          verify(ticketDAO).saveTicket(ticketCaptor.capture());
          Ticket savedTicket = ticketCaptor.getValue();

          assertEquals(savedTicket.getParkingSpot(), ticket.getParkingSpot());//ok dans @BeforeEach
          assertEquals(savedTicket.getVehicleRegNumber(), ticket.getVehicleRegNumber());//ok dans @BeforeEach
          assertEquals(savedTicket.getInTime(), ticket.getInTime());//ok dans @BeforeEach
          //  assertEquals(savedTicket.getOutTime(), ticket.getOutTime());
          //  assertEquals(savedTicket.getPrice(), ticket.getPrice());

          //ou   assertEquals(savedTicket , ticket);
    }

    @Test
    public void processExitingVehicleTest(){    
        // GIVEN
        ticketCaptor = ArgumentCaptor.forClass(Ticket.class); // meme chose que dans @Captor?
        Date outTime = new Date();
        ticket.setOutTime(outTime);

        when(ticketDAO.getNbTicket(anyString())).thenReturn(3);
        when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);

        // WHEN
        parkingService.processExitingVehicle();

        // THEN
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class)); // verifie que la methode updateParking est appelée une seule fois 
        verify(ticketDAO).saveTicket(ticketCaptor.capture());
        Ticket savedTicket = ticketCaptor.getValue();
        
        assertEquals(savedTicket.getParkingSpot(), ticket.getParkingSpot());
        assertEquals(savedTicket.getVehicleRegNumber(), ticket.getVehicleRegNumber());
        assertEquals(savedTicket.getPrice(), ticket.getPrice());
        assertEquals(savedTicket.getInTime(), ticket.getInTime());
        assertEquals(savedTicket.getOutTime(), ticket.getOutTime());
        assertEquals(true, ticket.getDiscount());   // test de la prise en compte de vehicule reccurents - methode getNbTicket dans processExitingVehicle

          //ou   assertEquals(savedTicket , ticket);
    }

    @Test
    public void processExitingVehicleTestUnableUpdate(){    // resultat : doit verifier affichage du message "Unable to update ticket information. Error occurred"
        // GIVEN
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
    }
}
        

