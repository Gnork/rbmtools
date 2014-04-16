package berlin.iconn.rbm.tools.clustering;

import java.util.Arrays;

public class LabeledData {
    private final float[] data;
    private final String label;
    
    public LabeledData(float[] data, String label){
        this.data = data;
        this.label = label;
    }
    
    public float[] getData(){
        return data;
    }
    
    public String getLabel(){
        return label;
    }
    
    @Override
    public boolean equals(Object other)
    {
        if (!(other instanceof LabeledData))
        {
            return false;
        }
        
        LabeledData otherDataSet = (LabeledData)other;
        return (Arrays.equals(data, otherDataSet.getData()) && this.label.equals(otherDataSet.getLabel()));
    }

    @Override
    public int hashCode()
    {
        return Arrays.hashCode(data);
    }
}