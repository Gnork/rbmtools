package berlin.iconn.rbm.tools.clustering;

public class DataSet {
    private final float[] data;
    private final String label;
    
    public DataSet(float[] data, String label){
        this.data = data;
        this.label = label;
    }
    
    public float[] getData(){
        return data;
    }
    
    public String getLabel(){
        return label;
    }
}