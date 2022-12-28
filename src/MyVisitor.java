import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.PointerPointer;
import org.bytedeco.llvm.LLVM.*;
import static org.bytedeco.llvm.global.LLVM.*;

public class MyVisitor extends SysYParserBaseVisitor<LLVMValueRef> {
    LLVMModuleRef module = null;
    LLVMBuilderRef builder = null;
    LLVMTypeRef i32Type = LLVMInt32Type();
    LLVMContextRef context = null;
    LLVMValueRef currentValueRef = null;
    Scope currentScope = new Scope("GLOBAL",null);
    LLVMBasicBlockRef currentBlock = null;

    String funcName = "";

    @Override
    public LLVMValueRef visitProgram(SysYParser.ProgramContext ctx) {
        //初始化LLVM
        LLVMInitializeCore(LLVMGetGlobalPassRegistry());
        LLVMLinkInMCJIT();
        LLVMInitializeNativeAsmPrinter();
        LLVMInitializeNativeAsmParser();
        LLVMInitializeNativeTarget();
        super.visitProgram(ctx);
        return null;
    }

    @Override
    public LLVMValueRef visitFuncDef(SysYParser.FuncDefContext ctx) {
        //生成返回值类型
        LLVMTypeRef returnType = i32Type;
        int paramNum;
        if(ctx.funcFParams()==null){
            paramNum = 0;
        }
        else {
            paramNum = ctx.funcFParams().funcFParam().size();
        }
        PointerPointer<Pointer> argumentTypes = new PointerPointer<>(paramNum);
        for(int i=0;i<paramNum;i++){
            argumentTypes.put(i,i32Type);
        }
        //生成函数类型
        LLVMTypeRef ft = LLVMFunctionType(returnType, argumentTypes, paramNum,0);
        //生成函数，即向之前创建的module中添加函数
        LLVMValueRef function = LLVMAddFunction(module,ctx.IDENT().getText(), ft);
        this.currentValueRef = function;
        this.currentScope.define(function);
        Scope scope = new Scope(ctx.IDENT().getText(),this.currentScope);
        this.currentScope = scope;

        LLVMBasicBlockRef Main_entry = LLVMAppendBasicBlockInContext(context,this.currentValueRef,
                LLVMGetValueName(this.currentValueRef).getString()+"Entry");
        this.currentBlock = Main_entry;

        super.visitFuncDef(ctx);
        this.currentScope = this.currentScope.getEnclosingScope();
        return null;
    }

    @Override
    public LLVMValueRef visitFuncFParams(SysYParser.FuncFParamsContext ctx) {
        LLVMPositionBuilderAtEnd(builder, this.currentBlock);
        for(int i=0;i<ctx.funcFParam().size();i++) {
            LLVMValueRef paramRef = LLVMBuildAlloca(builder, i32Type, ctx.funcFParam(i).IDENT().getText());
            LLVMBuildStore(builder, LLVMGetParam(this.currentValueRef, i), paramRef);
            this.currentScope.define(paramRef);
        }
        return super.visitFuncFParams(ctx);
    }

    @Override
    public LLVMValueRef visitFuncFParam(SysYParser.FuncFParamContext ctx) {
        return super.visitFuncFParam(ctx);
    }

    @Override
    public LLVMValueRef visitVarDecl(SysYParser.VarDeclContext ctx) {
        return super.visitVarDecl(ctx);
    }

    @Override
    public LLVMValueRef visitConstDecl(SysYParser.ConstDeclContext ctx) {
        return super.visitConstDecl(ctx);
    }

