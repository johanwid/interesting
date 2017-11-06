package edu.rice.prettypictures;

import java.util.function.Function;

public interface twoArgFunc extends FunctionTree {

  public double[] evaluate(double x, double y);

  class Add implements FunctionTree {

    @Override
    public FunctionTree make() {
      return this;
    }

    public double[] evaluate(double x, double y) {
      return new double[] {x + y, x + y, x + y};
    }
  }

  class Divide implements FunctionTree {

    @Override
    public FunctionTree make() {
      return this;
    }

    public double[] evaluate(double x, double y) {
      return y == 0 ? new double[] {0, 0, 0} : new double[] {x / y, x / y, x / y};
    }
  }
}