package pl.robertczarnik;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import com.drew.imaging.*;
import com.drew.metadata.*;
import com.drew.metadata.exif.ExifSubIFDDirectory;


public final class PhotosManagement {

    private PhotosManagement() { }


    public static void moveFiliesToProperDirectiories(Path dir) throws IOException {
        //move files to directories
        Files.list(dir)
                .parallel()
                .filter(p -> Files.isRegularFile(p))
                .forEach(PhotosManagement::moveToProperDirectory);
    }

    public static void renameDirectories(Path dir) throws IOException {
        //number of existing directories with phothos
        long nr = Files.list(dir)
                .filter(p -> Files.isDirectory(p))
                .filter(p -> p.getFileName().toString().matches("\\d+# \\d{2}.\\d{2}.\\d{2}")) //e.g. 11# 21.05.19
                .filter(p -> !p.getFileName().toString().startsWith("0"))
                .count();

        //list of directories to rename
        List<Path> listOfPaths = Files.list(dir)
                .filter(p -> Files.isDirectory(p))
                .filter(p -> p.getFileName().toString().matches("\\d{8}")) //e.g. 20190620
                .sorted()
                .collect(Collectors.toList());


        //rename paths
        for(Path dirPath : listOfPaths){
            nr++;
            String dirName = dirPath.getFileName().toString();

            //rename to 'nr# dd.mm.yy' pattern
            Files.move(dirPath, Paths.get(dirPath.getParent().toString(),nr+"# " + dirName.substring(6) +"."+ dirName.substring(4,6) +"."+ dirName.substring(2,4)));
        }
    }

    public static void updateSerialNumbers(Path dir) throws IOException {

        List<Path> directories = Files.list(dir)
                .filter(p -> p.getFileName().toString().matches("\\d+# \\d{2}.\\d{2}.\\d{2}")) //e.g. 11# 21.05.19
                .filter(p -> !p.getFileName().toString().startsWith("0"))
                .sorted((p1,p2) ->{
                    String s1 = p1.getFileName().toString().substring(p1.getFileName().toString().indexOf(" ")+1);
                    String s2 = p2.getFileName().toString().substring(p2.getFileName().toString().indexOf(" ")+1);
                    LocalDate date1 = LocalDate.parse(s1,DateTimeFormatter.ofPattern("dd.MM.yy"));
                    LocalDate date2 = LocalDate.parse(s2,DateTimeFormatter.ofPattern("dd.MM.yy"));

                    return date1.compareTo(date2); //sort by dates
                })
                .collect(Collectors.toList());


        int nr=1;
        for(Path path : directories){
            String changedFileName = path.getFileName().toString();
            int hashPos = changedFileName.indexOf("#");
            changedFileName = changedFileName.substring(hashPos+1);
            changedFileName = nr + "#" + changedFileName;


            Files.move(path.toAbsolutePath(),path.toAbsolutePath().resolveSibling(changedFileName));
            nr++;
        }
    }

    public static void makeVideo(String[] tab,Path workingDir,String ffmpegDir,String audioDir) throws IOException {
        if(tab.length==4) {
            Video video = new Video.Builder()
                    .dir(workingDir)
                    .frameTime(Integer.parseInt(tab[1]))
                    .audio(tab[2])
                    .ffmpegDir(tab[3])
                    .build();

            video.onePicFromEachDirVideo();
        }
        else if(tab.length==3){
            Video video = new Video.Builder()
                    .dir(workingDir)
                    .frameTime(Integer.parseInt(tab[1]))
                    .audio(tab[2])
                    .ffmpegDir(ffmpegDir)
                    .build();

            video.onePicFromEachDirVideo();
        }
        else if(tab.length==2){
            Video video = new Video.Builder()
                    .dir(workingDir)
                    .frameTime(Integer.parseInt(tab[1]))
                    .audio(audioDir)
                    .ffmpegDir(ffmpegDir)
                    .build();

            video.onePicFromEachDirVideo();
        }else if(tab.length==1){
            Video video = new Video.Builder()
                    .dir(workingDir)
                    .frameTime(1)
                    .audio(audioDir)
                    .ffmpegDir(ffmpegDir)
                    .build();

            video.onePicFromEachDirVideo();
        }
    }

    private static void moveToProperDirectory(Path path){
        Metadata metadata;

        try {
            metadata = ImageMetadataReader.readMetadata(path.toFile());
            ExifSubIFDDirectory directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
            Date date = null;


            if(directory != null) { // get date from metadadata
                date = directory.getDateOriginal(TimeZone.getDefault());
            } else if(path.getFileName().toString().matches("\\d{8}.*")){ //get date from image name
                String s1 = path.getFileName().toString().substring(0,8);

                try {
                    date = new SimpleDateFormat("yyyyMMdd").parse(s1);
                } catch (ParseException e) {
                    //date = null
                }
            }

            if(date != null){
                ZonedDateTime zonedDate = date.toInstant().atZone(TimeZone.getDefault().toZoneId()); //convert to ZonedDateTime

                //photos till 5am belongs to previous day
                if(zonedDate.getHour()<5)
                    zonedDate.minusDays(1);

                String dirName = ""+zonedDate.getYear() + String.format("%02d", zonedDate.getMonthValue()) + String.format("%02d",zonedDate.getDayOfMonth());
                Path dirPath = path.getParent().resolve(dirName);

                if(!Files.exists(dirPath)) Files.createDirectory(dirPath); //it is an atomic operation

                Files.move(
                        path,
                        dirPath.resolve(path.getFileName()),
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING
                );
            }
        } catch (ImageProcessingException | IOException e) {
            //skip file if any error occur
        }
    }
}
