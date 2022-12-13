public class ArrayType implements Type{
    Type element;
    int dimension;

    int accessDim;
    public ArrayType(Type element) {
        this.element = element;
    }

    public int getDimension() {
        return dimension;
    }

    public void setDimension(int dimension) {
        this.dimension = dimension;
    }

    public int getAccessDim() {
        return accessDim;
    }

    public void setAccessDim(int accessDim) {
        this.accessDim = accessDim;
    }
}
