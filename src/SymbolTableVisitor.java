

import java.util.ArrayList;

public class SymbolTableVisitor extends SysYParserBaseVisitor<Symbol> {

    private GlobalScope globalScope = null;

    private Scope currentScope = null;

    private int localScopeCounter = 0;

    private Symbol function;

    public boolean fault = false;

    private ArrayList<Symbol> all = new ArrayList<>();

    public ArrayList<Symbol> getAll(){
        return this.all;
    }
    @Override
    public Symbol visitProgram(SysYParser.ProgramContext ctx) {
        globalScope = new GlobalScope(null);
        currentScope = globalScope;
        super.visitChildren(ctx);
        return null;
    }

    @Override
    public Symbol visitConstDecl(SysYParser.ConstDeclContext ctx) {
        String typeName = ctx.bType().getText();
        Type type = (Type) globalScope.resolve(typeName);
        String varName;
        VariableSymbol varSymbol;
        for(int i=0;i<ctx.constDef().size();i++){
            Symbol symbol = visitConstDef(ctx.constDef(i));
            if(symbol==null){
                return null;
            }
            else{
                all.add(symbol);
                currentScope.define(symbol);
            }
        }
        return super.visitChildren(ctx);
    }

    @Override
    public Symbol visitConstDef(SysYParser.ConstDefContext ctx) {
        if(ctx.L_BRACKT().size()>0){
            String varName = ctx.IDENT().getText();
            if(currentScope.resolve(varName)!=null){
//                System.err.println("Error type 3 at Line " +ctx.start.getLine() + ": Redefined variable: " + varName + ".");
                this.fault = true;
                return null;
            }
            Type type = (Type) globalScope.resolve("int");
            ArrayType arrayType = new ArrayType(type);
            arrayType.setDimension(ctx.L_BRACKT().size());
            VariableSymbol variableSymbol = new VariableSymbol(varName,arrayType);
            return variableSymbol;
        }
        else{
            String varName = ctx.IDENT().getText();
            if(currentScope.resolve(varName)!=null){
//                System.err.println("Error type 3 at Line " +ctx.start.getLine() + ": Redefined variable: " + varName + ".");
                this.fault = true;
                return null;
            }
            Type type = (Type) globalScope.resolve("int");
            VariableSymbol variableSymbol = new VariableSymbol(varName,type);
            return variableSymbol;
        }
    }

    @Override
    public Symbol visitVarDecl(SysYParser.VarDeclContext ctx) {
        String typeName = ctx.bType().getText();
        Type type = (Type) globalScope.resolve(typeName);
        String varName;
        VariableSymbol varSymbol;
        for(int i=0;i<ctx.varDef().size();i++){
            Symbol symbol = visitVarDef(ctx.varDef(i));
            if(symbol==null){
                return null;
            }
            else{
                symbol.addLineno(ctx.start.getLine());
                symbol.addColumnno(ctx.start.getCharPositionInLine()+4);
                all.add(symbol);
                currentScope.define(symbol);
            }
        }
        return null;
    }

    @Override
    public Symbol visitVarDef(SysYParser.VarDefContext ctx) {
        if(ctx.L_BRACKT().size()>0){
            String varName = ctx.IDENT().getText();
            if(currentScope.resolve(varName)!=null){
//                System.err.println("Error type 3 at Line " +ctx.start.getLine() + ": Redefined variable: " + varName + ".");
                this.fault = true;
                return null;
            }
            Type type = (Type) globalScope.resolve("int");
            ArrayType arrayType = new ArrayType(type);
            arrayType.setDimension(ctx.L_BRACKT().size());
            VariableSymbol variableSymbol = new VariableSymbol(varName,arrayType);
            return variableSymbol;
        }
        else{
            String varName = ctx.IDENT().getText();
            if(currentScope.resolve(varName)!=null && !(currentScope.resolve(varName) instanceof FunctionSymbol)){
//                System.err.println("Error type 3 at Line " +ctx.start.getLine() + ": Redefined variable: " + varName + ".");
                this.fault = true;
                return null;
            }
            Type type = (Type) globalScope.resolve("int");
            VariableSymbol variableSymbol = new VariableSymbol(varName,type);
            return variableSymbol;
        }
    }



