package cz.mzk.recordmanager.server.springbatch;

import java.io.Serializable;

import org.springframework.batch.core.JobParameter.ParameterType;

public class JobParameterDeclaration implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private final String name;

	private final ParameterType type;

	private final boolean required;

	public JobParameterDeclaration(String name, ParameterType type, boolean required) {
		super();
		this.name = name;
		this.type = type;
		this.required = required;
	}

	public String getName() {
		return name;
	}

	public ParameterType getType() {
		return type;
	}

	public boolean isRequired() {
		return required;
	}
	
	public static JobParameterDeclaration param(String name, ParameterType type, boolean required) {
		return new JobParameterDeclaration(name, type, required);
	}

}
