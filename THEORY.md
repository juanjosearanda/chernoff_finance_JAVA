# Mathematical Theory: Chernoff Approximation Method

## 1. Introduction

The Chernoff approximation provides a powerful method for approximating solutions to parabolic partial differential equations (PDEs) and their corresponding evolution operators. This document explains the theoretical foundation underlying our option pricing implementation.

## 2. The Abstract Problem

### 2.1 Evolution Equations

Consider an evolution equation of the form:

```
∂u/∂t = Lu
```

where `L` is a linear differential operator. The formal solution is:

```
u(t) = e^(tL) u(0)
```

The challenge is to approximate the evolution operator `e^(tL)` numerically.

### 2.2 The Operator L

In our case, `L` is a second-order differential operator:

```
L = a(x)∂²/∂x² + b(x)∂/∂x + c(x)
```

or in function notation:

```
(Lf)(x) = a(x)f''(x) + b(x)f'(x) + c(x)f(x)
```

## 3. Chernoff's Theorem

### 3.1 Classical Chernoff Approximation

Chernoff's theorem states that if we have a family of operators `S(t)` satisfying certain conditions, then:

```
lim[n→∞] (S(t/n))^n = e^(tL)
```

in an appropriate topology.

### 3.2 The Translation-Based Chernoff Function

For our operator L, Remizov (2025) proposed the following Chernoff function:

```
(S(t)f)(x) = (1/4)f(x + 2√(a(x)t)) 
           + (1/4)f(x - 2√(a(x)t))
           + (1/2)f(x + 2b(x)t)
           + t·c(x)f(x)
```

### 3.3 Interpretation

Each term has a specific role:

1. **First two terms**: Handle the second derivative (diffusion)
   - Average of function values at shifted points
   - Shift magnitude: `±2√(a(x)t)`
   - Approximates `a(x)f''(x)` for small `t`

2. **Third term**: Handles the first derivative (drift)
   - Function value at shifted point
   - Shift magnitude: `2b(x)t`
   - Approximates `b(x)f'(x)` for small `t`

3. **Fourth term**: Handles the zeroth-order term
   - Direct multiplication by `t·c(x)`
   - Approximates `c(x)f(x)` for small `t`

## 4. Error Analysis

### 4.1 Convergence Rate

The error in the n-th approximation is bounded by:

```
||S(t/n)^n g - e^(tL)g|| ≤ (t²·e^(||c||·t))/n · C
```

where:
- `||·||` denotes an appropriate norm
- `||c||` is the supremum norm of the coefficient `c(x)`
- `C` is a constant depending on the function `g` and coefficients

### 4.2 Order of Convergence

This gives us **first-order convergence** in `1/n`:

```
Error = O(t²/n)
```

To achieve error `ε`, we need:

```
n ≥ t²·e^(||c||·t)·C/ε
```

## 5. Application to Black-Scholes

### 5.1 The Black-Scholes PDE

The Black-Scholes equation for option pricing is:

```
∂V/∂t + (1/2)σ²S²·∂²V/∂S² + rS·∂V/∂S - rV = 0
```

### 5.2 Transformation to Standard Form

Rearranging to match our operator notation:

```
∂V/∂t = (1/2)σ²S²·∂²V/∂S² + rS·∂V/∂S - rV
```

This gives us:

```
∂V/∂t = LV
```

where:

```
L = a(S)∂²/∂S² + b(S)∂/∂S + c(S)
```

with:

```
a(S) = (1/2)σ²S²
b(S) = rS
c(S) = -r
```

### 5.3 Variable Coefficients

For more realistic models, we allow:

```
a(S) = (1/2)σ(S)²S²    (local volatility)
b(S) = r(S)S            (state-dependent drift)
c(S) = -r(S)            (state-dependent discounting)
```

### 5.4 Backward Evolution

Option pricing requires solving backward in time:
- At maturity (t=T): `V(S,T) = Payoff(S)`
- At present (t=0): `V(S,0) = ?`

The solution is:

```
V(S,0) = e^(TL)[Payoff](S)
```

## 6. Numerical Implementation

### 6.1 Discretization

We discretize the spatial domain [S_min, S_max] into a grid:

```
S_i = S_min + i·ΔS,  i = 0, 1, ..., N
```

where `ΔS = (S_max - S_min)/N`.

### 6.2 Interpolation