    @Override
    public LLVMValueRef visitConstDef(SysYParser.ConstDefContext ctx) {
        LLVMPositionBuilderAtEnd(builder, this.currentBlock);
        LLVMValueRef varRef = LLVMBuildAlloca(builder, i32Type, ctx.IDENT().getText());
        LLVMValueRef initRef = null;
        if (ctx.constInitVal().constExp() != null) {
            if (ctx.constInitVal().constExp().exp() instanceof SysYParser.LvalExpContext) {
                initRef = visitLvalExp((SysYParser.LvalExpContext) ctx.constInitVal().constExp().exp());
            } else if (ctx.constInitVal().constExp().exp() instanceof SysYParser.PlusExpContext) {
                initRef = visitPlusExp((SysYParser.PlusExpContext) ctx.constInitVal().constExp().exp());
            } else if (ctx.constInitVal().constExp().exp() instanceof SysYParser.CallFuncExpContext) {
                initRef = visitCallFuncExp((SysYParser.CallFuncExpContext) ctx.constInitVal().constExp().exp());
            } else if (ctx.constInitVal().constExp().exp() instanceof SysYParser.MulExpContext) {
                initRef = visitMulExp((SysYParser.MulExpContext) ctx.constInitVal().constExp().exp());
            } else if (ctx.constInitVal().constExp().exp() instanceof SysYParser.NumberExpContext) {
                initRef = visitNumberExp((SysYParser.NumberExpContext) ctx.constInitVal().constExp().exp());
            } else if (ctx.constInitVal().constExp().exp() instanceof SysYParser.UnaryOpExpContext) {
                initRef = visitUnaryOpExp((SysYParser.UnaryOpExpContext) ctx.constInitVal().constExp().exp());
            }
        }
        if(initRef!=null) {
            LLVMBuildStore(builder, initRef, varRef);
        }
        this.currentScope.define(varRef);
        return null;
    }



    @Override
    public LLVMValueRef visitVarDef(SysYParser.VarDefContext ctx) {
        LLVMPositionBuilderAtEnd(builder, this.currentBlock);
        //Array
        if(ctx.L_BRACKT().size()!=0){
            LLVMTypeRef arrayType = LLVMArrayType(i32Type,Integer.parseInt(ctx.constExp(0).getText()));
            LLVMValueRef array = LLVMBuildAlloca(builder, arrayType, ctx.IDENT().getText());
            int n = Integer.parseInt(ctx.constExp(0).getText());
            for(int i=0;i<n;i++) {
                LLVMValueRef index = LLVMConstInt(i32Type,i,1);
                LLVMValueRef pointer = LLVMBuildGEP2(builder,i32Type,array,
                        new PointerPointer(new LLVMValueRef[]{index}),1,"pointer");
//                LLVMValueRef pointer = LLVMBuildGEP(builder,array, new PointerPointer(new LLVMValueRef[]{index}),1,"pointer");
                LLVMValueRef initRef = null;
                if (ctx.initVal().initVal(i) != null) {
                    if (ctx.initVal().initVal(i).exp() instanceof SysYParser.LvalExpContext) {
                        initRef = visitLvalExp((SysYParser.LvalExpContext) ctx.initVal().initVal(i).exp());
                    } else if (ctx.initVal().initVal(i).exp() instanceof SysYParser.PlusExpContext) {
                        initRef = visitPlusExp((SysYParser.PlusExpContext) ctx.initVal().initVal(i).exp());
                    } else if (ctx.initVal().initVal(i).exp() instanceof SysYParser.CallFuncExpContext) {
                        initRef = visitCallFuncExp((SysYParser.CallFuncExpContext) ctx.initVal().initVal(i).exp());
                    } else if (ctx.initVal().initVal(i).exp() instanceof SysYParser.MulExpContext) {
                        initRef = visitMulExp((SysYParser.MulExpContext) ctx.initVal().initVal(i).exp());
                    } else if (ctx.initVal().initVal(i).exp() instanceof SysYParser.NumberExpContext) {
                        initRef = visitNumberExp((SysYParser.NumberExpContext) ctx.initVal().initVal(i).exp());
                    } else if (ctx.initVal().initVal(i).exp() instanceof SysYParser.UnaryOpExpContext) {
                        initRef = visitUnaryOpExp((SysYParser.UnaryOpExpContext) ctx.initVal().initVal(i).exp());
                    }
                }
                else{
                    initRef = LLVMConstInt(i32Type, 0, /* signExtend */ 0);
                }
                if(initRef!=null){
                    LLVMBuildStore(builder,initRef,pointer);
                }
            }
            this.currentScope.define(array);
        }
        else {
            //Int
            LLVMValueRef varRef = LLVMBuildAlloca(builder, i32Type, ctx.IDENT().getText());
            LLVMValueRef initRef = null;
            if (ctx.initVal() != null) {
                if (ctx.initVal().exp() instanceof SysYParser.LvalExpContext) {
                    initRef = visitLvalExp((SysYParser.LvalExpContext) ctx.initVal().exp());
                } else if (ctx.initVal().exp() instanceof SysYParser.PlusExpContext) {
                    initRef = visitPlusExp((SysYParser.PlusExpContext) ctx.initVal().exp());
                } else if (ctx.initVal().exp() instanceof SysYParser.CallFuncExpContext) {
                    initRef = visitCallFuncExp((SysYParser.CallFuncExpContext) ctx.initVal().exp());
                } else if (ctx.initVal().exp() instanceof SysYParser.MulExpContext) {
                    initRef = visitMulExp((SysYParser.MulExpContext) ctx.initVal().exp());
                } else if (ctx.initVal().exp() instanceof SysYParser.NumberExpContext) {
                    initRef = visitNumberExp((SysYParser.NumberExpContext) ctx.initVal().exp());
                } else if (ctx.initVal().exp() instanceof SysYParser.UnaryOpExpContext) {
                    initRef = visitUnaryOpExp((SysYParser.UnaryOpExpContext) ctx.initVal().exp());
                }
            }
            if(initRef!=null) {
                LLVMBuildStore(builder, initRef, varRef);
            }
            this.currentScope.define(varRef);
        }
        return null;
    }

