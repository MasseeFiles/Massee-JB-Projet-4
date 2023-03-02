package com.parkit.parkingsystem.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;     // log4j fournit des fonctions permettant de gérer des traces et des historiques d'applications.

import java.sql.*;      // 

public class DataBaseConfig {       //classe correspondant à une base de données (sql)

    private static final Logger logger = LogManager.getLogger("DataBaseConfig"); //Creation d'un logger Databaseconfig : émission et stockage de messages suite à des événements.

    public Connection getConnection() throws ClassNotFoundException, SQLException {     // cree une session entre l'interface de connection et la base de données
        logger.info("Create DB connection");
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/test?serverTimezone=UTC&enabledTLSProtocols=TLSv1.2","root","codio");
    }

    public void closeConnection(Connection con){
        if(con!=null){
            try {
                con.close();
                logger.info("Closing DB connection");
            } catch (SQLException e) {
                logger.error("Error while closing connection",e);
            }
        }
    }

    public void closePreparedStatement(PreparedStatement ps) {
        if(ps!=null){
            try {
                ps.close();
                logger.info("Closing Prepared Statement");
            } catch (SQLException e) {
                logger.error("Error while closing prepared statement",e);
            }
        }
    }

    public void closeResultSet(ResultSet rs) {
        if(rs!=null){
            try {
                rs.close();
                logger.info("Closing Result Set");
            } catch (SQLException e) {
                logger.error("Error while closing result set",e);
            }
        }
    }
}
