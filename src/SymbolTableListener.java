import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import gen.*;

public class SymbolTableListener extends SysYParserBaseListener {

    private ArrayList<Symbol> all = new ArrayList<>();
    private GlobalScope globalScope = null;

    private Scope currentScope = null;

    private int localScopeCounter = 0;

    public boolean fault = false;

    public ArrayList<Symbol> getAll(){
        return this.all;
    }

    @Override
    public void enterProgram(SysYParser.ProgramContext ctx) {
        globalScope = new GlobalScope(null);
        currentScope = globalScope;
    }

    @Override
    public void enterFuncDef(SysYParser.FuncDefContext ctx) {
        String typeName = ctx.funcType().getText();
        Type type = (Type) globalScope.resolve(typeName);
        String funcName = ctx.IDENT().getText();
        int lineno = ctx.start.getLine();
        int columnno = ctx.start.getCharPositionInLine();
        FunctionSymbol functionSymbol = new FunctionSymbol(funcName,currentScope);
        functionSymbol.getType().setRetType(type);
        functionSymbol.addLineno(lineno);
        functionSymbol.addColumnno(columnno);
        if(ctx.funcFParams()!=null) {
            for (int i = 0; i < ctx.funcFParams().funcFParam().size(); i++) {
                SysYParser.FuncFParamContext funcFParamContext = ctx.funcFParams().funcFParam(i);
                String paraTypeName = funcFParamContext.bType().getText();
                Type paraType = (Type) globalScope.resolve(paraTypeName);
                String varName = funcFParamContext.IDENT().getText();
                VariableSymbol varSymbol = new VariableSymbol(varName, paraType);
                if (funcFParamContext.L_BRACKT().size() != 0) {
                    ArrayType arrayType = new ArrayType(paraType);
                    arrayType.element = type;
                    functionSymbol.getType().addParamsType(arrayType);
                } else {
                    functionSymbol.getType().addParamsType(type);
                }
                int paraLineno = funcFParamContext.start.getLine();
                int paraColumnno = funcFParamContext.start.getCharPositionInLine();
                varSymbol.addLineno(paraLineno);
                varSymbol.addColumnno(paraColumnno);
                currentScope.setListener(this);
                currentScope.define(varSymbol);
                if(!currentScope.checkDirty()){
                    all.add(varSymbol);
                }
            }
        }
        currentScope.setListener(this);
        currentScope.define(functionSymbol);//define the function symbol
        if(!currentScope.checkDirty()){
            all.add(functionSymbol);
        }
        currentScope = functionSymbol;
    }

    @Override
    public void enterBlock(SysYParser.BlockContext ctx) {
        LocalScope localScope = new LocalScope(currentScope);
        String localScopeName = localScope.getName() + localScopeCounter;
        localScope.setName(localScopeName);
        localScopeCounter++;
        currentScope = localScope;
    }

    @Override
    public void exitProgram(SysYParser.ProgramContext ctx) {
        currentScope = currentScope.getEnclosingScope();
    }

    @Override
    public void exitFuncDef(SysYParser.FuncDefContext ctx) {
        currentScope = currentScope.getEnclosingScope();
    }

    @Override
    public void exitBlock(SysYParser.BlockContext ctx) {
        currentScope = currentScope.getEnclosingScope();
    }

    //原来是exit
    @Override
    public void enterVarDecl(SysYParser.VarDeclContext ctx) {
        String typeName = ctx.bType().getText();
        Type type = (Type) globalScope.resolve(typeName);
        String varName;
        VariableSymbol varSymbol;
        for(int i=0;i<ctx.varDef().size();i++){
            int lineno = ctx.start.getLine();
            int columnno = ctx.varDef(i).start.getCharPositionInLine();
            varName = ctx.varDef(i).IDENT().getText();
            varSymbol = new VariableSymbol(varName, type);
            varSymbol.addLineno(lineno);
            varSymbol.addColumnno(columnno);
            currentScope.setListener(this);
            currentScope.define(varSymbol);
            if(!currentScope.checkDirty()){
                all.add(varSymbol);
            }
        }
    }

    @Override
    public void enterVarDef(SysYParser.VarDefContext ctx) {
        if(ctx.L_BRACKT().size()>0){
            String varName = ctx.IDENT().getText();
            Symbol symbol = currentScope.resolve(varName);
            ArrayType arrayType = new ArrayType(symbol.getType());
            symbol.setType(arrayType);
            arrayType.setDimension(ctx.L_BRACKT().size());
        }
    }

