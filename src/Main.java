import gen.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.*;
import java.util.ArrayList;

public class Main
{    
    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.err.println("input path is required");
        }
        String source = args[0];
        int targetLine = Integer.parseInt(args[1]);
        int targetCol = Integer.parseInt(args[2]);
        String change = args[3];
        CharStream input = CharStreams.fromFileName(source);
        SysYLexer sysYLexer = new SysYLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(sysYLexer);
        SysYParser sysYParser = new SysYParser(tokens);
        sysYParser.removeErrorListeners();
        myErrorListener errorListener = new myErrorListener();
        sysYParser.addErrorListener(errorListener);

        ParseTree tree = sysYParser.program();
        SymbolTableVisitor symbolTableVisitor = new SymbolTableVisitor();
        symbolTableVisitor.visit(tree);
        ParseTreeWalker walker = new ParseTreeWalker();
//        SymbolTableListener symtableListener = new SymbolTableListener();
//        walker.walk(symtableListener, tree);
//
        ArrayList<Symbol> arrayList = symbolTableVisitor.getAll();
        Symbol target = null;
        for(int i=0;i<arrayList.size();i++){
            for(int m=0;m<arrayList.get(i).getLineno().size();m++){
                if(arrayList.get(i).getLineno(m)==targetLine && arrayList.get(i).getColumnno(m)==targetCol){
                    target = arrayList.get(i);
                }
            }
        }

        if(!symbolTableVisitor.fault) {
            myVisitor visitor = new myVisitor();
            visitor.target = target;
            visitor.change = change;
            visitor.visit(tree);
        }

    }


}