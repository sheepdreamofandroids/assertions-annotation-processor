package assertj;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.lang.model.element.ExecutableElement;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;

public class MethodSourcerer extends MemberSourcerer {

    public MethodSourcerer(ClassSourcerer classSourcerer, ExecutableElement e) {
        super(classSourcerer, e, e.getReturnType());
    }

    @Override
    protected MethodSpec getter() {
        MethodSpec getter = MethodSpec
                .methodBuilder(simpleName.toString())
                .addAnnotation(AnnotationSpec.builder(AssertFor.class).addMember("value", "$T.class", rawBoxed).build())
                .returns(type)
                .addCode(
                        CodeBlock
                                .builder()
                                .beginControlFlow("try")
                                .addStatement("$T method = this.getClass().getDeclaredField($S)", methodClassName, simpleName)
                                .addStatement("method.setAccessible(true)")
                                .addStatement("return ($T) method.invoke($N)", type, assertee)
                                .nextControlFlow(
                                        "catch ($T | $T | $T | $T | $T e)",
                                        NoSuchMethodException.class,
                                        SecurityException.class,
                                        IllegalArgumentException.class,
                                        IllegalAccessException.class,
                                        InvocationTargetException.class)
                                .addStatement("throw new RuntimeException(e)")
                                .endControlFlow()
                                .build())
                .build();
        return getter;
    }

    public Object getO() {
        try {
            Method method = this.getClass().getDeclaredMethod("");
            method.setAccessible(true);
            return method.invoke(assertee);
        } catch (SecurityException | IllegalArgumentException | IllegalAccessException | NoSuchMethodException
                | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

}
