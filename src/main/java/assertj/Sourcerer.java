package assertj;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import org.junit.gen5.api.Assertions;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

public abstract class Sourcerer {

  protected final TypeMirror s;
  protected RoundEnvironment roundEnvironment;
  protected ProcessingEnvironment processingEnv;
  protected Element element;
  protected String assertee;
  protected String packageName;
  protected ClassName className;
  public static final ClassName fieldClassName = ClassName.get(Field.class);
  public static final ClassName methodClassName = ClassName.get(Method.class);
  public static final ClassName objectsClassName = ClassName.get(Objects.class);
  public static final ClassName assertionsClassName = ClassName.get(Assertions.class);
  protected final Types typeUtils;
  protected final Elements elementUtils;

  public Sourcerer(
      final TypeMirror s, final RoundEnvironment roundEnvironment, final ProcessingEnvironment processingEnv) {
    super();
    this.s = s;
    this.roundEnvironment = roundEnvironment;
    this.processingEnv = processingEnv;
    typeUtils = processingEnv.getTypeUtils();
    elementUtils = processingEnv.getElementUtils();
    element = typeUtils.asElement(s);
    assertee = "assertee";
    packageName = findPackageFor(element).toString();
    final ClassName cn = (ClassName) TypeName.get(s);
    final String asserterName = cn.simpleNames().stream().collect(Collectors.joining("_"));
    className = (ClassName) asserterName(TypeName.get(s));
    // ClassName.get(packageName, "Assert" + element.getSimpleName().toString());
  }

  public Sourcerer(final Sourcerer from) {
    this(from.s, from.roundEnvironment, from.processingEnv);
  }

  protected TypeName asserterName(final TypeName type) {
    final ClassName cname =
        type instanceof ParameterizedTypeName
            ? ((ParameterizedTypeName) type).rawType
            : (ClassName) type;
    // elementUtils.getPackageOf(typeUtils.asElement(e.asType())).getQualifiedName().toString();
    return ClassName.get(
        cname.packageName(),
        "Assert" + cname.simpleNames().stream().collect(Collectors.joining("_")));
  }

  protected CharSequence findPackageFor(Element e) {
    while (e != null)
      if (e.getKind() == ElementKind.PACKAGE) return ((PackageElement) e).getQualifiedName();
      else e = e.getEnclosingElement();
    return null;
  }

  public abstract void generate();

  public String typeExtension(final TypeName typeName) {
    if (typeName == TypeName.BOOLEAN) return "Boolean";
    else if (typeName == TypeName.BYTE) return "Byte";
    else if (typeName == TypeName.CHAR) return "Char";
    else if (typeName == TypeName.DOUBLE) return "Double";
    else if (typeName == TypeName.FLOAT) return "Float";
    else if (typeName == TypeName.INT) return "Int";
    else if (typeName == TypeName.LONG) return "Long";
    else if (typeName == TypeName.SHORT) return "Short";
    else return "";
  }
}
