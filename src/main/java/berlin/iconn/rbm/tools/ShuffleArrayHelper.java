/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package berlin.iconn.rbm.tools;

import java.util.Random;

/**
 *
 * @author Radek
 */
public class ShuffleArrayHelper<T> {

    public void shuffleArray(T[] a) {
        int n = a.length;
        Random random = new Random();
        random.nextInt();
        for (int i = 0; i < n; i++) {
            int change = i + random.nextInt(n - i);
            swap(a, i, change);
        }
    }

    private void swap(T[] a, int i, int change) {
        T helper = a[i];
        a[i] = a[change];
        a[change] = helper;
    }
}
