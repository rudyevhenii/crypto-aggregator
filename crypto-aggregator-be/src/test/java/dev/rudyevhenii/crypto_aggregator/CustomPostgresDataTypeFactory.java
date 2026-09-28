package dev.rudyevhenii.crypto_aggregator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dbunit.dataset.datatype.AbstractDataType;
import org.dbunit.dataset.datatype.DataType;
import org.dbunit.dataset.datatype.DataTypeException;
import org.dbunit.dataset.datatype.TypeCastException;
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory;
import org.postgresql.util.PGobject;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class CustomPostgresDataTypeFactory extends PostgresqlDataTypeFactory {

    @Override
    public DataType createDataType(int sqlType, String sqlTypeName) throws DataTypeException {
        if ("jsonb".equalsIgnoreCase(sqlTypeName)) {
            return JsonbDataType.INSTANCE;
        }
        if ("_text".equalsIgnoreCase(sqlTypeName) || "text[]".equalsIgnoreCase(sqlTypeName)) {
            return TextArrayDataType.INSTANCE;
        }
        return super.createDataType(sqlType, sqlTypeName);
    }

    private static class JsonbDataType extends AbstractDataType {

        public static final JsonbDataType INSTANCE = new JsonbDataType();
        private static final String DATA_TYPE = "jsonb";
        private static final ObjectMapper MAPPER = new ObjectMapper();

        private JsonbDataType() {
            super(DATA_TYPE, Types.OTHER, String.class, false);
        }

        @Override
        public Object getSqlValue(int column, ResultSet resultSet) throws SQLException {
            return resultSet.getString(column);
        }

        @Override
        public void setSqlValue(Object value, int column, PreparedStatement statement) throws SQLException {
            PGobject pgObject = new PGobject();
            pgObject.setType(DATA_TYPE);
            pgObject.setValue(value == null ? null : value.toString());

            statement.setObject(column, pgObject);
        }

        @Override
        public Object typeCast(Object value) {
            return value;
        }

        @Override
        public int compare(Object o1, Object o2) throws TypeCastException {
            if (o1 == null && o2 == null) return 0;
            if (o1 == null || o2 == null) return super.compare(o1, o2);

            try {
                JsonNode expectedJson = MAPPER.readTree(o1.toString());
                JsonNode actualJson = MAPPER.readTree(o2.toString());

                if (expectedJson.equals(actualJson)) {
                    return 0;
                }
            } catch (Exception e) {

            }
            return o1.toString().compareTo(o2.toString());
        }
    }

    private static class TextArrayDataType extends AbstractDataType {

        public static final TextArrayDataType INSTANCE = new TextArrayDataType();
        private static final String DATA_TYPE = "text[]";

        private TextArrayDataType() {
            super(DATA_TYPE, Types.OTHER, String.class, false);
        }

        @Override
        public Object getSqlValue(int column, ResultSet resultSet) throws SQLException {
            return resultSet.getString(column);
        }

        @Override
        public void setSqlValue(Object value, int column, PreparedStatement statement) throws SQLException {
            PGobject pgObject = new PGobject();
            pgObject.setType(DATA_TYPE);
            pgObject.setValue(value == null ? null : value.toString());

            statement.setObject(column, pgObject);
        }

        @Override
        public Object typeCast(Object value) {
            return value;
        }
    }
}
