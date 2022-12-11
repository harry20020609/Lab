import java.util.ArrayList;

public interface Symbol {
    public String getName();

    public Type getType();

    public void errOutput();

    public void setType(Type type);

    public void addLineno(int lineno);

    public void addColumnno(int columnno);

    public int getLineno(int index);

    public int getColumnno(int index);

    public ArrayList<Integer> getLineno();

    public ArrayList<Integer> getColumnno();

}