    @Override
    public Symbol visitFuncDef(SysYParser.FuncDefContext ctx) {
        FunctionType functionType = new FunctionType();
        functionType.retType = (Type) globalScope.resolve(ctx.funcType().getText());
        String functionName = ctx.IDENT().getText();
        if(currentScope.resolve(functionName)!=null){
//            System.err.println("Error type 4 at Line " +ctx.start.getLine() + ": Redefined function: " + functionName + ".");
            this.fault = true;
            return null;
        }
        FunctionSymbol functionSymbol = new FunctionSymbol(functionName,currentScope);
        functionSymbol.setType(functionType);
        functionSymbol.addLineno(ctx.start.getLine());
        functionSymbol.addColumnno(ctx.start.getCharPositionInLine()+ ctx.funcType().getText().length()+1);
        all.add(functionSymbol);
        currentScope.define(functionSymbol);
        currentScope = functionSymbol;
        if(ctx.funcFParams()!=null) {
            visitFuncFParams(ctx.funcFParams());
        }
        visitBlock(ctx.block());
        return null;
    }

    @Override
    public Symbol visitFuncFParams(SysYParser.FuncFParamsContext ctx) {
        FunctionSymbol functionSymbol = (FunctionSymbol) currentScope;
        for(int i=0;i<ctx.funcFParam().size();i++){
            Symbol paraSymbol = visitFuncFParam(ctx.funcFParam(i));
            if(paraSymbol!=null) {
                all.add(paraSymbol);
                currentScope.define(paraSymbol);
                functionSymbol.getType().paramsType.add(paraSymbol.getType());
            }
        }
        return null;
    }

    @Override
    public Symbol visitFuncFParam(SysYParser.FuncFParamContext ctx) {
        String typeName = ctx.bType().getText();
        Type type = (Type) globalScope.resolve(typeName);
        if(ctx.L_BRACKT().size()>0){
            String varName = ctx.IDENT().getText();
            if(currentScope.resolve(varName)!=null){
//                System.err.println("Error type 3 at Line " +ctx.start.getLine() + ": Redefined variable: " + varName + ".");
                this.fault = true;
                return null;
            }
            ArrayType arrayType = new ArrayType(type);
            VariableSymbol variableSymbol = new VariableSymbol(varName,arrayType);
            variableSymbol.addLineno(ctx.start.getLine());
            variableSymbol.addColumnno(ctx.start.getCharPositionInLine());
            return variableSymbol;
        }
        else{
            String varName = ctx.IDENT().getText();
            if(currentScope.resolve(varName)!=null){
//                System.err.println("Error type 3 at Line " +ctx.start.getLine() + ": Redefined variable: " + varName + ".");
                this.fault = true;
                return null;
            }
            VariableSymbol variableSymbol = new VariableSymbol(varName,type);
            variableSymbol.addLineno(ctx.start.getLine());
            variableSymbol.addColumnno(ctx.start.getCharPositionInLine());
            return variableSymbol;
        }
    }

    @Override
    public Symbol visitBlock(SysYParser.BlockContext ctx) {
        LocalScope localScope = new LocalScope(currentScope);
        String localScopeName = localScope.getName() + localScopeCounter;
        localScope.setName(localScopeName);
        localScopeCounter++;
        currentScope = localScope;
        super.visitBlock(ctx);
        currentScope = currentScope.getEnclosingScope();
        return null;
    }

