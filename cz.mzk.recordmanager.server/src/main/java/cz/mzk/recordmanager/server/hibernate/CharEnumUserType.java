package cz.mzk.recordmanager.server.hibernate;

import com.google.common.base.Objects;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.ParameterizedType;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class CharEnumUserType implements UserType, ParameterizedType {

	private Class<? extends CharValueEnum> enumClass;

	private Map<Character, CharValueEnum> values = null;

	@Override
	public int[] sqlTypes() {
		return new int[] { Types.CHAR };
	}

	@Override
	public Class<?> returnedClass() {
		return enumClass;
	}

	@Override
	public boolean equals(Object x, Object y) throws HibernateException {
		return Objects.equal(x, y);
	}

	@Override
	public int hashCode(Object x) throws HibernateException {
		return x.hashCode();
	}

	@Override
	public Object nullSafeGet(ResultSet resultSet, String[] strings, SharedSessionContractImplementor sharedSessionContractImplementor, Object o) throws HibernateException, SQLException {
		String value = resultSet.getString(strings[0]);
		Object result;
		if (value == null || value.isEmpty()) {
			return null;
		} else {
			result = values.get(value.charAt(0));
		}
		return result;
	}

	@Override
	public void nullSafeSet(PreparedStatement preparedStatement, Object o, int i, SharedSessionContractImplementor sharedSessionContractImplementor) throws HibernateException, SQLException {
		if (o == null) {
			preparedStatement.setObject(i, null);
		} else {
			char charValue = ((CharValueEnum) o).getValue();
			preparedStatement.setString(i, String.valueOf(charValue));
		}
	}

	@Override
	public Object deepCopy(Object value) throws HibernateException {
		return value;
	}

	@Override
	public boolean isMutable() {
		return false;
	}

	@Override
	public Serializable disassemble(Object value) throws HibernateException {
		return (Serializable) value;
	}

	@Override
	public Object assemble(Serializable cached, Object owner)
			throws HibernateException {
		return cached;
	}

	@Override
	public Object replace(Object original, Object target, Object owner)
			throws HibernateException {
		return original;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void setParameterValues(Properties parameters) {
		String enumClassName = parameters.getProperty("enumClassName");
		try {
			enumClass = (Class<CharValueEnum>) Class.forName(enumClassName);
		} catch (ClassNotFoundException cnfe) {
			throw new IllegalArgumentException(String.format("Class %s not found", enumClassName));
		}
		values = new HashMap<Character, CharValueEnum>();
		CharValueEnum[] constants = enumClass.getEnumConstants();
		if (constants == null) {
			throw new IllegalArgumentException(String.format("Class %s is not enum", enumClassName)); 
		}
		for (CharValueEnum constant : constants) {
			values.put(constant.getValue(), constant);
		}
	}

}
