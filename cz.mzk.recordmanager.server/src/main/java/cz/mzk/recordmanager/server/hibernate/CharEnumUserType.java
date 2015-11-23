package cz.mzk.recordmanager.server.hibernate;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.usertype.ParameterizedType;
import org.hibernate.usertype.UserType;

import com.google.common.base.Objects;

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
	public Object nullSafeGet(ResultSet rs, String[] names,
			SessionImplementor session, Object owner)
			throws HibernateException, SQLException {
		String value = rs.getString(names[0]);
		Object result = null;
		if (value == null || value.isEmpty()) {
			return null;
		} else {
			result = values.get(value.charAt(0));
		}
		return result;
	}

	@Override
	public void nullSafeSet(PreparedStatement st, Object value, int index,
			SessionImplementor session) throws HibernateException, SQLException {
		if (value == null) {
			st.setObject(index, null);
		} else {
			char charValue = ((CharValueEnum) value).getValue();
			st.setString(index, String.valueOf(charValue));
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
			values.put((Character) constant.getValue(), constant);
		}
	}

}