    @Override
    public LLVMValueRef visitBlock(SysYParser.BlockContext ctx) {
        return super.visitBlock(ctx);
    }

    @Override
    public LLVMValueRef visitStmt(SysYParser.StmtContext ctx) {
        if(ctx.RETURN()!=null){
            LLVMValueRef llvmValueRef = null;
            if(ctx.exp() instanceof SysYParser.MulExpContext){
                llvmValueRef = visitMulExp((SysYParser.MulExpContext) ctx.exp());
            }
            else if(ctx.exp() instanceof SysYParser.PlusExpContext){
                llvmValueRef = visitPlusExp((SysYParser.PlusExpContext) ctx.exp());
            }
            else if(ctx.exp() instanceof SysYParser.NumberExpContext) {
                llvmValueRef = visitNumberExp((SysYParser.NumberExpContext) ctx.exp());
            }
            else if(ctx.exp() instanceof SysYParser.UnaryOpExpContext){
                llvmValueRef = visitUnaryOpExp((SysYParser.UnaryOpExpContext) ctx.exp());
            }
            else if(ctx.exp() instanceof SysYParser.LvalExpContext){
                llvmValueRef = visitLvalExp((SysYParser.LvalExpContext) ctx.exp());
            }
            else if(ctx.exp() instanceof SysYParser.CallFuncExpContext){
                llvmValueRef = visitCallFuncExp((SysYParser.CallFuncExpContext) ctx.exp());
            }
            LLVMPositionBuilderAtEnd(builder,this.currentBlock);
            LLVMBuildRet(builder, llvmValueRef);
            return null;
        }
        else if(ctx.ASSIGN()!=null){
            LLVMValueRef lvalRef = visitLVal(ctx.lVal());
            LLVMValueRef llvmValueRef = null;
            if(ctx.exp() instanceof SysYParser.MulExpContext){
                llvmValueRef = visitMulExp((SysYParser.MulExpContext) ctx.exp());
            }
            else if(ctx.exp() instanceof SysYParser.PlusExpContext){
                llvmValueRef = visitPlusExp((SysYParser.PlusExpContext) ctx.exp());
            }
            else if(ctx.exp() instanceof SysYParser.NumberExpContext) {
                llvmValueRef = visitNumberExp((SysYParser.NumberExpContext) ctx.exp());
            }
            else if(ctx.exp() instanceof SysYParser.UnaryOpExpContext){
                llvmValueRef = visitUnaryOpExp((SysYParser.UnaryOpExpContext) ctx.exp());
            }
            else if(ctx.exp() instanceof SysYParser.LvalExpContext){
                llvmValueRef = visitLvalExp((SysYParser.LvalExpContext) ctx.exp());
            }
            else if(ctx.exp() instanceof SysYParser.CallFuncExpContext){
                llvmValueRef = visitCallFuncExp((SysYParser.CallFuncExpContext) ctx.exp());
            }
            LLVMBuildStore(builder,llvmValueRef,lvalRef);
            return null;
        }
        return super.visitStmt(ctx);
    }

