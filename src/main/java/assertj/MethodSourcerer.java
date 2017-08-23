package assertj;

import com.squareup.javapoet.CodeBlock.Builder;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.lang.model.element.ExecutableElement;

public class MethodSourcerer extends MemberSourcerer {

  public MethodSourcerer(
      final ClassSourcerer classSourcerer, final ExecutableElement e, String name) {
    super(classSourcerer, e, e.getReturnType(), name);
  }

  public Object getO() {
    try {
      final Method method = this.getClass().getDeclaredMethod("");
      method.setAccessible(true);
      return method.invoke(assertee);
    } catch (SecurityException
        | IllegalArgumentException
        | IllegalAccessException
        | NoSuchMethodException
        | InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected Builder retrieve(Builder code) {
    return code.addStatement(
            "$T method = this.getClass().getDeclaredMethod($S)", methodClassName, simpleName)
        .addStatement("method.setAccessible(true)")
        .addStatement("return ($T) method.invoke($N)", type, assertee)
        .nextControlFlow(
            "catch ($T | $T | $T | $T | $T e)",
            NoSuchMethodException.class,
            SecurityException.class,
            IllegalArgumentException.class,
            IllegalAccessException.class,
            InvocationTargetException.class);
  }
}
