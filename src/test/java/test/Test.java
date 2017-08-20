package test;

import assertj.AnnotationProcessor;
import assertj.ClassSourcerer;
import assertj.FieldSourcerer;
import assertj.Sourcerer;
import assertj.AssertsFor;

@AssertsFor({ Sourcerer.class, Dummy.class })
public class Test {

    @AssertsFor({ String.class })
    public void someTest() {

    }

}
