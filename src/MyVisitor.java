import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.PointerPointer;
import org.bytedeco.llvm.LLVM.*;

import java.util.Stack;

import static org.bytedeco.llvm.global.LLVM.*;

public class MyVisitor extends SysYParserBaseVisitor<LLVMValueRef> {
    LLVMModuleRef module = null;
    LLVMBuilderRef builder = null;
    LLVMTypeRef i32Type = LLVMInt32Type();
    LLVMContextRef context = null;
    LLVMValueRef currentFuncRef = null;
    Scope currentScope = new Scope("GLOBAL",null);
    LLVMBasicBlockRef currentBlock = null;
    LLVMValueRef zero = LLVMConstInt(i32Type, 0, /* signExtend */ 0);
    String funcName = "";
    Stack<LLVMBasicBlockRef> whileConds = new Stack<>();
    Stack<LLVMBasicBlockRef> whileEntrys = new Stack<>();
    boolean ret = false;
    int count = 0;
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
        if(ctx.funcType().getText().equals("int")){
            returnType = i32Type;
        }
        else{
            returnType = LLVMVoidType();
        }
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
        this.currentFuncRef = function;
        this.currentScope.define(ctx.IDENT().getText(),function);
        Scope scope = new Scope(ctx.IDENT().getText(),this.currentScope);
        this.currentScope = scope;