    @Override
    public void exitLVal(SysYParser.LValContext ctx) {
        String varName = ctx.IDENT().getText();
        currentScope.setListener(this);
        currentScope.checkVariable(varName,ctx.start.getLine());
        Symbol symbol = currentScope.resolve(varName);
        if(symbol==null){
            return;
        }
        symbol.addLineno(ctx.start.getLine());
        symbol.addColumnno(ctx.start.getCharPositionInLine());
        if(ctx.L_BRACKT().size()!=0){
            if(!(symbol.getType() instanceof ArrayType)){
                System.err.println("Error type 9 at Line "+ctx.start.getLine()+": Not an array: "+ctx.IDENT().getText()+".");
                fault = true;
            }
        }
    }

    @Override
    public void enterCallFuncExp(SysYParser.CallFuncExpContext ctx) {
        String funcName = ctx.IDENT().getText();
        currentScope.setListener(this);
        currentScope.checkFunction(funcName,ctx.start.getLine());
        Symbol symbol = currentScope.resolve(funcName);
        if(symbol==null){
            return;
        }
        if(!(symbol.getType() instanceof FunctionType)){
            System.err.println("Error type 10 at Line "+ctx.start.getLine()+": Not a function: "+ctx.IDENT().getText()+".");
            fault = true;
            return;
        }
        symbol.addLineno(ctx.start.getLine());
        symbol.addColumnno(ctx.start.getCharPositionInLine());
        FunctionType functionType = (FunctionType) symbol.getType();
        ArrayList<Type> arrayList = functionType.getParamsType();
        if(ctx.funcRParams()==null || ctx.funcRParams().param()==null){
            if(arrayList.size()!=0){
                System.err.println("Error type 8 at Line "+ctx.start.getLine()+": Function is not applicable for arguments.");
                fault = true;
                return;
            }
            else{
                return;
            }
        }
        if(ctx.funcRParams().param().size() != arrayList.size()){
            System.err.println("Error type 8 at Line "+ctx.start.getLine()+": Function is not applicable for arguments.");
            fault = true;
            return;
        }
        else{
            for(int i=0;i<ctx.funcRParams().param().size();i++){
                SysYParser.ParamContext paramContext = ctx.funcRParams().param(i);
                String paraName = paramContext.exp().getText();
                if(arrayList.get(i) instanceof ArrayType){
                    if(Pattern.compile("^[-+]?\\d+(\\.\\d+)?$").matcher(paraName).matches()){
                        System.err.println("Error type 8 at Line "+ctx.start.getLine()+": Function is not applicable for arguments.");
                        fault = true;
                        return;
                    }
                    else{
                        Symbol para = currentScope.resolve(paraName);
                        if(!(para.getType() instanceof ArrayType)){
                            System.err.println("Error type 8 at Line "+ctx.start.getLine()+": Function is not applicable for arguments.");
                            fault = true;
                            return;
                        }
                    }
                }
                else{
                    Symbol para = currentScope.resolve(paraName);
                    if(Pattern.compile("^[-+]?\\d+(\\.\\d+)?$").matcher(paraName).matches()){
                        continue;
                    }
                    if(para == null){
                        return;
                    }
                    if(para.getType() instanceof ArrayType){
                        System.err.println("Error type 8 at Line "+ctx.start.getLine()+": Function is not applicable for arguments.");
                        fault = true;
                        return;
                    }
                }
                return;
            }
        }
    }

