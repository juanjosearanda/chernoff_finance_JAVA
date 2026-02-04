import java.util.function.DoubleUnaryOperator;

/**
 * OptionPricingModel maps the Black-Scholes PDE to the Chernoff operator framework.
 * 
 * The Black-Scholes PDE: dV/dt + 0.5*σ²*S²*d²V/dS² + r*S*dV/dS - r*V = 0
 * 
 * Maps to the operator L with coefficients:
 * - a(x) = 0.5 * σ(x)² * x²
 * - b(x) = r(x) * x
 * - c(x) = -r(x)
 */
public class OptionPricingModel {
    
    /**
     * Enumeration of option types.
     */
    public enum OptionType {
        CALL, PUT
    }
    
    /**
     * Create the a(x) coefficient for the Black-Scholes operator.
     * a(x) = 0.5 * σ(x)² * x²
     */
    public static DoubleUnaryOperator createACoefficient(DoubleUnaryOperator volatility) {
        return x -> 0.5 * Math.pow(volatility.applyAsDouble(x), 2) * x * x;
    }
    
    /**
     * Create the b(x) coefficient for the Black-Scholes operator.
     * b(x) = r(x) * x
     */
    public static DoubleUnaryOperator createBCoefficient(DoubleUnaryOperator interestRate) {
        return x -> interestRate.applyAsDouble(x) * x;
    }
    
    /**
     * Create the c(x) coefficient for the Black-Scholes operator.
     * c(x) = -r(x)
     */
    public static DoubleUnaryOperator createCCoefficient(DoubleUnaryOperator interestRate) {
        return x -> -interestRate.applyAsDouble(x);
    }
    
    /**
     * Create a constant volatility function.
     */
    public static DoubleUnaryOperator constantVolatility(double sigma) {
        return x -> sigma;
    }
    
    /**
     * Create a constant interest rate function.
     */
    public static DoubleUnaryOperator constantRate(double r) {
        return x -> r;
    }
    
    /**
     * Create a local volatility function with a simple model.
     * Example: σ(S) = σ₀ + σ₁ * sin(S/S₀)
     */
    public static DoubleUnaryOperator localVolatility(double sigma0, double sigma1, double S0) {
        return x -> sigma0 + sigma1 * Math.sin(x / S0);
    }
    
    /**
     * Create a variable interest rate function.
     * Example: r(S) = r₀ + r₁ * (S/S₀ - 1)
     */
    public static DoubleUnaryOperator variableRate(double r0, double r1, double S0) {
        return x -> r0 + r1 * (x / S0 - 1.0);
    }
    
    /**
     * Create a call option payoff function.
     * Payoff = max(S - K, 0)
     */
    public static DoubleUnaryOperator callPayoff(double strikePrice) {
        return x -> Math.max(0.0, x - strikePrice);
    }
    
    /**
     * Create a put option payoff function.
     * Payoff = max(K - S, 0)
     */
    public static DoubleUnaryOperator putPayoff(double strikePrice) {
        return x -> Math.max(0.0, strikePrice - x);
    }
    
    /**
     * Create a digital call option payoff.
     * Payoff = 1 if S > K, 0 otherwise
     */
    public static DoubleUnaryOperator digitalCallPayoff(double strikePrice) {
        return x -> (x > strikePrice) ? 1.0 : 0.0;
    }
    
    /**
     * Create a digital put option payoff.
     * Payoff = 1 if S < K, 0 otherwise
     */
    public static DoubleUnaryOperator digitalPutPayoff(double strikePrice) {
        return x -> (x < strikePrice) ? 1.0 : 0.0;
    }
    
    /**
     * Price an option using the Chernoff method.
     * 
     * @param optionType Type of option (CALL or PUT)
     * @param currentPrice Current asset price S₀
     * @param strikePrice Strike price K
     * @param timeToMaturity Time to maturity T
     * @param volatility Volatility function σ(x)
     * @param interestRate Interest rate function r(x)
     * @param numIterations Number of Chernoff iterations
     * @param gridSize Size of computational grid
     * @return Option price
     */
    public static double priceOption(OptionType optionType, double currentPrice, 
                                     double strikePrice, double timeToMaturity,
                                     DoubleUnaryOperator volatility, 
                                     DoubleUnaryOperator interestRate,
                                     int numIterations, int gridSize) {
        // Define grid boundaries (wider range for numerical stability)
        double xMin = Math.max(0.01, currentPrice * 0.2);
        double xMax = currentPrice * 3.0;
        
        // Create payoff function
        DoubleUnaryOperator payoff;
        if (optionType == OptionType.CALL) {
            payoff = callPayoff(strikePrice);
        } else {
            payoff = putPayoff(strikePrice);
        }
        
        // Create operator coefficients
        DoubleUnaryOperator a = createACoefficient(volatility);
        DoubleUnaryOperator b = createBCoefficient(interestRate);
        DoubleUnaryOperator c = createCCoefficient(interestRate);
        
        // Create solver
        ChernoffSolver solver = new ChernoffSolver(a, b, c, xMin, xMax, gridSize);
        
        // Solve backward in time (from maturity to present)
        Grid solution = solver.approximateEvolution(payoff, timeToMaturity, numIterations);
        
        // Return the option value at current price
        return solution.getValue(currentPrice);
    }
    
    /**
     * Price an option with adaptive error control.
     */
    public static PricingResult priceOptionAdaptive(OptionType optionType, double currentPrice,
                                                   double strikePrice, double timeToMaturity,
                                                   DoubleUnaryOperator volatility,
                                                   DoubleUnaryOperator interestRate,
                                                   double tolerance, int maxIterations,
                                                   int gridSize) {
        double xMin = Math.max(0.01, currentPrice * 0.2);
        double xMax = currentPrice * 3.0;
        
        DoubleUnaryOperator payoff;
        if (optionType == OptionType.CALL) {
            payoff = callPayoff(strikePrice);
        } else {
            payoff = putPayoff(strikePrice);
        }
        
        DoubleUnaryOperator a = createACoefficient(volatility);
        DoubleUnaryOperator b = createBCoefficient(interestRate);
        DoubleUnaryOperator c = createCCoefficient(interestRate);
        
        ChernoffSolver solver = new ChernoffSolver(a, b, c, xMin, xMax, gridSize);
        ChernoffSolver.ConvergenceResult result = 
            solver.approximateEvolutionAdaptive(payoff, timeToMaturity, tolerance, maxIterations);
        
        double price = result.solution.getValue(currentPrice);
        
        return new PricingResult(price, result.estimatedError, result.iterations, result.converged);
    }
    
    /**
     * Result class for option pricing with error information.
     */
    public static class PricingResult {
        public final double price;
        public final double estimatedError;
        public final int iterations;
        public final boolean converged;
        
        public PricingResult(double price, double estimatedError, int iterations, boolean converged) {
            this.price = price;
            this.estimatedError = estimatedError;
            this.iterations = iterations;
            this.converged = converged;
        }
        
        @Override
        public String toString() {
            return String.format("Price: %.4f, Error: %.6f, Iterations: %d, Converged: %s",
                               price, estimatedError, iterations, converged);
        }
    }
}
