import java.util.function.DoubleUnaryOperator;

/**
 * ChernoffSolver implements the Chernoff approximation method for solving
 * second-order ODEs of the form: L = a(x)f''(x) + b(x)f'(x) + c(x)f(x)
 * 
 * Based on Theorem 6 from Remizov (2025) using the translation-based approach.
 */
public class ChernoffSolver {
    private final DoubleUnaryOperator a; // Coefficient of f''(x)
    private final DoubleUnaryOperator b; // Coefficient of f'(x)
    private final DoubleUnaryOperator c; // Coefficient of f(x)
    
    private final double xMin;
    private final double xMax;
    private final int gridSize;
    
    /**
     * Create a Chernoff solver with variable coefficients.
     * 
     * @param a Coefficient function a(x) for f''(x)
     * @param b Coefficient function b(x) for f'(x)
     * @param c Coefficient function c(x) for f(x)
     * @param xMin Minimum x value for the grid
     * @param xMax Maximum x value for the grid
     * @param gridSize Number of grid points
     */
    public ChernoffSolver(DoubleUnaryOperator a, DoubleUnaryOperator b, 
                         DoubleUnaryOperator c, double xMin, double xMax, int gridSize) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.xMin = xMin;
        this.xMax = xMax;
        this.gridSize = gridSize;
    }
    
    /**
     * Apply a single Chernoff step S(t) to a function.
     * 
     * S(t)f(x) = 1/4*f(x + 2*sqrt(a(x)*t)) + 1/4*f(x - 2*sqrt(a(x)*t)) 
     *          + 1/2*f(x + 2*b(x)*t) + t*c(x)*f(x)
     */
    private void applyChernoffStep(Grid currentState, Grid nextState, double t) {
        double[] xValues = currentState.getXValues();
        double[] newValues = new double[gridSize];
        
        for (int i = 0; i < gridSize; i++) {
            double x = xValues[i];
            double aVal = a.applyAsDouble(x);
            double bVal = b.applyAsDouble(x);
            double cVal = c.applyAsDouble(x);
            
            // Calculate shift amounts
            double sqrtATerm = 2.0 * Math.sqrt(Math.abs(aVal) * t);
            double bTerm = 2.0 * bVal * t;
            
            // Evaluate shifted functions using interpolation
            double f_plus = currentState.getValue(x + sqrtATerm);
            double f_minus = currentState.getValue(x - sqrtATerm);
            double f_b_shift = currentState.getValue(x + bTerm);
            double f_current = currentState.getValue(x);
            
            // Apply the Chernoff formula
            newValues[i] = 0.25 * f_plus + 0.25 * f_minus 
                         + 0.5 * f_b_shift + t * cVal * f_current;
        }
        
        nextState.setValues(newValues);
    }
    
    /**
     * Approximate the evolution e^(tL)f using the Chernoff method.
     * 
     * @param payoff Initial function (payoff function in finance context)
     * @param time Total evolution time
     * @param n Number of iterations (accuracy improves with n)
     * @return Grid containing the evolved function
     */
    public Grid approximateEvolution(DoubleUnaryOperator payoff, double time, int n) {
        if (n <= 0) {
            throw new IllegalArgumentException("Number of iterations must be positive");
        }
        if (time < 0) {
            throw new IllegalArgumentException("Time must be non-negative");
        }
        
        double dt = time / n;
        
        // Initialize grids
        Grid currentGrid = new Grid(xMin, xMax, gridSize);
        Grid nextGrid = new Grid(xMin, xMax, gridSize);
        
        currentGrid.initialize(payoff);
        
        // Apply Chernoff step n times: (S(t/n))^n
        for (int step = 0; step < n; step++) {
            applyChernoffStep(currentGrid, nextGrid, dt);
            
            // Swap grids for next iteration
            Grid temp = currentGrid;
            currentGrid = nextGrid;
            nextGrid = temp;
        }
        
        return currentGrid;
    }
    
    /**
     * Approximate evolution with adaptive error control.
     * Increases n until convergence is achieved within tolerance.
     * 
     * @param payoff Initial function
     * @param time Total evolution time
     * @param tolerance Desired accuracy
     * @param maxIterations Maximum number of iterations to try
     * @return ConvergenceResult containing the solution and error estimate
     */
    public ConvergenceResult approximateEvolutionAdaptive(DoubleUnaryOperator payoff, 
                                                         double time, double tolerance, 
                                                         int maxIterations) {
        int n = 100; // Start with reasonable number of iterations
        Grid previousResult = null;
        double error = Double.MAX_VALUE;
        
        // Estimate ||c|| for error bound
        double cNorm = estimateCNorm();
        
        while (n <= maxIterations && error > tolerance) {
            Grid currentResult = approximateEvolution(payoff, time, n);
            
            if (previousResult != null) {
                // Calculate difference between n and n/2 iterations
                error = computeGridDifference(previousResult, currentResult);
                
                // Check theoretical error bound: t^2 * e^(||c||*t) / n * C
                double theoreticalBound = (time * time * Math.exp(cNorm * time)) / n;
                
                if (error <= tolerance) {
                    return new ConvergenceResult(currentResult, error, n, true);
                }
            }
            
            previousResult = currentResult;
            n *= 2; // Double the number of iterations
        }
        
        // Return best result even if not converged
        return new ConvergenceResult(previousResult, error, n / 2, false);
    }
    
    /**
     * Estimate the norm of coefficient c(x) over the grid.
     */
    private double estimateCNorm() {
        double maxC = 0.0;
        double[] xValues = new Grid(xMin, xMax, gridSize).getXValues();
        
        for (double x : xValues) {
            maxC = Math.max(maxC, Math.abs(c.applyAsDouble(x)));
        }
        
        return maxC;
    }
    
    /**
     * Compute the maximum difference between two grids.
     */
    private double computeGridDifference(Grid grid1, Grid grid2) {
        double[] xValues = grid1.getXValues();
        double maxDiff = 0.0;
        
        for (double x : xValues) {
            double diff = Math.abs(grid1.getValue(x) - grid2.getValue(x));
            maxDiff = Math.max(maxDiff, diff);
        }
        
        return maxDiff;
    }
    
    /**
     * Result class containing convergence information.
     */
    public static class ConvergenceResult {
        public final Grid solution;
        public final double estimatedError;
        public final int iterations;
        public final boolean converged;
        
        public ConvergenceResult(Grid solution, double estimatedError, 
                                int iterations, boolean converged) {
            this.solution = solution;
            this.estimatedError = estimatedError;
            this.iterations = iterations;
            this.converged = converged;
        }
    }
}
