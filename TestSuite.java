import java.util.function.DoubleUnaryOperator;

/**
 * Automated test suite for the Chernoff Option Pricing Engine.
 * Run this to validate the implementation without interactive input.
 */
public class TestSuite {
    
    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("  Chernoff Approximation Test Suite");
        System.out.println("=================================================\n");
        
        int testsPassed = 0;
        int totalTests = 0;
        
        // Test 1: Grid interpolation
        System.out.println("Test 1: Grid Interpolation");
        totalTests++;
        if (testGridInterpolation()) {
            testsPassed++;
            System.out.println("✓ PASSED\n");
        } else {
            System.out.println("✗ FAILED\n");
        }
        
        // Test 2: Simple call option
        System.out.println("Test 2: European Call Option (ATM)");
        totalTests++;
        if (testCallOptionATM()) {
            testsPassed++;
            System.out.println("✓ PASSED\n");
        } else {
            System.out.println("✗ FAILED\n");
        }
        
        // Test 3: Simple put option
        System.out.println("Test 3: European Put Option (ATM)");
        totalTests++;
        if (testPutOptionATM()) {
            testsPassed++;
            System.out.println("✓ PASSED\n");
        } else {
            System.out.println("✗ FAILED\n");
        }
        
        // Test 4: Put-Call Parity
        System.out.println("Test 4: Put-Call Parity");
        totalTests++;
        if (testPutCallParity()) {
            testsPassed++;
            System.out.println("✓ PASSED\n");
        } else {
            System.out.println("✗ FAILED\n");
        }
        
        // Test 5: Local volatility
        System.out.println("Test 5: Local Volatility Model");
        totalTests++;
        if (testLocalVolatility()) {
            testsPassed++;
            System.out.println("✓ PASSED\n");
        } else {
            System.out.println("✗ FAILED\n");
        }
        
        // Test 6: Convergence
        System.out.println("Test 6: Convergence Analysis");
        totalTests++;
        if (testConvergence()) {
            testsPassed++;
            System.out.println("✓ PASSED\n");
        } else {
            System.out.println("✗ FAILED\n");
        }
        
