
import gen.SysYLexer;
import gen.SysYParser;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.*;

public class Main
{    
    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.err.println("input path is required");
        }
        String source = args[0];
        CharStream input = CharStreams.fromFileName(source);
        SysYLexer sysYLexer = new SysYLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(sysYLexer);
        SysYParser sysYParser = new SysYParser(tokens);
        sysYParser.removeErrorListeners();
        myErrorListener errorListener = new myErrorListener();
        sysYParser.addErrorListener(errorListener);

        ParseTree tree = sysYParser.program();
        ParseTreeWalker walker = new ParseTreeWalker();
        SymbolTableListener symtableListener = new SymbolTableListener();
        walker.walk(symtableListener, tree);

//        myVisitor visitor = new myVisitor();
//        if(!errorListener.fault) {
//            visitor.visit(tree);
//        }

    }


}