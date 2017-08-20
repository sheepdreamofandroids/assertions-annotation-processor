package assertj;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.AnnotationValueVisitor;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.AbstractAnnotationValueVisitor8;
import javax.tools.Diagnostic;

import org.assertj.assertions.generator.cli.AssertionGeneratorLauncher;

import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.JavaFile.Builder;

public class AnnotationProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        List<String> neededClasses = new ArrayList<>();
        Set<TypeMirror> toGenerate = Stream
                .concat(
                        roundEnv.getElementsAnnotatedWith(AssertsFor.class).stream().flatMap(
                                e -> Arrays.asList(e.getAnnotationsByType(AssertsFor.class)).stream().flatMap(
                                        AnnotationProcessor::typeMirrors)),
                        roundEnv.getElementsAnnotatedWith(AssertFor.class).stream().flatMap(
                                e -> Arrays.asList(e.getAnnotationsByType(AssertFor.class)).stream().map(AnnotationProcessor::typeMirror)))
                .collect(Collectors.toSet());
        toGenerate.forEach(s -> {
            try {
                generateAssertionsFor(s, roundEnv);
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        });
        // // neededClasses.add("-H");
        // Set<? extends Element> elementsAnnotatedWith =
        // roundEnv.getElementsAnnotatedWith(AssertsFor.class);
        // for (Element annotatedElement : elementsAnnotatedWith) {
        // if (!(annotatedElement.getKind() == ElementKind.CLASS ||
        // annotatedElement.getKind() == ElementKind.METHOD)) {
        // error(annotatedElement, "Only classes and methods can be annotated with @%s",
        // AssertsFor.class.getSimpleName());
        // return true; // Exit processing
        // }
        // Arrays
        // .asList(annotatedElement.getAnnotationsByType(AssertsFor.class))
        // .stream()
        // .flatMap(AnnotationProcessor::classnames)
        // .collect(Collectors.toSet());
        // for (AssertsFor assertFor :
        // annotatedElement.getAnnotationsByType(AssertsFor.class)) {
        // // Class<?>[] value = assertFor.value();
        // // for (Class<?> class1 : value) {
        // // System.out.println(class1.getCanonicalName());
        // // }
        // // }
        // // for (AnnotationMirror annotationMirror :
        // // annotatedElement.getAnnotationMirrors()) {
        // // Set<Map.Entry<? extends ExecutableElement, ? extends AnnotationValue>>
        // // elementValuesWithDefaults = (Set) processingEnv
        // // .getElementUtils()
        // // .getElementValuesWithDefaults(annotationMirror)
        // // .entrySet();
        // // for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> e :
        // // elementValuesWithDefaults) {
        // // if (e.getKey().getSimpleName().contentEquals("value")) {
        // // List<AnnotationValue> value = (List<AnnotationValue>)
        // // e.getValue().getValue();
        // // for (AnnotationValue annotationValue : value) {
        // // System.out.println(annotationValue.getClass());
        // // annotationValue.accept(new EmptyAnnotationValueVisitor<Void, Void>() {
        // // @Override
        // // public Void visitType(TypeMirror t, Void p) {
        // // neededClasses.add(t.toString());
        // // return null;
        // // }
        // // }, null);
        // // }
        // // }
        // // System.out.println(e.getKey() + " ---> " + e.getValue());
        // // }
        // // annotationMirror.getElementValues()
        // neededClasses.addAll(classnames(assertFor).collect(Collectors.toSet()));
        // // System.out.println(value);
        // }
        // }
        // System.out.println(neededClasses);
        // // processingEnv.getMessager()getFiler().
        // try {
        // new AssertionGeneratorLauncher().main(neededClasses.toArray(new
        // String[neededClasses.size()]));
        // } catch (IOException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        return true;
    }

    static Stream<String> classnames(AssertsFor assertFor) {
        try {
            return Arrays.asList(assertFor.value()).stream().map(Class::getCanonicalName);
        } catch (MirroredTypesException mte) {
            return mte.getTypeMirrors().stream().map(Object::toString);
        }
    }

    static String classname(AssertFor assertFor) {
        try {
            return assertFor.value().getCanonicalName();
        } catch (MirroredTypeException mte) {
            return mte.getTypeMirror().toString();
        }
    }

    static Stream<? extends TypeMirror> typeMirrors(AssertsFor assertFor) {
        try {
            assertFor.value();
            return null;
        } catch (MirroredTypesException mte) {
            return mte.getTypeMirrors().stream();
        }
    }

    static TypeMirror typeMirror(AssertFor assertFor) {
        try {
            assertFor.value();
            return null;
        } catch (MirroredTypeException mte) {
            return mte.getTypeMirror();
        }
    }

    void generateAssertionsFor(TypeMirror s, RoundEnvironment roundEnvironment) throws IOException {
        // TypeElement typeElement = processingEnv.getElementUtils().getTypeElement(s);
        // Writer writer = processingEnv.getFiler().createSourceFile(c).openWriter();
        if (s.getKind() == TypeKind.DECLARED)
            new ClassSourcerer(s, roundEnvironment, this.processingEnv).generate();
        ;
    }

    private void error(Element e, String msg, Object... args) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, String.format(msg, args), e);
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        HashSet<String> set = new HashSet<String>();
        set.add(AssertsFor.class.getCanonicalName());
        return set;
    }
}
