package berlin.iconn.rbm.image;

import berlin.iconn.rbm.main.BenchmarkModel;
import berlin.iconn.rbm.tools.ShuffleArrayHelper;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;

public class ImageManager {

    private static String startDirectory = "CBIR_Project/images";

    private Pic[] images;
    private File imageDirectory;
    private HashMap<String, List<Pic>> imageGroups;

	private BenchmarkModel benchmarkModel;

    public ImageManager(File imageDirectory, BenchmarkModel benchmarkModel) {
        this.benchmarkModel = benchmarkModel;
    	this.imageDirectory = imageDirectory;
        this.imageGroups = new HashMap<String, List<Pic>>();
        this.loadImages(imageDirectory);
    }
    
    public ImageManager(File imageDirectory) {
    	this.imageDirectory = imageDirectory;
    	this.loadImages(imageDirectory);
    }

    public int getImageCount() {
        return images.length;
    }
    
    public Pic[] getImages() {
    	return this.images;
    }

    public Set<String> getGroupNames() {
        return imageGroups.keySet();
    }

    public List<Pic> getImageInGroup(String groupName) {
        return imageGroups.get(groupName);
    }

    /**
     * Lade alles Bilder in ihrer verkleinerten Version von der Festplatte
     *
     * @param imageDirectory
     */
    public void loadImages(final File imageDirectory) {
        this.imageDirectory = imageDirectory;

        // besorge alle gültige Bilddateien aus dem Verzeichnis
        final File[] imageFiles = imageDirectory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return (name.endsWith("jpg") || name.endsWith("png") || name.endsWith("gif"));
            }
        });

        // erlaube nur ca. 65536 Bilder
        final int maxImages = 65536;
        if (imageFiles.length > maxImages) {
            System.out.println("Too many images, restricting to " + maxImages);
        }
        final int imageCount = Math.min(maxImages, imageFiles.length);

        // lade alle Bilder
        List<Pic> list = new ArrayList<Pic>();
        for (int i = 0; i < imageCount; i++) {
            Pic image = loadImage(imageFiles[i]);
            if (image != null) {
                image.setId(i);
                list.add(image);
            }
        }

        // packe die Bilder in ihre Endgültige Form
        this.images = new Pic[list.size()];
        for (int i = 0; i < list.size(); i++) {
            addImage(list.get(i), i);
        }

        // wieviele Bilder gibt es pro Gruppe
        for (List<Pic> groupImages : imageGroups.values()) {
            for (Pic pic : groupImages) {
                pic.setTypeOccurrence(groupImages.size());
            }
        }
        
        if (!this.benchmarkModel.isSorted()) {
            Pic[] picsShuffled = new Pic[this.images.length];
            System.arraycopy(this.images, 0, picsShuffled, 0, this.images.length);
            ShuffleArrayHelper<Pic> picsShuffler = new ShuffleArrayHelper<>();
            picsShuffler.shuffleArray(picsShuffled);
            
            images = picsShuffled;
        }
        
        int index = 0;
        for(Pic pic : images) {
        	pic.setRank(index);
        	index++;
        }
    }
    
    private void addImage(Pic image, int index) {

        // das Bild wird zur allgemeinen Liste hinzugefügt
        images[index] = image;

        // und in eine Gruppenliste einsortiert
        String key = image.getCategory();
        if (imageGroups.containsKey(key)) {
            imageGroups.get(key).add(image);
        } else {
            List<Pic> imageList = new ArrayList<Pic>();
            imageList.add(image);
            imageGroups.put(key, imageList);
        }
    }

    public Pic get(int index) {
        if (index < 0 || index > images.length - 1) {
            return null;
        }

        return images[index];
    }

    /**
     * Lade das Bild von der Festplatte und verkleinere es
     *
     * @param imageFile
     * @return
     */
    private Pic loadImage(File imageFile) {
    	
    	int edgeLength = this.benchmarkModel.getImageEdgeSize();
    	boolean binarize = this.benchmarkModel.isBinarizeImages();
    	boolean invert = this.benchmarkModel.isInvertImages();
    	float minData = this.benchmarkModel.getMinData();
    	float maxData = this.benchmarkModel.getMaxData();
    	boolean isRgb = this.benchmarkModel.isRgb();
    	
        float[] imageData = null;
        try {
        	imageData = DataConverter.processPixelData(ImageIO.read(imageFile), edgeLength, binarize, invert, minData, maxData, isRgb);
        } catch (Exception e) {
            System.out.println("Could not load: " + imageFile.getAbsolutePath());
            e.printStackTrace();
            return null;
        }

        int iw = edgeLength;
        int ih = edgeLength;

        int maxOrigImgSize = Math.max(iw, ih);

        // maximale Kantenlänge
        float thumbSize = 128;

        //skalierungsfaktor bestimmen:
        float scale = (maxOrigImgSize > thumbSize) ? thumbSize / maxOrigImgSize : 1;

        int nw = (int) (iw * scale);
        int nh = (int) (ih * scale);

        String filename = imageFile.getName().toLowerCase();
        Pic currPic = new Pic();
        currPic.setName(filename);
        currPic.setType(filename.split("[_]")[0]);
        currPic.setOrigWidth(iw);
        currPic.setOrigHeight(ih);
        currPic.setData(imageData);
        
        BufferedImage image = DataConverter.pixelDataToImage(imageData, minData, isRgb);
        
        // Bild verkleinern
        BufferedImage currBi = new BufferedImage(nw, nh, BufferedImage.TYPE_INT_ARGB);
        Graphics2D big = currBi.createGraphics();
        big.drawImage(image, 0, 0, nw, nh, null);
        currPic.setDisplayImage(currBi);

        return currPic;
    }

    public String getImageSetName() {
        return imageDirectory.toPath().getFileName().toString();
    }

    public String getNameFromIndex(int index) {
        LinkedList<String> groupList = new LinkedList<>(getGroupNames());
        groupList.add(0, "All");
        return groupList.get(index);
    }

	public float[][] getImageData() {
		float[][] data = new float[this.images.length][];
		for(int i = 0; i < this.images.length; i++) {
			data[i] = this.images[i].getData();
		}
		return data;
	}

	public void applyChanges(BenchmarkModel benchmarkModel) {
		this.benchmarkModel = benchmarkModel;
		loadImages(this.imageDirectory);
	}
}