    @Override
    public void enterMulExp(SysYParser.MulExpContext ctx) {
        for(int i=0;i<ctx.exp().size();i++){
            int count = 0;
            String s = ctx.exp(i).getText();
            if(s.contains("(") || s.contains("[")){
                if(s.contains("(")){
                    s = s.substring(0,s.indexOf("("));
                }
                else{
                    count = calNegDim(s);
                    s = s.substring(0,s.indexOf("["));
                }
            }
            if(currentScope.resolve(s)!=null){
                Symbol symbol = currentScope.resolve(s);
                if(symbol.getType() instanceof FunctionType){
                    FunctionType type = (FunctionType) symbol.getType();
                    BasicTypeSymbol retType = (BasicTypeSymbol)  type.getRetType();
                    if(!retType.name.equals("int")){
                        System.err.println("Error type 6 at Line "+ctx.start.getLine()+": type.Type mismatched for operands.");
                        fault = true;
                        break;
                    }
                }
                else{
                    if(symbol.getType() instanceof ArrayType){
                        ArrayType at = (ArrayType) symbol.getType();
                        int dim = at.getDimension();
                        if(dim-count>0){
                            System.err.println("Error type 6 at Line "+ctx.start.getLine()+": type.Type mismatched for operands.");
                            fault = true;
                        }
                    }
                    else{
                        BasicTypeSymbol bt = (BasicTypeSymbol) symbol.getType();
                        if(!bt.name.equals("int")){
                            System.err.println("Error type 6 at Line "+ctx.start.getLine()+": type.Type mismatched for operands.");
                            fault = true;
                        }
                    }
                }
            }
        }
    }


    @Override
    public void enterPlusExp(SysYParser.PlusExpContext ctx) {
        for(int i=0;i<ctx.exp().size();i++){
            int count = 0;
            String s = ctx.exp(i).getText();
            if(s.contains("(") || s.contains("[")){
                if(s.contains("(")){
                    s = s.substring(0,s.indexOf("("));
                }
                else{
                    count = calNegDim(s);
                    s = s.substring(0,s.indexOf("["));
                }
            }
            if(currentScope.resolve(s)!=null){
                Symbol symbol = currentScope.resolve(s);
                if(symbol.getType() instanceof FunctionType){
                    FunctionType type = (FunctionType) symbol.getType();
                    BasicTypeSymbol retType = (BasicTypeSymbol)  type.getRetType();
                    if(!retType.name.equals("int")){
                        System.err.println("Error type 6 at Line "+ctx.start.getLine()+": type.Type mismatched for operands.");
                        fault = true;
                        break;
                    }
                }
                else{
                    if(symbol.getType() instanceof ArrayType){
                        ArrayType at = (ArrayType) symbol.getType();
                        int dim = at.getDimension();
                        if(dim-count>0){
                            System.err.println("Error type 6 at Line "+ctx.start.getLine()+": type.Type mismatched for operands.");
                            fault = true;
                        }
                    }
                    else{
                        BasicTypeSymbol bt = (BasicTypeSymbol) symbol.getType();
                        if(!bt.name.equals("int")){
                            System.err.println("Error type 6 at Line "+ctx.start.getLine()+": type.Type mismatched for operands.");
                            fault = true;
                        }
                    }
                }
            }
        }
    }

    public int calNegDim(String s){
        int count = 0;
        int index = s.indexOf("[");
        while(index>=0){
            count++;
            index = s.indexOf("[",index+1);
        }
        return count;
    }

    public String getOff(String s){
        if(s.contains("(") || s.contains("[")){
            if(s.contains("(")){
                s = s.substring(0,s.indexOf("("));
            }
            else{
                s = s.substring(0,s.indexOf("["));
            }
        }
        return s;
    }


