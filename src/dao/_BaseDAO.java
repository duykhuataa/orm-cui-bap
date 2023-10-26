package dao;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class _BaseDAO<T> {

    protected Class<T> __modelClass;

    protected String tableName = "";
    protected String strictTableName = "";

    protected String _queryString = "";
    private ArrayList<String> _params = new ArrayList<>();

    protected boolean _isUpdateStatement = false;

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
        this.strictTableName = getStrictTableName(tableName);
    }

    private String getStrictTableName(String tName) {
        return '[' + tName + ']';
    }

    private String getStrictColName(String colName) {
        if (colName.charAt(0) != '[') {
            colName = strictTableName + "." + colName;
        }
        return colName;
    }

    public _BaseDAO select() {
        _queryString += "SELECT * FROM " + strictTableName + "\n";
        return this;
    }

    public _BaseDAO select(String... cols) {
        StringJoiner joiner = new StringJoiner(", ");
        for (String col : cols) {
            col = getStrictColName(col);
            joiner.add(col);
        }

        _queryString += "SELECT " + joiner.toString() + "\n";
        return this;
    }

    public _BaseDAO from(String... tables) {
        StringJoiner joiner = new StringJoiner(", ");
        for (String table : tables) {
            joiner.add(getStrictTableName(table));
        }

        _queryString += "FROM " + joiner.toString() + "\n";
        return this;
    }

    public _BaseDAO where(String col, T val) {
        return this.where(col, "=", val);
    }

    public _BaseDAO where(String col, String op, T val) {
        col = getStrictColName(col);
        _queryString += "WHERE " + col + " " + op + " ?\n";
        _params.add(String.valueOf(val));
        return this;
    }

    public _BaseDAO and(String col, T val) {
        return this.and(col, "=", val);
    }

    public _BaseDAO and(String col, String op, T val) {
        col = getStrictColName(col);
        _queryString += "\tAND " + col + " " + op + " ?\n";
        _params.add(String.valueOf(val));
        return this;
    }

    public _BaseDAO or(String col, T val) {
        return this.or(col, "=", val);
    }

    public _BaseDAO or(String col, String op, T val) {
        col = getStrictColName(col);
        _queryString += "\tOR " + col + " " + op + " ?\n";
        _params.add(String.valueOf(val));
        return this;
    }

    private String __strictDestTableName;

    private _BaseDAO _join(String type, String destTableName) {
        __strictDestTableName = getStrictTableName(destTableName);
        _queryString += "\t" + type + " JOIN " + __strictDestTableName + "\n";
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
        _queryString += "\t\tON " + strictTableName + "." + col + " = " + __strictDestTableName + "." + destCol + "\n";
        __strictDestTableName = "";
        return this;
    }

    public _BaseDAO orderBy(String col, String order) {
        col = getStrictColName(col);
        _queryString += "ORDER BY " + col + " " + order + "\n";
        return this;
    }

    public _BaseDAO paginate(int itemsPerPage, int page) {
        int offset = (page - 1) * itemsPerPage;
        _queryString += "OFFSET " + offset + " ROWS FETCH NEXT " + itemsPerPage + " ROWS ONLY";
        return this;
    }

    public _BaseDAO update(String col, String val) {
        _isUpdateStatement = true;

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

    public T getWhere(String col, String op, int val) {
        return this.getWhere(col, op, String.valueOf(val));
    }

    public T getWhere(String col, String op, String val) {
        ArrayList<T> ret = this.select()
                .where(col, op, val)
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

        _isUpdateStatement = true;
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
            _isUpdateStatement = true;

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

    private PreparedStatement _currPreStmt = null;

    private void _prepStmt() {
        try {
            _currPreStmt = connection.prepareStatement(_queryString);
            for (int i = 1; i <= _params.size(); i++) {
                _currPreStmt.setString(i, _params.get(i - 1));
            }

            _queryString = "";
            _params = new ArrayList<>();
        } catch (SQLException ex) {
            Logger.getLogger(_BaseDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public _BaseDAO printCurrentStatement() {
        String _outQueryString = _queryString;
        for (String param : _params) {
            _outQueryString = _outQueryString.replaceFirst("\\?", param);
        }
        System.out.print("===== BEGIN STATEMENT ===== \n" + _outQueryString);
        System.out.println("===== END OF STATEMENT =====");
        return this;
    }

    public ArrayList<T> exec() {
        ArrayList<T> resultList = new ArrayList<>();

        this._prepStmt();
        try {
            if (_isUpdateStatement) {
                _currPreStmt.executeUpdate();
                _isUpdateStatement = false;
            } else {
                ResultSet rs = _currPreStmt.executeQuery();
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

    public ResultSet execBigQuery() {
        this._prepStmt();
        try {
            return _currPreStmt.executeQuery();
        } catch (SQLException ex) {
            Logger.getLogger(_BaseDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public void printResultSet() {
        try {
            ResultSet rs = this.execBigQuery();
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();

            System.out.println("\n===== STATEMENT EXECUTED ===== \n===== BEGIN RESULTSET ====");
            for (int i = 1; i <= columnCount; i++) {
                String colName = rsmd.getColumnName(i);
                System.out.print(String.format("%-20s", colName));
            }
            System.out.println();

            while (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    String value = rs.getString(i);
                    value = (value == null) ? "<null>" : value;
                    System.out.print(String.format("%-20s", value));
                }
                System.out.println();
            }
            System.out.println("===== END OF RESULTSET =====");

        } catch (SQLException ex) {
            Logger.getLogger(_BaseDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
