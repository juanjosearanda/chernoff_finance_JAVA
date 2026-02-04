import java.util.Scanner;
import java.util.function.DoubleUnaryOperator;

/**
 * Main console application for option pricing using the Chernoff approximation method.
 */
public class Main {
    
    private static final Scanner scanner = new Scanner(System.in);
    
    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("  Chernoff Approximation Option Pricing Engine");
        System.out.println("=================================================");
        System.out.println();
        
        try {
            // Get user inputs
            OptionPricingModel.OptionType optionType = getOptionType();
            double currentPrice = getDoubleInput("Current Asset Price (S₀): ", 0.01, Double.MAX_VALUE);
            double strikePrice = getDoubleInput("Strike Price (K): ", 0.01, Double.MAX_VALUE);
            double timeToMaturity = getDoubleInput("Time to Maturity (T in years): ", 0.001, 100.0);
            
            // Volatility
            System.out.println("\nVolatility Configuration:");
            System.out.println("  1. Constant volatility");
            System.out.println("  2. Local volatility: σ(S) = σ₀ + σ₁*sin(S/S₀)");
            int volChoice = getIntInput("Choose option [1-2]: ", 1, 2);
            
            DoubleUnaryOperator volatility;
            if (volChoice == 1) {
                double sigma = getDoubleInput("Volatility (σ): ", 0.001, 10.0);
                volatility = OptionPricingModel.constantVolatility(sigma);
            } else {
                double sigma0 = getDoubleInput("Base volatility (σ₀): ", 0.001, 10.0);
                double sigma1 = getDoubleInput("Volatility amplitude (σ₁): ", -1.0, 1.0);
                volatility = OptionPricingModel.localVolatility(sigma0, sigma1, currentPrice);
                System.out.println("Using: σ(S) = " + sigma0 + " + " + sigma1 + "*sin(S/" + currentPrice + ")");
            }
            
            // Interest rate
            System.out.println("\nInterest Rate Configuration:");
            System.out.println("  1. Constant interest rate");
            System.out.println("  2. Variable rate: r(S) = r₀ + r₁*(S/S₀ - 1)");
            int rateChoice = getIntInput("Choose option [1-2]: ", 1, 2);
            
            DoubleUnaryOperator interestRate;
            if (rateChoice == 1) {
                double rate = getDoubleInput("Interest Rate (r): ", -0.5, 1.0);
                interestRate = OptionPricingModel.constantRate(rate);
            } else {
                double r0 = getDoubleInput("Base rate (r₀): ", -0.5, 1.0);
                double r1 = getDoubleInput("Rate sensitivity (r₁): ", -1.0, 1.0);
                interestRate = OptionPricingModel.variableRate(r0, r1, currentPrice);
                System.out.println("Using: r(S) = " + r0 + " + " + r1 + "*(S/" + currentPrice + " - 1)");
            }
            
            // Numerical parameters
            System.out.println("\nNumerical Parameters:");
            System.out.println("  1. Fixed iterations (fast, n=1000)");
            System.out.println("  2. Adaptive with error control (slower, more accurate)");
            int methodChoice = getIntInput("Choose option [1-2]: ", 1, 2);
            
            // Execute pricing
            System.out.println("\n-------------------------------------------------");
            System.out.println("Computing option price...");
            System.out.println("-------------------------------------------------");
            
            if (methodChoice == 1) {
                int numIterations = 1000;
                int gridSize = 500;
                
                long startTime = System.currentTimeMillis();
                double price = OptionPricingModel.priceOption(
                    optionType, currentPrice, strikePrice, timeToMaturity,
                    volatility, interestRate, numIterations, gridSize
                );
                long endTime = System.currentTimeMillis();
                
                printResults(optionType, currentPrice, strikePrice, timeToMaturity,
                           price, Double.NaN, numIterations, true, endTime - startTime);
                
            } else {
                double tolerance = getDoubleInput("Error tolerance: ", 1e-10, 0.1);
                int maxIterations = getIntInput("Max iterations (power of 2, e.g., 8192): ", 100, 100000);
                int gridSize = 500;
                
                long startTime = System.currentTimeMillis();
                OptionPricingModel.PricingResult result = 
                    OptionPricingModel.priceOptionAdaptive(
                        optionType, currentPrice, strikePrice, timeToMaturity,
                        volatility, interestRate, tolerance, maxIterations, gridSize
                    );
                long endTime = System.currentTimeMillis();
                
                printResults(optionType, currentPrice, strikePrice, timeToMaturity,
                           result.price, result.estimatedError, result.iterations,
                           result.converged, endTime - startTime);
            }
            
            // Option to run another calculation
            System.out.println("\n-------------------------------------------------");
            System.out.print("Run another calculation? (y/n): ");
            String answer = scanner.nextLine().trim().toLowerCase();
            if (answer.equals("y") || answer.equals("yes")) {
                System.out.println("\n");
                main(args);
            }
            
        } catch (Exception e) {
            System.err.println("\nError: " + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
        }
        
        System.out.println("\nThank you for using the Chernoff Option Pricing Engine!");
    }
    
