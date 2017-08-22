package assertj;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec.Builder;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

public abstract class MemberSourcerer extends Sourcerer {
  // private final Element e;
  private final Builder assertion;
  private final TypeMirror asType;
  protected TypeName type;
  private final TypeName boxed;
  protected TypeName rawBoxed;
  protected String typExtension;
  protected Name simpleName;

  MemberSourcerer(final ClassSourcerer from, final Element e, final TypeMirror typeMirror) {
    super(from);
    this.assertion = from.assertion;
    // this.e = e;
    if (typeMirror instanceof DeclaredType)
      if (((DeclaredType) typeMirror).asElement().getModifiers().contains(Modifier.PRIVATE))
        throw new IllegalArgumentException(e + " of type " + typeMirror + " is private");
    asType = typeMirror;
    type = TypeName.get(asType);
    boxed = type.box();
    rawBoxed = raw(boxed);
    typExtension = typeExtension(type);
    simpleName = e.getSimpleName();
  }

  @Override
  public void generate() {
    final MethodSpec getter = getter();

    assertion.addMethod(getter);

    // simple equality
    assertion.addMethod(
        MethodSpec.methodBuilder(simpleName + "Is")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(type, "f")
            .addJavadoc("Compare using Objects::equal")
            .addCode(
                CodeBlock.builder()
                    .addStatement("$T.assertEquals($N(), f)", assertionsClassName, getter)
                    .build())
            .build());

    if (asType.getKind() != TypeKind.BOOLEAN) { // overkill
      // using lambda
      final TypeName predicateType =
          typExtension == null || typExtension.length() == 0 //
              ? ParameterizedTypeName.get(ClassName.get(Predicate.class), boxed)
              : ClassName.get("java.util.function", typExtension + "Predicate");
      ParameterSpec param = ParameterSpec.builder(predicateType, "f").build();
      assertion.addMethod(
          MethodSpec.methodBuilder(simpleName + "_")
              .addModifiers(Modifier.PUBLIC)
              .addParameter(param)
              .addCode(
                  CodeBlock.builder()
                      .addStatement(
                          "$T.assertTrue($N.test($N()))", assertionsClassName, param, getter)
                      .build())
              .build());

      // using lambda on AsserterType
      //      final TypeName predicateType2 =
      //              typExtension == null || typExtension.length() == 0 //
      //                  ? ParameterizedTypeName.get(ClassName.get(Predicate.class), boxed)
      //                  : ClassName.get("java.util.function", typExtension + "Predicate");
      //          ParameterSpec param = ParameterSpec.builder(predicateType2, "f").build();
      final Element typeElementx = typeUtils.asElement(asType);
      if (typeElementx != null) {
        final TypeName asserter = asserterName(type);
        final TypeName pType = ParameterizedTypeName.get(ClassName.get(Consumer.class), asserter);
        param = ParameterSpec.builder(pType, "f").build();
        assertion.addMethod(
            MethodSpec.methodBuilder(simpleName.toString())
                .addModifiers(Modifier.PUBLIC)
                .addParameter(param)
                .addCode(
                    CodeBlock.builder()
                        .addStatement("$N.accept($T.that($N()))", param, raw(asserter), getter)
                        .build())
                .build());
      }
    }
  }

  public MethodSpec getter() {
    com.squareup.javapoet.MethodSpec.Builder methodB =
        MethodSpec.methodBuilder(simpleName.toString()).addModifiers(Modifier.PUBLIC);
    if (rawBoxed != null)
      methodB =
          methodB.addAnnotation(
              AnnotationSpec.builder(AssertFor.class)
                  .addMember("value", "$T.class", rawBoxed)
                  .build());
    return methodB
        .returns(type)
        .addCode(
            retrieve(CodeBlock.builder().beginControlFlow("try"))
                .addStatement("throw new RuntimeException(e)")
                .endControlFlow()
                .build())
        .build();
  }

  protected abstract com.squareup.javapoet.CodeBlock.Builder retrieve(
      com.squareup.javapoet.CodeBlock.Builder code);
}
