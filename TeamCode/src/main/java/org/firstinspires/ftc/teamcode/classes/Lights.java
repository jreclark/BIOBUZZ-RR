package org.firstinspires.ftc.teamcode.classes;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.Telemetry;

public class Lights {
    private HardwareMap hardwareMap;
    private Telemetry telemetry;

    ElapsedTime timer = new ElapsedTime(ElapsedTime.Resolution.MILLISECONDS);

    private Servo leftLight, rightLight;
    private Spindexer spindexer;

    private boolean onCycle = true;
    private double sparkleTimer = 0.05;
    private double rainbowSetting = 0.277;
    private boolean rainbowDirection = true;
    Spindexer.DetectedColor color;

    public Lights (HardwareMap hardwareMap, Telemetry telemetry) {
        this.hardwareMap = hardwareMap;
        this.telemetry = telemetry;
        this.spindexer = spindexer;

        leftLight = hardwareMap.get(Servo.class, "leftLight");
        rightLight = hardwareMap.get(Servo.class, "rightLight");

        timer.reset();

    }

    public void setYellow(){
        leftLight.setPosition(0.388);
        rightLight.setPosition(0.388);
    }

    public void setRed(){
        leftLight.setPosition(0.279);
        rightLight.setPosition(0.279);
    }

    public void setGreen() {
        leftLight.setPosition(0.500);
        rightLight.setPosition(0.500);
    }

    public void setPurple() {
        leftLight.setPosition(0.722);
        rightLight.setPosition(0.722);
    }

    public void setOff(){
        leftLight.setPosition(0.1);
        rightLight.setPosition(0.1);
    }

    public void showDetectedColor() {
        color = spindexer.getIntakeColor();
        if (color.equals(Spindexer.DetectedColor.PURPLE)) {
            setPurple();
        } else if (color.equals(Spindexer.DetectedColor.GREEN)) {
            setGreen();
        } else {
            setOff();
        }
    }

    public void set(double setPoint){
        leftLight.setPosition(setPoint);
        rightLight.setPosition(setPoint);
    }

    public void sparkle(){
        if(timer.seconds() >= sparkleTimer){
            onCycle = !onCycle;
            if(onCycle){
                setYellow();
            } else {
                setOff();
            }
        }
    }

    public void rainbow(){
        double rainbowStep = 0.0005;
        set(rainbowSetting);
        if(rainbowDirection && rainbowSetting < 0.722){
            rainbowSetting = rainbowSetting + rainbowStep;
        } else if (!rainbowDirection && rainbowSetting > 0.277){
            rainbowSetting = rainbowSetting - rainbowStep;
        }
        if (rainbowSetting <= 0.277){
            rainbowDirection = true;
            rainbowSetting = 0.277;
        } else if (rainbowSetting >= 0.722) {
            rainbowDirection = false;
            rainbowSetting = 0.722;
        }
//        setYellow();
    }


}
