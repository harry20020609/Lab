import java.util.LinkedHashMap;
import java.util.Map;

public class BaseScope implements Scope{
    private final Scope enclosingScope;
    public final Map<String, Symbol> symbols = new LinkedHashMap<>();
    private String name;

    @Override
    public void setListener(SymbolTableListener listener) {
        this.listener = listener;
    }

    @Override
    public SymbolTableListener getListener() {
        return listener;
    }

    public BaseScope(String name, Scope enclosingScope) {
        this.name = name;
        this.enclosingScope = enclosingScope;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Scope getEnclosingScope() {
        return this.enclosingScope;
    }

    public Map<String, Symbol> getSymbols() {
        return this.symbols;
    }

    public boolean dirty = false;

    public SymbolTableListener listener;

    public boolean checkDirty(){
        return dirty;
    }

    public void define(Symbol symbol) {
        symbols.put(symbol.getName(), symbol);
    }

    public void checkVariable(String name, int lineno){
        if(!symbols.containsKey(name)){
            if(enclosingScope==null){
                System.err.println("Error type 1 at Line "+lineno+": Undefined variable: "+name+".");
                listener.fault = true;
                listener.redefine = true;
                return ;
            }
            enclosingScope.checkVariable(name,lineno);
        }
    }

    public void checkFunction(String name, int lineno){
        if(!symbols.containsKey(name)){
            if(enclosingScope==null){
                System.err.println("Error type 2 at Line "+lineno+": Undefined function: "+name+".");
                listener.fault = true;
                listener.redefine = true;
                return ;
            }
            enclosingScope.checkFunction(name,lineno);
        }
    }

    public Symbol resolve(String name) {
        Symbol symbol = symbols.get(name);
        if (symbol != null) {
            return symbol;
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
