import arduino.Arduino;

import javax.swing.JOptionPane;


public class UserBeat implements Runnable {

    private Arduino arduino;
    private HeartWise heartWise;
    private boolean isCalibrated = false;
    private Thread t;
    public boolean running;


    public UserBeat(Arduino arduino, HeartWise heartWise) {
        this.arduino = arduino;
        this.heartWise = heartWise;
    }

    public Thread getT() {
        return t;
    }

    @Override
    public void run() {
        if (!this.isCalibrated){
            this.isCalibrated = this.calibrate();
            System.out.println(this.isCalibrated);
            if (!this.isCalibrated){
                JOptionPane.showMessageDialog(null, "Calibration unsuccessful, please try again.");
                return;
            }
            System.out.println(1);
        }
        System.out.println("calibration successful");
        while (this.running) {
            if (this.arduino.serialRead().equals("1\n") && this.heartWise.isStarted) {
                this.heartWise.userBeats.add(new Long(System.currentTimeMillis()).intValue());
                //SoundEffect.USER_BEAT.play();
            }
        }
    }


    public boolean calibrate() {
        int count = 0;
        int successCount = 0;
        while (count < 5) {
            long calibrationStartTime = System.currentTimeMillis();
            int beatCount = 0;
            while (System.currentTimeMillis() - calibrationStartTime < 5000) {
                if (this.arduino.serialRead().equals("1\n")) {
                    System.out.println(1);
                    beatCount++;
                }
            }
            if (beatCount > 4) {
                if (successCount >= 3) {
                    return true;
                } else {
                    successCount++;
                }
            }
            count++;

        }
        return false;
    }

    public void start(){
        if (t == null){
            t = new Thread(this);
            t.start();
        }
    }

    public boolean isCalibrated() {
        return isCalibrated;
    }

    public void setCalibrated(boolean calibrated) {
        isCalibrated = calibrated;
    }
}
