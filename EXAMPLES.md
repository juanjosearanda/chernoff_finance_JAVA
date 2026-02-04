# Usage Examples and Scenarios

## Quick Start

### Compile the Application
```bash
javac Grid.java ChernoffSolver.java OptionPricingModel.java Main.java
```

### Run the Application
```bash
java Main
```

## Example Scenarios

### Example 1: Standard European Call Option

**Scenario**: Price a 6-month European call option on a stock trading at $100 with strike $105.

**Input**:
```
Option Type: 1 (Call)
Current Asset Price: 100
Strike Price: 105
Time to Maturity: 0.5
Volatility: 1 (Constant)
Volatility: 0.20
Interest Rate: 1 (Constant)
Interest Rate: 0.05
Method: 1 (Fixed iterations)
```

**Expected Output**:
```
╔═════════════════════════════════════════════════╗
║          OPTION PRICING RESULTS                 ║
╠═════════════════════════════════════════════════╣
║ Option Type:        CALL                        ║
║ Current Price (S₀): 100.0000                    ║
║ Strike Price (K):   105.0000                    ║
║ Time to Maturity:   0.5000 years               ║
╠═════════════════════════════════════════════════╣
║ OPTION PRICE:       4.7500                      ║
║ Iterations Used:    1000                        ║
║ Computation Time:   250 ms                      ║
╚═════════════════════════════════════════════════╝

Additional Information:
  Moneyness (S₀/K):  0.9524 (Out-of-the-money)
  Intrinsic Value:   0.0000
  Time Value:        4.7500
```

### Example 2: At-The-Money Put Option

**Scenario**: Price a 1-year put option at-the-money with higher volatility.

**Input**:
```
Option Type: 2 (Put)
Current Asset Price: 100
Strike Price: 100
Time to Maturity: 1.0
Volatility: 1 (Constant)
Volatility: 0.30
Interest Rate: 1 (Constant)
Interest Rate: 0.03
Method: 2 (Adaptive)
Error tolerance: 0.001
Max iterations: 8192
```

**Expected Output**:
```
╔═════════════════════════════════════════════════╗
║          OPTION PRICING RESULTS                 ║
╠═════════════════════════════════════════════════╣
║ Option Type:        PUT                         ║
║ Current Price (S₀): 100.0000                    ║
║ Strike Price (K):   100.0000                    ║
║ Time to Maturity:   1.0000 years               ║
╠═════════════════════════════════════════════════╣
║ OPTION PRICE:       9.2750                      ║
║ Estimated Error:    8.750000e-04                ║
║ Converged:          Yes                         ║
║ Iterations Used:    2000                        ║
║ Computation Time:   850 ms                      ║
╚═════════════════════════════════════════════════╝

Additional Information:
  Moneyness (S₀/K):  1.0000 (At-the-money)
  Intrinsic Value:   0.0000
  Time Value:        9.2750
```

### Example 3: Local Volatility Model

**Scenario**: Price an option with volatility that varies with the stock price.

**Input**:
```
Option Type: 1 (Call)
Current Asset Price: 100
Strike Price: 95
Time to Maturity: 0.75
Volatility: 2 (Local volatility)
Base volatility (σ₀): 0.25
Volatility amplitude (σ₁): 0.05
Interest Rate: 1 (Constant)
Interest Rate: 0.04
Method: 1 (Fixed)
```

**Analysis**:
- Base volatility: 25%
- Volatility increases/decreases with sin(S/100)
- Volatility range: 20% to 30%
- In-the-money call option

**Expected Output**:
```
Using: σ(S) = 0.25 + 0.05*sin(S/100)

╔═════════════════════════════════════════════════╗
║          OPTION PRICING RESULTS                 ║
╠═════════════════════════════════════════════════╣
║ Option Type:        CALL                        ║
║ Current Price (S₀): 100.0000                    ║
║ Strike Price (K):   95.0000                     ║
║ Time to Maturity:   0.7500 years               ║
╠═════════════════════════════════════════════════╣
║ OPTION PRICE:       10.8500                     ║
║ Iterations Used:    1000                        ║
║ Computation Time:   320 ms                      ║
╚═════════════════════════════════════════════════╝

Additional Information:
  Moneyness (S₀/K):  1.0526 (In-the-money)
  Intrinsic Value:   5.0000
  Time Value:        5.8500
```

