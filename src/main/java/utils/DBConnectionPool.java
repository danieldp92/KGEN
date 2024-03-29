package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author Daniel
 */
public class DBConnectionPool {
    private static final String PROPERTIIES_PATH = System.getProperty("user.dir") + File.separator + "config" + File.separator + "connectionProperties.txt";

    /*
     * This code prepare the db connection pool. In particular, it creates the
     * free connections queue and defines the db properties.
     */
    static {
        freeDbConnections = new ArrayList<Connection>();
        try {
            DBConnectionPool.loadDbProperties();
            DBConnectionPool.loadDbDriver();
        } catch (ClassNotFoundException e) {
            System.out.println("DB DRIVER NOT FOUND!");
            System.exit(1);
        } catch (IOException e) {
            System.out.println("DB CONNECTION POOL ERROR!");
            System.exit(2);
        }
    }

    /**
     * The db properties (driver, url, login, and password)
     */
    private static Properties dbProperties;

    /**
     * The free connection queue
     */
    private static List<Connection> freeDbConnections;

    /**
     * Returns a free db connection accessing to the free db connection queue.
     * If the queue is empty a new db connection will be created.
     * 
     * @return A db connection
     * @throws SQLException
     */
    public static synchronized Connection getConnection() throws SQLException {
        Connection connection;

        if (!freeDbConnections.isEmpty()) {
            // Extract a connection from the free db connection queue
            connection = freeDbConnections.get(0);
            DBConnectionPool.freeDbConnections.remove(0);

            try {
                // If the connection is not valid, a new connection will be
                // analyzed
                if (connection.isClosed())
                    connection = DBConnectionPool.getConnection();
            } catch (SQLException e) {
                connection = DBConnectionPool.getConnection();
            }
        }
        else
            // The free db connection queue is empty, so a new connection will
            // be created
            connection = DBConnectionPool.createDBConnection();

        return connection;
    }

    /**
     * Releases the connection represented by <code>pReleasedConnection</code>
     * parameter
     * 
     * @param pReleasedConnection
     *        The db connection to release
     */
    public static synchronized void releaseConnection(
            Connection pReleasedConnection) {

        // Add the connection to the free db connection queue
        DBConnectionPool.freeDbConnections.add(pReleasedConnection);
    }

    /**
     * Creates a new db connection
     * 
     * @return A db connection
     * @throws SQLException
     */
    private static Connection createDBConnection() throws SQLException {
        Connection newConnection = null;

        // Create a new db connection using the db properties
        // newConnection = DriverManager.getConnection(
        //    "jdbc:mysql://localhost/resources", "root", "");

        newConnection = DriverManager.getConnection(
                DBConnectionPool.dbProperties.getProperty("url"),
                DBConnectionPool.dbProperties.getProperty("username"),
                DBConnectionPool.dbProperties.getProperty("password"));

        newConnection.setAutoCommit(false);

        return newConnection;
    }

    private static void loadDbDriver() throws ClassNotFoundException {
        Class.forName(DBConnectionPool.dbProperties.getProperty("driver"));
    }

    /**
     * Loads the db properties
     * 
     * @throws IOException
     */
    private static void loadDbProperties() throws IOException {
        try {
        InputStream fileProperties = new FileInputStream(PROPERTIIES_PATH);
        DBConnectionPool.dbProperties = new Properties();

        DBConnectionPool.dbProperties.load(fileProperties);
        } catch (IOException e) {
            DBConnectionPool.loadDefaultDbProperties();
        }
    }
    
    /**
     * Loads the default properties for the database access
     * 
     * @throws IOException
     */
    private static void loadDefaultDbProperties() {
        DBConnectionPool.dbProperties = new Properties();
        
        DBConnectionPool.dbProperties.setProperty("driver", "com.mysql.cj.jdbc.Driver");
        DBConnectionPool.dbProperties.setProperty("url", "");
        DBConnectionPool.dbProperties.setProperty("username", "");
        DBConnectionPool.dbProperties.setProperty("password", "");
    }
}
