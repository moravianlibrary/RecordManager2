package cz.mzk.recordmanager.server.util.db;

import org.hibernate.dialect.PostgreSQL9Dialect;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.type.IntegerType;

public class CustomizedPostgreSQL9Dialect extends PostgreSQL9Dialect {

	public CustomizedPostgreSQL9Dialect() {
		super();
		registerFunction("levenshtein", new StandardSQLFunction("levenshtein", new IntegerType()));
	}
}
