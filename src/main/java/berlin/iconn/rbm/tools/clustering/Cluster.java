package berlin.iconn.rbm.tools.clustering;

import java.util.LinkedList;
import java.util.List;

public class Cluster {
       
    private final String label;
    private List<float[]> data;
    private float[] center;
    
    public Cluster(String label){
        this.data = new LinkedList<float[]>();
        this.label = label;
    }
    
    public void init(){
        this.center = center();
    }
    
    public void reset(){
        this.data = new LinkedList<float[]>();
    }
    
    private float[] center(){
        if(data.isEmpty()) return null;
        
        int size = data.size();
        int len = data.get(0).length;
        
        float[] result = new float[len];
        
        for(float[] v : data){
            for(int i = 0; i < v.length; ++i){
                result[i] += v[i];
            }
        }
        
        for(int i = 0; i < len; ++i){
            result[i] /= size;
        }
        return result;
    }
    
    public void addVector(float[] v){
        this.data.add(v);
    }
    
    public List<float[]> getData(){
        return data;
    }
    
    public String getLabel(){
        return label;
    }
    
    public float[] getCenter(){
        return center;
    }
    
    public float getDistanceToCenter(float[] v){
        float sum = 0f;
        for(int i = 0; i < center.length; ++i){
            sum += (center[i] - v[i]) * (center[i] - v[i]);
        }
        return (float) Math.sqrt(sum);
    }

}
