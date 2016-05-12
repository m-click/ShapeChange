package de.interactive_instruments.ShapeChange.FOL;


public class FunctionBinaryComparisonPredicate extends BinaryComparisonPredicate {
	
	private final String funcName;


	public FunctionBinaryComparisonPredicate(String funcName) {
		super();
		this.funcName = funcName;
	}


	@Override
	public String toString() {
		return funcName + "("  + exprLeft.toString() + ", " + exprRight.toString() + ")";
	}


	public String getFuncName() {
		return funcName;
	}
}
