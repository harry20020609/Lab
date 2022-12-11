public class ArrayType implements Type{
    Type element;
    int dimension;
    public ArrayType(Type element) {
        this.element = element;
    }

    public int getDimension() {
        return dimension;
    }

    public void setDimension(int dimension) {
        this.dimension = dimension;
    }
}
