package dao;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class _BaseDAO<T> {

    protected Class<T> __modelClass;

    protected String tableName = "";
    protected String strictTableName = "";

    protected boolean isUpdateStatement = false;
    protected String _queryString = "";
    private ArrayList<String> _params = new ArrayList<>();

    protected Connection connection;

    public _BaseDAO() {
        String hostname = "localhost";
        String port = "1433";
        String name = "local-isp392-main-db";
        String username = "sa";
        String password = "sa";

        try {
            String url = "jdbc:sqlserver://" + hostname + ":" + port + ";database=" + name;
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            this.connection = DriverManager.getConnection(url, username, password);
        } catch (ClassNotFoundException | SQLException ex) {
        }
    }

    public _BaseDAO(Class<T> modelClass, String tableName) {
        this();

        this.__modelClass = modelClass;

        this.tableName = tableName;
        this.strictTableName = getStrict(tableName);
    }

    private String getStrict(String tName) {
        return '[' + tName + ']';
    }

    public _BaseDAO select() {
        _queryString += "SELECT * FROM " + strictTableName + "\n";
        return this;
    }

    public _BaseDAO where(String col, String op, String val) {
        if (col.charAt(0) != '[') {
            col = strictTableName + "." + col;
        }
        _queryString += "WHERE " + col + " " + op + " ?\n";
        _params.add(val);
        return this;
    }

    public _BaseDAO and(String col, String op, String val) {
        _queryString += "AND " + col + " " + op + " ?\n";
        _params.add(val);
        return this;
    }

    public _BaseDAO or(String col, String op, String val) {
        _queryString += "OR " + col + " " + op + " ?\n";
        _params.add(val);
        return this;
    }

    private String __destTableName;

    public _BaseDAO innerJoin(String destTableName) {
        __destTableName = getStrict(destTableName);
        _queryString += "INNER JOIN " + __destTableName + "\n";
        return this;
    }

    public _BaseDAO on(String sameCol) {
        return on(sameCol, sameCol);
    }
    
    public _BaseDAO on(String col, String destCol) {
        _queryString += "ON " + strictTableName + "." + col + " = " + __destTableName + "." + destCol + "\n";
        __destTableName = "";
        return this;
    }

    public _BaseDAO orderBy(String col, String condition) {
        _queryString += "ORDER BY " + col + " " + condition + "\n";
        return this;
    }

    public ArrayList<T> getAll() {
        return select().exec();
    }

    public _BaseDAO update(String col, String val) {
        isUpdateStatement = true;

        _queryString += "UPDATE " + tableName + "\n";
        _queryString += "SET " + col + " = ?\n";

        _params.add(val);
        return this;
    }

    private T mapResultSetToModel(ResultSet resultSet) {
        try {
            T model = __modelClass.newInstance();
            Field[] fields = __modelClass.getDeclaredFields();

            for (Field field : fields) {
                String fieldName = field.getName();
                Class<?> fieldType = field.getType();

                Object value = null;
                if (fieldType == int.class) {
                    value = resultSet.getInt(fieldName);
                } else if (fieldType == String.class) {
                    value = resultSet.getString(fieldName);
                } else if (fieldType == Float.class) {
                    value = resultSet.getFloat(fieldName);
                } else if (fieldType == boolean.class) {
                    value = resultSet.getBoolean(fieldName);
                } else if (fieldType == byte.class) {
                    value = resultSet.getByte(fieldName);
                } else if (fieldType == Timestamp.class) {
                    value = resultSet.getTimestamp(fieldName);
                }
                field.setAccessible(true);
                field.set(model, value);
            }

            return model;
        } catch (SQLException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ArrayList<T> exec() {
        System.out.println("Prepared statement: \n" + _queryString);
        ArrayList<T> resultList = new ArrayList<>();

        try ( PreparedStatement ps = connection.prepareStatement(_queryString);) {
            for (int i = 1; i <= _params.size(); i++) {
                ps.setString(i, _params.get(i - 1));
            }

            if (isUpdateStatement) {
                ps.executeUpdate();
            } else {
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    T model = mapResultSetToModel(rs);
                    resultList.add(model);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(_BaseDAO.class.getName()).log(Level.SEVERE, null, ex);
        }

        return resultList;
    }
}
