package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
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
        dataBasePrepareService = new DataBasePrepareService();  
    }

    @BeforeEach
    private void setUpPerTest() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        dataBasePrepareService.clearDataBaseEntries();     //  methode pour vider BDD et remettre tt emplacements à libre avant chaque test
    }

    @AfterAll
    private static void tearDown(){
    }

    @Test
    public void testParkingACar(){
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        //WHEN
        parkingService.processIncomingVehicle();

        //THEN
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

        Date inTimeTest = new Date();
        inTimeTest.setTime( System.currentTimeMillis() - (  60 * 60 * 1000) ); //Configuration du ticket testé pour 1 heure de stationnement
        Ticket ticketTest = ticketDAO.getTicket("ABCDEF");
        ticketTest.setInTime(inTimeTest);
        ticketDAO.saveTicket(ticketTest);

        //WHEN
        parkingService.processExitingVehicle();

        //THEN
        Ticket ticketSaved = ticketDAO.getTicket("ABCDEF");
        Double savedPrice = ticketSaved.getPrice();
        assertTrue( savedPrice > 0 );

        Date savedOutTime = ticketSaved.getOutTime();
        assertNotNull(savedOutTime);
    }

        @Test
    public void testParkingLotExitRecurringUser(){  

        testParkingACar();  //  creation d'un premier ticket pour un vehicule immatriculé "ABCDEF"

        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        Date inTimeTest = new Date();
        inTimeTest.setTime( System.currentTimeMillis() - (  120 * 60 * 1000) ); // test pour un stationnement de 2h - recuperation du ticket le plus ancien avec (ticketDAO.getTicket)
        Ticket ticketTest = ticketDAO.getTicket("ABCDEF");
        ticketTest.setInTime(inTimeTest);
        ticketDAO.saveTicket(ticketTest); // creation d'un 2e ticket pour un vehicule immatriculé "ABCDEF"

        //WHEN
        parkingService.processExitingVehicle(); // pas de creation de ticket (ticketDAO.updateTicket)

        //THEN
        Ticket ticketSaved = ticketDAO.getTicket("ABCDEF");
        Double expectedSavedPrice = 2 * 1.5 * 0.95;
        Double actualSavedPrice = ticketSaved.getPrice();

        assertEquals(expectedSavedPrice, actualSavedPrice, 0.01);   //assertion avec delta à cause des variables "Date"
    }
}
