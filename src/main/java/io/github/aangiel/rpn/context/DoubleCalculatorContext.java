package io.github.aangiel.rpn.context;

import java.util.function.Function;

/**
 * @see CalculatorContext
 */
public final class DoubleCalculatorContext extends CalculatorContext<Double> {
    public DoubleCalculatorContext() {
        super(double.class, Math.class);
    }

    @Override
    protected void populateDefaultOperations() {
        addFunctionOrOperator("+", 2, args -> args.get(0) + args.get(1));
        addFunctionOrOperator("-", 2, args -> args.get(0) - args.get(1));
        addFunctionOrOperator("*", 2, args -> args.get(0) * args.get(1));
        addFunctionOrOperator("/", 2, args -> args.get(0) / args.get(1));
    }

    @Override
    protected void populateConstants() {
        addFunctionOrOperator("pi", 0, args -> Math.PI);
        addFunctionOrOperator("e", 0, args -> Math.E);
    }

    @Override
    public Function<String, Double> getConstructor() {
        return Double::valueOf;
    }

    @Override
    protected CalculatorContext<Double> self() {
        return this;
    }
}