    @Override
    public LLVMValueRef visitCallFuncExp(SysYParser.CallFuncExpContext ctx) {
//        LLVMValueRef funcRef = LLVMGetNamedFunction(module,ctx.IDENT().getText());
        this.funcName = ctx.IDENT().getText();
        if(ctx.funcRParams()==null){
            LLVMValueRef funcRef = this.currentScope.resolve(this.funcName);
            LLVMValueRef[] args = {};
            return LLVMBuildCall(builder,funcRef,new PointerPointer(args),0,"returnValue");
        }
        return visitFuncRParams(ctx.funcRParams());
    }

    @Override
    public LLVMValueRef visitFuncRParams(SysYParser.FuncRParamsContext ctx) {
        LLVMValueRef funcRef = this.currentScope.resolve(this.funcName);
        int n = 0;
        if(ctx.param()==null){
            n = 0;
        }
        else{
            n = ctx.param().size();
        }
        LLVMValueRef[] args = new LLVMValueRef[n];
        for(int i=0;i<ctx.param().size();i++){
            args[i] = visitParam(ctx.param(i));
        }
        return LLVMBuildCall(builder,funcRef,new PointerPointer(args),n,"returnValue");
    }

    @Override
    public LLVMValueRef visitParam(SysYParser.ParamContext ctx) {
        LLVMValueRef llvmValueRef = null;
        if(ctx.exp() instanceof SysYParser.MulExpContext){
            llvmValueRef = visitMulExp((SysYParser.MulExpContext) ctx.exp());
        }
        else if(ctx.exp() instanceof SysYParser.PlusExpContext){
            llvmValueRef = visitPlusExp((SysYParser.PlusExpContext) ctx.exp());
        }
        else if(ctx.exp() instanceof SysYParser.NumberExpContext) {
            llvmValueRef = visitNumberExp((SysYParser.NumberExpContext) ctx.exp());
        }
        else if(ctx.exp() instanceof SysYParser.UnaryOpExpContext){
            llvmValueRef = visitUnaryOpExp((SysYParser.UnaryOpExpContext) ctx.exp());
        }
        else if(ctx.exp() instanceof SysYParser.LvalExpContext){
            llvmValueRef = visitLvalExp((SysYParser.LvalExpContext) ctx.exp());
        }
        else if(ctx.exp() instanceof  SysYParser.CallFuncExpContext){
            llvmValueRef = visitCallFuncExp((SysYParser.CallFuncExpContext) ctx.exp());
        }
        return llvmValueRef;
    }

    @Override
    public LLVMValueRef visitLVal(SysYParser.LValContext ctx) {
        LLVMValueRef retValue = null;
        if(ctx.L_BRACKT().size()!=0){
            LLVMValueRef array = this.currentScope.resolve(ctx.IDENT().getText());
            LLVMValueRef index = null;
            if(ctx.exp(0) instanceof SysYParser.MulExpContext){
                index = visitMulExp((SysYParser.MulExpContext) ctx.exp(0));
            }
            else if(ctx.exp(0) instanceof SysYParser.PlusExpContext){
                index = visitPlusExp((SysYParser.PlusExpContext) ctx.exp(0));
            }
            else if(ctx.exp(0) instanceof SysYParser.NumberExpContext) {
                index = visitNumberExp((SysYParser.NumberExpContext) ctx.exp(0));
            }
            else if(ctx.exp(0) instanceof SysYParser.UnaryOpExpContext){
                index = visitUnaryOpExp((SysYParser.UnaryOpExpContext) ctx.exp(0));
            }
            else if(ctx.exp(0) instanceof SysYParser.LvalExpContext){
                index = visitLvalExp((SysYParser.LvalExpContext) ctx.exp(0));
            }
            else if(ctx.exp(0) instanceof  SysYParser.CallFuncExpContext){
                index = visitCallFuncExp((SysYParser.CallFuncExpContext) ctx.exp(0));
            }
            LLVMValueRef pointer = LLVMBuildGEP2(builder,i32Type,array,
                    new PointerPointer(new LLVMValueRef[]{index}),1,"pointer");
            retValue = LLVMBuildLoad(builder,pointer,ctx.IDENT().getText());
        }
        else{
            LLVMValueRef temp = this.currentScope.resolve(ctx.IDENT().getText());
            retValue = LLVMBuildLoad(builder,temp,ctx.IDENT().getText());
        }
        return retValue;
    }

