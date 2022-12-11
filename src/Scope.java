import java.util.Map;
public interface Scope {
    public String getName();

    public void setName(String name);

    public Scope getEnclosingScope();

    public Map<String, Symbol> getSymbols();

    public void define(Symbol symbol);

    public Symbol resolve(String name);

    public void checkVariable(String varName, int line);

    public void checkFunction(String funcName, int line);

    public boolean checkDirty();

    public void setListener(SymbolTableListener listener);

    public SymbolTableListener getListener();
}