    @Override
    public Symbol visitStmt(SysYParser.StmtContext ctx) {
        if(ctx.ASSIGN()!=null){
            Symbol lvalSymbol = visitLVal(ctx.lVal());
            Symbol expSymbol = null;
            if(ctx.exp() instanceof SysYParser.LvalExpContext){
                expSymbol = visitLvalExp((SysYParser.LvalExpContext) ctx.exp());
            }
            else if (ctx.exp() instanceof SysYParser.CallFuncExpContext){
                expSymbol = visitCallFuncExp((SysYParser.CallFuncExpContext) ctx.exp());
            }
            else if (ctx.exp() instanceof SysYParser.MulExpContext){
                expSymbol = visitMulExp((SysYParser.MulExpContext) ctx.exp());
            }
            else if (ctx.exp() instanceof SysYParser.PlusExpContext){
                expSymbol = visitPlusExp((SysYParser.PlusExpContext) ctx.exp());
            }
            else if (ctx.exp() instanceof SysYParser.NumberExpContext){
                expSymbol = visitNumberExp((SysYParser.NumberExpContext) ctx.exp());
            }
            if(lvalSymbol==null || expSymbol==null){
                return null;
            }
            if(lvalSymbol instanceof FunctionSymbol){
                System.err.println("Error type 11 at Line "+ctx.start.getLine()+": The left-hand side of an assignment must be a variable.");
                this.fault = true;
                return null;
            }
            String lvalType = lvalSymbol.getType().toString();
            String expType;
            if(expSymbol instanceof BasicTypeSymbol){
                expType = "int";
            }
            else{
                expType = expSymbol.getType().toString();
            }
            if(lvalSymbol.getType() instanceof ArrayType){
                lvalType = "int";
                ArrayType arrayType = (ArrayType) lvalSymbol.getType();
                arrayType.getDimension();
                for(int j=0;j<arrayType.getAccessDim();j++){
                    lvalType = lvalType + "int";
                }
            }
            if(expSymbol.getType() instanceof ArrayType){
                expType = "int";
                ArrayType arrayType = (ArrayType) expSymbol.getType();
                for(int j=0;j<arrayType.getAccessDim();j++){
                    expType = expType + "int";
                }
            }
            else if(expSymbol.getType() instanceof FunctionType){
                FunctionType functionType = (FunctionType) expSymbol.getType();
                expType = functionType.getRetType().toString();
            }
            if(!lvalType.equals(expType)){
//                System.err.println("Error type 5 at Line "+ctx.start.getLine()+": type.Type mismatched for assignment.");
                this.fault = true;
                return null;
            }
        }
        else if(ctx.RETURN()!=null){
            Symbol expSymbol = null;
            if(ctx.exp() instanceof SysYParser.LvalExpContext){
                expSymbol = visitLvalExp((SysYParser.LvalExpContext) ctx.exp());
                if(expSymbol instanceof FunctionSymbol){
                    System.err.println("Error type 7 at Line "+ctx.start.getLine()+": type.Type mismatched for return.");
                    this.fault = true;
                    return null;
                }
            }
            else if (ctx.exp() instanceof SysYParser.CallFuncExpContext){
                expSymbol = visitCallFuncExp((SysYParser.CallFuncExpContext) ctx.exp());
            }
            else if (ctx.exp() instanceof SysYParser.MulExpContext){
                expSymbol = visitMulExp((SysYParser.MulExpContext) ctx.exp());
            }
            else if (ctx.exp() instanceof SysYParser.PlusExpContext){
                expSymbol = visitPlusExp((SysYParser.PlusExpContext) ctx.exp());
            }
            else if (ctx.exp() instanceof SysYParser.NumberExpContext){
                expSymbol = visitNumberExp((SysYParser.NumberExpContext) ctx.exp());
            }
            if(expSymbol==null){
                return null;
            }
            FunctionSymbol functionSymbol = (FunctionSymbol) currentScope.getEnclosingScope();
            if(functionSymbol.getType().getRetType().toString().equals("void")){
                if(expSymbol instanceof BasicTypeSymbol){
                    System.err.println("Error type 7 at Line "+ctx.start.getLine()+": type.Type mismatched for return.");
                    this.fault = true;
                    return null;
                }
                if(expSymbol.getType().toString().equals("int")){
                    System.err.println("Error type 7 at Line "+ctx.start.getLine()+": type.Type mismatched for return.");
                    this.fault = true;
                    return null;
                }
            }
            else{
                if(expSymbol instanceof FunctionSymbol){
                    FunctionType functionType = (FunctionType) expSymbol.getType();
                    if(!functionType.getRetType().toString().equals("int")){
                        System.err.println("Error type 7 at Line "+ctx.start.getLine()+": type.Type mismatched for return.");
                        this.fault = true;
                        return null;
                    }
                }
            }
            currentScope = currentScope.getEnclosingScope();
            return null;
        }
        return super.visitStmt(ctx);
    }