    @Override
    public LLVMValueRef visitMulExp(SysYParser.MulExpContext ctx) {
        LLVMValueRef llvmValueRef1 = null;
        if(ctx.exp(0) instanceof SysYParser.MulExpContext){
            llvmValueRef1 = visitMulExp((SysYParser.MulExpContext) ctx.exp(0));
        }
        else if(ctx.exp(0) instanceof SysYParser.PlusExpContext){
            llvmValueRef1 = visitPlusExp((SysYParser.PlusExpContext) ctx.exp(0));
        }
        else if(ctx.exp(0) instanceof SysYParser.NumberExpContext) {
            llvmValueRef1 = visitNumberExp((SysYParser.NumberExpContext) ctx.exp(0));
        }
        else if(ctx.exp(0) instanceof SysYParser.UnaryOpExpContext){
            llvmValueRef1 = visitUnaryOpExp((SysYParser.UnaryOpExpContext) ctx.exp(0));
        }
        else if(ctx.exp(0) instanceof SysYParser.LvalExpContext){
            llvmValueRef1 = visitLvalExp((SysYParser.LvalExpContext) ctx.exp(0));
        }
        else if(ctx.exp(0) instanceof  SysYParser.CallFuncExpContext){
            llvmValueRef1 = visitCallFuncExp((SysYParser.CallFuncExpContext) ctx.exp(0));
        }
        LLVMValueRef llvmValueRef2 = null;
        if(ctx.exp(1) instanceof SysYParser.MulExpContext){
            llvmValueRef2 = visitMulExp((SysYParser.MulExpContext) ctx.exp(1));
        }
        else if(ctx.exp(1) instanceof SysYParser.PlusExpContext){
            llvmValueRef2 = visitPlusExp((SysYParser.PlusExpContext) ctx.exp(1));
        }
        else if(ctx.exp(1) instanceof SysYParser.NumberExpContext) {
            llvmValueRef2 = visitNumberExp((SysYParser.NumberExpContext) ctx.exp(1));
        }
        else if(ctx.exp(1) instanceof SysYParser.UnaryOpExpContext){
            llvmValueRef2 = visitUnaryOpExp((SysYParser.UnaryOpExpContext) ctx.exp(1));
        }
        else if(ctx.exp(1) instanceof SysYParser.LvalExpContext){
            llvmValueRef2 = visitLvalExp((SysYParser.LvalExpContext) ctx.exp(1));
        }
        else if(ctx.exp(1) instanceof  SysYParser.CallFuncExpContext){
            llvmValueRef1 = visitCallFuncExp((SysYParser.CallFuncExpContext) ctx.exp(1));
        }
        LLVMValueRef llvmValueRef = null;
        if(ctx.MUL()!=null){
            llvmValueRef = LLVMBuildMul(builder,llvmValueRef1,llvmValueRef2,"");
        }
        else if(ctx.DIV()!=null){
            llvmValueRef = LLVMBuildSDiv(builder,llvmValueRef1,llvmValueRef2,"");
        }
        else if(ctx.MOD()!=null){
            llvmValueRef = LLVMBuildSRem(builder,llvmValueRef1,llvmValueRef2,"");
        }
        return llvmValueRef;
    }

