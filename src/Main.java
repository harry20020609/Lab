import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.bytedeco.llvm.LLVM.*;
import org.bytedeco.javacpp.*;
import static org.bytedeco.llvm.global.LLVM.*;
import java.io.*;

public class Main
{
    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.err.println("input path is required");
        }
        String source = args[0];

        BytePointer error = new BytePointer();
        LLVMContextRef context = LLVMContextCreate();
        LLVMModuleRef module = LLVMModuleCreateWithNameInContext("moudle",context);
        LLVMBuilderRef builder = LLVMCreateBuilderInContext(context);

        CharStream input = CharStreams.fromFileName(source);
        SysYLexer sysYLexer = new SysYLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(sysYLexer);
        SysYParser sysYParser = new SysYParser(tokens);
        ParseTree tree = sysYParser.program();
        MyVisitor myVisitor = new MyVisitor();

        myVisitor.module = module;
        myVisitor.builder = builder;
        myVisitor.context = context;
        myVisitor.visit(tree);
        LLVMDumpModule(module);
        if (LLVMPrintModuleToFile(module, "test.ll", error) != 0) {    // module是你自定义的LLVMModuleRef对象
            LLVMDisposeMessage(error);
        }
    }
}