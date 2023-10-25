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

    protected String _queryString = "";
    private ArrayList<String> _params = new ArrayList<>();

    protected boolean isUpdateStatement = false;

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

    public _BaseDAO where(String col, String op, T val) {
        if (col.charAt(0) != '[') {
            col = strictTableName + "." + col;
        }
        _queryString += "WHERE " + col + " " + op + " ?\n";
        _params.add(String.valueOf(val));
        return this;
    }

    public _BaseDAO and(String col, T val) {
        return this.and(col, "=", val);
    }

    public _BaseDAO and(String col, String op, T val) {
        if (col.charAt(0) != '[') {
            col = strictTableName + "." + col;
        }
        _queryString += "AND " + col + " " + op + " ?\n";
        _params.add(String.valueOf(val));
        return this;
    }

    public _BaseDAO or(String col, String op, String val) {
        if (col.charAt(0) != '[') {
            col = strictTableName + "." + col;
        }
        _queryString += "OR " + col + " " + op + " ?\n";
        _params.add(val);
        return this;
    }

    private String __strictDestTableName;

    private _BaseDAO _join(String type, String destTableName) {
        __strictDestTableName = getStrict(destTableName);
        _queryString += type + " JOIN " + __strictDestTableName + "\n";
        return this;
    }

    public _BaseDAO join(String destTableName) {
        return this._join("", destTableName);
    }

    public _BaseDAO leftJoin(String destTableName) {
        return this._join("LEFT", destTableName);
    }

    public _BaseDAO innerJoin(String destTableName) {
        return this._join("INNER", destTableName);
    }

    public _BaseDAO rightJoin(String destTableName) {
        return this._join("RIGHT", destTableName);
    }

    public _BaseDAO on(String commonCol) {
        return on(commonCol, commonCol);
    }

    public _BaseDAO on(String col, String destCol) {
        _queryString += "ON " + strictTableName + "." + col + " = " + __strictDestTableName + "." + destCol + "\n";
        __strictDestTableName = "";
        return this;
    }

    public _BaseDAO orderBy(String col, String order) {
        if (col.charAt(0) != '[') {
            col = strictTableName + "." + col;
        }
        _queryString += "ORDER BY " + col + " " + order + "\n";
        return this;
    }

    public _BaseDAO paginate(int itemsPerPage, int page) {
        int offset = (page - 1) * itemsPerPage;
        _queryString += "OFFSET " + offset + " ROWS FETCH NEXT " + itemsPerPage + " ROWS ONLY";
        return this;
    }

    public _BaseDAO update(String col, String val) {
        isUpdateStatement = true;

        _queryString += "UPDATE " + strictTableName + "\n";
        _queryString += "SET " + col + " = ?\n";

        _params.add(val);
        return this;
    }

    public T getWhere(String col, int val) {
        return this.getWhere(col, String.valueOf(val));
    }

    public T getWhere(String col, String val) {
        ArrayList<T> ret = this.select()
                .where(col, "=", val)
                .exec();

        return ret.get(0);
    }

    public ArrayList<T> getAll() {
        return select().exec();
    }

    public void create(T model) {
        StringBuilder columns = new StringBuilder();
        StringBuilder placeholders = new StringBuilder();

        Field[] fields = model.getClass().getDeclaredFields();

        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            try {
                field.setAccessible(true);
                if (i != 0) {
                    Object value = field.get(model);

                    if (value != null) {
                        columns.append(field.getName()).append(", ");
                        placeholders.append("?, ");
                        _params.add(String.valueOf(value));
                        System.out.println(value);
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        columns.setLength(columns.length() - 2);
        placeholders.setLength(placeholders.length() - 2);

        isUpdateStatement = true;
        _queryString = String.format("INSERT INTO %s (%s) VALUES (%s)", strictTableName, columns, placeholders);
        this.exec();
    }

    public void update(T model) {
        Field[] fields = model.getClass().getDeclaredFields();
        StringBuilder updateSet = new StringBuilder();

        Field idField = fields[0];
        idField.setAccessible(true);

        try {
            Integer id = (Integer) idField.get(model);

            for (Field field : fields) {
                if (field != idField) {
                    field.setAccessible(true);
                    Object value = field.get(model);
                    if (value != null) {
                        updateSet.append(field.getName()).append(" = ?, ");
                        _params.add(String.valueOf(value));
                    }
                }
            }

            updateSet.setLength(updateSet.length() - 2);

            _queryString = String.format("UPDATE %s \nSET %s WHERE %s = ?",
                    strictTableName, updateSet, fields[0].getName());
            _params.add(String.valueOf(id));
            isUpdateStatement = true;

            exec();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private T mapResultSetToModel(ResultSet rs) {
        try {
            T model = __modelClass.newInstance();
            Field[] fields = __modelClass.getDeclaredFields();

            for (Field field : fields) {
                String fieldName = field.getName();
                Class<?> fieldType = field.getType();

                Object value = null;
                if (fieldType == int.class) {
                    value = rs.getInt(fieldName);
                } else if (fieldType == String.class) {
                    value = rs.getString(fieldName);
                } else if (fieldType == Float.class) {
                    value = rs.getFloat(fieldName);
                } else if (fieldType == boolean.class) {
                    value = rs.getBoolean(fieldName);
                } else if (fieldType == byte.class) {
                    value = rs.getByte(fieldName);
                } else if (fieldType == Timestamp.class) {
                    value = rs.getTimestamp(fieldName);
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

        this._queryString = "";
        this.isUpdateStatement = false;
        this._params = new ArrayList<>();

        return resultList;
    }
}
