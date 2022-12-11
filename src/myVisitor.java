import gen.SysYParserBaseVisitor;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;

public class myVisitor extends SysYParserBaseVisitor<Void> {
    public String[] colors = {"CONST[orange]","INT[orange]","VOID[orange]","IF[orange]","ELSE[orange]","WHILE[orange]","BREAK[orange]","CONTINUE[orange]","RETURN[orange]",
            "PLUS[blue]","MINUS[blue]","MUL[blue]","DIV[blue]","MOD[blue]","ASSIGN[blue]","EQ[blue]","NEQ[blue]","LT[blue]","GT[blue]","LE[blue]","GE[blue]",
            "NOT[blue]","AND[blue]","OR[blue]","L_PAREN=25","R_PAREN=26","L_BRACE=27","R_BRACE=28","L_BRACKT=29","R_BRACKT=30",
            "COMMA=31","SEMICOLON=32", "IDENT[red]","INTEGR_CONST[green]","WS=35","LINE_COMMENT=36","MULTILINE_COMMENT=37"};

    public String[] ruleNames ={
            "program", "compUnit", "decl", "constDecl", "bType", "constDef", "constInitVal",
            "varDecl", "varDef", "initVal", "funcDef", "funcType", "funcFParams",
            "funcFParam", "block", "blockItem", "stmt", "exp", "cond", "lVal", "number",
            "unaryOp", "funcRParams", "param", "constExp"
    };

    RuleNode father;

    int spaceCount = 0;
    @Override
    public Void visitChildren(RuleNode node) {
        String space = "";
        for(int i=0;i<spaceCount;i++){
            space = space + "  ";
        }
        String res = ruleNames[node.getRuleContext().getRuleIndex()];
        System.err.println(space + res.substring(0,1).toUpperCase()+res.substring(1));
        spaceCount++;
        Void result = this.defaultResult();
        int n = node.getChildCount();
        for(int i = 0; i < n && this.shouldVisitNextChild(node, result); ++i) {
            ParseTree c = node.getChild(i);
            Void childResult = c.accept(this);
            result = this.aggregateResult(result, childResult);
        }
        spaceCount--;
        return result;
    }

    @Override
    public Void visitTerminal(TerminalNode node) {
        String space = "";
        for(int i=0;i<spaceCount;i++){
            space = space + "  ";
        }
        if(node.getSymbol().getType()!=-1 && (node.getSymbol().getType()<=24 || node.getSymbol().getType()==33 || node.getSymbol().getType()==34)) {
            String text = node.getText();
            if(node.getText().startsWith("0x")||node.getText().startsWith("0X")){
                String temp = node.getText().substring(2);
                text = String.valueOf(Integer.parseInt(temp,16));
            }
            else if(node.getText().startsWith("0") && node.getText().length()>1){
                String temp = node.getText().substring(1);
                text = String.valueOf(Integer.parseInt(temp,8));
            }
            System.err.println(space + text + " " + colors[node.getSymbol().getType() - 1]);
        }
        return super.visitTerminal(node);
    }
}
