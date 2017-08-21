package test;

import assertj.AssertsFor;
import assertj.Sourcerer;

@AssertsFor({Sourcerer.class, Dummy.class})
public class Test {

  @AssertsFor({String.class})
  public void someTest() {}
}
