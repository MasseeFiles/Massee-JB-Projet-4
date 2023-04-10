package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.service.FareCalculatorService;
import java.util.Date;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.when;

import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    private static void setUp() throws Exception{
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();  //  objet pour vider BDD et remettre tt emplacements à libre avant chaque test - before each 
    }

    @BeforeEach
    private void setUpPerTest() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        dataBasePrepareService.clearDataBaseEntries();
    }

    @AfterAll
    private static void tearDown(){
    }

    @Test
    public void testParkingACar(){
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        //WHEN
        parkingService.processIncomingVehicle();

        //TODO: check that a ticket is actualy saved in DB and Parking table is updated with availability
        Ticket ticketSaved = ticketDAO.getTicket("ABCDEF");
        assertNotNull(ticketSaved);    //  ticket = null au début de ticketDAO.getTicket()

        ParkingSpot parkingSpotSaved = ticketSaved.getParkingSpot();
        Boolean availabilityParkingSpotSaved = parkingSpotSaved.isAvailable();
        assertFalse(availabilityParkingSpotSaved);    
    }

    @Test
    public void testParkingLotExit(){
        testParkingACar();  //execution du test précedent dans ce test

        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        //Configuration du ticket testé pour 1 heure de stationnement
        Date inTimeTest = new Date();
        inTimeTest.setTime( System.currentTimeMillis() - (  60 * 60 * 1000) );
        Ticket ticketTest = ticketDAO.getTicket("ABCDEF");
        ticketTest.setInTime(inTimeTest);
        ticketDAO.saveTicket(ticketTest);

        //WHEN
        parkingService.processExitingVehicle();

        //TODO: check that the fare generated and out time are populated correctly in the database
        
        Ticket ticketSaved = ticketDAO.getTicket("ABCDEF");
        Double savedPrice = ticketSaved.getPrice();
        assertTrue( savedPrice > 0 );

        Date savedOutTime = ticketSaved.getOutTime();
        assertNotNull(savedOutTime);
    }

        @Test
    public void testParkingLotExitRecurringUser(){  //Sauvegarde de 2 tickets avec le meme numero d'immatriculation dans la base pour tester un vehicule recurrent
        testParkingACar();  //Sauvegarde du 1er ticket ???

        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
/*
        //Création d'un premier ticket de stationnement pour le vehicule immatriculé "ABCDEF"
        Ticket firstTicket = new Ticket();
        firstTicket.setVehicleRegNumber("ABCDEF");
        ticketDAO.saveTicket(firstTicket);

*/

        //Sauvegarde du 2e ticket - configuration pour 1 heure de stationnement
        Date inTimeTest = new Date();
        inTimeTest.setTime( System.currentTimeMillis() - (  60 * 60 * 1000) );
        Ticket ticketTest = ticketDAO.getTicket("ABCDEF");
        ticketTest.setInTime(inTimeTest);
        ticketDAO.saveTicket(ticketTest);

        //WHEN
        parkingService.processExitingVehicle();

        Ticket ticketSaved = ticketDAO.getTicket("ABCDEF");

        Double actualSavedPrice = ticketSaved.getPrice();
        assertTrue (1.42 < actualSavedPrice);
        assertTrue (actualSavedPrice <1.43);
    }
}
