package jlox.tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

public class GenerateAst {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.out.println("Usage: generate-ast <output-directory>");
            System.exit(64);
        }

        String outputDir = args[0];

        defineAst(outputDir, "Expr", Arrays.asList(
                "Binary   : Expr left, Token op, Expr right",
                "Grouping : Expr expression",
                "Literal  : Object value",
                "Unary    : Token op, Expr right"));
    }

    private static void defineAst(String outputDir, String baseName, List<String> types) throws IOException {
        String path = outputDir + "/" + baseName + ".java";
        PrintWriter writer = new PrintWriter(path, "UTF-8");

        writer.println("package jlox;");
        writer.println();
        writer.println("import java.util.List;");
        writer.println();
        writer.println("abstract class " + baseName + "{");

        defineVisitor(writer, baseName, types);

        for (String type : types) {
            String[] info = type.split(":");
            String className = info[0].trim();
            String fields = info[1].trim();

            defineType(writer, baseName, className, fields);
        }

        writer.println();
        writer.println("    abstract<R> R accept(Visitor<R> visitor);");

        writer.println("}");
        writer.close();
    }

    private static void defineVisitor(PrintWriter writer, String baseName, List<String> types) {
        writer.println("    interface Visitor<R> {");

        for (String type : types) {
            String typeName = type.split(":")[0].trim();
            writer.println(
                    "        R visit" + typeName + baseName + "(" + typeName + " " + baseName.toLowerCase() + ");");
        }

        writer.println("    }");
    }

    private static void defineType(PrintWriter writer, String baseName, String className, String fields) {
        writer.println("    public static class " + className + " extends " + baseName + " {");

        // fileds
        String[] fieldList = fields.split(", ");
        for (String field : fieldList) {
            writer.println("        final " + field + ";");
        }

        // constructor
        writer.println("        " + "public " + className + "(" + fields + ") {");

        for (String field : fieldList) {
            String name = field.split(" ")[1].trim();
            writer.println("            this." + name + " = " + name + ";");
        }

        writer.println("        }");

        // visitor pattern
        writer.println();
        writer.println("        @Override");
        writer.println("        <R> R accept(Visitor<R> visitor) {");
        writer.println("            return visitor.visit" + className + baseName + "(this);");
        writer.println("        }");

        writer.println("    }");
    }
}
