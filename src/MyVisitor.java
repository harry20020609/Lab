import org.bytedeco.llvm.LLVM.*;
import static org.bytedeco.llvm.global.LLVM.*;


public class MyVisitor extends SysYParserBaseVisitor<LLVMValueRef> {


    //创建module
    LLVMModuleRef module = null;
    //初始化IRBuilder，后续将使用这个builder去生成LLVM IR
    LLVMBuilderRef builder = null;
    //考虑到我们的语言中仅存在int一个基本类型，可以通过下面的语句为LLVM的int型重命名方便以后使用
    LLVMTypeRef i32Type = LLVMInt32Type();

    LLVMContextRef context = null;

    LLVMValueRef currentScope = null;

    LLVMBasicBlockRef currentBlock = null;
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

        //生成函数类型
        LLVMTypeRef ft = LLVMFunctionType(returnType, i32Type, /* argumentCount */ 0, /* isVariadic */ 0);
        //生成函数，即向之前创建的module中添加函数
        LLVMValueRef function = LLVMAddFunction(module, /*functionName:String*/"main", ft);
        this.currentScope = function;
        return super.visitFuncDef(ctx);
    }

    @Override
    public LLVMValueRef visitBlock(SysYParser.BlockContext ctx) {
        LLVMBasicBlockRef Main_entry = LLVMAppendBasicBlockInContext(context,this.currentScope,"mainEntry");
        this.currentBlock = Main_entry;
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
            LLVMPositionBuilderAtEnd(builder,this.currentBlock);
            LLVMBuildRet(builder, llvmValueRef);
            return null;
        }
        return super.visitStmt(ctx);
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
            val = symbol + val;
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
        LLVMValueRef llvmValueRef = LLVMConstInt(i32Type, Long.parseLong(ctx.getText()), /* signExtend */ 0);
        return llvmValueRef;
    }
}