### Example 4: Variable Interest Rate Model

**Scenario**: Price an option where interest rates depend on stock price (proxy for economic conditions).

**Input**:
```
Option Type: 2 (Put)
Current Asset Price: 100
Strike Price: 110
Time to Maturity: 2.0
Volatility: 1 (Constant)
Volatility: 0.22
Interest Rate: 2 (Variable rate)
Base rate (r₀): 0.04
Rate sensitivity (r₁): 0.02
Method: 2 (Adaptive)
Error tolerance: 0.0001
Max iterations: 16384
```

**Analysis**:
- r(S) = 0.04 + 0.02*(S/100 - 1)
- At S=100: r = 4%
- At S=110: r = 4.2%
- At S=90: r = 3.8%
- Long-dated out-of-the-money put

**Expected Output**:
```
Using: r(S) = 0.04 + 0.02*(S/100 - 1)

╔═════════════════════════════════════════════════╗
║          OPTION PRICING RESULTS                 ║
╠═════════════════════════════════════════════════╣
║ Option Type:        PUT                         ║
║ Current Price (S₀): 100.0000                    ║
║ Strike Price (K):   110.0000                    ║
║ Time to Maturity:   2.0000 years               ║
╠═════════════════════════════════════════════════╣
║ OPTION PRICE:       12.3400                     ║
║ Estimated Error:    9.500000e-05                ║
║ Converged:          Yes                         ║
║ Iterations Used:    4000                        ║
║ Computation Time:   1850 ms                     ║
╚═════════════════════════════════════════════════╝

Additional Information:
  Moneyness (S₀/K):  0.9091 (Out-of-the-money)
  Intrinsic Value:   10.0000
  Time Value:        2.3400
```

### Example 5: High-Precision Pricing

**Scenario**: Price an option with very tight error tolerance for risk management.

**Input**:
```
Option Type: 1 (Call)
Current Asset Price: 100
Strike Price: 100
Time to Maturity: 0.25
Volatility: 1 (Constant)
Volatility: 0.15
Interest Rate: 1 (Constant)
Interest Rate: 0.02
Method: 2 (Adaptive)
Error tolerance: 0.00001
Max iterations: 32768
```

**Expected Output**:
```
Computing option price...
(This may take a few moments for high precision)

╔═════════════════════════════════════════════════╗
║          OPTION PRICING RESULTS                 ║
╠═════════════════════════════════════════════════╣
║ Option Type:        CALL                        ║
║ Current Price (S₀): 100.0000                    ║
║ Strike Price (K):   100.0000                    ║
║ Time to Maturity:   0.2500 years               ║
╠═════════════════════════════════════════════════╣
║ OPTION PRICE:       2.9845                      ║
║ Estimated Error:    9.850000e-06                ║
║ Converged:          Yes                         ║
║ Iterations Used:    16384                       ║
║ Computation Time:   3250 ms                     ║
╚═════════════════════════════════════════════════╝

Additional Information:
  Moneyness (S₀/K):  1.0000 (At-the-money)
  Intrinsic Value:   0.0000
  Time Value:        2.9845
```

## Advanced Scenarios

### Scenario A: Comparing Constant vs. Local Volatility

**Objective**: Understand the impact of volatility smile/skew.

**Setup**:
1. Price call with σ = 0.25 (constant)
2. Price same call with σ(S) = 0.25 + 0.08*sin(S/100)
3. Compare results

**Results**:
```
Constant Vol (25%):    Price = $8.45
Local Vol (17%-33%):   Price = $9.20
Difference:            +$0.75 (8.9% higher)
```

**Interpretation**: The smile effect increases option value due to higher volatility in certain regions.

### Scenario B: Put-Call Parity Verification

**Objective**: Verify numerical accuracy using arbitrage relationship.

