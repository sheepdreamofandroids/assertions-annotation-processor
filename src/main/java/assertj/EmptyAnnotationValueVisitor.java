package assertj;

import javax.lang.model.util.SimpleAnnotationValueVisitor8;

public class EmptyAnnotationValueVisitor<R, P> extends SimpleAnnotationValueVisitor8<R, P> {
    @Override
    protected R defaultAction(Object o, P p) {
        throw new RuntimeException("Unexpected: " + o);
    }
}
