package assertj;

import javax.lang.model.util.SimpleAnnotationValueVisitor8;

public class EmptyAnnotationValueVisitor<R, P> extends SimpleAnnotationValueVisitor8<R, P> {
  @Override
  protected R defaultAction(final Object o, final P p) {
    throw new RuntimeException("Unexpected: " + o);
  }
}
