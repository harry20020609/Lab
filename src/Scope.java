import org.bytedeco.llvm.LLVM.*;
import static org.bytedeco.llvm.global.LLVM.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class Scope{
    private Scope enclosingScope = null;
    public final Map<String, LLVMValueRef> table = new LinkedHashMap<>();
    private String name;

    public Scope(String name, Scope enclosingScope) {
        this.name = name;
        this.enclosingScope = enclosingScope;
    }

    public String getName() {
        return this.name;
    }

    public Scope getEnclosingScope() {
        return this.enclosingScope;
    }

    public void define(LLVMValueRef valueRef) {
        table.put(LLVMGetValueName(valueRef).getString(), valueRef);
    }

    public LLVMValueRef resolve(String name) {
        LLVMValueRef valueRef = table.get(name);
        if (valueRef != null) {
            return valueRef;
        }
        if (enclosingScope != null) {
            return enclosingScope.resolve(name);
        }
        return null;
    }

    public String toString() {
        return null;
    }
}