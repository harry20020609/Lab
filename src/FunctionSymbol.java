import java.util.ArrayList;

public class FunctionSymbol extends BaseScope implements Symbol{

    private ArrayList<Integer> lineno = new ArrayList<>();

    private ArrayList<Integer> columnno = new ArrayList<>();

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

    public ArrayList<Integer> getLineno(){
        return lineno;
    }

    public int getLineno(int index){
        return lineno.get(index);
    }

    public void addLineno(int lineno){
        this.lineno.add(lineno);
    }

    public ArrayList<Integer> getColumnno(){
        return columnno;
    }

    public int getColumnno(int index){
        return columnno.get(index);
    }

    public void addColumnno(int columnno){
        this.columnno.add(columnno);
    }

    @Override
    public void errOutput() {
        System.err.println("Error type 4 at Line " + this.lineno + ": Redefined function: " + this.getName() + ".");
    }
}
