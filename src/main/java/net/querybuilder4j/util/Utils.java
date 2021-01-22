package net.querybuilder4j.util;

import net.querybuilder4j.exceptions.SqlTypeNotRecognizedException;

public class Utils {

    public static String getJdbcSqlType(int jdbcType) {
        if (jdbcType == 2003) { return "ARRAY"; }
        else if (jdbcType == -5) { return "BIG_INT"; }
        else if (jdbcType == -2) { return "BINARY"; }
        else if (jdbcType == -7) { return "BIT"; }
        else if (jdbcType == 2004) { return "BLOB"; }
        else if (jdbcType == 16) { return "BOOLEAN"; }
        else if (jdbcType == 1) { return "CHAR"; }
        else if (jdbcType == 2005) { return "CLOB"; }
        else if (jdbcType == 70) { return "DATA_LINK"; }
        else if (jdbcType == 91) { return "DATE"; }
        else if (jdbcType == 3) { return "DECIMAL"; }
        else if (jdbcType == 2001) { return "DISTINCT"; }
        else if (jdbcType == 8) { return "DOUBLE"; }
        else if (jdbcType == 6) { return "FLOAT"; }
        else if (jdbcType == 4) { return "INTEGER"; }
        else if (jdbcType == -16) { return "LONGNVARCHAR"; }
        else if (jdbcType == -1) { return "LONGVARCHAR"; }
        else if (jdbcType == -15) { return "NCHAR"; }
        else if (jdbcType == 2) { return "NUMERIC"; }
        else if (jdbcType == -9) { return "NVARCHAR"; }
        else if (jdbcType == 5) { return "SMALL_INT"; }
        else if (jdbcType == 92) { return "TIME"; }
        else if (jdbcType == 2013) { return "TIME_WITH_TIMEZONE"; }
        else if (jdbcType == 93) { return "TIMESTAMP"; }
        else if (jdbcType == 2014) { return "TIMESTAMP_WITH_TIMEZONE"; }
        else if (jdbcType == -6) { return "TINY_INT"; }
        else if (jdbcType == 12) { return "VARCHAR"; }
        else { throw new SqlTypeNotRecognizedException(jdbcType);
        }
    }

}
