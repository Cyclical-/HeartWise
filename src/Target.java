import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class Target implements Runnable {

    private HeartWise heartWise;
    private Parser parser;
    private ArrayList<Integer> beats;
    private long startTime;
    private Thread t;
    public boolean running;


    public Target(HeartWise heartWise, Parser parser, ArrayList<Integer> beats){
        this.heartWise = heartWise;
        this.parser = parser;
        this.beats = beats;
    }

    public void start(){
        if (t == null){
            t = new Thread(this);
            t.start();
        }
    }

    public Thread getT() {
        return t;
    }

    @Override
    public void run(){
        while(!this.heartWise.isStarted){}
        long currentTime;
        int currentBeat = 0;
        this.running = true;
        this.startTime = System.currentTimeMillis();
        long endTime = startTime + this.parser.getRunTime();
        /*
        do{
            currentTime = System.currentTimeMillis();
            if (currentTime == this.getAdjustedTime(this.beats.get(currentBeat))){
                System.out.println("target");
                SoundEffect.TARGET_BEAT.play();
                currentBeat++;
            }
        } while (currentTime <= endTime && this.running);

         */
        int interval = 0;
        if (this.heartWise.currentCourse.equals("Beginner")) {
            Timer t = new Timer(900, null);
            t.addActionListener(new ActionListener() {
                int count = 10;
                int interval = 0;

                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    if (count > 0) {
                        SoundEffect.TARGET_BEAT.play();
                        count--;
                    } else  if (interval == 0){
                        count = 15;
                        t.setDelay(1000);
                        interval++;
                    } else if (interval == 1){
                        count = 8;
                        t.setDelay(700);
                        interval++;

                    } else if (interval == 2){
                        t.stop();
                    }
                }

            });
            t.start();
            while (t.isRunning()) {
                if (!this.running) {
                    t.stop();
                    return;
                }
            }
        } else if (this.heartWise.currentCourse.equals("Intermediate")) {

        }
    }

    private long getAdjustedTime(int timeStamp){
        return ((long)timeStamp) +  this.startTime;
    }


}
