import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;


public class Parser {

    private int[][] beats;
    public int length;
    private Scanner input;
    private volatile ArrayList<Integer> timeStamps;
    private int offset;
    private int runTime;
    private HashMap<String, File> files;
    private String[] fileNames;



    public Parser(int startOffset){
        this.offset = startOffset;
        this.timeStamps = new ArrayList<>();
        this.files = new HashMap<>();
        this.scanFiles();
    }

    private void scanFiles(){
        File dir = new File(HeartWise.COURSE_DIRECTORY);
        File[] dirFiles = dir.listFiles();
        if (dirFiles != null) {
            this.fileNames = new String[dirFiles.length];
            int i = 0;
            for (File f : dirFiles){
                String name = f.getName().replaceFirst("[.][^.]+$", "");
                this.files.put(name, f);
                this.fileNames[i] = name;
                i++;
            }
        }
    }

    public void selectFile(String fileName){
        try {
            this.input = new Scanner(this.files.get(fileName));
            this.parse();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    private void parse(){
        if (input.hasNext()){
            this.length = input.nextInt();
            this.beats = new int[2][this.length];
            for (int i = 0; i < this.length; i++){
                if (input.hasNextLine()){
                    this.beats[0][i] = input.nextInt();
                    this.beats[1][i] = input.nextInt();
                }
            }
        }
        int currentTime = this.offset;
        for (int i = 0; i < this.length; i++){
            for (int beatNum = 0; beatNum < this.beats[0][i]; beatNum++){
                this.timeStamps.add(currentTime);
                currentTime += this.beats[1][i];
            }
        }
        this.runTime = currentTime;
    }

    public ArrayList<Integer> getTimeStamps(){
        return this.timeStamps;
    }

    public int getRunTime() {
        return runTime;
    }


    public String[] getFileNames() {
        return fileNames;
    }

    public int[][] getBeats() {
        return beats;
    }



}

