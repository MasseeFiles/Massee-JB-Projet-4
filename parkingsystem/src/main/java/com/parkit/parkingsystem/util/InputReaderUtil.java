package com.parkit.parkingsystem.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Scanner;

public class InputReaderUtil {

    private static Scanner scan = new Scanner(System.in);       //creation objet scanner - permet à l'appli de lire données entrées au clavier (system.in)
    private static final Logger logger = LogManager.getLogger("InputReaderUtil");       // Logger : message pour indiqur qu'action realisée dans le rapport

    public int readSelection() {
        try {
            int input = Integer.parseInt(scan.nextLine());      //lecture et conversion de numeros inscrits sur plaque en entiers
            return input;
        }catch(Exception e){
            logger.error("Error while reading user input from Shell", e);
            System.out.println("Error reading input. Please enter valid number for proceeding further");
            return -1;
        }
    }

    public String readVehicleRegistrationNumber() throws Exception {       // Methode permettant la lecture de la plaque immatriculationdu vehicule
        try {
            String vehicleRegNumber= scan.nextLine();       // Recuperation du numero d'immatriculation du vehicule par lecture de clavier
            if(vehicleRegNumber == null || vehicleRegNumber.trim().length()==0) {
                throw new IllegalArgumentException("Invalid input provided");
            }
            return vehicleRegNumber;
        }catch(Exception e){
            logger.error("Error while reading user input from Shell", e);       //shell - carrosserie
            System.out.println("Error reading input. Please enter a valid string for vehicle registration number");
            throw e;
        }
    }


}