        // Summary
        System.out.println("=================================================");
        System.out.printf("Test Results: %d/%d passed (%.1f%%)%n", 
                         testsPassed, totalTests, 100.0 * testsPassed / totalTests);
        System.out.println("=================================================");
    }
    
    private static boolean testGridInterpolation() {
        try {
            Grid grid = new Grid(0.0, 10.0, 11);
            grid.initialize(x -> x * x);
            
            // Test exact values
            double val0 = grid.getValue(0.0);
            double val5 = grid.getValue(5.0);
            double val10 = grid.getValue(10.0);
            
            System.out.printf("  Value at 0.0: %.4f (expected 0.0)%n", val0);
            System.out.printf("  Value at 5.0: %.4f (expected 25.0)%n", val5);
            System.out.printf("  Value at 10.0: %.4f (expected 100.0)%n", val10);
            
            // Test interpolation
            double val2_5 = grid.getValue(2.5);
            System.out.printf("  Value at 2.5: %.4f (expected ~6.25)%n", val2_5);
            
            return Math.abs(val0 - 0.0) < 1e-10 
                && Math.abs(val5 - 25.0) < 1e-10 
                && Math.abs(val10 - 100.0) < 1e-10
                && Math.abs(val2_5 - 6.25) < 0.1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private static boolean testCallOptionATM() {
        try {
            double S0 = 100.0;
            double K = 100.0;
            double T = 1.0;
            double sigma = 0.2;
            double r = 0.05;
            
            DoubleUnaryOperator vol = OptionPricingModel.constantVolatility(sigma);
            DoubleUnaryOperator rate = OptionPricingModel.constantRate(r);
            
            double price = OptionPricingModel.priceOption(
                OptionPricingModel.OptionType.CALL, S0, K, T, vol, rate, 1000, 500
            );
            
            // Black-Scholes analytical value for this case is approximately 10.45
            double expectedPrice = 10.45;
            double tolerance = 0.5; // Allow 5% error
            
            System.out.printf("  Computed price: %.4f%n", price);
            System.out.printf("  Expected price: %.4f (±%.2f)%n", expectedPrice, tolerance);
            System.out.printf("  Difference: %.4f%n", Math.abs(price - expectedPrice));
            
            return Math.abs(price - expectedPrice) < tolerance;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private static boolean testPutOptionATM() {
        try {
            double S0 = 100.0;
            double K = 100.0;
            double T = 1.0;
            double sigma = 0.2;
            double r = 0.05;
            
            DoubleUnaryOperator vol = OptionPricingModel.constantVolatility(sigma);
            DoubleUnaryOperator rate = OptionPricingModel.constantRate(r);
            
            double price = OptionPricingModel.priceOption(
                OptionPricingModel.OptionType.PUT, S0, K, T, vol, rate, 1000, 500
            );
            
            // Black-Scholes analytical value for ATM put is approximately 5.57
            double expectedPrice = 5.57;
            double tolerance = 0.5;
            
            System.out.printf("  Computed price: %.4f%n", price);
            System.out.printf("  Expected price: %.4f (±%.2f)%n", expectedPrice, tolerance);
            System.out.printf("  Difference: %.4f%n", Math.abs(price - expectedPrice));
            
            return Math.abs(price - expectedPrice) < tolerance;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private static boolean testPutCallParity() {
        try {
            double S0 = 100.0;
            double K = 105.0;
            double T = 0.5;
            double sigma = 0.25;
            double r = 0.03;
            
            DoubleUnaryOperator vol = OptionPricingModel.constantVolatility(sigma);
            DoubleUnaryOperator rate = OptionPricingModel.constantRate(r);
            
            double callPrice = OptionPricingModel.priceOption(
                OptionPricingModel.OptionType.CALL, S0, K, T, vol, rate, 1000, 500
            );
            
            double putPrice = OptionPricingModel.priceOption(
                OptionPricingModel.OptionType.PUT, S0, K, T, vol, rate, 1000, 500
            );
            
            // Put-Call Parity: C - P = S0 - K*e^(-rT)
            double lhs = callPrice - putPrice;
            double rhs = S0 - K * Math.exp(-r * T);
            double parity_error = Math.abs(lhs - rhs);
            
            System.out.printf("  Call price: %.4f%n", callPrice);
            System.out.printf("  Put price: %.4f%n", putPrice);
            System.out.printf("  C - P = %.4f%n", lhs);
            System.out.printf("  S₀ - K·e^(-rT) = %.4f%n", rhs);
            System.out.printf("  Parity error: %.6f%n", parity_error);
            
            return parity_error < 0.5; // Allow small numerical error
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private static boolean testLocalVolatility() {
        try {
            double S0 = 100.0;
            double K = 100.0;
            double T = 1.0;
            double r = 0.05;
            
            // Local volatility model
            DoubleUnaryOperator vol = OptionPricingModel.localVolatility(0.2, 0.05, S0);
            DoubleUnaryOperator rate = OptionPricingModel.constantRate(r);
            
            double price = OptionPricingModel.priceOption(
                OptionPricingModel.OptionType.CALL, S0, K, T, vol, rate, 1000, 500
            );
            
            System.out.printf("  Call price with local vol: %.4f%n", price);
            System.out.printf("  (Using σ(S) = 0.2 + 0.05·sin(S/100))%n");
            
            // Should be positive and reasonable
            return price > 5.0 && price < 20.0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private static boolean testConvergence() {
        try {
            double S0 = 100.0;
            double K = 100.0;
            double T = 1.0;
            double sigma = 0.2;
            double r = 0.05;
            
            DoubleUnaryOperator vol = OptionPricingModel.constantVolatility(sigma);
            DoubleUnaryOperator rate = OptionPricingModel.constantRate(r);
            
            // Test with different iteration counts
            int[] iterations = {100, 500, 1000, 2000};
            double[] prices = new double[iterations.length];
            
            System.out.println("  Testing convergence with increasing iterations:");
            for (int i = 0; i < iterations.length; i++) {
                prices[i] = OptionPricingModel.priceOption(
                    OptionPricingModel.OptionType.CALL, S0, K, T, vol, rate, 
                    iterations[i], 500
                );
                System.out.printf("    n=%4d: price=%.6f", iterations[i], prices[i]);
                if (i > 0) {
                    double change = Math.abs(prices[i] - prices[i-1]);
                    System.out.printf("  (Δ=%.6f)", change);
                }
                System.out.println();
            }
            
            // Check that differences decrease (convergence)
            double diff1 = Math.abs(prices[1] - prices[0]);
            double diff2 = Math.abs(prices[2] - prices[1]);
            double diff3 = Math.abs(prices[3] - prices[2]);
            
            return diff1 > diff2 && diff2 > diff3;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
