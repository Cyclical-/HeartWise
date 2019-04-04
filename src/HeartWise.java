import arduino.Arduino;
import arduino.PortDropdownMenu;
import mdlaf.MaterialLookAndFeel;
import mdlaf.animation.MaterialUIMovement;
import mdlaf.utils.MaterialColors;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class HeartWise {

    final static String COURSE_DIRECTORY = "res\\courses";

    Arduino arduino;
    JFrame settingsFrame = new JFrame("Settings");
    JButton goButton;
    Parser fileParser;
    String currentCourse;
    ArrayList<Thread> threads;
    public volatile ArrayList<Integer> userBeats;
    private JLabel timerField;
    public boolean isStarted = false;
    private Timer timer;
    private Target target;
    private UserBeat userBeat;


    public HeartWise() {
        try {
            UIManager.setLookAndFeel(new MaterialLookAndFeel());
            SoundEffect.init();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error! Crashing now.");
            return;
        }
        this.userBeats = new ArrayList<>();
        this.threads = new ArrayList<>();
        this.fileParser = new Parser(0);
        setupSettings();
        settingsFrame.setResizable(true);
        settingsFrame.setVisible(true);
    }

    public static void main(String args[]) {
        new HeartWise();
    }


    public void populateSettings() {

        JPanel centerPanel = new JPanel();

        timerField = new JLabel();
        timerField.setText("00:00:00");
        timerField.setFont(new Font("Monospaced", Font.PLAIN, 64));
        centerPanel.setBackground(MaterialColors.WHITE);
        centerPanel.add(timerField);
        Border padding = BorderFactory.createEmptyBorder(150, 30, 100, 30);
        centerPanel.setBorder(padding);
        settingsFrame.add(centerPanel, BorderLayout.CENTER);


        //Set up port selection
        PortDropdownMenu ports = new PortDropdownMenu();
        ports.refreshMenu();
        JButton connectButton = new JButton("Connect");
        connectButton.setBackground(MaterialColors.LIGHT_BLUE_400);
        connectButton.setForeground(MaterialColors.WHITE);
        connectButton.setMaximumSize(new Dimension(200, 200));
        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.setBackground(MaterialColors.LIGHT_BLUE_400);
        btnRefresh.setForeground(MaterialColors.WHITE);
        btnRefresh.addActionListener(e -> ports.refreshMenu());
        JPanel topPanel = new JPanel();
        topPanel.add(ports);
        topPanel.add(btnRefresh);
        topPanel.add(connectButton);
        connectButton.addActionListener(e -> {
            try {
                if (ports.getSelectedItem() != null) {
                    if (connectButton.getText().equals("Connect")) {
                        arduino = new Arduino(ports.getSelectedItem().toString(), 115200);
                        if (arduino.openConnection()) {
                            connectButton.setText("Disconnect");
                            ports.setEnabled(false);
                            btnRefresh.setEnabled(false);
                            goButton.setEnabled(true);
                            settingsFrame.pack();
                        }
                    } else {
                        arduino.closeConnection();
                        goButton.setEnabled(false);
                        connectButton.setText("Connect");
                        ports.setEnabled(true);
                        btnRefresh.setEnabled(true);
                        settingsFrame.pack();
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "No port selected!");
                }
            } catch (NullPointerException nullException) {
                nullException.printStackTrace();
            }
        });
        JPanel spacer = new JPanel();
        topPanel.add(spacer);
        // Set up file selection
        JComboBox fileSelector = new JComboBox(fileParser.getFileNames());
        JButton chooseFileButton = new JButton("Select");
        chooseFileButton.setBackground(MaterialColors.LIGHT_BLUE_400);
        chooseFileButton.setForeground(MaterialColors.WHITE);
        topPanel.add(fileSelector);
        topPanel.add(chooseFileButton);
        chooseFileButton.addActionListener(e -> {
            this.currentCourse = (String) fileSelector.getSelectedItem();
            this.fileParser.selectFile(currentCourse);
            this.setTimer();
        });
        settingsFrame.add(topPanel, BorderLayout.NORTH);

        // Add start button

        goButton = new JButton("Start Course");
        JPanel bottomPanel = new JPanel();
        goButton.setEnabled(false);
        bottomPanel.add(goButton);
        goButton.addActionListener(e -> {
            if (connectButton.getText().equals("Disconnect")) {
                this.startCourse();
            } else {
                JOptionPane.showMessageDialog(null, "Arduino not connected. (You should never have been able to press this...)");
            }
        });

        settingsFrame.add(bottomPanel, BorderLayout.SOUTH);
        MaterialUIMovement.add(bottomPanel, MaterialColors.GRAY_200);


    }


    public void startCourse() {
        this.userBeat = new UserBeat(this.arduino, this);
        this.target = new Target(this, this.fileParser, this.fileParser.getTimeStamps());
        userBeat.start();
        this.userBeat.running = true;
        this.threads.add(userBeat.getT());
        long start = System.currentTimeMillis();
        while (start + 20000 > System.currentTimeMillis()) {};
        if (userBeat.isCalibrated()) {
            userBeat.running = false;
            return;
        }
        this.target.running = true;
        target.start();
        System.out.println("Starting course..");
        this.isStarted = true;
        HeartWise h = this;
        timer = new Timer(100, new ActionListener() {
            private int count = fileParser.getRunTime();

            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (count <= 0) {
                    timerField.setText("00:00:00");
                    h.endCourse();
                    timer.stop();
                    JOptionPane.showMessageDialog(null, "Your synchronization proximity score: " + calculateScore());
                } else {
                    timerField.setText(formatInterval(count));
                }
                count -= 100;
            }
        });
        timer.start();
    }


    public long calculateScore() {
        /*
        * iterate over the target beat array
        * for each beat interval, calculate the avg bpm in that interval and compare to the users bpm in that interval
        *
         */
        ArrayList<Long> errorList = new ArrayList<>();
        int[][] beats = this.fileParser.getBeats();
        int currentTime = 0;
        for (int i = 0; i < beats.length; i++){ //iterate over the array of beats
            System.out.println(beats[0][i] + " " + beats[1][i]);
            int intervalTime = (beats[0][i]*beats[1][i]);
            long targetBPM = 60000L*beats[0][i]/(intervalTime);
            long numUserBeats = 0;
            for (int j = 0; j <= this.userBeats.size(); j++){
                if (this.userBeats.get(i) > currentTime && this.userBeats.get(i) <= currentTime + intervalTime) {
                    numUserBeats++;
                }
            }
            long userBPM = 60000L*numUserBeats/intervalTime;
            errorList.add(Math.abs(targetBPM - userBPM));
            currentTime += intervalTime;
        }
        long error = 0;
        for (long e : errorList){
            error += e;
        }
        return error / errorList.size();

    }

    /*
    public void process(ArrayList<Integer> Target, ArrayList<Integer> UserData) {
        int i = 1;//user counter
        int j = 1;//target counter
        long currentError;
        int targetIBI, userIBI;
        boolean toggle = false;//toggles every time user misses a beat so that
        int missedBeat = 0;//increment on every missed beat
        this.error.add(0, 0L); //adjust this for offset of first beat
        while (UserData.get(i) != null && Target.get(j) != null) {
            targetIBI = Target.get(j) - Target.get(j - 1);
            userIBI = UserData.get(i) - UserData.get(i - 1);
            currentError = targetIBI - userIBI + this.error.get(i - 1);//error counts with userbeat, not with targetbeat
            if (currentError > (targetIBI / 2)) { //if the user misses a beat
                currentError = targetIBI - currentError;
                missedBeat++;
                if (userIBI > targetIBI) {
                    i++;
                    j = j + 2;
                    //increment userbeat once and Target beat twice
                    //if the user misses a beat from going too long
                } else {
                    i++;
                    //only increment userbeat if the user misses a beat
                    //from going too short
                }
            } else {
                i++;
                j++;
            }
        }
    }
    */

    public void endCourse() {
        this.isStarted = false;
        this.target.running = false;
        this.userBeat.running = false;
        this.target = null;
        this.userBeat = null;
        this.recordData();
    }

    private void recordData() {

    }

    private void setTimer() {
        int fullTime = this.fileParser.getRunTime();
        this.timerField.setText(formatInterval(fullTime));
    }

    private static String formatInterval(final long l) {
        final long hr = TimeUnit.MILLISECONDS.toHours(l);
        final long min = TimeUnit.MILLISECONDS.toMinutes(l - TimeUnit.HOURS.toMillis(hr));
        final long sec = TimeUnit.MILLISECONDS.toSeconds(l - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min));
        final long ms = TimeUnit.MILLISECONDS.toMillis(l - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min) - TimeUnit.SECONDS.toMillis(sec));
        return String.format("%02d:%02d:%01d", min, sec, ms);
    }


    public void setupSettings() {
        settingsFrame.setMinimumSize(new Dimension(600, 600));
        settingsFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        BorderLayout layout = new BorderLayout();
        //layout.setVgap(150);
        settingsFrame.setLayout(layout);
        populateSettings();
        settingsFrame.setBackground(MaterialColors.WHITE);
        settingsFrame.pack();
        settingsFrame.getContentPane();
    }

}
