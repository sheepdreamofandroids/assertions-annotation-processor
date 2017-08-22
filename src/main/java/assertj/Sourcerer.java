package assertj;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeVariableName;
import com.squareup.javapoet.WildcardTypeName;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import org.junit.gen5.api.Assertions;

public abstract class Sourcerer {

  protected final TypeMirror s;
  protected RoundEnvironment roundEnvironment;
  protected ProcessingEnvironment processingEnv;
  protected Element element;
  protected String assertee;
  protected String packageName;
  protected ClassName className;
  protected final TypeVariableName[] typeVariables;

  public static final ClassName fieldClassName = ClassName.get(Field.class);
  public static final ClassName methodClassName = ClassName.get(Method.class);
  public static final ClassName objectsClassName = ClassName.get(Objects.class);
  public static final ClassName assertionsClassName = ClassName.get(Assertions.class);
  protected final Types typeUtils;
  protected final Elements elementUtils;

  public Sourcerer(
      final TypeMirror s,
      final RoundEnvironment roundEnvironment,
      final ProcessingEnvironment processingEnv) {
    super();
    this.s = s;
    this.roundEnvironment = roundEnvironment;
    this.processingEnv = processingEnv;
    typeUtils = processingEnv.getTypeUtils();
    elementUtils = processingEnv.getElementUtils();
    element = typeUtils.asElement(s);
    assertee = "assertee";
    packageName = findPackageFor(element).toString();
    final TypeName type = TypeName.get(s);
    // final ClassName cn = (ClassName) type;
    // final String asserterName =
    // cn.simpleNames().stream().collect(Collectors.joining("_"));
    className = (ClassName) asserterName(type);
    // final TypeKind kind = s.getKind();

    final List<TypeVariableName> x =
        element instanceof TypeElement
            ? ((TypeElement) element)
                .getTypeParameters()
                .stream()
                .map(tpe -> TypeVariableName.get(tpe))
                .collect(Collectors.toList())
            : Collections.emptyList();
    typeVariables = x.toArray(new TypeVariableName[x.size()]);
  }

  public Sourcerer(final Sourcerer from) {
    this(from.s, from.roundEnvironment, from.processingEnv);
  }

  protected TypeName asserterName(final TypeName type) {
    return type instanceof ParameterizedTypeName
        ? ParameterizedTypeName.get(
            asserterSimpleName(((ParameterizedTypeName) type).rawType),
            ((ParameterizedTypeName) type).typeArguments.toArray(new TypeName[] {}))
        : type instanceof TypeVariableName ? type : asserterSimpleName((ClassName) type);
  }

  private ClassName asserterSimpleName(final ClassName cname) {
    final ClassName c =
        ClassName.get(
            cname.packageName(),
            "Assert" + cname.simpleNames().stream().collect(Collectors.joining("_")));
    return c;
  }

  protected CharSequence findPackageFor(Element e) {
    while (e != null)
      if (e.getKind() == ElementKind.PACKAGE) return ((PackageElement) e).getQualifiedName();
      else e = e.getEnclosingElement();
    return null;
  }

  public abstract void generate();

  public String typeExtension(final TypeName typeName) {
    if (typeName == TypeName.INT) return "Int";
    // else if (typeName == TypeName.BOOLEAN) return "Boolean";
    // else if (typeName == TypeName.BYTE) return "Byte";
    // else if (typeName == TypeName.CHAR) return "Char";
    else if (typeName == TypeName.DOUBLE) return "Double";
    // else if (typeName == TypeName.FLOAT) return "Float";
    else if (typeName == TypeName.LONG) return "Long";
    // else if (typeName == TypeName.SHORT) return "Short";
    else return "";
  }

  public TypeName raw(final TypeName type) {
    return type instanceof ParameterizedTypeName
        ? ((ParameterizedTypeName) type).rawType
        : type instanceof ArrayTypeName
            ? raw(((ArrayTypeName) type).componentType)
            : type instanceof WildcardTypeName
                ? null
                : type instanceof TypeVariableName ? null : type;
  }
}
