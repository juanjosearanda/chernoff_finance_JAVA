import java.util.Arrays;
import java.util.function.DoubleUnaryOperator;

/**
 * Grid class for discrete function representation with linear interpolation.
 * Efficiently handles function evaluation at arbitrary points using binary search.
 */
public class Grid {
    private final double[] xValues;
    private double[] fValues;
    private final double xMin;
    private final double xMax;
    private final double dx;
    
    /**
     * Create a grid from xMin to xMax with numPoints points.
     */
    public Grid(double xMin, double xMax, int numPoints) {
        if (numPoints < 2) {
            throw new IllegalArgumentException("Grid must have at least 2 points");
        }
        if (xMax <= xMin) {
            throw new IllegalArgumentException("xMax must be greater than xMin");
        }
        
        this.xMin = xMin;
        this.xMax = xMax;
        this.dx = (xMax - xMin) / (numPoints - 1);
        this.xValues = new double[numPoints];
        this.fValues = new double[numPoints];
        
        for (int i = 0; i < numPoints; i++) {
            xValues[i] = xMin + i * dx;
        }
    }
    
    /**
     * Initialize the grid with a function.
     */
    public void initialize(DoubleUnaryOperator function) {
        for (int i = 0; i < xValues.length; i++) {
            fValues[i] = function.applyAsDouble(xValues[i]);
        }
    }
    
    /**
     * Get function value at point x using linear interpolation.
     */
    public double getValue(double x) {
        // Clamp to boundaries
        if (x <= xMin) return fValues[0];
        if (x >= xMax) return fValues[fValues.length - 1];
        
        // Find the interval containing x using binary search
        int idx = Arrays.binarySearch(xValues, x);
        
        if (idx >= 0) {
            // Exact match
            return fValues[idx];
        } else {
            // Interpolation needed
            int insertionPoint = -(idx + 1);
            int i0 = insertionPoint - 1;
            int i1 = insertionPoint;
            
            // Linear interpolation
            double x0 = xValues[i0];
            double x1 = xValues[i1];
            double f0 = fValues[i0];
            double f1 = fValues[i1];
            
            double alpha = (x - x0) / (x1 - x0);
            return f0 + alpha * (f1 - f0);
        }
    }
    
    /**
     * Set function values directly.
     */
    public void setValues(double[] newValues) {
        if (newValues.length != fValues.length) {
            throw new IllegalArgumentException("Array length mismatch");
        }
        System.arraycopy(newValues, 0, fValues, 0, fValues.length);
    }
    
    /**
     * Get a copy of current function values.
     */
    public double[] getValues() {
        return Arrays.copyOf(fValues, fValues.length);
    }
    
    /**
     * Get grid points.
     */
    public double[] getXValues() {
        return Arrays.copyOf(xValues, xValues.length);
    }
    
    /**
     * Get number of grid points.
     */
    public int size() {
        return xValues.length;
    }
    
    /**
     * Convert grid to a DoubleUnaryOperator for use in other calculations.
     */
    public DoubleUnaryOperator toFunction() {
        return this::getValue;
    }
    
    /**
     * Apply a transformation to all grid values.
     */
    public void transform(DoubleUnaryOperator transformation) {
        for (int i = 0; i < fValues.length; i++) {
            fValues[i] = transformation.applyAsDouble(fValues[i]);
        }
    }
    
    /**
     * Get maximum absolute value on the grid (for norm calculations).
     */
    public double maxAbsValue() {
        double max = 0.0;
        for (double val : fValues) {
            max = Math.max(max, Math.abs(val));
        }
        return max;
    }
}
