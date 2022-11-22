import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.*;
import java.util.List;

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
        myVisitor visitor = new myVisitor();
        if(!errorListener.fault) {
            visitor.visit(tree);
        }
//        sysYLexer.removeErrorListeners();
//        myErrorListener errorListener = new myErrorListener();
//        sysYLexer.addErrorListener(errorListener);
//        List<? extends Token> sysYLexerAllTokens = sysYLexer.getAllTokens();
//        if(!errorListener.fault) {
//            for (Token token : sysYLexerAllTokens) {
//                String text = token.getText();
//                if(token.getText().startsWith("0x")||token.getText().startsWith("0X")){
//                    String temp = token.getText().substring(2);
//                    text = String.valueOf(Integer.parseInt(temp,16));
//                }
//                else if(token.getText().startsWith("0") && token.getText().length()>1){
//                    String temp = token.getText().substring(1);
//                    text = String.valueOf(Integer.parseInt(temp,8));
//                }
//                System.err.println(sysYLexer.getRuleNames()[token.getType() - 1] + " " + text + " at Line " + token.getLine()+".");
//            }
//        }
    }


}