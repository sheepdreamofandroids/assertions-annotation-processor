package test;

import java.io.IOException;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

public class RunCompiler {
  public static void main(final String[] args) throws IOException {
    final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    for (final String string : args) System.out.println("arg: " + string);
    //        compiler.getStandardFileManager(null, null, null).setLocation(StandardLocation.SOURCE_OUTPUT, ImmutableList.of(new File("target/generated")));
    compiler.run(System.in, System.out, System.err, args);
  }
}
