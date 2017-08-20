package test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import javax.tools.JavaCompiler;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

public class RunCompiler {
    public static void main(String[] args) throws IOException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        for (String string : args) {
            System.out.println("arg: "+string);
        }
//        compiler.getStandardFileManager(null, null, null).setLocation(StandardLocation.SOURCE_OUTPUT, ImmutableList.of(new File("target/generated")));
        compiler.run(System.in, System.out, System.err, args);
    }
}
