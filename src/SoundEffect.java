import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;
import java.net.URL;

public enum SoundEffect {
    USER_BEAT("res\\sounds\\userBeat.wav"),
    TARGET_BEAT("res\\sounds\\targetBeat.wav"),
    COURSE_START("res\\sounds\\courseStart.wav");

    private Clip clip;

    SoundEffect(String fileName){
        try{
            //File f = new File(fileName);
            URL url = new File(fileName).toURI().toURL();
            AudioInputStream stream = AudioSystem.getAudioInputStream(url);
            clip = AudioSystem.getClip();
            stream.getFormat();
            clip.open(stream);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public void play(){
        if(clip.isRunning()){
            clip.stop();
        }
        clip.setFramePosition(0);
        clip.start();
    }

    static void init(){
        values();
    }
}
