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

            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
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

/* 
    il faut indiquer le comportement de getTicket quand appelé sur l'objet ticketDAO - ok dans @BeforeEach
    il faut indiquer qu'il y a eu un update de ticket , donc que (ticketDAO.updateTicket(ticket)) est à true - ok dans @BeforeEach
    il faut definir un objet parkingservice pour lancer methode processExitingVehicle() - ok dans @BeforeEach
    il faut indiquer que l'attribut isAvailable de l'objet parkingspot est à false - ok dans @BeforeEach
    il faut indiquer le comportement de getNbTicket quand il est appelé sur l'objet ticketDAO
*/

/*
    //  code avec tapSystemOut
        //GIVEN
        String text = tapSystemOut(() -> { //expression lambda pour definir le texte à comparer - tapSystemOut permet de recuperer 
        print("Heureux de vous revoir ! En tant qu’utilisateur régulier de notre parking, vous allez obtenir une remise de 5%");
        });

        Assert.assertEquals("Hello Baeldung Readers!!", text.trim());
}
        when(ticketDAO.getNbTicket(anyString())).thenReturn (3);    // cas d'un vehicule recurrent avec 3 passages enregistrés
        //WHEN
        parkingService.processIncomingVehicle();

        //THEN
        assertEquals("Heureux de vous revoir ! En tant qu’utilisateur régulier de notre parking, vous allez obtenir une remise de 5%", ); 
    }
*/

      //  code avec ArgumentCaptor

        //GIVEN
        ticketCaptor = ArgumentCaptor.forClass(Ticket.class); // meme chose que dans @captor?

        //WHEN
        parkingService.processIncomingVehicle();

        //THEN
        verify(ticketDAO).saveTicket(ticketCaptor.capture());
        Ticket savedTicket = ticketCaptor.getValue();

        assertEquals(savedTicket.getParkingSpot(), ticket.getParkingSpot());//ok
        assertEquals(savedTicket.getVehicleRegNumber(), ticket.getVehicleRegNumber());//ok
        //assertEquals(savedTicket.getPrice(), ticket.getPrice());
        assertEquals(savedTicket.getInTime(), ticket.getInTime());//ok
        // assertEquals(savedTicket.getOutTime(), ticket.getOutTime());
        assertEquals(savedTicket.getDiscount(), ticket.getDiscount());

        //ou   assertEquals(savedTicket , ticket);

    }

    @Test
    public void processExitingVehicleTest(){    
        // GIVEN
        ticketCaptor = ArgumentCaptor.forClass(Ticket.class);
/* 
    il faut indiquer le comportement de getTicket quand appelé sur l'objet ticketDAO - ok dans @BeforeEach
    il faut indiquer qu'il y a eu un update de ticket , donc que (ticketDAO.updateTicket(ticket)) est à true - ok dans @BeforeEach
    il faut definir un objet parkingservice pour lancer methode processExitingVehicle() - ok dans @BeforeEach
    il faut indiquer que l'attribut isAvailable de l'objet parkingspot est à false - ok dans @BeforeEach
    il faut indiquer le comportement de getNbTicket quand il est appelé sur l'objet ticketDAO
*/
        when(ticketDAO.getNbTicket(anyString())).thenReturn(3);

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
        assertEquals(savedTicket.getDiscount(), ticket.getDiscount());  //pour tester methode getNbTicket
        //  ou  assertEquals(true, ticket.getDiscount());

    }
}
