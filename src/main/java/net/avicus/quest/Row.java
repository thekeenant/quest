package net.avicus.quest;

import net.avicus.quest.database.DatabaseException;
import net.avicus.quest.query.select.SelectResult;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A row from a result set. ResultSet's are silly because they
 * go away. We create a row to keep data/metadata from a single row.
 */
public class Row {
    private final List<String> columnNames;
    private final List<RowValue> values;

    public Row(List<String> columnNames, List<RowValue> values) {
        this.columnNames = columnNames;
        this.values = values;
    }

    public boolean hasColumn(int number) {
        int index = number - 1;
        return index >= 0 && index < this.values.size();
    }

    public boolean hasColumn(String column) {
        int number = this.columnNames.indexOf(column);
        return hasColumn(number);
    }

    public String getColumnName(int number) {
        int index = number - 1;
        if (index < 0 || index >= this.columnNames.size()) {
            throw new DatabaseException("Invalid column number: " + number + ".");
        }
        return this.columnNames.get(index);
    }

    public int getColumnCount() {
        return this.columnNames.size();
    }

    public RowValue getValue(int number) throws DatabaseException {
        int index = number - 1;
        if (index < 0 || index >= this.values.size()) {
            throw new DatabaseException("Invalid column number: " + number + ".");
        }
        return this.values.get(index);
    }

    public RowValue getValue(String column) {
        int number = this.columnNames.indexOf(column) + 1;
        return getValue(number);
    }

    public static Row fromSelectResultSet(SelectResult result, ResultSet set) {
        List<RowValue> values = new ArrayList<>();
        for (int i = 1; i <= result.getColumnCount(); i++) {
            try {
                values.add(new RowValue(set.getObject(i)));
            } catch (SQLException e) {
                throw new DatabaseException(e);
            }
        }
        return new Row(result.getColumnNames(), values);
    }

    public static Row fromResultSet(ResultSet set) {
        List<String> columnNames = new ArrayList<>();
        List<RowValue> values = new ArrayList<>();

        try {
            for (int i = 1; i <= set.getMetaData().getColumnCount(); i++) {
                columnNames.add(set.getMetaData().getColumnName(i));
                values.add(new RowValue(set.getObject(i)));
            }
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }

        return new Row(columnNames, values);
    }

    public static Row fromRowValues(Map<String, RowValue> data) {
        return new Row(new ArrayList<>(data.keySet()), new ArrayList<>(data.values()));
    }

    public static Row fromObjects(Map<String, Object> data) {
        List<RowValue> values = data.values().stream().map(RowValue::new).collect(Collectors.toList());
        return new Row(new ArrayList<>(data.keySet()), values);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Row(");
        for (int i = 0; i < this.columnNames.size(); i++) {
            sb.append(this.columnNames.get(i));
            sb.append("[").append(i + 1).append("]");
            sb.append("=");
            sb.append(this.values.get(i));
            if (i != this.columnNames.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append(")");
        return sb.toString();
    }
}