    @Override
    public Symbol visitPlusExp(SysYParser.PlusExpContext ctx) {
        Symbol exp1 = null;
        boolean num1 = false;
        if(ctx.exp(0) instanceof SysYParser.LvalExpContext){
            exp1 = visitLvalExp((SysYParser.LvalExpContext) ctx.exp(0));
        }
        else if(ctx.exp(0) instanceof SysYParser.CallFuncExpContext){
            exp1 = visitCallFuncExp((SysYParser.CallFuncExpContext) ctx.exp(0));
        }
        else if(ctx.exp(0) instanceof SysYParser.NumberExpContext){
            num1 = true;
        }
        Symbol exp2 = null;
        boolean num2 = false;
        if(ctx.exp(1) instanceof SysYParser.LvalExpContext){
            exp2 = visitLvalExp((SysYParser.LvalExpContext) ctx.exp(1));
        }
        else if(ctx.exp(1) instanceof SysYParser.CallFuncExpContext){
            exp2 = visitCallFuncExp((SysYParser.CallFuncExpContext) ctx.exp(1));
        }
        else if(ctx.exp(1) instanceof SysYParser.NumberExpContext){
            num2 = true;
        }
        if((exp1 instanceof FunctionSymbol) || (exp2 instanceof FunctionSymbol)){
            System.err.println("Error type 6 at Line "+ctx.start.getLine()+": type.Type mismatched for operands.");
            this.fault = true;
            return null;
        }
        if((exp1 == null && num1==false) || (exp2==null && num2==false)){
            System.err.println("Error type 6 at Line "+ctx.start.getLine()+": type.Type mismatched for operands.");
            this.fault = true;
            return null;
        }
        String exp1Type = "int";
        if(num1){
            //blank
        }
        else if(exp1.getType() instanceof FunctionType){
            FunctionType functionType = (FunctionType) exp1.getType();
            exp1Type = functionType.getRetType().toString();
        }
        else if(exp1.getType() instanceof ArrayType){
            ArrayType arrayType = (ArrayType) exp1.getType();
            if(arrayType.getDimension()-arrayType.getAccessDim()!=0){
                exp1Type = "intint";
            }
        }
        String exp2Type = "int";
        if(num2){
            //blank
        }
        else if(exp2.getType() instanceof FunctionType){
            FunctionType functionType = (FunctionType) exp2.getType();
            exp1Type = functionType.getRetType().toString();
        }
        else if(exp2.getType() instanceof ArrayType){
            ArrayType arrayType = (ArrayType) exp2.getType();
            if(arrayType.getDimension()-arrayType.getAccessDim()!=0){
                exp2Type = "intint";
            }
        }
        if(!exp1Type.equals("int") || !exp2Type.equals("int")){
            System.err.println("Error type 6 at Line "+ctx.start.getLine()+": type.Type mismatched for operands.");
            this.fault = true;
            return null;
        }
        super.visitPlusExp(ctx);
        return new BasicTypeSymbol("int");
    }

    @Override
    public Symbol visitMulExp(SysYParser.MulExpContext ctx) {
        return super.visitMulExp(ctx);
    }

    @Override
    public Symbol visitLvalExp(SysYParser.LvalExpContext ctx) {
        return super.visitLvalExp(ctx);
    }

