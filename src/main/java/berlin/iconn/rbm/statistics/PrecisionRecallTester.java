/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package berlin.iconn.rbm.statistics;

import berlin.iconn.rbm.sorter.ASorter;
import berlin.iconn.rbm.sorter.SorterRBMFeatures;
import berlin.iconn.rbm.image.ImageManager;
import berlin.iconn.rbm.image.ImagePair;
import berlin.iconn.rbm.image.Pic;
import gnu.trove.map.hash.TIntDoubleHashMap;
import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;

/**
 *
 * @author Moritz
 */
public class PrecisionRecallTester {

    private final ASorter sorter;
    private final ImageManager imageManager;
    private final ForkJoinPool pool;
    private float[][] recalls;
    private float[][] precisions;
    private boolean isStarted;
    private int meanAveragePrecision;

    public PrecisionRecallTester(float[][] featureVectors, ImageManager imageManager) {
        this.imageManager = imageManager;
        this.pool = new ForkJoinPool();
        Pic[] images = imageManager.getImages();
        for (int i = 0; i < images.length; i++) {
            images[i].setFeatureVector(featureVectors[i]);
        }
        sorter = new SorterRBMFeatures(images, pool);
    }

    public float test(Pic queryImage, int num, TIntDoubleHashMap lookup) {
        Pic[] allImages = imageManager.getImages();
        ImagePair[] result = new ImagePair[allImages.length];

        // durchlaufe alle Bilder
        for (int i = 0; i < allImages.length; i++) {
            Pic searchImage = allImages[i];
            float distance = readDistanceFor(queryImage.getId(), searchImage.getId(), lookup);
            result[i] = new ImagePair(queryImage, searchImage, distance);
        }

        // sortiere die Suchbilder nach der Distance zum Query Bild
        Arrays.sort(result);

        // berechne die eigentliche Average Precision
        return analyse(result, num);
    }

    public PrecisionRecallTestResult testAll() {
        final TIntDoubleHashMap lookup = createDistanceLookupTable();

        Pic[] images = imageManager.getImages();

        // starte die komplexe Analyse 
        start(images.length);

        // Java 8: ohne Lambda Expression
        // paralle Berechnungen
        float[] averagePrecisions = new float[images.length];
        ForkTest ft = new ForkTest(this, lookup, images, 0, images.length, averagePrecisions);
        pool.invoke(ft);

        float mAP = 0;
        for (int i = 0; i < averagePrecisions.length; i++) {
            mAP += averagePrecisions[i];
        }
        mAP /= images.length;

        return new PrecisionRecallTestResult(mAP, sorter.getName(), "all", generatePRTable());
    }

    /**
     * Ermittle die Mean Average Precision f������r alle angegebenen Bilder
     *
     * @param imageGroup
     * @param queryImages
     * @return
     */
    public PrecisionRecallTestResult test(String imageGroup) {
        Pic[] queryImages = imageManager.getImageInGroup(imageGroup).toArray(new Pic[0]);

        // starte die komplexe Analyse 
        start(queryImages.length);

        // berechne die Average Precision f������r jedes Bild aus
        float mAP = 0;
        for (int i = 0; i < queryImages.length; i++) {
            mAP += test(queryImages[i], i);
        }
        mAP /= queryImages.length;

        return new PrecisionRecallTestResult(mAP, sorter.getName(), imageGroup, generatePRTable());
    }

    public float test(Pic queryImage) {
        return test(queryImage, 0);
    }

    /**
     *
     *
     * @param queryImage
     * @param table
     * @return
     */
    private float test(Pic queryImage, int num) {

        // sortiere alle Bilder nach der ������������������hnlichkeit zum Querybild 
        ImagePair[] result = sortBySimilarity(queryImage);

        // berechne die eigentliche Average Precision
        return analyse(result, num);
    }

    public void start(int numTestRuns) {
        this.recalls = new float[numTestRuns][];
        this.precisions = new float[numTestRuns][];
        this.isStarted = true;
        this.meanAveragePrecision = 0;
    }

    private float[][] generatePRTable() {
        int numTestRun = precisions.length;
        int numPics = precisions[0].length;

        float[][] prTable = new float[4][numPics];
        for (int pic = 0; pic < numPics; pic++) {

            // z��hle alle Testergebnisse zusammen
            float avgPrecision = 0, avgRecall = 0;
            for (int runs = 0; runs < numTestRun; runs++) {
                avgPrecision += precisions[runs][pic];
                avgRecall += recalls[runs][pic];
            }

            prTable[2][pic] = (float) avgPrecision / numTestRun;
            prTable[3][pic] = (float) avgRecall / numTestRun;
        }
        return prTable;
    }

