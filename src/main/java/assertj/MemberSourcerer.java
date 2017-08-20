package assertj;

import java.util.function.Predicate;

import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import com.google.common.base.Strings;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec.Builder;

public abstract class MemberSourcerer extends Sourcerer {
    // private final Element e;
    private Builder assertion;
    private TypeMirror asType;
    protected TypeName type;
    private TypeName boxed;
    protected TypeName rawBoxed;
    protected String typExtension;
    protected Name simpleName;

    MemberSourcerer(ClassSourcerer from, Element e, TypeMirror typeMirror) {
        super(from);
        this.assertion = from.assertion;
        // this.e = e;

        asType = typeMirror;
        type = TypeName.get(asType);
        boxed = type.box();
        rawBoxed = boxed instanceof ParameterizedTypeName ? ((ParameterizedTypeName) boxed).rawType : boxed;
        typExtension = typeExtension(type);
        simpleName = e.getSimpleName();
    }

    @Override
    public void generate() {
        MethodSpec getter = getter();

        assertion.addMethod(getter);

        // simple equality
        assertion.addMethod(
                MethodSpec
                        .methodBuilder(simpleName + "Is")
                        .addParameter(type, "f")
                        .addJavadoc("Compare using Objects::equal")
                        .addCode(CodeBlock.builder().addStatement("$T.assertEquals($N(), f)", assertionsClassName, getter).build())
                        .build());

        if (asType.getKind() != TypeKind.BOOLEAN) {// overkill
            // using lambda
            TypeName predicateType = Strings.isNullOrEmpty(typExtension)//
                    ? ParameterizedTypeName.get(ClassName.get(Predicate.class), boxed)
                    : ClassName.get("java.util.function", typExtension + "Predicate");
            ParameterSpec param = ParameterSpec.builder(predicateType, "f").build();
            assertion.addMethod(
                    MethodSpec
                            .methodBuilder(simpleName + "Is")
                            .addParameter(param)
                            .addCode(
                                    CodeBlock
                                            .builder()
                                            .addStatement("$T.assertTrue($N.test($N()))", assertionsClassName, param, getter)
                                            .build())
                            .build());

            // using lambda on AsserterType
            Element typeElementx = typeUtils.asElement(asType);
            if (typeElementx != null) {
                TypeName asserter = asserterName(type);
                TypeName pType = ParameterizedTypeName.get(ClassName.get(Predicate.class), asserter);
                param = ParameterSpec.builder(pType, "f").build();
                assertion.addMethod(
                        MethodSpec
                                .methodBuilder(simpleName + "Is_")
                                .addParameter(param)
                                .addCode(
                                        CodeBlock
                                                .builder()
                                                .addStatement(
                                                        "$T.assertTrue($N.test($T.that($N())))",
                                                        assertionsClassName,
                                                        param,
                                                        asserter,
                                                        getter)
                                                .build())
                                .build());
            }
        }
    }

    protected abstract MethodSpec getter();
}