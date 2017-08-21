package assertj;

import javax.lang.model.element.VariableElement;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;

public class FieldSourcerer extends MemberSourcerer {

  FieldSourcerer(final ClassSourcerer from, final VariableElement e) {
    super(from, e, e.asType());
  }

  @Override
public MethodSpec getter() {
    final MethodSpec getter =
        MethodSpec.methodBuilder(simpleName.toString())
            .addAnnotation(
                AnnotationSpec.builder(AssertFor.class)
                    .addMember("value", "$T.class", rawBoxed)
                    .build())
            .returns(type)
            .addCode(
                CodeBlock.builder()
                    .beginControlFlow("try")
                    .addStatement(
                        "$T field = this.getClass().getDeclaredField($S)",
                        fieldClassName,
                        simpleName)
                    .addStatement("field.setAccessible(true)")
                    .addStatement("return ($T) field.get$N($N)", type, typExtension, assertee)
                    .nextControlFlow(
                        "catch ($T | $T | $T | $T e)",
                        NoSuchFieldException.class,
                        SecurityException.class,
                        IllegalArgumentException.class,
                        IllegalAccessException.class)
                    .addStatement("throw new RuntimeException(e)")
                    .endControlFlow()
                    .build())
            .build();
    return getter;
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