    /**
     * Liefert eine Liste aller Bilder sorter nach deren ������hnlichkeit zum
     * Querybild
     *
     * @param queryImage
     * @return
     */
    public ImagePair[] sortBySimilarity(Pic queryImage) {
        Pic[] allImages = imageManager.getImages();
        ImagePair[] result = new ImagePair[allImages.length];

        // durchlaufe alle Bilder
        for (int i = 0; i < allImages.length; i++) {
            Pic searchImage = allImages[i];
            float distance = sorter.getDistance(queryImage.getFeatureVector(), searchImage.getFeatureVector());
            result[i] = new ImagePair(queryImage, searchImage, distance);
        }

        // sortiere die Suchbilder nach der Distance zum Query Bild
        Arrays.sort(result);

        return result;
    }

    /**
     * besorgt aus der Lookup Tabelle die Distanz f������r die beiden Ids
     *
     * @param id1
     * @param id2
     * @param lookup
     * @return
     */
    private static float readDistanceFor(int id1, int id2, TIntDoubleHashMap lookup) {
        if (id1 == id2) {
            return 0;
        }
        if (id1 < id2) {
            return (float)lookup.get((id1 << 16) | id2);
        }
        return (float)lookup.get((id2 << 16) | id1);
    }

    /**
     * Analysiert und liefert die Average Precision zur��ck
     *
     * @param sortedArray
     * @return
     */
    public float analyse(ImagePair[] sortedArray, int num) {
        return (true) ? complexAnalysis(sortedArray, num) : calcMeanAveragePrecision(sortedArray);
    }

    private float complexAnalysis(ImagePair[] sortedArray, int num) {

        // existiert schon der passende Array
        if (isStarted == false) {
            System.out.println("Need to start the Complex Analysis first.");
            return 0;
        }

        // wurde die Anzahl an Testruns noch nicht ��berschritten
        if (num > precisions.length) {
            System.out.println("Reached the maximum amount of analysis for this table.");
            return 0;
        }

        int numPics = sortedArray.length;
        float[] precision = new float[numPics];
        float[] recall = new float[numPics];

        float precisionSum = 0;
        int currRelevantRetrieved = 0;

        for (int pos = 0; pos < sortedArray.length; pos++) {
            ImagePair imagePair = sortedArray[pos];
            if (imagePair.isSameCategory()) {
                currRelevantRetrieved++;
                precisionSum += (float) currRelevantRetrieved / (pos + 1);
            }

            precision[pos] = (float) currRelevantRetrieved / (pos + 1);
            recall[pos] = (float) currRelevantRetrieved / imagePair.getQueryImage().getTypeOccurrence();
        }

        // speichere in globalen Array
        precisions[num] = precision;
        recalls[num] = recall;
        meanAveragePrecision += precisionSum / currRelevantRetrieved;

        return precisionSum / currRelevantRetrieved;
    }

    public static float calcMeanAveragePrecision(ImagePair[] sortedArray) {
        float precisionSum = 0;
        int currRelevantRetrieved = 0;

        for (int pos = 0; pos < sortedArray.length; pos++) {
            ImagePair imagePair = sortedArray[pos];

            if (imagePair.isSameCategory()) {
                currRelevantRetrieved++;
                precisionSum += (float) currRelevantRetrieved / (pos + 1);
            }
        }

        return precisionSum / currRelevantRetrieved;
    }

    /**
     * Erzeuge eine Lookup Tabelle f������r alle Distanzen. So das diese nur einmal
     * berechnet werden m������ssen.
     *
     * @return
     */
    private TIntDoubleHashMap createDistanceLookupTable() {
        Pic[] allImages = imageManager.getImages();
        int initialCapacity = (allImages.length - 1) * allImages.length / 2;
        TIntDoubleHashMap map = new TIntDoubleHashMap(initialCapacity);

        for (int i = 0; i < allImages.length - 1; i++) {
            Pic p1 = allImages[i];
            for (int j = i + 1; j < allImages.length; j++) {
                Pic p2 = allImages[j];
                int key = (p1.getId() << 16) | p2.getId();
                float dist = sorter.getDistance(p1.getFeatureVector(), p2.getFeatureVector());
                map.put(key, dist);
            }
        }

        return map;
    }
}
