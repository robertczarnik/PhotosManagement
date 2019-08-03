package pl.robertczarnik;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.Scanner;

public class Controller {
    private String workingDir = Paths.get("").toAbsolutePath().toString();
    private String ffmpegPath;
    private String audioPath;
    private String frameTime;
    private String configPath;

    public Controller(){}

    // read properties from config file
    public boolean init() throws IOException {

        // get path to this class (path to jar )
        String path = Controller.class.getProtectionDomain().getCodeSource().getLocation().getPath();

        // transform to Windows path
        path = path.substring(1);
        path=path.replace("/","\\");


        configPath = Paths.get(path).getParent().toAbsolutePath().resolve("config.properties").toString();

        // create config file if doesnt exist and fill with default properties
        if(!Files.exists(Paths.get(configPath))){
            Files.createFile(Paths.get(configPath));

            try (OutputStream output = new FileOutputStream(configPath)) {
                Properties prop = new Properties();

                // default properties
                prop.setProperty("ffmpegPath","D:\\ffmpeg\\bin\\ffmpeg.exe");
                prop.setProperty("audioPath","D:\\SampleAudio");
                prop.setProperty("frameTime","1");

                // save properties
                prop.store(output, null);
                prop.clear();
            }
        }

        try (InputStream input = new FileInputStream(configPath)) {
            Properties prop = new Properties();

            // load a properties file
            prop.load(input);

            // get the property value
            ffmpegPath = prop.getProperty("ffmpegPath");
            audioPath = prop.getProperty("audioPath");
            frameTime = prop.getProperty("frameTime");

            showConfig();
            System.out.println("Type \"help\" to get more informations");
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }


        return true;
    }

    public void mainLoop() throws IOException {
        Scanner in = new Scanner(System.in);
        String prompt = "=> ";
        boolean exit = false;

        while (true) {
            System.out.print(prompt);
            String[] tab = in.nextLine().split(" ");

            if (tab.length == 0) continue; //if only spaces was entered

            String opt = tab[0];

            switch (opt) {
                case "help":
                    showHelp();
                    break;
                case "move":
                    PhotosManagement.moveFiliesToProperDirectiories(Paths.get(workingDir));
                    System.out.println("Done!");
                    break;
                case "rename":
                    PhotosManagement.renameDirectories(Paths.get(workingDir));
                    System.out.println("Done!");
                    break;
                case "update":
                    PhotosManagement.updateSerialNumbers(Paths.get(workingDir));
                    System.out.println("Done!");
                    break;
                case "video":
                    PhotosManagement.makeVideo(tab, Paths.get(workingDir), ffmpegPath, audioPath, Integer.parseInt(frameTime));
                    System.out.println("Done!");
                    break;
                case "config":
                    showConfig();
                    break;
                case "changeProp":
                    changeProp(tab);
                    break;
                case "changeDir":
                    changeDir(tab);
                    break;
                case "":
                    // skip enters
                    break;
                case "exit":
                    exit=true;
                    break;
                default:
                    System.out.println("command not recognized, type \"help\" to get possible commands");
            }

            if(exit) break;
        }
    }

    private void changeProp(String [] tab) throws IOException {
        if(tab.length==3){
            try (OutputStream output = new FileOutputStream(configPath)) {
                Properties prop = new Properties();

                // rewirte all properties
                prop.setProperty("ffmpegPath",ffmpegPath);
                prop.setProperty("audioPath",audioPath);
                prop.setProperty("frameTime",frameTime);

                // set the property value
                prop.setProperty(tab[1], tab[2]);

                // save properties
                prop.store(output, null);
                prop.clear();
            }

            if(tab[1].equals("frameTime")) frameTime=tab[2];
        }
        else{
            System.out.println("wrong number of parameters");
        }
    }

    private void changeDir(String [] tab){
        if(tab.length==2){
            workingDir=tab[1];
        }
        else{
            System.out.println("wrong number of parameters");
        }
    }

    private void showConfig(){
        System.out.println("Actual ffmpeg directory:  " + ffmpegPath);
        System.out.println("Actual audio directory:   " + audioPath);
        System.out.println("Actual working directory: " + workingDir);
        System.out.println("Actual frameTime:         " + frameTime);
    }

    private void showHelp(){
        System.out.println("move - move images to proper directories");
        System.out.println("rename - rename directories to \"serialNumber# dd.mm.yy\" pattern");
        System.out.println("update - actualization of serial numbers in directories names");
        System.out.println("video frameTime audioPath ffmpegPath - make video from one image from each folder");
        System.out.println("config - show configuration");
        System.out.println("changeProp propertyName newValue - set property, propertyName=audioPath|ffmpegPath|frameTime)");
        System.out.println("changeDir dir - change working dir");
        System.out.println("exit - exit this application");
    }
}
