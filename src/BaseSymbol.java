import java.util.ArrayList;

public class BaseSymbol implements Symbol{
    final String name;
    Type type;

    private ArrayList<Integer> lineno = new ArrayList<>();

    private ArrayList<Integer> columnno = new ArrayList<>();

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

    public String toString() {
        return null;
    }
}
