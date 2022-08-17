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
        } catch (Exception e) {
            logger.error(e);
            e.printStackTrace();
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
            e.printStackTrace();
        }
        catch(SQLException e){
            logger.info("Couldn't connect to database.");
            e.printStackTrace();
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
            e.printStackTrace();
        }
        catch(SQLException e){
            logger.info("Couldn't close database.");
            e.printStackTrace();
        }
    }

    public synchronized static boolean checkExist(Long metaId, String type, String orderCode) {
        try {
            loadProperty();
            mysqlConnect();
            state = con.createStatement();
            result = state.executeQuery("select * from tbl_metas where order_code = '"+ orderCode +"' and type = '"+ type +"' and meta_id = "+ metaId);
            while(result.next()){
                return true;
            }
            return false;
        } catch(SQLException e){
            logger.error(e);
            e.printStackTrace();
        } finally {
            closeConnection();
        }
        return false;
    }

    public synchronized static void insertItem(Long metaId, String metaName, Long orderId, String urlDownload, String investDate, String type, String orderCode){
        try {
            loadProperty();
            mysqlConnect();
            //using PreparedStatement
            pstate = con.prepareStatement("insert into tbl_metas(meta_id, meta_name, order_id, url_download, invest_date, type, order_code)"+
                    "values(?,?,?,?,?,?,?)");
            pstate.setLong(1, metaId);
            pstate.setString(2, metaName);
            pstate.setLong(3, orderId);
            pstate.setString(4, urlDownload);
            pstate.setString(5, investDate);
            pstate.setString(6, type);
            pstate.setString(7, orderCode);
            int value = pstate.executeUpdate();
        } catch(SQLException e){
            logger.error(e);
            e.printStackTrace();
        } finally {
            closeConnection();
        }
    }

}
