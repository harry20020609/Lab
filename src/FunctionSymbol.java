public class FunctionSymbol extends BaseScope implements Symbol{

    private int lineno = -1;

    private int columnno = -1;

    private FunctionType type;

    @Override
    public FunctionType getType() {
        return type;
    }

    public void setType(FunctionType type){
        this.type = type;
    }

    public void setType(Type type){
        System.out.println("don't use this function");
    }
    public FunctionSymbol(String name, Scope enclosingScope) {
        super(name, enclosingScope);
        this.type = new FunctionType();
    }

    public int getLineno(){
        return lineno;
    }

    public void setLineno(int lineno){
        this.lineno = lineno;
    }

    public int getColumnno(){
        return columnno;
    }

    public void setColumnno(int columnno){
        this.columnno = columnno;
    }

    @Override
    public void errOutput() {
        System.err.println("Error type 4 at Line " + this.lineno + ": Redefined function: " + this.getName() + ".");
    }
}
