package com.spotifyxp.deps.de.werwolf2303.sql;

import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class SQLTable implements SQLElement {
    private SQLSession.SQLSessionPrivate sqlSession;
    private final String name;

    public SQLTable(String name) {
        this.name = name;
    }

    public boolean exists() throws SQLException {
        boolean ret = false;
        sqlSession.connect();
        DatabaseMetaData metaData = sqlSession.getConnection().getMetaData();
        try (ResultSet resultSet = metaData.getTables(null, null, name, null)) {
            try {
                if (resultSet.getString("TABLE_NAME").equals(name)) {
                    ret = true;
                }
            } catch (NullPointerException ignored) {
            }
        }
        sqlSession.disconnect();
        return ret;
    }

    public boolean tryExists() {
        try {
            return exists();
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean create(SQLEntryPair... pairs) throws SQLException {
        sqlSession.connect();
        StringBuilder command = new StringBuilder();
        command.append("CREATE TABLE ").append(name).append("(");
        int counter = 1;
        for (SQLEntryPair pair : pairs) {
            String additional = "";
            if (!pair.canBeNull()) {
                additional = " NOT NULL";
            }
            if (counter == pairs.length) {
                command.append(pair.getName()).append(" ").append(pair.getType().getRealType()).append(additional);
            } else {
                command.append(pair.getName()).append(" ").append(pair.getType().getRealType()).append(additional).append(",").append(" ");
            }
            counter++;
        }
        command.append(");");
        sqlSession.getConnection().createStatement().execute(command.toString());
        sqlSession.disconnect();
        return exists();
    }

    public boolean tryCreate(SQLEntryPair... pairs) {
        try {
            return create(pairs);
        } catch (SQLException e) {
            return false;
        }
    }

    public ArrayList<SQLEntryPair> getTableDefinition() throws SQLException {
        sqlSession.connect();
        ArrayList<SQLEntryPair> pairs = new ArrayList<>();
        DatabaseMetaData metaData = sqlSession.getConnection().getMetaData();
        try (ResultSet resultSet = metaData.getColumns(null, null, name, null)) {
            while (resultSet.next()) {
                String columnName = resultSet.getString("COLUMN_NAME");
                String dataType = resultSet.getString("TYPE_NAME");
                pairs.add(new SQLEntryPair(columnName, resultSet.getInt("NULLABLE") == DatabaseMetaData.columnNullable, dataType));
            }
        }
        sqlSession.disconnect();
        return pairs;
    }

    public ArrayList<SQLEntryPair> tryGetTableDefinition() {
        try {
            return getTableDefinition();
        } catch (SQLException e) {
            return new ArrayList<>();
        }
    }

    public ArrayList<SQLEntryPair> parseTable(int amount, int offset) throws SQLException {
        sqlSession.connect();
        ArrayList<SQLEntryPair> pairs = new ArrayList<>();
        ArrayList<SQLEntryPair> tableDefinition = getTableDefinition();
        sqlSession.connect();
        ResultSet resultSet = sqlSession.getConnection().createStatement().executeQuery("SELECT * FROM " + name);
        int counter = 0;
        while (resultSet.next()) {
            if (counter == amount + offset) {
                break;
            }
            if (counter >= offset) {
                for (SQLEntryPair pair : tableDefinition) {
                    SQLEntryPair p = null;
                    switch (pair.getType()) {
                        case DATE:
                            p = new SQLEntryPair(pair.getName(), resultSet.getDate(pair.getName()));
                            break;
                        case STRING:
                            p = new SQLEntryPair(pair.getName(), resultSet.getString(pair.getName()));
                            break;
                        case BOOLEAN:
                            p = new SQLEntryPair(pair.getName(), resultSet.getBoolean(pair.getName()));
                            break;
                        case INTEGER:
                            p = new SQLEntryPair(pair.getName(), resultSet.getInt(pair.getName()));
                            break;
                    }
                    pairs.add(p);
                }
            }
            counter++;
        }
        sqlSession.disconnect();
        return pairs;
    }

    public ArrayList<SQLEntryPair> tryParseTable(int amount, int offset) {
        try {
            return parseTable(amount, offset);
        } catch (SQLException e) {
            return new ArrayList<>();
        }
    }

    public void insertIntoTable(SQLInsert... inserts) throws SQLException {
        sqlSession.connect();
        StringBuilder sqlcom = new StringBuilder();
        sqlcom.append("INSERT INTO ").append(name).append(" (");
        int counter = 1;
        ArrayList<SQLEntryPair> defs = getTableDefinition();
        sqlSession.connect();
        for (SQLEntryPair p : defs) {
            if (counter == defs.size()) {
                sqlcom.append(p.getName());
            } else {
                sqlcom.append(p.getName()).append(", ");
            }
            counter++;
        }
        sqlcom.append(") VALUES (");
        for (int i = 0; i < inserts.length; i++) {
            if (i == inserts.length - 1) {
                sqlcom.append("?");
            } else {
                sqlcom.append("?, ");
            }
        }
        sqlcom.append(")");
        PreparedStatement statement = sqlSession.getConnection().prepareStatement(sqlcom.toString());
        counter = 1;
        for (SQLInsert in : inserts) {
            switch (in.getType()) {
                case BOOLEAN:
                    statement.setBoolean(counter, in.getBoolean());
                    break;
                case STRING:
                    statement.setString(counter, in.getString());
                    break;
                case DATE:
                    statement.setDate(counter, in.getDate());
                    break;
                case INTEGER:
                    statement.setInt(counter, in.getInteger());
                    break;
            }
            counter++;
        }
        statement.executeUpdate();
        sqlSession.disconnect();
    }

    public void tryInsertIntoTable(SQLInsert... inserts) {
        try {
            insertIntoTable(inserts);
        } catch (SQLException ignored) {
        }
    }

    public void clearTable() throws SQLException {
        sqlSession.connect();
        sqlSession.getConnection().createStatement().executeUpdate("DELETE FROM " + name);
        sqlSession.disconnect();
    }

    public void tryClearTable() {
        try {
            clearTable();
        } catch (SQLException ignored) {
        }
    }

    public ArrayList<SQLEntryPair> parseTableBackwards(int amount, int offset, String counterName) throws SQLException {
        sqlSession.connect();
        ArrayList<SQLEntryPair> pairs = new ArrayList<>();
        ArrayList<SQLEntryPair> tableDefinition = getTableDefinition();
        sqlSession.connect();
        ResultSet resultSet = sqlSession.getConnection().createStatement().executeQuery("SELECT * FROM " + name + " ORDER BY " + counterName + " DESC");
        int counter = 0;
        while (resultSet.next()) {
            if (counter == amount + offset) {
                break;
            }
            if (counter >= offset) {
                for (SQLEntryPair pair : tableDefinition) {
                    SQLEntryPair p = null;
                    switch (pair.getType()) {
                        case DATE:
                            p = new SQLEntryPair(pair.getName(), resultSet.getDate(pair.getName()), SQLEntryTypes.DATE);
                            break;
                        case STRING:
                            p = new SQLEntryPair(pair.getName(), resultSet.getString(pair.getName()), SQLEntryTypes.STRING);
                            break;
                        case BOOLEAN:
                            p = new SQLEntryPair(pair.getName(), resultSet.getBoolean(pair.getName()), SQLEntryTypes.BOOLEAN);
                            break;
                        case INTEGER:
                            p = new SQLEntryPair(pair.getName(), resultSet.getInt(pair.getName()), SQLEntryTypes.INTEGER);
                            break;
                    }
                    pairs.add(p);
                }
            }
            counter++;
        }
        sqlSession.disconnect();
        return pairs;
    }

    public ArrayList<SQLEntryPair> tryParseTableBackwards(int amount, int offset, String counterName) {
        try {
            return parseTableBackwards(amount, offset, counterName);
        } catch (SQLException e) {
            return new ArrayList<>();
        }
    }

    public int getRowCount() throws SQLException {
        sqlSession.connect();
        int ret = 0;
        String query = "SELECT COUNT(*) AS row_count FROM " + name;
        try (ResultSet resultSet = sqlSession.getConnection().createStatement().executeQuery(query)) {
            if (resultSet.next()) {
                ret = resultSet.getInt("row_count");
            }
        }
        sqlSession.disconnect();
        return ret;
    }

    public int tryGetRowCount() {
        try {
            return getRowCount();
        } catch (SQLException e) {
            return 0;
        }
    }

    @Override
    public void provideSession(SQLSession.SQLSessionPrivate sqlSession) {
        this.sqlSession = sqlSession;
    }
}
