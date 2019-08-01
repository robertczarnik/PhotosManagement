package pl.robertczarnik;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class Video {
    private Path dir;
    private int frameTime;
    private String ffmpegDir;
    private Path audio;

    public static final class Builder{
        private Path dir;
        private int frameTime = 1;
        private String ffmpegDir = "D:\\ffmpeg\\bin\\ffmpeg.exe";;
        private String audio = "D:\\SampleAudio";

        public Builder dir(Path dir){
            this.dir = dir;
            return this;
        }

        public Builder frameTime(int frameTime){
            this.frameTime = frameTime;
            return this;
        }

        public Builder ffmpegDir(String ffmpegDir){
            this.ffmpegDir = ffmpegDir;
            return this;
        }

        public Builder audio(String audio){
            this.audio = audio;
            return this;
        }

        public Video build(){
            if(dir == null){
                throw new IllegalStateException("dir cannot be null");
            }

            Video video = new Video();
            video.dir = this.dir;
            video.frameTime = this.frameTime;
            video.ffmpegDir = this.ffmpegDir;
            video.audio = Paths.get(this.audio);
            return video;
        }
    }

    public void onePicFromEachDirVideo() throws IOException {
        //one image from each dir
        System.out.println("getting images");
        List<Path> images = Files.list(dir)
                .filter(p -> Files.isDirectory(p))
                .filter(p -> p.getFileName().toString().contains("#"))
                .filter(p -> !p.getFileName().toString().startsWith("0"))
                .sorted((p1,p2) ->{
                    int l1 = Integer.parseInt(p1.getFileName().toString().substring(0,p1.getFileName().toString().indexOf("#")));
                    int l2 = Integer.parseInt(p2.getFileName().toString().substring(0,p2.getFileName().toString().indexOf("#")));
                    return Integer.compare(l1,l2);
                })
                .map(Video::getRandomImage)
                .filter(p -> Files.isRegularFile(p))
                .collect(Collectors.toList());


        Path tempDir = Files.createTempDirectory(dir,"images");
        int nr=0;


        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        System.out.println("processing images");
        //process all images - using Thread Pool
        for(Path path : images){
            String imageOutStringPath = tempDir.toAbsolutePath()+"\\"+String.format("img-%03d.jpg",nr);
            String command = ffmpegDir + " -y -i \"" +  path.toAbsolutePath() + "\" -vf scale=-1:1080,pad=1920:1080:(ow-iw)/2 " + imageOutStringPath;
            Runnable worker = new Worker(new processImageTask(path,command));
            executor.execute(worker);
            nr++;
        }

        executor.shutdown();
        while (!executor.isTerminated()) { // wait for all task to finish
            Thread.yield();
        }


        System.out.println("making video");
        //build ffmpeg command to make a video from images and execute it
        Process proc = Runtime.getRuntime().exec(ffmpegDir + " -y -framerate 1/" + frameTime + " -i " + "\""+ tempDir.toAbsolutePath().toString() + "\\img-%03d.jpg\" -c:v libx264 -r 30 -pix_fmt yuv420p " + "\"" + dir.toAbsolutePath().toString() + "\\tmpVideo.mp4\"");

        proc.getErrorStream().close();


        try {
            proc.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        System.out.println("adding music");
        // add music
        // if mp3 file add only this file to video
        // if directory concatenate all mp3 and add this to video
        // final video duration is min(videoDuration,audioDuration)
        if(Files.isRegularFile(audio) && (audio.toString().endsWith(".mp3"))) { //one file
            Process lastProc = Runtime.getRuntime().exec(ffmpegDir + " -y -i tmpVideo.mp4 -i \"" + audio.toString() + "\" -c copy -map 0:v:0 -map 1:a:0 -shortest specialVideo.mp4");

            try {
                lastProc.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }else { //dir with music
            Path mergedAudioPath = Files.createTempFile(dir.toAbsolutePath(),"audio",".mp3");

            if(concatenateAudio(audio,mergedAudioPath)){
                Process lastProc = Runtime.getRuntime().exec(ffmpegDir + " -y -i tmpVideo.mp4 -i \"" + mergedAudioPath.toString() + "\" -c copy -map 0:v:0 -map 1:a:0 -shortest specialVideo.mp4");

                try {
                    lastProc.waitFor();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            Files.delete(mergedAudioPath);
        }
        System.out.println("clean up");
        //remove tmp video
        Files.deleteIfExists(dir.toAbsolutePath().resolve("tmpVideo.mp4"));
    }

    private boolean concatenateAudio(Path audioPath, Path mergedAudioPath) throws IOException {
        //get mp3 paths
        List<Path> audioPaths = Files.list(audioPath)
                .filter(p -> p.toString().endsWith(".mp3"))
                .map(Path::toAbsolutePath)
                .collect(Collectors.toList());

        //build command
        StringBuilder command = new StringBuilder();
        command.append(ffmpegDir);
        command.append(" -y -i \"concat:");

        for(Path path : audioPaths) {
            command.append(path.toString()).append("|");
        }

        command.deleteCharAt(command.length()-1);
        command.append("\"");
        command.append(" -acodec copy ");
        command.append(mergedAudioPath.toString());

        //execute
        try {
            Process p = Runtime.getRuntime().exec(command.toString());
            p.waitFor();
        } catch (IOException | InterruptedException e) {
            return false; //cant concatenate
        }
        return true;
    }

    private static Path getRandomImage(Path path){
        try {
            int imageCount = (int)Files.list(path).count();
            if(imageCount==0) return path;

            Random r = new Random();
            List<Path> pathList = Files.list(path).collect(Collectors.toList());

            int index = r.nextInt(imageCount);
            int attempts=0;

            // accept only images
            while(!pathList.get(index).toString().endsWith("jpg") && !pathList.get(index).toString().endsWith("png"))
            {
                index=r.nextInt(imageCount);
                attempts++;

                if(attempts > 10) return path; // 10 attempts to get random image
            }
            return pathList.get(index);

        } catch (IOException e) {
            return path; // return folder, it will be filtred later
        }
    }
}