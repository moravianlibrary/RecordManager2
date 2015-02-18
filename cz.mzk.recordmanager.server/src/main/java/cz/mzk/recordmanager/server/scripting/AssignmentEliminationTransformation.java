package cz.mzk.recordmanager.server.scripting;

import java.util.HashSet;
import java.util.Set;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@GroovyASTTransformation
public class AssignmentEliminationTransformation implements ASTTransformation {
	
	private static Logger logger = LoggerFactory.getLogger(AssignmentEliminationTransformation.class);
	
	private static final ConstantExpression CONSTANT_NULL = ConstantExpression.NULL;
	
	private final Set<String> variablesToExclude;
	
	private final Set<String> variables = new HashSet<String>();
	
	public AssignmentEliminationTransformation(Set<String> variablesToExclude) {
		this.variablesToExclude = variablesToExclude;
	}
	
	public AssignmentEliminationTransformation() {
		this(new HashSet<String>());
	}

	@Override
	public void visit(ASTNode[] nodes, final SourceUnit source) {
		BlockStatement stm = source.getAST().getStatementBlock();
		stm.visit(new CodeVisitorSupport() {
			
		    public void visitBinaryExpression(BinaryExpression exp) {
		    	Expression left = exp.getLeftExpression();
		    	Token op = exp.getOperation();
		    	if (left instanceof VariableExpression && op.getType() == Types.ASSIGN) {
		    		VariableExpression var = (VariableExpression) left;
		    		String variable = var.getName();
		    		variables.add(variable);
		    		if (variablesToExclude.contains(variable)) {
		    			logger.debug("Field {} is overriden, setting to null in {}", variable, source.getName());
		    			exp.setRightExpression(CONSTANT_NULL);
		    		}
		    	}
		    }
		    
		});
		
	}

	public Set<String> getVariablesToExclude() {
		return variablesToExclude;
	}

	public Set<String> getVariables() {
		return variables;
	}


}
