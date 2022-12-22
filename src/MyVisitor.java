import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.llvm.LLVM.*;
import static org.bytedeco.llvm.global.LLVM.*;



public class MyVisitor extends SysYParserBaseVisitor<LLVMValueRef> {
    public static final BytePointer error = new BytePointer();
    @Override
    public LLVMValueRef visitProgram(SysYParser.ProgramContext ctx) {
        //初始化LLVM
        LLVMInitializeCore(LLVMGetGlobalPassRegistry());
        LLVMLinkInMCJIT();
        LLVMInitializeNativeAsmPrinter();
        LLVMInitializeNativeAsmParser();
        LLVMInitializeNativeTarget();
        //创建module
        LLVMModuleRef module = LLVMModuleCreateWithName("moudle");
        //初始化IRBuilder，后续将使用这个builder去生成LLVM IR
        LLVMBuilderRef builder = LLVMCreateBuilder();
        //考虑到我们的语言中仅存在int一个基本类型，可以通过下面的语句为LLVM的int型重命名方便以后使用
        LLVMTypeRef i32Type = LLVMInt32Type();
        super.visitProgram(ctx);
        LLVMPrintModuleToFile(module,"test.ll",error);
        return null;
    }

    @Override
    public LLVMValueRef visitStmt(SysYParser.StmtContext ctx) {
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
        LLVMModule
        return ;
    }

    @Override
    public LLVMValueRef visitPlusExp(SysYParser.PlusExpContext ctx) {
        return super.visitPlusExp(ctx);
    }

    @Override
    public LLVMValueRef visitNumberExp(SysYParser.NumberExpContext ctx) {
        return super.visitNumberExp(ctx);
    }

    @Override
    public LLVMValueRef visitNumber(SysYParser.NumberContext ctx) {
        return super.visitNumber(ctx);
    }
}
