package com.ksf.job.contract.database;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.sql.*;
import java.util.Properties;

public class MysqlConnection {

    private static final Logger logger = LogManager.getLogger();

    private static String driver = "com.mysql.jdbc.Driver";
    private static String connection;
    private static String user;
    private static String password;

    private static Connection con = null;
    private static Statement state = null;
    private static ResultSet result;
    private static PreparedStatement pstate;

    public static void loadProperty() {
        Properties prop = new Properties();
        String fileName = "app.cfg";
        try (FileInputStream fis = new FileInputStream(fileName)) {
            prop.load(fis);
            connection = prop.getProperty("mysql.connection");
            user = prop.getProperty("mysql.user");
            password = prop.getProperty("mysql.password");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void mysqlConnect(){
        try{
            System.setProperty("file.encoding" , "UTF-8");
            Class.forName(driver);
            con = DriverManager.getConnection(connection, user, password);
        }
        catch(ClassNotFoundException e){
            logger.info("Couldn't load driver.");
        }
        catch(SQLException e){
            logger.info("Couldn't connect to database.");
        }
    }

    public static void closeConnection(){
        try{
            if(!con.isClosed()){
                con.close();
            }
        }
        catch(NullPointerException e){
            logger.info("Couldn't load driver.");
        }
        catch(SQLException e){
            logger.info("Couldn't close database.");
        }
    }

    public static boolean checkExist(Long metaId) {
        try {
            loadProperty();
            mysqlConnect();
            state = con.createStatement();
            result = state.executeQuery("select * from tbl_metas where meta_id = "+ metaId);
            while(result.next()){
                return true;
            }
            return false;
        } catch(SQLException e){
            logger.info("Query error.");
        } finally {
            closeConnection();
        }
        return false;
    }

    public static void insertItem(Long metaId, String metaName, Long orderId, String urlDownload, String investDate, String type){
        try {
            loadProperty();
            mysqlConnect();
            //using PreparedStatement
            pstate = con.prepareStatement("insert into tbl_metas(meta_id, meta_name, order_id, url_download, invest_date, type)"+
                    "values(?,?,?,?,?,?)");
            pstate.setLong(1, metaId);
            pstate.setString(2, metaName);
            pstate.setLong(3, orderId);
            pstate.setString(4, urlDownload);
            pstate.setString(5, investDate);
            pstate.setString(6, type);
            int value = pstate.executeUpdate();

            logger.info("OK, 1 row "+ metaId);
        } catch(SQLException e){
            logger.info("Query error.");
        } finally {
            closeConnection();
        }
    }

}
