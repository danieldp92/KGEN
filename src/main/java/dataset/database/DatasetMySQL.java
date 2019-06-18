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

    private Dataset dataset;

    private Connection connection;
    private PreparedStatement preparedStatement;
    private Statement statement;
    private ResultSet resultSet;

    private List<String> sqlWords;


    public DatasetMySQL (Dataset dataset) {
        this.dataset = dataset;

        this.sqlWords = new ArrayList<>();
        this.sqlWords.add("match");
    }

    public void createDatabase (String name) throws SQLException {
        createSchema(name);

        DatasetRow row = dataset.getRow(0);
        addTable(row);

        addRows(dataset);
    }

    public Dataset selectQuery (String query) {
        Dataset dataset = null;
        DatasetRow header = new DatasetRow();

        ArrayList<DatasetColumn> columns = new ArrayList<DatasetColumn>();

        try {
            connection = DBConnectionPool.getConnection();
            statement = connection.createStatement();
            resultSet = statement.executeQuery(query);

            //Generate the new header, reading result set metadata
            ResultSetMetaData metaData = resultSet.getMetaData();
            for (int i = 0; i < metaData.getColumnCount(); i++) {
                String attributeName = metaData.getColumnName(i+1);
                if (attributeName.contains(UNIQUE_TAG)) {
                    attributeName.replaceAll(UNIQUE_TAG, "");
                }

                Attribute headerAttribute = new Attribute(attributeName, null);

                boolean pk = ((com.mysql.cj.jdbc.result.ResultSetMetaData)metaData).getFields()[i].isPrimaryKey();
                headerAttribute.setPrimaryKey(pk);

                AttributeType attributeType = this.dataset.getHeaderAttribute(attributeName).getType();
                headerAttribute.setType(attributeType);

                header.add(headerAttribute);
            }

            //Generate attributes columns
            for (int i = 0; i < header.size(); i++) {
                columns.add(new DatasetColumn());
            }


            while (resultSet.next()) {
                for (int i = 0; i < header.size(); i++) {
                    Attribute attribute = (Attribute) header.get(i);
                    Attribute newAttribute = (Attribute) attribute.clone();

                    switch (attribute.getType().type) {
                        case AttributeType.TYPE_INT:
                            int intValue = resultSet.getInt(i+1);
                            if (resultSet.wasNull()) {
                                newAttribute.setValue(null);
                            } else {
                                newAttribute.setValue(intValue);
                            }

                            break;
                        case AttributeType.TYPE_DOUBLE:
                            double doubleValue = resultSet.getDouble(i+1);
                            if (resultSet.wasNull()) {
                                newAttribute.setValue(null);
                            } else {
                                newAttribute.setValue(doubleValue);
                            }

                            break;
                        default:
                            String stringValue = resultSet.getString(i+1);
                            newAttribute.setValue(stringValue);

                            break;
                    }

                    columns.get(i).add(newAttribute);
                }
            }

            dataset = new Dataset(header, columns);

        } catch (SQLException e) {
            System.out.println("CONNECTION FAILED");
            return null;
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }

                if (statement != null) {
                    statement.close();
                }

                if (connection != null) {
                    DBConnectionPool.releaseConnection(connection);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }


        return dataset;
    }


    private int createSchema (String schemaName) throws SQLException {
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

    private void addTable (DatasetRow header) throws SQLException {
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

    private void addRows (Dataset dataset) {
        try {
            connection = DBConnectionPool.getConnection();

            System.out.print("Row 0");
            for (int i = 0; i < dataset.getDatasetSize(); i++) {
                DatasetRow actualRow = dataset.getRow(i);
                String insertQuesry = generateInsertQuery(actualRow);

                System.out.print("\rRow " + (i+1));
                preparedStatement = connection.prepareStatement(insertQuesry);

                int indexOfNotNull = 1;
                for (int j = 0; j < actualRow.size(); j++) {
                    //Take the j attribute of the i row
                    Attribute attribute = (Attribute) actualRow.get(j);

                    if (attribute.getValue() != null) {
                        switch (attribute.getType().type) {
                            case AttributeType.TYPE_INT:
                                preparedStatement.setInt(indexOfNotNull, (Integer) attribute.getValue());
                                break;
                            case AttributeType.TYPE_DOUBLE:
                                preparedStatement.setDouble(indexOfNotNull, (Double) attribute.getValue());
                                break;
                            default:
                                preparedStatement.setString(indexOfNotNull, (String) attribute.getValue());
                                break;
                        }

                        indexOfNotNull++;
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

    private String generateInsertQuery (DatasetRow row) {
        String insert = "INSERT INTO " + TABLE_NAME + " (";
        String names = "";
        String values = "";

        for (Object attributeObj : row) {
            Attribute attribute = (Attribute) attributeObj;

            if (attribute.getValue() != null) {
                //Names section
                names += ", " + attribute.getName();

                if (sqlWords.contains(attribute.getName().toLowerCase())) {
                    names += UNIQUE_TAG;
                }

                //Values
                values += ", ?";
            }
        }

        insert += names.substring(2) + ") VALUES (" + values.substring(2) + ")";

        return insert;
    }
}
