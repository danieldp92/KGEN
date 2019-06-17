package dataset.database;

import dataset.beans.Attribute;
import dataset.beans.Dataset;
import dataset.beans.DatasetColumn;
import dataset.beans.DatasetRow;
import dataset.type.AttributeType;
import dataset.type.QuasiIdentifier;
import utils.DBConnectionPool;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatasetMySQL {
    private static final String UNIQUE_TAG = "_tag";
    private static final String TABLE_NAME = "dataset";

    private Connection connection;
    private PreparedStatement preparedStatement;
    private Statement statement;
    private ResultSet resultSet;

    private List<String> sqlWords;



    public DatasetMySQL () {
        this.sqlWords = new ArrayList<>();
        this.sqlWords.add("match");
    }

    public void createDatabase (Dataset dataset, String name) throws SQLException {
        createSchema(name);

        DatasetRow row = new DatasetRow();
        for (DatasetColumn column : dataset.getColumns()) {
            row.add(column.get(0));
        }

        addTable(row);

        addRows(dataset);
    }

    public boolean isCreated (String schemaName) throws SQLException {
        boolean isCreated = false;

        //String selectDBQuery= "SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = " + schemaName;

        try {
            connection = DBConnectionPool.getConnection();
            resultSet = connection.getMetaData().getCatalogs();

            while (resultSet.next()) {
                if (resultSet.getString(1).equals(schemaName)) {
                    isCreated = true;
                }

                System.out.println(resultSet.getString(1));
            }

        } finally {
            if (resultSet != null) {
                resultSet.close();
            }

            if (statement != null) {
                statement.close();
            }

            if (connection != null) {
                DBConnectionPool.releaseConnection(connection);
            }
        }

        return isCreated;
    }

    public int createSchema (String schemaName) throws SQLException {
        int result = 0;

        String createQuery = "CREATE DATABASE IF NOT EXISTS " + schemaName;
        try {
            connection = DBConnectionPool.getConnection();
            statement = connection.createStatement();

            result = statement.executeUpdate(createQuery);
        } finally {
            if (statement != null) {
                statement.close();
            }

            if (connection != null) {
                DBConnectionPool.releaseConnection(connection);
            }
        }

        return result;
    }

    public void addTable (DatasetRow header) throws SQLException {
        String tableQuery = generateQueryTable(header);

        System.out.println(tableQuery);

        try {
            connection = DBConnectionPool.getConnection();
            statement = connection.createStatement();

            statement.executeUpdate(tableQuery);
            connection.commit();
        } catch (SQLSyntaxErrorException ex) {
            if (!(ex.getMessage().contains("already exists"))) {
                ex.printStackTrace();
            }
        } finally {
            if (statement != null) {
                statement.close();
            }

            if (connection != null) {
                DBConnectionPool.releaseConnection(connection);
            }
        }

    }

    public void addRows (Dataset dataset) {
        try {
            connection = DBConnectionPool.getConnection();

            String insertQuesry = generateInsertQuery(dataset.getHeader());
            System.out.println(insertQuesry);

            for (int i = 0; i < dataset.getDatasetSize(); i++) {
                System.out.println("Row " + (i+1));
                preparedStatement = connection.prepareStatement(insertQuesry);

                for (int j = 0; j < dataset.getColumns().size(); j++) {
                    //Take the j attribute of the i row
                    Attribute attribute = (Attribute) dataset.getColumns().get(j).get(i);

                    switch (attribute.getType().type) {
                        case AttributeType.TYPE_INT:
                            if (attribute.getValue() != null) {
                                preparedStatement.setInt(j + 1, (Integer) attribute.getValue());
                            } else {
                                preparedStatement.setInt(j + 1, 0);
                            }

                            break;
                        case AttributeType.TYPE_DOUBLE:
                            if (attribute.getValue() != null) {
                                preparedStatement.setDouble(j + 1, (Double) attribute.getValue());
                            } else {
                                preparedStatement.setDouble(j + 1, 0);
                            }

                            break;
                        default:
                            if (attribute.getValue() != null) {
                                preparedStatement.setString(j + 1, (String) attribute.getValue());
                            } else {
                                preparedStatement.setString(j + 1, null);
                            }

                            break;
                    }
                }

                preparedStatement.execute();
                connection.commit();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }

                if (connection != null) {
                    DBConnectionPool.releaseConnection(connection);
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private String generateQueryTable(DatasetRow row) throws SQLSyntaxErrorException {
        String tableQuery = "CREATE TABLE " + TABLE_NAME + " ";

        String pk = null;

        String bracketQuery = "";

        for (Object attributeObj : row) {
            Attribute attribute = (Attribute) attributeObj;

            bracketQuery += ", " + attribute.getName();
            if (sqlWords.contains(attribute.getName().toLowerCase())) {
                bracketQuery += UNIQUE_TAG;
            }


            switch (attribute.getType().type) {
                case AttributeType.TYPE_INT:
                    bracketQuery += " INT";
                    break;
                case AttributeType.TYPE_DOUBLE:
                    bracketQuery += " DOUBLE";
                    break;
                default:
                    bracketQuery += " VARCHAR(200)";
                    break;
            }


            if (attribute.isPrimaryKey()) {
                if (pk != null) {
                    throw new SQLSyntaxErrorException("TOO MANY PRIMARY KEYS");
                }

                pk = attribute.getName();
                bracketQuery += " NOT NULL";
            } else {
                bracketQuery += " NULL";
            }
        }


        bracketQuery += ", PRIMARY KEY (" + pk + "))";
        bracketQuery = "(" + bracketQuery.substring(2);

        tableQuery += bracketQuery;

        return tableQuery;
    }

    private String generateInsertQuery (DatasetRow header) {
        String insert = "INSERT INTO " + TABLE_NAME + " (";
        String names = "";
        String values = "";

        for (Object attributeObj : header) {
            Attribute attribute = (Attribute) attributeObj;

            //Names section
            names += ", " + attribute.getName();

            if (sqlWords.contains(attribute.getName().toLowerCase())) {
                names += UNIQUE_TAG;
            }

            //Values
            values += ", ?";
        }

        insert += names.substring(2) + ") VALUES (" + values.substring(2) + ")";

        return insert;
    }
}