    private static OptionPricingModel.OptionType getOptionType() {
        System.out.println("Option Type:");
        System.out.println("  1. Call Option");
        System.out.println("  2. Put Option");
        int choice = getIntInput("Choose option [1-2]: ", 1, 2);
        return (choice == 1) ? OptionPricingModel.OptionType.CALL : OptionPricingModel.OptionType.PUT;
    }
    
    private static double getDoubleInput(String prompt, double min, double max) {
        while (true) {
            System.out.print(prompt);
            try {
                double value = Double.parseDouble(scanner.nextLine().trim());
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.printf("Please enter a value between %.4f and %.4f%n", min, max);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }
    
    private static int getIntInput(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            try {
                int value = Integer.parseInt(scanner.nextLine().trim());
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.printf("Please enter a value between %d and %d%n", min, max);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter an integer.");
            }
        }
    }
    
    private static void printResults(OptionPricingModel.OptionType optionType,
                                    double currentPrice, double strikePrice,
                                    double timeToMaturity, double price,
                                    double error, int iterations,
                                    boolean converged, long computeTimeMs) {
        System.out.println("\n╔═════════════════════════════════════════════════╗");
        System.out.println("║          OPTION PRICING RESULTS                 ║");
        System.out.println("╠═════════════════════════════════════════════════╣");
        System.out.printf("║ Option Type:        %-27s ║%n", optionType);
        System.out.printf("║ Current Price (S₀): %27.4f ║%n", currentPrice);
        System.out.printf("║ Strike Price (K):   %27.4f ║%n", strikePrice);
        System.out.printf("║ Time to Maturity:   %27.4f years ║%n", timeToMaturity);
        System.out.println("╠═════════════════════════════════════════════════╣");
        System.out.printf("║ OPTION PRICE:       %27.4f ║%n", price);
        
        if (!Double.isNaN(error)) {
            System.out.printf("║ Estimated Error:    %27.6e ║%n", error);
            System.out.printf("║ Converged:          %-27s ║%n", converged ? "Yes" : "No");
        }
        
        System.out.printf("║ Iterations Used:    %27d ║%n", iterations);
        System.out.printf("║ Computation Time:   %27d ms ║%n", computeTimeMs);
        System.out.println("╚═════════════════════════════════════════════════╝");
        
        // Additional analysis
        double moneyness = currentPrice / strikePrice;
        String moneyStatus;
        if (moneyness > 1.05) {
            moneyStatus = (optionType == OptionPricingModel.OptionType.CALL) 
                ? "In-the-money" : "Out-of-the-money";
        } else if (moneyness < 0.95) {
            moneyStatus = (optionType == OptionPricingModel.OptionType.CALL) 
                ? "Out-of-the-money" : "In-the-money";
        } else {
            moneyStatus = "At-the-money";
        }
        
        System.out.println("\nAdditional Information:");
        System.out.printf("  Moneyness (S₀/K):  %.4f (%s)%n", moneyness, moneyStatus);
        System.out.printf("  Intrinsic Value:   %.4f%n", 
            Math.max(0, (optionType == OptionPricingModel.OptionType.CALL) 
                ? currentPrice - strikePrice 
                : strikePrice - currentPrice));
        System.out.printf("  Time Value:        %.4f%n", 
            price - Math.max(0, (optionType == OptionPricingModel.OptionType.CALL) 
                ? currentPrice - strikePrice 
                : strikePrice - currentPrice));
    }
}
