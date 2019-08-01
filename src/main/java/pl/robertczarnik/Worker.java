package pl.robertczarnik;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifIFD0Directory;
import javaxt.io.Image;

import java.io.File;
import java.io.IOException;

public class Worker implements Runnable {
    private final processImageTask task;

    public Worker(final processImageTask task){
        this.task=task;
    }

    @Override
    public void run() {
        try {
            rotateMyImage(task.getImagePath().toString()); // rotate to proper orientation
            Process p = Runtime.getRuntime().exec(task.getCommand());

            try {
                p.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            //skip image
        }
    }

    //use rotate method and save roatated image
    private static void rotateMyImage(String imageFilename) {
        File imageFile =  new File(imageFilename);
        Image image = new Image(imageFilename);
        int orientation;

        try {
            orientation = readImageInformation(imageFile);
        } catch (IOException | MetadataException | ImageProcessingException e) {
            return; // dont rotate if any exception appears
        }

        rotate(orientation, image);
        image.saveAs(imageFilename);
    }

    //roate image depending on its orientation
    private static void rotate(int orientation, Image image) {

        switch(orientation) {
            case 1:
                return;
            case 2:
                image.flip();
                break;
            case 3:
                image.rotate(180.0D);
                break;
            case 4:
                image.flip();
                image.rotate(180.0D);
                break;
            case 5:
                image.flip();
                image.rotate(270.0D);
                break;
            case 6:
                image.rotate(90.0D);
                break;
            case 7:
                image.flip();
                image.rotate(90.0D);
                break;
            case 8:
                image.rotate(270.0D);
        }

    }

    //if could read metadata return image orienatation, otherwise return default orientation (1)
    private static int readImageInformation(File imageFile)  throws IOException, MetadataException, ImageProcessingException {
        Metadata metadata = ImageMetadataReader.readMetadata(imageFile);
        Directory directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);

        int orientation=1; // default orientation

        if (directory != null) {
            try {
                orientation = directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);
            } catch (MetadataException me) {
                //accept default orienatation
            }
        }
        return orientation;
    }
}
