package assertj;

import java.io.IOException;
import java.util.List;

import javax.annotation.processing.FilerException;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.NoType;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic.Kind;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

public class ClassSourcerer extends Sourcerer {
    public final TypeSpec.Builder assertion;

    public ClassSourcerer(final TypeMirror s, final RoundEnvironment roundEnvironment, final ProcessingEnvironment processingEnv) {
        super(s, roundEnvironment, processingEnv);

        assertion = TypeSpec
                .classBuilder(className)
                .addModifiers(Modifier.PUBLIC)
                .addField(TypeName.get(s), assertee, Modifier.PRIVATE)
                .addMethod(
                        MethodSpec
                                .constructorBuilder()
                                .addParameter(ParameterSpec.builder(TypeName.get(s), assertee).build())
                                .addStatement("this.$N = $N", assertee, assertee)
                                .build())
                .addMethod(
                        MethodSpec
                                .methodBuilder("that")
                                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                                .addParameter(TypeName.get(s), assertee)
                                .returns(className)
                                .addStatement("return new $T($N)", className, assertee)
                                .build());
    }

    @Override
    public void generate() {
        final List<? extends Element> enclosedElements = element.getEnclosedElements();
        for (final Element e : enclosedElements)
            try {
                if (e.getKind() == ElementKind.FIELD && !e.getModifiers().contains(Modifier.STATIC))
                    new FieldSourcerer(this, (VariableElement) e).generate();
                if (e.getKind() == ElementKind.METHOD && !e.getModifiers().contains(Modifier.STATIC)) {
                    final ExecutableElement executableElement = (ExecutableElement) e;
                    if (executableElement.getParameters().size() == 0 && !(executableElement.getReturnType() instanceof NoType))
                        new MethodSourcerer(this, (ExecutableElement) e).generate();
                }
            } catch (Exception ex) {
                ex.printStackTrace(System.err);
                processingEnv.getMessager().printMessage(Kind.WARNING, "Couldn't generate assertions for", e);
            }
        try {
            JavaFile.builder(packageName, assertion.build()).build().writeTo(processingEnv.getFiler());
        } catch (final FilerException e1) {
            // Just don't recreate the file
        } catch (final IOException e1) {
            throw new RuntimeException(e1);
        }
    }
}