        LLVMBasicBlockRef Main_entry = LLVMAppendBasicBlockInContext(context,this.currentFuncRef,
                LLVMGetValueName(this.currentFuncRef).getString()+"Entry");
        this.currentBlock = Main_entry;
        LLVMPositionBuilderAtEnd(builder, this.currentBlock);
        super.visitFuncDef(ctx);
        this.currentScope = this.currentScope.getEnclosingScope();
        if(!this.ret) {
            LLVMBuildRetVoid(builder);
            this.ret = false;
        }
        return null;
    }

    @Override
    public LLVMValueRef visitFuncFParams(SysYParser.FuncFParamsContext ctx) {
//        LLVMPositionBuilderAtEnd(builder, this.currentBlock);
        for(int i=0;i<ctx.funcFParam().size();i++) {
            LLVMValueRef paramRef = LLVMBuildAlloca(builder, i32Type, ctx.funcFParam(i).IDENT().getText());
            LLVMBuildStore(builder, LLVMGetParam(this.currentFuncRef, i), paramRef);
            this.currentScope.define(ctx.funcFParam(i).IDENT().getText(),paramRef);
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
//        LLVMPositionBuilderAtEnd(builder, this.currentBlock);
        if(currentScope.getName().equals("GLOBAL")){
            if(ctx.L_BRACKT().size()!=0){
                int num = Integer.parseInt(ctx.constExp(0).getText());
                LLVMTypeRef arrayType = LLVMArrayType(i32Type, num);
                LLVMValueRef array = LLVMAddGlobal(module,arrayType,ctx.IDENT().getText());
                LLVMValueRef[] initRefs = new LLVMValueRef[num];
                for(int i=0;i<num;i++){
                    if (ctx.constInitVal().constInitVal(i) != null) {
                        if (ctx.constInitVal().constInitVal(i).constExp().exp() instanceof SysYParser.NumberExpContext) {
                            initRefs[i] = visitNumberExp((SysYParser.NumberExpContext) ctx.constInitVal().constInitVal(i).constExp().exp());
                        }
                    }
                    else{
                        initRefs[i] = zero;
                    }
                }
                PointerPointer valuepointer = new PointerPointer(initRefs);
                LLVMSetInitializer(array,LLVMConstArray(arrayType,valuepointer,num));
                this.currentScope.define(ctx.IDENT().getText(),array);
            }
            else{
                LLVMValueRef globalVar = LLVMAddGlobal(module,i32Type,ctx.IDENT().getText());
                LLVMValueRef initRef = zero;
                if(ctx.constInitVal()!=null){
                    if (ctx.constInitVal().constExp().exp() instanceof SysYParser.NumberExpContext) {
                        initRef = visitNumberExp((SysYParser.NumberExpContext) ctx.constInitVal().constExp().exp());
                    }
                }
                LLVMSetInitializer(globalVar,initRef);
                this.currentScope.define(ctx.IDENT().getText(),globalVar);
            }
        }
        else {
            if (ctx.L_BRACKT().size() != 0) {
                int num = Integer.parseInt(ctx.constExp(0).getText());
                LLVMTypeRef arrayType = LLVMArrayType(i32Type, num);
                LLVMValueRef array = LLVMBuildAlloca(builder, arrayType, ctx.IDENT().getText());
                int n = Integer.parseInt(ctx.constExp(0).getText());
                for (int i = 0; i < n; i++) {
                    LLVMValueRef index = LLVMConstInt(i32Type, i, 1);
                    PointerPointer valuePointer = new PointerPointer(new LLVMValueRef[]{zero, index});
                    LLVMValueRef pointer = LLVMBuildGEP(builder, array, valuePointer, 2, "pointer");
                    LLVMValueRef initRef = null;
                    if (ctx.constInitVal().constInitVal(i) != null) {
                        if (ctx.constInitVal().constInitVal(i).constExp().exp() instanceof SysYParser.LvalExpContext) {
                            initRef = visitLvalExp
                                    ((SysYParser.LvalExpContext) ctx.constInitVal().constInitVal(i).constExp().exp());
                        } else if (ctx.constInitVal().constInitVal(i).constExp().exp() instanceof SysYParser.PlusExpContext) {
                            initRef = visitPlusExp
                                    ((SysYParser.PlusExpContext) ctx.constInitVal().constInitVal(i).constExp().exp());
                        } else if (ctx.constInitVal().constInitVal(i).constExp().exp() instanceof SysYParser.CallFuncExpContext) {
                            initRef = visitCallFuncExp
                                    ((SysYParser.CallFuncExpContext) ctx.constInitVal().constInitVal(i).constExp().exp());
                        } else if (ctx.constInitVal().constInitVal(i).constExp().exp() instanceof SysYParser.MulExpContext) {
                            initRef = visitMulExp
                                    ((SysYParser.MulExpContext) ctx.constInitVal().constInitVal(i).constExp().exp());
                        } else if (ctx.constInitVal().constInitVal(i).constExp().exp() instanceof SysYParser.NumberExpContext) {
                            initRef = visitNumberExp
                                    ((SysYParser.NumberExpContext) ctx.constInitVal().constInitVal(i).constExp().exp());
                        } else if (ctx.constInitVal().constInitVal(i).constExp().exp() instanceof SysYParser.UnaryOpExpContext) {
                            initRef = visitUnaryOpExp
                                    ((SysYParser.UnaryOpExpContext) ctx.constInitVal().constInitVal(i).constExp().exp());
                        }else if(ctx.constInitVal().constInitVal(i).constExp().exp() instanceof SysYParser.ExpParenthesisContext){
                            initRef = visitExpParenthesis
                                    ((SysYParser.ExpParenthesisContext) ctx.constInitVal().constInitVal(i).constExp().exp());
                        }
                    } else {
                        initRef = LLVMConstInt(i32Type, 0, /* signExtend */ 0);
                    }
                    if (initRef != null) {
                        LLVMBuildStore(builder, initRef, pointer);
                    }
                }
                this.currentScope.define(ctx.IDENT().getText(),array);
            } else {
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
                    } else if (ctx.constInitVal().constExp().exp() instanceof SysYParser.ExpParenthesisContext){
                        initRef = visitExpParenthesis((SysYParser.ExpParenthesisContext) ctx.constInitVal().constExp().exp());
                    }
                }
                if (initRef != null) {
                    LLVMBuildStore(builder, initRef, varRef);
                }
                this.currentScope.define(ctx.IDENT().getText(),varRef);
            }
        }
        return null;
    }



    @Override
    public LLVMValueRef visitVarDef(SysYParser.VarDefContext ctx) {
//        LLVMPositionBuilderAtEnd(builder, this.currentBlock);
        if(currentScope.getName().equals("GLOBAL")){
            if(ctx.L_BRACKT().size()!=0){
                int num = Integer.parseInt(ctx.constExp(0).getText());
                LLVMTypeRef arrayType = LLVMArrayType(i32Type, num);
                LLVMValueRef array = LLVMAddGlobal(module,arrayType,ctx.IDENT().getText());
                LLVMValueRef[] initRefs = new LLVMValueRef[num];
                for(int i=0;i<num;i++){
                    if(ctx.initVal()!=null) {
                        if (ctx.initVal().initVal(i) != null) {
                            if (ctx.initVal().initVal(i).exp() instanceof SysYParser.NumberExpContext) {
                                initRefs[i] = visitNumberExp((SysYParser.NumberExpContext) ctx.initVal().initVal(i).exp());
                            }
                        } else {
                            initRefs[i] = zero;
                        }
                    }
                    else{
                        initRefs[i] = zero;
                    }
                }
                PointerPointer valuepointer = new PointerPointer(initRefs);
                LLVMSetInitializer(array,LLVMConstArray(arrayType,valuepointer,num));
                this.currentScope.define(ctx.IDENT().getText(),array);
            }
            else{
                LLVMValueRef globalVar = LLVMAddGlobal(module,i32Type,ctx.IDENT().getText());
                LLVMValueRef initRef = zero;
                if(ctx.initVal()!=null){
                    if (ctx.initVal().exp() instanceof SysYParser.NumberExpContext) {
                        initRef = visitNumberExp((SysYParser.NumberExpContext) ctx.initVal().exp());
                    }
                }
                LLVMSetInitializer(globalVar,initRef);
                this.currentScope.define(ctx.IDENT().getText(),globalVar);
            }
        }
        else {
            //Array
            if (ctx.L_BRACKT().size() != 0) {
                int num = Integer.parseInt(ctx.constExp(0).getText());
                LLVMTypeRef arrayType = LLVMArrayType(i32Type, num);
                LLVMValueRef array = LLVMBuildAlloca(builder, arrayType, ctx.IDENT().getText());
                int n = Integer.parseInt(ctx.constExp(0).getText());
                for (int i = 0; i < n; i++) {
                    LLVMValueRef index = LLVMConstInt(i32Type, i, 1);
                    PointerPointer valuePointer = new PointerPointer(new LLVMValueRef[]{zero, index});
                    LLVMValueRef pointer = LLVMBuildGEP(builder, array, valuePointer, 2, "pointer");
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
                        }else if(ctx.initVal().initVal(i).exp() instanceof SysYParser.ExpParenthesisContext){
                            initRef = visitExpParenthesis((SysYParser.ExpParenthesisContext) ctx.initVal().initVal(i).exp());
                        }
                    } else {
                        initRef = LLVMConstInt(i32Type, 0, /* signExtend */ 0);
                    }
                    if (initRef != null) {
                        LLVMBuildStore(builder, initRef, pointer);
                    }
                }
                this.currentScope.define(ctx.IDENT().getText(),array);
            } else {
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
                    }else if(ctx.initVal().exp() instanceof SysYParser.ExpParenthesisContext){
                        initRef = visitExpParenthesis((SysYParser.ExpParenthesisContext) ctx.initVal().exp());
                    }
                }
                if (initRef != null) {
                    LLVMBuildStore(builder, initRef, varRef);
                }
                this.currentScope.define(ctx.IDENT().getText(),varRef);
            }
        }
        return null;
    }

    @Override
    public LLVMValueRef visitBlock(SysYParser.BlockContext ctx) {
        if(ctx.getParent().getRuleIndex()!=10){
//            LLVMBasicBlockRef entry = LLVMAppendBasicBlockInContext(context,this.currentFuncRef,
//                    LLVMGetValueName(this.currentFuncRef).getString()+"Block");
//            LLVMBasicBlockRef past = this.currentBlock;
            Scope scope = new Scope("localscope"+String.valueOf(count),this.currentScope);
            count++;
            this.currentScope = scope;
//            this.currentBlock = entry;
//            LLVMBuildBr(builder, this.currentBlock);
//            LLVMPositionBuilderAtEnd(builder, this.currentBlock);
            super.visitBlock(ctx);
//            this.currentBlock = past;
            this.currentScope = this.currentScope.getEnclosingScope();
//            LLVMBuildBr(builder, this.currentBlock);
//            LLVMPositionBuilderAtEnd(builder, this.currentBlock);
            return null;
        }
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
            else if(ctx.exp() instanceof SysYParser.ExpParenthesisContext){
                llvmValueRef = visitExpParenthesis((SysYParser.ExpParenthesisContext) ctx.exp());
            }
//            LLVMPositionBuilderAtEnd(builder,this.currentBlock);
            LLVMBuildRet(builder, llvmValueRef);
            this.ret = true;
            return null;
        }
        else if(ctx.ASSIGN()!=null){
            LLVMValueRef lvalRef = null;
            if(ctx.lVal().L_BRACKT().size()!=0){
                LLVMValueRef array = this.currentScope.resolve(ctx.lVal().IDENT().getText());
                LLVMValueRef index = null;
                if(ctx.lVal().exp(0) instanceof SysYParser.MulExpContext){
                    index = visitMulExp((SysYParser.MulExpContext) ctx.lVal().exp(0));
                }
                else if(ctx.lVal().exp(0) instanceof SysYParser.PlusExpContext){
                    index = visitPlusExp((SysYParser.PlusExpContext) ctx.lVal().exp(0));
                }
                else if(ctx.lVal().exp(0) instanceof SysYParser.NumberExpContext) {
                    index = visitNumberExp((SysYParser.NumberExpContext) ctx.lVal().exp(0));
                }
                else if(ctx.lVal().exp(0) instanceof SysYParser.UnaryOpExpContext){
                    index = visitUnaryOpExp((SysYParser.UnaryOpExpContext) ctx.lVal().exp(0));
                }
                else if(ctx.lVal().exp(0) instanceof SysYParser.LvalExpContext){
                    index = visitLvalExp((SysYParser.LvalExpContext) ctx.lVal().exp(0));
                }
                else if(ctx.lVal().exp(0) instanceof  SysYParser.CallFuncExpContext){
                    index = visitCallFuncExp((SysYParser.CallFuncExpContext) ctx.lVal().exp(0));
                }
                else if(ctx.lVal().exp(0) instanceof SysYParser.ExpParenthesisContext){
                    index = visitExpParenthesis((SysYParser.ExpParenthesisContext) ctx.lVal().exp(0));
                }
                PointerPointer valuePointer = new PointerPointer (new LLVMValueRef[]{zero, index}) ;
                LLVMValueRef pointer = LLVMBuildGEP(builder,array,valuePointer,2,"pointer");
                lvalRef = pointer;
            }
            else {
                lvalRef = this.currentScope.resolve(ctx.lVal().IDENT().getText());
            }
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
            else if(ctx.exp() instanceof SysYParser.ExpParenthesisContext){
                llvmValueRef = visitExpParenthesis((SysYParser.ExpParenthesisContext) ctx.exp());
            }
            LLVMBuildStore(builder,llvmValueRef,lvalRef);
            return null;
        }
        else if(ctx.IF()!=null){
            LLVMValueRef condition = null;
            if(ctx.cond() instanceof SysYParser.ExpCondContext){
                condition = visitExpCond((SysYParser.ExpCondContext) ctx.cond());
            }
            else if(ctx.cond() instanceof SysYParser.LtCondContext){
                condition = visitLtCond((SysYParser.LtCondContext) ctx.cond());
            }
            else if(ctx.cond() instanceof SysYParser.EqCondContext){
                condition = visitEqCond((SysYParser.EqCondContext) ctx.cond());
            }
            else if(ctx.cond() instanceof SysYParser.AndCondContext){
                condition = visitAndCond((SysYParser.AndCondContext) ctx.cond());
            }
            else if(ctx.cond() instanceof SysYParser.OrCondContext){
                condition = visitOrCond((SysYParser.OrCondContext) ctx.cond());
            }
            condition = LLVMBuildICmp(builder, LLVMIntNE, condition, zero, "cmp");
            LLVMBasicBlockRef if_true = LLVMAppendBasicBlockInContext(context,this.currentFuncRef, "if_true");
            LLVMBasicBlockRef if_false = LLVMAppendBasicBlockInContext(context,this.currentFuncRef, "if_false");
            LLVMBasicBlockRef ret = LLVMAppendBasicBlockInContext(context,this.currentFuncRef, "entry");
            LLVMBuildCondBr(builder,condition,if_true,if_false);
            this.currentBlock = if_true;
            LLVMPositionBuilderAtEnd(builder,if_true);
            visitStmt(ctx.stmt(0));
            LLVMBuildBr(builder,ret);
            this.currentBlock = if_false;
            LLVMPositionBuilderAtEnd(builder,if_false);
            if(ctx.ELSE()!=null){
                visitStmt(ctx.stmt(1));
            }
            LLVMBuildBr(builder,ret);
            this.currentBlock = ret;
            LLVMPositionBuilderAtEnd(builder,ret);
            return null;
        }
        else if(ctx.WHILE()!=null){
            LLVMBasicBlockRef whileCondition = LLVMAppendBasicBlockInContext(context,this.currentFuncRef, "whileCondition");
            LLVMBuildBr(builder,whileCondition);
            this.currentBlock = whileCondition;
            LLVMPositionBuilderAtEnd(builder,whileCondition);
            LLVMValueRef condition = null;
            if(ctx.cond() instanceof SysYParser.ExpCondContext){
                condition = visitExpCond((SysYParser.ExpCondContext) ctx.cond());
            }
            else if(ctx.cond() instanceof SysYParser.LtCondContext){
                condition = visitLtCond((SysYParser.LtCondContext) ctx.cond());
            }
            else if(ctx.cond() instanceof SysYParser.EqCondContext){
                condition = visitEqCond((SysYParser.EqCondContext) ctx.cond());
            }
            else if(ctx.cond() instanceof SysYParser.AndCondContext){
                condition = visitAndCond((SysYParser.AndCondContext) ctx.cond());
            }
            else if(ctx.cond() instanceof SysYParser.OrCondContext){
                condition = visitOrCond((SysYParser.OrCondContext) ctx.cond());
            }
            condition = LLVMBuildICmp(builder, LLVMIntNE, condition, zero, "cmp");
            LLVMBasicBlockRef whileBody = LLVMAppendBasicBlockInContext(context,this.currentFuncRef,"whileBody");
            LLVMBasicBlockRef entry = LLVMAppendBasicBlockInContext(context,this.currentFuncRef,"entry");
            LLVMBuildCondBr(builder,condition,whileBody,entry);
            this.whileConds.push(whileCondition);
            this.whileEntrys.push(entry);

            this.currentBlock = whileBody;
            LLVMPositionBuilderAtEnd(builder,whileBody);
            visitStmt(ctx.stmt(0));
            LLVMBuildBr(builder,whileCondition);

            this.currentBlock = entry;
            LLVMPositionBuilderAtEnd(builder,entry);
            this.whileEntrys.pop();
            this.whileConds.pop();
            return null;
        }
        else if(ctx.BREAK()!=null){
            LLVMBasicBlockRef dest = this.whileEntrys.lastElement();
//            this.whileConds.pop();
            LLVMBuildBr(builder,dest);
            return null;
        }
        else if(ctx.CONTINUE()!=null){
            LLVMBasicBlockRef dest = this.whileConds.lastElement();
            LLVMBuildBr(builder,dest);
            return null;
        }
        return super.visitStmt(ctx);
    }

    @Override
    public LLVMValueRef visitExpCond(SysYParser.ExpCondContext ctx) {
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
        else if(ctx.exp() instanceof  SysYParser.ExpParenthesisContext){
            llvmValueRef = visitExpParenthesis((SysYParser.ExpParenthesisContext) ctx.exp());
        }
        return llvmValueRef;
    }

    @Override
    public LLVMValueRef visitLtCond(SysYParser.LtCondContext ctx) {
        LLVMValueRef cond1 = null;
        if(ctx.cond(0) instanceof SysYParser.ExpCondContext){
            cond1 = visitExpCond((SysYParser.ExpCondContext) ctx.cond(0));
        }
        else if(ctx.cond(0) instanceof SysYParser.LtCondContext){
            cond1 = visitLtCond((SysYParser.LtCondContext) ctx.cond(0));
        }
        else if(ctx.cond(0) instanceof SysYParser.OrCondContext){
            cond1 = visitLtCond((SysYParser.LtCondContext) ctx.cond(0));
        }
        else if(ctx.cond(0) instanceof SysYParser.AndCondContext){
            cond1 = visitAndCond((SysYParser.AndCondContext) ctx.cond(0));
        }
        else if(ctx.cond(0) instanceof SysYParser.EqCondContext){
            cond1 = visitEqCond((SysYParser.EqCondContext) ctx.cond(0));
        }
        LLVMValueRef cond2 = null;
        if(ctx.cond(1) instanceof SysYParser.ExpCondContext){
            cond2 = visitExpCond((SysYParser.ExpCondContext) ctx.cond(1));
        }
        else if(ctx.cond(1) instanceof SysYParser.LtCondContext){
            cond2 = visitLtCond((SysYParser.LtCondContext) ctx.cond(1));
        }
        else if(ctx.cond(1) instanceof SysYParser.OrCondContext){
            cond2 = visitLtCond((SysYParser.LtCondContext) ctx.cond(1));
        }
        else if(ctx.cond(1) instanceof SysYParser.AndCondContext){
            cond2 = visitAndCond((SysYParser.AndCondContext) ctx.cond(1));
        }
        else if(ctx.cond(1) instanceof SysYParser.EqCondContext){
            cond2 = visitEqCond((SysYParser.EqCondContext) ctx.cond(1));
        }
        LLVMValueRef cmp = null;
        if(ctx.LT()!=null){
            cmp = LLVMBuildICmp(builder, LLVMIntSLT, cond1, cond2, "cmp");
        }
        else if(ctx.GT()!=null){
            cmp = LLVMBuildICmp(builder, LLVMIntSGT, cond1, cond2, "cmp");
        }
        else if(ctx.LE()!=null){
            cmp = LLVMBuildICmp(builder, LLVMIntSLE, cond1, cond2, "cmp");
        }
        else if(ctx.GE()!=null){
            cmp = LLVMBuildICmp(builder, LLVMIntSGE, cond1, cond2, "cmp");
        }
        return LLVMBuildZExt(builder, cmp, LLVMInt32Type(), "cmp");
    }

    @Override
    public LLVMValueRef visitOrCond(SysYParser.OrCondContext ctx) {
        LLVMValueRef cond1 = null;
        if(ctx.cond(0) instanceof SysYParser.ExpCondContext){
            cond1 = visitExpCond((SysYParser.ExpCondContext) ctx.cond(0));
        }
        else if(ctx.cond(0) instanceof SysYParser.LtCondContext){
            cond1 = visitLtCond((SysYParser.LtCondContext) ctx.cond(0));
        }
        else if(ctx.cond(0) instanceof SysYParser.OrCondContext){
            cond1 = visitLtCond((SysYParser.LtCondContext) ctx.cond(0));
        }
        else if(ctx.cond(0) instanceof SysYParser.AndCondContext){
            cond1 = visitAndCond((SysYParser.AndCondContext) ctx.cond(0));
        }
        else if(ctx.cond(0) instanceof SysYParser.EqCondContext){
            cond1 = visitEqCond((SysYParser.EqCondContext) ctx.cond(0));
        }
        LLVMValueRef cond2 = null;
        if(ctx.cond(1) instanceof SysYParser.ExpCondContext){
            cond2 = visitExpCond((SysYParser.ExpCondContext) ctx.cond(1));
        }
        else if(ctx.cond(1) instanceof SysYParser.LtCondContext){
            cond2 = visitLtCond((SysYParser.LtCondContext) ctx.cond(1));
        }
        else if(ctx.cond(1) instanceof SysYParser.OrCondContext){
            cond2 = visitLtCond((SysYParser.LtCondContext) ctx.cond(1));
        }
        else if(ctx.cond(1) instanceof SysYParser.AndCondContext){
            cond2 = visitAndCond((SysYParser.AndCondContext) ctx.cond(1));
        }
        else if(ctx.cond(1) instanceof SysYParser.EqCondContext){
            cond2 = visitEqCond((SysYParser.EqCondContext) ctx.cond(1));
        }
        LLVMValueRef orResult = LLVMBuildOr(builder,cond1,cond2,"orResult");
        return LLVMBuildZExt(builder, orResult, LLVMInt32Type(), "orResult");
    }

    @Override
    public LLVMValueRef visitAndCond(SysYParser.AndCondContext ctx) {
        LLVMValueRef cond1 = null;
        if(ctx.cond(0) instanceof SysYParser.ExpCondContext){
            cond1 = visitExpCond((SysYParser.ExpCondContext) ctx.cond(0));
        }
        else if(ctx.cond(0) instanceof SysYParser.LtCondContext){
            cond1 = visitLtCond((SysYParser.LtCondContext) ctx.cond(0));
        }
        else if(ctx.cond(0) instanceof SysYParser.OrCondContext){
            cond1 = visitLtCond((SysYParser.LtCondContext) ctx.cond(0));
        }
        else if(ctx.cond(0) instanceof SysYParser.AndCondContext){
            cond1 = visitAndCond((SysYParser.AndCondContext) ctx.cond(0));
        }
        else if(ctx.cond(0) instanceof SysYParser.EqCondContext){
            cond1 = visitEqCond((SysYParser.EqCondContext) ctx.cond(0));
        }
        LLVMValueRef cond2 = null;
        if(ctx.cond(1) instanceof SysYParser.ExpCondContext){
            cond2 = visitExpCond((SysYParser.ExpCondContext) ctx.cond(1));
        }
        else if(ctx.cond(1) instanceof SysYParser.LtCondContext){
            cond2 = visitLtCond((SysYParser.LtCondContext) ctx.cond(1));
        }
        else if(ctx.cond(1) instanceof SysYParser.OrCondContext){
            cond2 = visitLtCond((SysYParser.LtCondContext) ctx.cond(1));
        }
        else if(ctx.cond(1) instanceof SysYParser.AndCondContext){
            cond2 = visitAndCond((SysYParser.AndCondContext) ctx.cond(1));
        }
        else if(ctx.cond(1) instanceof SysYParser.EqCondContext){
            cond2 = visitEqCond((SysYParser.EqCondContext) ctx.cond(1));
        }
        LLVMValueRef andResult = LLVMBuildAnd(builder,cond1,cond2,"andResult");
        return LLVMBuildZExt(builder, andResult, LLVMInt32Type(), "andResult");
    }

    @Override
    public LLVMValueRef visitEqCond(SysYParser.EqCondContext ctx) {
        LLVMValueRef cond1 = null;
        if(ctx.cond(0) instanceof SysYParser.ExpCondContext){
            cond1 = visitExpCond((SysYParser.ExpCondContext) ctx.cond(0));
        }
        else if(ctx.cond(0) instanceof SysYParser.LtCondContext){
            cond1 = visitLtCond((SysYParser.LtCondContext) ctx.cond(0));
        }
        else if(ctx.cond(0) instanceof SysYParser.OrCondContext){
            cond1 = visitLtCond((SysYParser.LtCondContext) ctx.cond(0));
        }
        else if(ctx.cond(0) instanceof SysYParser.AndCondContext){
            cond1 = visitAndCond((SysYParser.AndCondContext) ctx.cond(0));
        }
        else if(ctx.cond(0) instanceof SysYParser.EqCondContext){
            cond1 = visitEqCond((SysYParser.EqCondContext) ctx.cond(0));
        }
        LLVMValueRef cond2 = null;
        if(ctx.cond(1) instanceof SysYParser.ExpCondContext){
            cond2 = visitExpCond((SysYParser.ExpCondContext) ctx.cond(1));
        }
        else if(ctx.cond(1) instanceof SysYParser.LtCondContext){
            cond2 = visitLtCond((SysYParser.LtCondContext) ctx.cond(1));
        }
        else if(ctx.cond(1) instanceof SysYParser.OrCondContext){
            cond2 = visitLtCond((SysYParser.LtCondContext) ctx.cond(1));
        }
        else if(ctx.cond(1) instanceof SysYParser.AndCondContext){
            cond2 = visitAndCond((SysYParser.AndCondContext) ctx.cond(1));
        }
        else if(ctx.cond(1) instanceof SysYParser.EqCondContext){
            cond2 = visitEqCond((SysYParser.EqCondContext) ctx.cond(1));
        }
        LLVMValueRef cmp = null;
        if(ctx.EQ()!=null){
            cmp = LLVMBuildICmp(builder,LLVMIntEQ,cond1,cond2,"cmp");
        }
        else{
            cmp = LLVMBuildICmp(builder,LLVMIntNE,cond1,cond2,"cmp");
        }
        return LLVMBuildZExt(builder, cmp, LLVMInt32Type(), "cmp");
    }

    @Override
    public LLVMValueRef visitCallFuncExp(SysYParser.CallFuncExpContext ctx) {
//        LLVMValueRef funcRef = LLVMGetNamedFunction(module,ctx.IDENT().getText());
        this.funcName = ctx.IDENT().getText();
        if(ctx.funcRParams()==null){
            LLVMValueRef funcRef = this.currentScope.resolve(this.funcName);
            LLVMValueRef[] args = {};
            LLVMTypeRef fnType = LLVMTypeOf(funcRef);
            LLVMTypeRef returnType = LLVMGetReturnType(fnType);
            int typeKind = LLVMGetTypeKind(LLVMGetReturnType(returnType));
            boolean isVoid = (typeKind == LLVMVoidTypeKind);
            if(isVoid){
                return LLVMBuildCall(builder, funcRef, new PointerPointer(args), 0, "");
            }
            else {
                return LLVMBuildCall(builder, funcRef, new PointerPointer(args), 0, "returnValue");
            }
        }
        return visitFuncRParams(ctx.funcRParams());
    }

    @Override
    public LLVMValueRef visitFuncRParams(SysYParser.FuncRParamsContext ctx) {
        LLVMValueRef funcRef = this.currentScope.resolve(this.funcName);
        int n = -1;
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
        LLVMTypeRef fnType = LLVMTypeOf(funcRef);
        LLVMTypeRef returnType = LLVMGetReturnType(fnType);
        int typeKind = LLVMGetTypeKind(LLVMGetReturnType(returnType));
        boolean isVoid = (typeKind == LLVMVoidTypeKind);
        if(isVoid){
            return LLVMBuildCall(builder, funcRef, new PointerPointer(args), n, "");
        }
        else {
            return LLVMBuildCall(builder, funcRef, new PointerPointer(args), n, "returnValue");
        }
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
        else if(ctx.exp() instanceof SysYParser.ExpParenthesisContext){
            llvmValueRef = visitExpParenthesis((SysYParser.ExpParenthesisContext) ctx.exp());
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
            else if(ctx.exp(0) instanceof SysYParser.ExpParenthesisContext){
                index = visitExpParenthesis((SysYParser.ExpParenthesisContext) ctx.exp(0));
            }
            PointerPointer valuePointer = new PointerPointer (new LLVMValueRef[]{zero, index}) ;
            LLVMValueRef pointer = LLVMBuildGEP(builder,array,valuePointer,2,"pointer");
            retValue = LLVMBuildLoad(builder,pointer,ctx.IDENT().getText());
        }
        else{
            LLVMValueRef temp = this.currentScope.resolve(ctx.IDENT().getText());
            retValue = LLVMBuildLoad(builder,temp,ctx.IDENT().getText());
        }
        return retValue;
    }

    @Override
    public LLVMValueRef visitExpParenthesis(SysYParser.ExpParenthesisContext ctx) {
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
        else if(ctx.exp() instanceof  SysYParser.ExpParenthesisContext){
            llvmValueRef = visitExpParenthesis((SysYParser.ExpParenthesisContext) ctx.exp());
        }
        return llvmValueRef;
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
        else if(ctx.exp(0) instanceof  SysYParser.ExpParenthesisContext){
            llvmValueRef1 = visitExpParenthesis((SysYParser.ExpParenthesisContext) ctx.exp(0));
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
            llvmValueRef2 = visitCallFuncExp((SysYParser.CallFuncExpContext) ctx.exp(1));
        }
        else if(ctx.exp(1) instanceof  SysYParser.ExpParenthesisContext){
            llvmValueRef2 = visitExpParenthesis((SysYParser.ExpParenthesisContext) ctx.exp(1));
        }
        LLVMValueRef llvmValueRef = null;
        if(ctx.MUL()!=null){
            llvmValueRef = LLVMBuildMul(builder,llvmValueRef1,llvmValueRef2,"mul");
        }
        else if(ctx.DIV()!=null){
            llvmValueRef = LLVMBuildSDiv(builder,llvmValueRef1,llvmValueRef2,"div");
        }
        else if(ctx.MOD()!=null){
            llvmValueRef = LLVMBuildSRem(builder,llvmValueRef1,llvmValueRef2,"mod");
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
        else if(ctx.exp(0) instanceof  SysYParser.ExpParenthesisContext){
            llvmValueRef1 = visitExpParenthesis((SysYParser.ExpParenthesisContext) ctx.exp(0));
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
            llvmValueRef2 = visitCallFuncExp((SysYParser.CallFuncExpContext) ctx.exp(1));
        }
        else if(ctx.exp(1) instanceof  SysYParser.ExpParenthesisContext){
            llvmValueRef2 = visitExpParenthesis((SysYParser.ExpParenthesisContext) ctx.exp(1));
        }
        LLVMValueRef llvmValueRef = null;
        if(ctx.PLUS()!=null){
            llvmValueRef = LLVMBuildAdd(builder,llvmValueRef1,llvmValueRef2,"plus");
        }
        else if(ctx.MINUS()!=null){
            llvmValueRef = LLVMBuildSub(builder,llvmValueRef1,llvmValueRef2,"minus");
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
        else if(ctx.exp() instanceof  SysYParser.ExpParenthesisContext){
            llvmValueRef = visitExpParenthesis((SysYParser.ExpParenthesisContext) ctx.exp());
        }
        String val = String.valueOf(LLVMConstIntGetSExtValue(llvmValueRef));
        String symbol = ctx.unaryOp().getText();
        if(symbol.equals("!")){
            LLVMValueRef tmp = LLVMBuildICmp(builder, LLVMIntNE, llvmValueRef, zero, "tmp");
            LLVMValueRef tmp1 = LLVMBuildXor(builder, tmp, LLVMConstInt(LLVMInt1Type(), 1, 0), "tmp");
            return LLVMBuildZExt(builder, tmp1, LLVMInt32Type(), "tmp");
        }
        else{
            if(!symbol.equals("+")){
                LLVMValueRef res = LLVMBuildNeg(builder,llvmValueRef,"tmp");
                return res;
            }
            else{
                return llvmValueRef;
            }
        }
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