    @Override
    public Symbol visitCallFuncExp(SysYParser.CallFuncExpContext ctx) {
        String functionName = ctx.IDENT().getText();
        Symbol functionSymbol = currentScope.resolve(functionName);
        if(functionSymbol==null){
//            System.err.println("Error type 2 at Line "+ctx.start.getLine()+": Undefined function: "+functionName+".");
            this.fault = true;
            return null;
        }
        if(!(functionSymbol instanceof FunctionSymbol)){
            System.err.println("Error type 10 at Line "+ctx.start.getLine()+": Not a function: "+functionName+".");
            this.fault = true;
            return null;
        }
        FunctionSymbol trueFunctionSymbol = (FunctionSymbol) functionSymbol;
        int paraNum = 0;
        if(ctx.funcRParams()==null){
            paraNum = 0;
        }
        else{
            paraNum = ctx.funcRParams().param().size();
        }
        if(paraNum!=trueFunctionSymbol.getType().getParamsType().size()){
            System.err.println("Error type 8 at Line "+ctx.start.getLine()+": Function is not applicable for arguments.");
            this.fault = true;
            return null;
        }
        else{
            this.function = functionSymbol;
            visitFuncRParams(ctx.funcRParams());
        }
        functionSymbol.addLineno(ctx.start.getLine());
        functionSymbol.addColumnno(ctx.start.getCharPositionInLine());
        return functionSymbol;
    }

    @Override
    public Symbol visitLVal(SysYParser.LValContext ctx) {
        String name = ctx.IDENT().getText();
        Symbol symbol = currentScope.resolve(name);
        if(symbol==null){
            System.err.println("Error type 1 at Line "+ctx.start.getLine()+": Undefined variable: "+name+".");
            this.fault = true;
            return null;
        }
        if(symbol.getType() instanceof ArrayType){
            ArrayType arrayType = (ArrayType) symbol.getType();
            arrayType.setAccessDim(arrayType.getDimension()-ctx.L_BRACKT().size());
            if(arrayType.getAccessDim()<0){
                System.err.println("Error type 9 at Line "+ctx.start.getLine()+": Not an array: "+name+".");
                this.fault = true;
                return null;
            }
        }
        else{
            if(ctx.L_BRACKT().size()!=0){
                System.err.println("Error type 9 at Line "+ctx.start.getLine()+": Not an array: "+name+".");
                this.fault = true;
                return null;
            }
        }
        symbol.addLineno(ctx.start.getLine());
        symbol.addColumnno(ctx.start.getCharPositionInLine());
        super.visitLVal(ctx);
        return symbol;
    }

    @Override
    public Symbol visitNumberExp(SysYParser.NumberExpContext ctx) {
        return super.visitNumberExp(ctx);
    }

    @Override
    public Symbol visitNumber(SysYParser.NumberContext ctx) {
        return new BasicTypeSymbol("int");
    }

    @Override
    public Symbol visitFuncRParams(SysYParser.FuncRParamsContext ctx) {
        FunctionSymbol functionSymbol = (FunctionSymbol) this.function;
        int count;
        if(ctx==null){
            count = 0;
        }
        else{
            count = ctx.param().size();
        }
        for(int i=0;i<count;i++){
            Symbol symbol = visitParam(ctx.param(i));
            if(symbol == null){
                return null;
            }
            if(symbol.getType() instanceof ArrayType){
                if(!(functionSymbol.getType().getParamsType().get(i) instanceof ArrayType)){
                    System.err.println("Error type 8 at Line "+ctx.start.getLine()+": Function is not applicable for arguments.");
                    this.fault = true;
                    return null;
                }
            }
            else{
                if(!(functionSymbol.getType().getParamsType().get(i).toString().equals("int"))){
                    System.err.println("Error type 8 at Line "+ctx.start.getLine()+": Function is not applicable for arguments.");
                    this.fault = true;
                    return null;
                }
            }
        }
        return null;
    }
}