The Chernoff formula requires evaluating the function at shifted points:
- `S + 2√(a(S)t/n)`
- `S - 2√(a(S)t/n)`
- `S + 2b(S)t/n`

These points typically don't coincide with grid points, so we use **linear interpolation**.

### 6.3 Algorithm

```
Input: Payoff function, time T, number of iterations n
Initialize: V^(0) = Payoff on grid
For k = 1 to n:
    For each grid point S_i:
        Compute shifts using a(S_i), b(S_i), c(S_i)
        Evaluate V^(k-1) at shifted points (with interpolation)
        Apply Chernoff formula to get V^(k)(S_i)
Return: V^(n)
```

### 6.4 Complexity

- **Time**: O(n·N) where n = iterations, N = grid size
- **Space**: O(N) for storing two grid states

## 7. Stability Considerations

### 7.1 Positivity

For option pricing, we need `V(S,t) ≥ 0` for all S,t. The Chernoff method preserves positivity under certain conditions on the coefficients.

### 7.2 Boundary Conditions

Natural boundary conditions emerge from the grid structure:
- At `S = 0`: Call value → 0, Put value → K
- At `S → ∞`: Call value → S, Put value → 0

### 7.3 Grid Range

The grid must be wide enough to capture the relevant dynamics:
- Too narrow: boundary effects dominate
- Too wide: wasted computation

A good heuristic: `[0.2·S₀, 3·S₀]` for most options.

## 8. Extensions

### 8.1 Time-Dependent Coefficients

If coefficients depend on time, we can use:

```
e^(TL) ≈ e^(δt·L(t_{n-1})) · e^(δt·L(t_{n-2})) · ... · e^(δt·L(t_0))
```

with `δt = T/n`.

### 8.2 American Options

American options require checking at each time step:

```
V(S,t) = max(Payoff(S), ContinuationValue(S,t))
```

This breaks the linearity but can still be handled numerically.

### 8.3 Multi-Dimensional Problems

For multiple underlying assets, the operator becomes:

```
L = Σᵢⱼ aᵢⱼ(x)∂²/∂xᵢ∂xⱼ + Σᵢ bᵢ(x)∂/∂xᵢ + c(x)
```

The Chernoff approximation extends naturally but computational cost increases exponentially.

## 9. Comparison with Other Methods

### 9.1 Finite Differences

Traditional finite difference methods discretize derivatives directly:

```
f''(x) ≈ (f(x+h) - 2f(x) + f(x-h))/h²
```

**Advantages of Chernoff**:
- Inherently stable for appropriate step sizes
- Natural handling of variable coefficients
- Direct approximation of evolution operator

**Disadvantages**:
- Higher computational cost per step
- Requires interpolation

### 9.2 Monte Carlo

Monte Carlo simulates stochastic paths:

```
V(S₀) = e^(-rT) E[Payoff(S_T)]
```

**Advantages of Chernoff**:
- Deterministic (no random sampling)
- Gets entire price surface, not just one point
- Better for Greeks computation

**Disadvantages**:
- Limited to lower dimensions
- More complex implementation

### 9.3 Analytical Formulas

When available (e.g., Black-Scholes), analytical formulas are fastest and most accurate.

**Chernoff is valuable when**:
- No analytical solution exists
- Variable coefficients are present
- Exotic payoffs are involved

## 10. Validation Strategies

### 10.1 Known Solutions

Test against Black-Scholes formula for constant parameters.

### 10.2 Put-Call Parity

Verify: `C - P = S₀ - Ke^(-rT)`

### 10.3 Convergence Studies

Check that error decreases as O(1/n) as iterations increase.

### 10.4 Greeks

Compute delta, gamma, etc., and verify they satisfy relationships:
- Delta of call at strike: ≈ 0.5 for ATM
- Gamma positive for long positions
- Theta negative for long options

## 11. Conclusion

The Chernoff approximation provides a robust, intuitive method for solving parabolic PDEs arising in mathematical finance. Its translation-based formula directly captures the diffusion, drift, and discount effects present in the Black-Scholes equation, making it particularly well-suited for option pricing with variable coefficients.

---

**References**:
1. Chernoff, H. (1968). "Note on Product Approximations to the Exponential Function"
2. Remizov, I. (2025). "Translation-Based Chernoff Approximations for Second-Order ODEs"
3. Shreve, S. (2004). "Stochastic Calculus for Finance II"
