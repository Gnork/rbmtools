/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package berlin.iconn.rbm.tools;

/**
 *
 * @author Moritz
 */
public class KeyValue<K, V> {
    private K key;
    private V value;
    
    public KeyValue(K key, V value) {
        this.key = key;
        this.value = value;
    }
    
    public V get(K key) {
        if(key.equals(this.key)) {
            return value;
        }
        return null;
    }
}
