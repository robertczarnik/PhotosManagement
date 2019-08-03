package pl.robertczarnik;

import java.io.*;

public class Main {
    public static void main(String[] args) throws IOException {
        Controller controller = new Controller();

        if(controller.init()){
            controller.mainLoop();
        }else{
            System.out.println("Error - cant get properties");
        }
    }
}
