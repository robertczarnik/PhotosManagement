// do sortowania zdjec dodac mozliwosc okreslenia godzin od do jak je klasyfikowac
// zaimplementowac refres/[-h
// zrobic plik config w ktorym beda przechowywane sciezki, ffmpeg dir, default working dir etc

package pl.robertczarnik;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {
        Path workingDir;
        String ffmpegDir="D:\\ffmpeg\\bin\\ffmpeg.exe";
        String audioDir="D:\\SampleAudio";
        String prompt = "=> ";


        switch(args.length){
            case 0:
                workingDir = Paths.get("").toAbsolutePath();
                break;
            case 1:
                workingDir = Paths.get(args[0]).toAbsolutePath();
                break;
            default:
                System.out.println("Wrong number of arguments");
                return;
        }

        System.out.println("Actual ffmpeg directory:  " + ffmpegDir);
        System.out.println("Actual working directory: " + workingDir);
        System.out.println("Type \"help\" to get more informations");

        Scanner in = new Scanner(System.in);

        while(true){
            System.out.print(prompt);
            String[] tab = in.nextLine().split(" ");

            if(tab.length==0) continue; //if only spaces was entered

            String opt = tab[0];

            if(opt.equals("help")){
                System.out.println("move - move images to proper directories");
                System.out.println("rename - rename directories to \"serialNumber# dd.mm.yy\" pattern");
                System.out.println("update - actualization of serial numbers in directories names");
                System.out.println("video frameTime audioPath ffmpegDir - make video from one image from each folder");
                System.out.println("config - show configuration");
                System.out.println("exit - exit this application");
            }
            else if(opt.equals("move")){
                PhotosManagement.moveFiliesToProperDirectiories(workingDir);
                System.out.println("Done!");
            }
            else if(opt.equals("rename")){
                PhotosManagement.renameDirectories(workingDir);
                System.out.println("Done!");
            }
            else if(opt.equals("update")){
                PhotosManagement.updateSerialNumbers(workingDir);
                System.out.println("Done!");
            }
            else if(opt.equals("video")){
                PhotosManagement.makeVideo(tab,workingDir,ffmpegDir,audioDir);
                System.out.println("Done!");
            }
            else if(opt.equals("config")) {
                System.out.println("Actual ffmpeg directory:  " + ffmpegDir);
                System.out.println("Actual working directory: " + workingDir);
            }
            else if(opt.equals("")){
                // skip enters
            }
            else if(opt.equals("exit")){
                break;
            }
            else{
                System.out.println("command not recognized, type \"help\" to get possible commands");
            }

        }
    }
}
