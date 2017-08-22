package test;

import assertj.AssertSourcerer;
import assertj.AssertsFor;
import assertj.Sourcerer;

@AssertsFor({Sourcerer.class, Dummy.class})
public class Test {

  @AssertsFor({String.class})
  public void someTest() {}

  public void someOtherTest() {
    AssertSourcerer.that(null).assertee($ -> $.hashIs(13));
    final Dummy dummy = new Dummy();
    //      new DummyAs
  }
}
