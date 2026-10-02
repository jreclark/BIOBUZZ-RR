package org.firstinspires.ftc.teamcode.classes;

import androidx.annotation.NonNull;

import com.ThermalEquilibrium.homeostasis.Controllers.Feedback.PIDEx;
import com.ThermalEquilibrium.homeostasis.Controllers.Feedforward.BasicFeedforward;
import com.ThermalEquilibrium.homeostasis.Parameters.FeedforwardCoefficients;
import com.ThermalEquilibrium.homeostasis.Parameters.PIDCoefficientsEx;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Action;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.Telemetry;

public class Shooter_BIOBUZZ {
    private HardwareMap hardwareMap;
    private Telemetry telemetry;

    public DcMotorEx flywheel; //Control Hub Motor Port 3
    public Servo gate; //Control Hub Servo Port 5
    PIDFCoefficients pidfCoefficients;

    //PIDEx Setup
    public static double Kp = 0.003;//0.003
    public static double Ki = 0;
    public static double Kd = 0.0002;//0.0002
    public static double Kv = 0.0004; //1.1;
    public static double Ka = 0; //0.2;
    public static double Ks = 0; //0.001;
    public static double targetAccelTime = 0.5; //seconds

    PIDCoefficientsEx pidExCoeff = new PIDCoefficientsEx(Kp, Ki, Kd, 0.9, 10, 1);
    PIDEx motorController = new PIDEx(pidExCoeff);
    boolean shooterEnabled = true;
    public boolean shooterIdle = false;

    public double shootPos = 0;
    public double closePos = 0.125;

    FeedforwardCoefficients ffCoeff = new FeedforwardCoefficients(Kv,Ka,Ks);
    BasicFeedforward motorFFController = new BasicFeedforward(ffCoeff);


    public static final int ticksPerRev = 28;
    public int targetRPM = 500;
    public int targetRPS = targetRPM / 60;
    public int targetSpeed = targetRPS * ticksPerRev;
    public int idleSpeed = 1000; // rpm
    public double speedTol = 2 / 100.0; //Percent
    private static int reference = 1517; // targetSpeed (ticks/sec) for 3250 RPM

    public Shooter_BIOBUZZ(HardwareMap hardwareMap, Telemetry telemetry) {
        this.hardwareMap = hardwareMap;
        this.telemetry = telemetry;

        flywheel = hardwareMap.get(DcMotorEx.class, "flywheel");
        flywheel.setDirection(DcMotorSimple.Direction.FORWARD);
        flywheel.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

//        pidfCoefficients =  flywheel.getPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER);
//        pidfCoefficients.i = 0;
//        pidfCoefficients.d = 0.5;
//        pidfCoefficients.f = 2;
//        flywheel.setPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER, pidfCoefficients);

        gate = hardwareMap.get(Servo.class, "gate");
        gate.setDirection(Servo.Direction.REVERSE);

        gate.setPosition(closePos);
    }

    public void enable(){
        shooterEnabled = true;
    }
    public void disable(){
        shooterEnabled = false;
    }

    public void updateController(){
        flywheel.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        double currentSpeed = flywheel.getVelocity();
        double targetAccel = (targetSpeed) / targetAccelTime;
        double pidOutput = motorController.calculate(targetSpeed, currentSpeed);
        double ffOutput = motorFFController.calculate(0, targetSpeed, targetAccel);

        if (targetSpeed != 0 && shooterEnabled) {
            flywheel.setPower(Range.clip(pidOutput + ffOutput, -1.0, 1.0));
        } else {
            flywheel.setPower(0);
        }

        telemetry.addData("currentSpeed: ", currentSpeed);
        telemetry.addData("targetSpeed: ", targetSpeed);
        telemetry.addData("targetRPM: ", targetRPM);
//        telemetry.addData("targetAccel: ", targetAccel);
        telemetry.addData("pidOutput: ", pidOutput);
        telemetry.addData("ffoutput: ", ffOutput);
    }
    public Action updateFlywheel() {
        return new Action() {
            private boolean initialized = false;

            @Override
            public boolean run(@NonNull TelemetryPacket packet) {
                updateController();
                return true;
            }
        };
    }

    public void shoot() {
        gate.setPosition(shootPos);
    }
    public void unshoot() { gate.setPosition(closePos); }

    public void stop() {
        flywheel.setPower(0);
    }

    public void setTargetSpeed(int rpm){
        if(shooterIdle){
            targetRPM = idleSpeed;
        } else {
            targetRPM =rpm;
        }
        targetRPS = targetRPM / 60;
        targetSpeed = targetRPS * ticksPerRev;
    }

    public int getTargetRPM(){
        return targetRPM;
    }

    public void spinUp(int target) {
        if (shooterIdle){
            setTargetSpeed(idleSpeed);
            enable();
        } else {
            setTargetSpeed(target);
            enable();
        }
        enable();
    }

    public void idle() {
        setTargetSpeed(1000); //Was zero
        shooterIdle = true;
//        flywheel.setPower(0); //Was not commented
//        disable(); //Was not commented
        //Uncomment below to keep idling instead of stopping
        //flywheel.setVelocity(idleSpeed);
    }

    public void setNotIdle(){
        shooterIdle = false;
    }

    public boolean atSpeed() {
        return Math.abs(flywheel.getVelocity() - targetSpeed) <= (targetSpeed * speedTol);
    }

    public void adjustHood() {
        //TODO: do math to adjust hood based off of Limelight distance to tag data
    }

    public void setGate(double pos){
        gate.setPosition(pos);
    }

    public double getSpeed() {
        return flywheel.getVelocity();
    }
    public PIDFCoefficients showPIDFVals(){
        PIDFCoefficients pidfCoefficients = flywheel.getPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER);
//        telemetry.addData("P = ", pidfCoefficients.p);
//        telemetry.addData("I = ", pidfCoefficients.i);
//        telemetry.addData("D = ", pidfCoefficients.d);
//        telemetry.addData("F = ", pidfCoefficients.f);
        return pidfCoefficients;
    }
    public void setPIDF(PIDFCoefficients pidf){
        flywheel.setVelocityPIDFCoefficients(pidf.p, pidf.i, pidf.d, pidf.f);
    }

    public void setPIDFExCoeeficients(PIDCoefficientsEx pidExCoeff, FeedforwardCoefficients ffCoeff){
        motorController = new PIDEx(pidExCoeff);
        motorFFController = new BasicFeedforward(ffCoeff);
    }

//    public void setupShooter(){
//        setTargetSpeed(2700);
//    }

    public void setupShooter(int rpm){
        setTargetSpeed(rpm);
    }

    public void motorTest(double pwr){
        flywheel.setPower(pwr);
    }



}
