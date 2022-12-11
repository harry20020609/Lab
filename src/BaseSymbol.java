
public class BaseSymbol implements Symbol{
    final String name;
    Type type;

    private int lineno = -1;

    private int columnno = -1;

    public BaseSymbol(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    public void setType(Type type){
        this.type = type;
    }

    @Override
    public void errOutput() {
        System.err.println("Error type 3 at Line " +this.lineno + ": Redefined variable: " + this.name + ".");
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
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

    public String toString() {
        return null;
    }
}
