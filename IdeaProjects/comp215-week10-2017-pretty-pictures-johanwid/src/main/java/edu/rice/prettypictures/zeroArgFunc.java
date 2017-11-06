package edu.rice.prettypictures;

public interface zeroArgFunc extends FunctionTree {

  class Constant implements zeroArgFunc {
    private double c;

    public FunctionTree make(double constant){
      c = constant;
      return this;
    }

    public double[] evaluate(double x, double y) {
      return new double[] {c, c, c};
    }
  }

  class VarX implements zeroArgFunc {

    public FunctionTree make() {
      return this;
    }

    public double[] evaluate(double x, double y) {
      return new double[] {x, x, x};
    }
  }

  class VarX implements zeroArgFunc {

    public FunctionTree make() {
      return this;
    }

    public double[] evaluate(double x, double y) {
      return new double[] {x, x, x};
    }
  }

  class VarY implements zeroArgFunc {

    public FunctionTree make() {
      return this;
    }

    public double[] evaluate(double x, double y) {
      return new double[] {y, y, y};
    }
  }

}
