import java.util.ArrayList;

public class FunctionType implements Type{
    Type retType;
    ArrayList<Type> paramsType;

    public FunctionType(){
        paramsType = new ArrayList<Type>();
    }

    public void setRetType(Type retType) {
        this.retType = retType;
    }

    public Type getRetType() {
        return retType;
    }

    public void addParamsType(Type type){
        paramsType.add(type);
    }

    public ArrayList<Type> getParamsType(){
        return paramsType;
    }
}
