package net.querybuilder4j.constants;

import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

public class Constants {

    public static final String DATABASE_URL = "url";

    public static final String DATABASE_USERNAME = "username";

    public static final String DATABASE_PASSWORD = "password";

    public static final String DATABASE_TYPE = "databaseType";

    /**
     * A Map with the keys being JDBC Types and the values being booleans.  The boolean values represent whether filters
     * with the given JDBC type should be quoted.  For example, if a column's JDBC type is INTEGER, then the value in the
     * typeMappings field is false, because integers do not need to be wrapped in quotes in a SQL WHERE clause.  On the
     * other hand, if a column's JDBC type is NVARCHAR, then the value in the typeMappings field is true, because varchar's
     * do not need to be wrapped in quotes in a SQL WHERE clause.
     */
    public final static Map<Integer, Boolean> TYPE_MAPPINGS = new HashMap<Integer, Boolean>() {{
        put(Types.ARRAY, true);
        put(Types.BIGINT, false);
        put(Types.BINARY, true);
        put(Types.BIT, false);
        put(Types.BLOB, true);
        put(Types.BOOLEAN, false);
        put(Types.CHAR, true);
        put(Types.CLOB, true);
//        put(Types.DATALINK, false);
        put(Types.DATE, true);
        put(Types.DECIMAL, true);
        put(Types.DISTINCT, true);
        put(Types.DOUBLE, false);
        put(Types.FLOAT, false);
        put(Types.INTEGER, false);
        put(Types.JAVA_OBJECT, true);
        put(Types.LONGNVARCHAR, true);
        put(Types.LONGVARBINARY, true);
        put(Types.LONGVARCHAR, true);
        put(Types.NCHAR, true);
        put(Types.NCLOB, true);
        put(Types.NULL, true);
        put(Types.NUMERIC, false);
        put(Types.NVARCHAR, true);
        put(Types.OTHER, true);
        put(Types.REAL, false);
        put(Types.REF, true);
        put(Types.REF_CURSOR, true);
        put(Types.ROWID, false);
        put(Types.SMALLINT, false);
        put(Types.SQLXML, true);
        put(Types.STRUCT, true);
        put(Types.TIME, true);
        put(Types.TIME_WITH_TIMEZONE, true);
        put(Types.TIMESTAMP, true);
        put(Types.TIMESTAMP_WITH_TIMEZONE, true);
        put(Types.TINYINT, false);
        put(Types.VARBINARY, true);
        put(Types.VARCHAR, true);
    }};


}
