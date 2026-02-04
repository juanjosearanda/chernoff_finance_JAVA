#!/bin/bash

# Compilation and Execution Script for Chernoff Option Pricing Engine

echo "================================================="
echo "  Chernoff Option Pricing Engine - Build Script"
echo "================================================="
echo ""

# Clean previous builds
echo "Cleaning previous builds..."
rm -f *.class

# Compile all Java files
echo "Compiling Java source files..."
javac Grid.java ChernoffSolver.java OptionPricingModel.java Main.java

# Check compilation status
if [ $? -eq 0 ]; then
    echo "✓ Compilation successful!"
    echo ""
    echo "To run the application, execute:"
    echo "  java Main"
    echo ""
    echo "Or run it now? (y/n)"
    read -r response
    if [[ "$response" =~ ^([yY][eE][sS]|[yY])$ ]]; then
        echo ""
        java Main
    fi
else
    echo "✗ Compilation failed!"
    exit 1
fi