    @Override
    public LLVMValueRef visitPlusExp(SysYParser.PlusExpContext ctx) {
        LLVMValueRef llvmValueRef1 = null;
        if(ctx.exp(0) instanceof SysYParser.MulExpContext){
            llvmValueRef1 = visitMulExp((SysYParser.MulExpContext) ctx.exp(0));
        }
        else if(ctx.exp(0) instanceof SysYParser.PlusExpContext){
            llvmValueRef1 = visitPlusExp((SysYParser.PlusExpContext) ctx.exp(0));
        }
        else if(ctx.exp(0) instanceof SysYParser.NumberExpContext) {
            llvmValueRef1 = visitNumberExp((SysYParser.NumberExpContext) ctx.exp(0));
        }
        else if(ctx.exp(0) instanceof SysYParser.UnaryOpExpContext){
            llvmValueRef1 = visitUnaryOpExp((SysYParser.UnaryOpExpContext) ctx.exp(0));
        }
        else if(ctx.exp(0) instanceof SysYParser.LvalExpContext){
            llvmValueRef1 = visitLvalExp((SysYParser.LvalExpContext) ctx.exp(0));
        }
        else if(ctx.exp(0) instanceof  SysYParser.CallFuncExpContext){
            llvmValueRef1 = visitCallFuncExp((SysYParser.CallFuncExpContext) ctx.exp(0));
        }
        LLVMValueRef llvmValueRef2 = null;
        if(ctx.exp(1) instanceof SysYParser.MulExpContext){
            llvmValueRef2 = visitMulExp((SysYParser.MulExpContext) ctx.exp(1));
        }
        else if(ctx.exp(1) instanceof SysYParser.PlusExpContext){
            llvmValueRef2 = visitPlusExp((SysYParser.PlusExpContext) ctx.exp(1));
        }
        else if(ctx.exp(1) instanceof SysYParser.NumberExpContext) {
            llvmValueRef2 = visitNumberExp((SysYParser.NumberExpContext) ctx.exp(1));
        }
        else if(ctx.exp(1) instanceof SysYParser.UnaryOpExpContext){
            llvmValueRef2 = visitUnaryOpExp((SysYParser.UnaryOpExpContext) ctx.exp(1));
        }
        else if(ctx.exp(1) instanceof SysYParser.LvalExpContext){
            llvmValueRef2 = visitLvalExp((SysYParser.LvalExpContext) ctx.exp(1));
        }
        else if(ctx.exp(1) instanceof  SysYParser.CallFuncExpContext){
            llvmValueRef1 = visitCallFuncExp((SysYParser.CallFuncExpContext) ctx.exp(1));
        }
        LLVMValueRef llvmValueRef = null;
        if(ctx.PLUS()!=null){
            llvmValueRef = LLVMBuildAdd(builder,llvmValueRef1,llvmValueRef2,"");
        }
        else if(ctx.MINUS()!=null){
            llvmValueRef = LLVMBuildSub(builder,llvmValueRef1,llvmValueRef2,"");
        }
        return llvmValueRef;
    }

    @Override
    public LLVMValueRef visitUnaryOpExp(SysYParser.UnaryOpExpContext ctx) {
        LLVMValueRef llvmValueRef = null;
        if(ctx.exp() instanceof SysYParser.MulExpContext){
            llvmValueRef = visitMulExp((SysYParser.MulExpContext) ctx.exp());
        }
        else if(ctx.exp() instanceof SysYParser.PlusExpContext){
            llvmValueRef = visitPlusExp((SysYParser.PlusExpContext) ctx.exp());
        }
        else if(ctx.exp() instanceof SysYParser.NumberExpContext) {
            llvmValueRef = visitNumberExp((SysYParser.NumberExpContext) ctx.exp());
        }
        else if(ctx.exp() instanceof SysYParser.UnaryOpExpContext){
            llvmValueRef = visitUnaryOpExp((SysYParser.UnaryOpExpContext) ctx.exp());
        }
        else if(ctx.exp() instanceof SysYParser.LvalExpContext){
            llvmValueRef = visitLvalExp((SysYParser.LvalExpContext) ctx.exp());
        }
        else if(ctx.exp() instanceof  SysYParser.CallFuncExpContext){
            llvmValueRef = visitCallFuncExp((SysYParser.CallFuncExpContext) ctx.exp());
        }
        String val = String.valueOf(LLVMConstIntGetSExtValue(llvmValueRef));
        String symbol = ctx.unaryOp().getText();
        if(symbol.equals("!")){
            if(Long.valueOf(val)!=0){
                val = "0";
            }
            else{
                val = "1";
            }
        }
        else{
            if(!symbol.equals("+")){
                if(val.startsWith("-")){
                    val = val.substring(1);
                }
                else{
                    val = symbol + val;
                }
            }
        }
        LLVMValueRef res = LLVMConstInt(i32Type,Long.valueOf(val),0);
        return res;
    }

    @Override
    public LLVMValueRef visitNumberExp(SysYParser.NumberExpContext ctx) {
        return visitNumber(ctx.number());
    }

    @Override
    public LLVMValueRef visitNumber(SysYParser.NumberContext ctx) {
        String value = ctx.getText();
        if(value.startsWith("0x")||value.startsWith("0X")){
            String temp = value.substring(2);
            value = String.valueOf(Integer.parseInt(temp,16));
        }
        else if(value.startsWith("0") && value.length()>1){
            String temp = value.substring(1);
            value = String.valueOf(Integer.parseInt(temp,8));
        }
        LLVMValueRef llvmValueRef = LLVMConstInt(i32Type, Long.parseLong(value), /* signExtend */ 0);
        return llvmValueRef;
    }
}