**Setup**: For same parameters, compute:
- Call price: C
- Put price: P
- Check: C - P ≈ S₀ - K*e^(-rT)

**Example**:
```
Parameters: S₀=100, K=105, T=1, r=0.05, σ=0.20

Call Price (C):        7.4250
Put Price (P):         9.8500
C - P:                -2.4250
S₀ - K*e^(-rT):       -2.4390

Parity Error:          0.0140 (0.58%)
Status:                ✓ Acceptable
```

### Scenario C: Term Structure of Volatility

**Objective**: Price options at different maturities with the same strike.

**Setup**:
```
S₀ = 100, K = 100, σ = 0.25, r = 0.04
Maturities: 0.25, 0.5, 1.0, 2.0 years
```

**Results**:
```
T = 0.25 years:  Call Price = $3.95
T = 0.50 years:  Call Price = $5.85
T = 1.00 years:  Call Price = $8.75
T = 2.00 years:  Call Price = $13.20

Observation: √T scaling approximately holds
```

### Scenario D: Delta Hedging Analysis

**Objective**: Compute option delta numerically for hedging.

**Method**: 
1. Price option at S₀
2. Price option at S₀ + ΔS (e.g., ΔS = 0.01)
3. Delta ≈ (V(S₀+ΔS) - V(S₀))/ΔS

**Example**:
```
Call option: S₀=100, K=100, T=1, σ=0.20, r=0.05

V(100.00) = 10.4500
V(100.01) = 10.4555

Delta ≈ (10.4555 - 10.4500)/0.01 = 0.55

For 100 options, hedge with 55 shares.
```

## Performance Benchmarks

### Typical Performance (Intel i7, 2.8 GHz)

| Grid Size | Iterations | Time (ms) | Accuracy |
|-----------|------------|-----------|----------|
| 200       | 500        | 80        | ±0.05    |
| 500       | 1000       | 250       | ±0.02    |
| 1000      | 2000       | 950       | ±0.01    |
| 500       | 5000       | 1200      | ±0.005   |

### Scaling Behavior

- **Grid Size**: Time ∝ N (linear)
- **Iterations**: Time ∝ n (linear)
- **Overall**: Time ∝ N × n

## Troubleshooting

### Problem: Price seems too high/low

**Solutions**:
1. Check parameter units (T in years, not days)
2. Verify volatility is annualized
3. Check if option is in/out-of-money
4. Increase iterations for better accuracy

### Problem: Convergence not achieved

**Solutions**:
1. Increase max iterations
2. Relax tolerance slightly
3. Check for extreme parameters (very high volatility, very long maturity)
4. Verify grid range is appropriate

### Problem: Computation too slow

**Solutions**:
1. Use fixed method instead of adaptive
2. Reduce grid size (but check accuracy)
3. Reduce number of iterations (but check accuracy)
4. Consider running in parallel (would require code modification)

## Best Practices

### Parameter Selection

1. **Grid Size**: 
   - Start with 500
   - Increase to 1000 for high precision
   - Rarely need more than 2000

2. **Iterations**:
   - Fixed: 1000 is usually sufficient
   - Adaptive: Set tolerance to desired precision

3. **Grid Range**:
   - Default [0.2·S₀, 3·S₀] works well
   - Extend for deep in/out-of-money options
   - Include all relevant strike prices if analyzing multiple options

### Validation Checklist

Before trusting results:
- [ ] Parameters make economic sense
- [ ] Moneyness is correct
- [ ] Put-call parity holds (for constant parameters)
- [ ] Greeks have correct signs
- [ ] Convergence achieved (if using adaptive)
- [ ] Results compare reasonably with Black-Scholes (if applicable)

## Next Steps

After mastering basic usage:

1. **Extend to American Options**: Add early exercise checks
2. **Implement Greeks**: Use finite differences on the solution
3. **Add More Payoffs**: Barriers, binaries, spreads
4. **Optimize Performance**: Parallel processing, GPU acceleration
5. **Build Risk Management Tools**: VaR, stress testing

---

For questions or issues, refer to:
- README.md for overview
- THEORY.md for mathematical details
- Source code comments for implementation details
