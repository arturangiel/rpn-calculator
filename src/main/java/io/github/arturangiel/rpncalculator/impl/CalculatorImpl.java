package io.github.arturangiel.rpncalculator.impl;

import io.github.arturangiel.rpncalculator.Calculator;
import io.github.arturangiel.rpncalculator.CalculatorContext;
import io.github.arturangiel.rpncalculator.exception.*;
import io.github.arturangiel.rpncalculator.math.FunctionValue;
import org.apache.log4j.Logger;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.NoSuchElementException;

/**
 * Class for Reverse Polish Notation calculations
 */
public class CalculatorImpl<T extends Number> implements Calculator<T> {

    Logger logger = Logger.getLogger(Calculator.class);

    private CalculatorContext<T> context;

    public CalculatorImpl(CalculatorContext<T> context) {
        this.context = context;
    }

    public CalculatorImpl() {
//        context = new CalculatorContext<>();
//        this.context = context.getDefaultContext();
    }

    /**
     * Main method for Reverse Polish Notation Calculator
     *
     * @param equation String with equation to calculate
     * @return result of calculation
     * @throws CalculatorException abstract Exception (@link #BadEquationException), #BadItemException, #CalculatorArithmeticException, #LackOfArgumentsException, #UnexpectedException
     */
    @Override
    public T calculate(String equation) throws CalculatorException {

        logger.info(String.format("Calculating equation: %s", equation));

        String[] equationSplit = equation.trim().split("\\s+");
        logger.debug(String.format("Equation split with whitespace regex: %s", Arrays.toString(equationSplit)));

        ArrayDeque<T> stack = new ArrayDeque<>();

        for (int i = 0; i < equationSplit.length; i++) {

            String item = equationSplit[i];
            logger.debug(String.format("Processing item '%s' at position %d", item, (i + 1)));

            Constructor<T> constructor;

            try {
                constructor = context.getClazz().getConstructor(String.class, long.class);
            } catch (NoSuchMethodException e) {
                try {
                    constructor = context.getClazz().getConstructor(String.class);
                } catch (NoSuchMethodException ex) {
                    logger.error("No constructor");
                    throw new UnexpectedException("No constructors (String, long) and (String)");
                }
            }

            try {
                T toPush;
                if (constructor.getParameterCount() == 1)
                    toPush = constructor.newInstance(item);
                else
                    toPush = constructor.newInstance(item, context.getPrecision());
                stack.push(toPush);
                logger.debug(String.format("Item '%s' pushed into the stack: %s", item, stack));
            } catch (InvocationTargetException e) {
                if (NumberFormatException.class.equals(e.getTargetException().getClass()))
                    calculateOnStack(item, stack, i);
                else
                    e.printStackTrace();
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        if (stack.size() == 1) {
            logger.info(String.format("Result = %s", stack.peek()));
            return stack.pop();
        } else {
            logger.error(String.format("Unexpectedly left on stack: %s", stack));
            throw new BadEquationException((Deque<Number>) stack);
        }
    }

    private void calculateOnStack(String operator, ArrayDeque<T> stack, int position)
            throws CalculatorException {

        logger.debug(String.format("Processing operator/function '%s' at position %d", operator, (position + 1)));

        try {
            FunctionValue<T> functionValue = context.getFunctions().get(operator);
            @SuppressWarnings("unchecked")
            T[] arguments = (T[]) Array.newInstance(context.getClazz(), functionValue.getParametersCount());
            for (int i = arguments.length; i > 0; i--) {
                arguments[i - 1] = stack.pop();
            }
            logger.debug(String.format("Took %d arguments (%s) from stack: %s", arguments.length, Arrays.toString(arguments), stack));
            T applied = functionValue.getFunction().apply(arguments);
            stack.push(applied);
            logger.debug(String.format("Pushed value %s into the stack: %s", applied, stack));
        } catch (NullPointerException e) {
            logger.error(String.format("Operator/function '%s' at position %d unsupported", operator, (position + 1)));
            throw new BadItemException(operator, position);
        } catch (NoSuchElementException e) {
            logger.error(String.format("Unexpected end of stack for operator '%s' at position %d", operator, (position + 1)));
            throw new LackOfArgumentsException(operator, position);
        }
    }

    @Override
    public CalculatorContext<T> getContext() {
        return context;
    }
}