    @Override
    public void enterStmt(SysYParser.StmtContext ctx) {
        if(ctx.ASSIGN()!=null){
            if(currentScope.resolve(ctx.exp().getText())==null){
                if(!Pattern.compile("^[-+]?\\d+(\\.\\d+)?$").matcher(ctx.exp().getText()).matches()){
                    return;
                }
            }
            String LName = ctx.lVal().getText();
            String RName = ctx.exp().getText();
            int LCount = calNegDim(LName);
            int RCount = calNegDim(RName);
            RName = getOff(RName);
            LName = getOff(LName);
            Symbol LSymbol = currentScope.resolve(LName);
            Symbol RSymbol = currentScope.resolve(RName);
            if(LSymbol==null){
                System.err.println("Error type 11 at Line "+ctx.start.getLine()+": The left-hand side of an assignment must be a variable.");
                fault = true;
                return;
            }
            if(LSymbol.getType() instanceof FunctionType){
                System.err.println("Error type 11 at Line "+ctx.start.getLine()+": The left-hand side of an assignment must be a variable.");
                fault = true;
                return;
            }
            if(RSymbol == null){
                return ;
            }
            if(RSymbol.getType() instanceof FunctionType){
                if(LCount>0){
                    ArrayType Lat = (ArrayType) LSymbol.getType();
                    if(Lat.getDimension()-LCount != 0){
                        System.err.println("Error type 5 at Line " + ctx.start.getLine() + ": type.Type mismatched for assignment.");
                        fault = true;
                    }
                }
                else {
                    FunctionType rType = (FunctionType) RSymbol.getType();
                    BasicTypeSymbol bt = (BasicTypeSymbol) rType.getRetType();
                    if (bt.name.equals("void") || rType.getRetType().getClass() == LSymbol.getType().getClass()) {
                        System.err.println("Error type 5 at Line " + ctx.start.getLine() + ": type.Type mismatched for assignment.");
                        fault = true;
                    }
                }
            }
            else{
                if(RSymbol.getType() instanceof ArrayType){
                    if(LSymbol.getType() instanceof ArrayType){
                        ArrayType Rat = (ArrayType) RSymbol.getType();
                        ArrayType Lat = (ArrayType) LSymbol.getType();
                        if(Rat.getDimension()-RCount != Lat.getDimension()-LCount){
                            System.err.println("Error type 5 at Line " + ctx.start.getLine() +": type.Type mismatched for assignment.");
                            fault = true;
                        }
                    }
                    else{
                        ArrayType Rat = (ArrayType) RSymbol.getType();
                        if(Rat.getDimension() - RCount != 0){
                            System.err.println("Error type 5 at Line " + ctx.start.getLine() +": type.Type mismatched for assignment.");
                            fault = true;
                        }
                    }
                }
                else{
                    if(LCount>0){
                        ArrayType Lat = (ArrayType) LSymbol.getType();
                        if(Lat.getDimension()-LCount != 0){
                            System.err.println("Error type 5 at Line " + ctx.start.getLine() + ": type.Type mismatched for assignment.");
                            fault = true;
                        }
                    }
                    else {
                        if (RSymbol.getType().getClass() != LSymbol.getType().getClass()) {
                            System.err.println("Error type 5 at Line " + ctx.start.getLine() + ": type.Type mismatched for assignment.");
                            fault = true;
                        }
                    }
                }
            }
        }
        else if(ctx.RETURN()!=null){
            String funcName = this.currentScope.getEnclosingScope().getName();
            Symbol funcSymbol = currentScope.resolve(funcName);
            FunctionType funcType = (FunctionType) funcSymbol.getType();
            BasicTypeSymbol bs = (BasicTypeSymbol) funcType.getRetType();
            if(bs.name.equals("void")){
                if(ctx.exp()!=null){
                    System.err.println("Error type 7 at Line "+ctx.start.getLine()+": type.Type mismatched for return.");fault = true;
                    return;
                }
            }
            else{
                if(ctx.exp()==null){
                    System.err.println("Error type 7 at Line "+ctx.start.getLine()+": type.Type mismatched for return.");fault = true;
                    return;
                }

                String varName = ctx.exp().getText();
                if(Pattern.compile("^[-+]?\\d+(\\.\\d+)?$").matcher(varName).matches()){
                    return;
                }
                int count = calNegDim(varName);
                varName = getOff(varName);
                Symbol varSymbol = currentScope.resolve(varName);

                if(count>0){
                    ArrayType at = (ArrayType) varSymbol.getType();
                    if(at.getDimension()-count!=0){
                        System.err.println("Error type 7 at Line "+ctx.start.getLine()+": type.Type mismatched for return.");fault = true;
                        return;
                    }
                }
                //TODO
                else if(varSymbol instanceof FunctionSymbol){
                    if(ctx.exp().getText().contains("(") ){
                        FunctionSymbol functionSymbol = (FunctionSymbol) varSymbol;
                        FunctionType functionType = (FunctionType) functionSymbol.getType();
                        BasicTypeSymbol basicTypeSymbol = (BasicTypeSymbol) functionType.getRetType();
                        if(basicTypeSymbol.name.equals("int")){
                            return ;
                        }
                        else{
                            System.err.println("Error type 7 at Line "+ctx.start.getLine()+": type.Type mismatched for return.");fault = true;
                        }
                    }
                    else {
                        System.err.println("Error type 7 at Line " + ctx.start.getLine() + ": type.Type mismatched for return.");fault = true;
                    }
                }

            }
        }
    }
}
