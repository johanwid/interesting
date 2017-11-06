package edu.rice.prettypictures;

import java.util.function.Function;

public interface oneArgFunc extends FunctionTree {

  double[] evaluate(double x, double y);

  FunctionTree make(FunctionTree childFunc);

  class Negate implements FunctionTree {
    private FunctionTree child;

    public FunctionTree make(FunctionTree childFunc) {
      child = childFunc;
      return this;
    }

    public double[] evaluate(double x) {
      return new double[] {-1 * x, -1 * x, -1 * x};
    }
  }

  class Sine implements FunctionTree {

    public FunctionTree make() {
      return this;
    }

    public double[] evaluate(double x){
      return new double[] {Math.sin(x), Math.sin(x), Math.sin(x)};
    }
  }
}