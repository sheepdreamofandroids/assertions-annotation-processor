package assertj;

import com.squareup.javapoet.CodeBlock.Builder;
import javax.lang.model.element.VariableElement;

public class FieldSourcerer extends MemberSourcerer {

  FieldSourcerer(final ClassSourcerer from, final VariableElement e, String name) {
    super(from, e, e.asType(), name);
  }

  @Override
  protected Builder retrieve(Builder code) {
    return code.addStatement(
            "$T field = this.getClass().getDeclaredField($S)", fieldClassName, simpleName)
        .addStatement("field.setAccessible(true)")
        .addStatement("return ($T) field.get$N($N)", type, typExtension, assertee)
        .nextControlFlow(
            "catch ($T | $T | $T | $T e)",
            NoSuchFieldException.class,
            SecurityException.class,
            IllegalArgumentException.class,
            IllegalAccessException.class);
  }

  // public Object getO() {
  // try {
  // Field field = this.getClass().getDeclaredField("0");
  // field.setAccessible(true);
  // return field.get(assertee);
  // } catch (NoSuchFieldException | SecurityException | IllegalArgumentException
  // | IllegalAccessException e) {
  // throw new RuntimeException(e);
  // }
  // }
  //

}
