package org.firstinspires.ftc.teamcode.classes;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;

import org.firstinspires.ftc.robotcore.external.Telemetry;

public class Lift {
    private HardwareMap hardwareMap;
    private Telemetry telemetry;
    //leftClimbMotor - Expansion Hub Motor Port 2
    //rightClimbMotor - Control Hub Motor Port 2
    public DcMotorEx leftClimbMotor, rightClimbMotor;

    public static double KV = 1.0;
    public static double KP = 1.0;
    public static double KvP = 1.0;
    public static double KvD = 1.0;
    public static double KvI = 1.0;
    public static double KF = 1.0;

    public int liftTarget = 17500;

    public Lift (HardwareMap hardwareMap, Telemetry telemetry) {
        this.hardwareMap = hardwareMap;
        this.telemetry = telemetry;

        leftClimbMotor = hardwareMap.get(DcMotorEx.class, "leftClimbMotor");
        rightClimbMotor = hardwareMap.get(DcMotorEx.class, "rightClimbMotor");

        leftClimbMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightClimbMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        leftClimbMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rightClimbMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        leftClimbMotor.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.BRAKE);
        rightClimbMotor.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.BRAKE);

        rightClimbMotor.setDirection(DcMotorSimple.Direction.REVERSE);

        setPIDFValues();

    }

    public void runLift() {
//        leftClimbMotor.setTargetPosition(liftTarget);
//        rightClimbMotor.setTargetPosition(liftTarget);
//
//        leftClimbMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
//        rightClimbMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        leftClimbMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightClimbMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        if(leftClimbMotor.getCurrentPosition() < liftTarget){
            leftClimbMotor.setPower(0.6);
        }
        else {
            leftClimbMotor.setPower(0);
        }

        if(rightClimbMotor.getCurrentPosition() < liftTarget){
            rightClimbMotor.setPower(0.6);
        } else {
            rightClimbMotor.setPower(0);
        }

    }

    public void runLiftBalanced(boolean simulate) {

        double balanceFactor = 0.001;

        leftClimbMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        rightClimbMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        int leftPos = leftClimbMotor.getCurrentPosition();
        int rightPos = rightClimbMotor.getCurrentPosition();

        int diff = leftPos - rightPos;
        double leftPower = 0.9 - balanceFactor*diff;
        double rightPower = 0.9+ balanceFactor*diff;

        telemetry.addData("Lift Correction: ", diff * balanceFactor);
        telemetry.addData("Left Power: ", leftPower);
        telemetry.addData("Right Power: ", rightPower);

        if (!simulate) {
            if (leftPos < liftTarget) {
                leftClimbMotor.setPower(leftPower);
            } else {
                leftClimbMotor.setPower(0);
            }

            if (rightPos < liftTarget) {
                rightClimbMotor.setPower(rightPower);
            } else {
                rightClimbMotor.setPower(0);
            }
        }

    }

    public void stopLift() {
        leftClimbMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        rightClimbMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        leftClimbMotor.setPower(0);
        rightClimbMotor.setPower(0);
    }

    public void setPIDFValues() {
        leftClimbMotor.setPIDFCoefficients(DcMotor.RunMode.RUN_TO_POSITION, new PIDFCoefficients(KP, 0, 0, 0));
        rightClimbMotor.setPIDFCoefficients(DcMotor.RunMode.RUN_TO_POSITION, new PIDFCoefficients(KP, 0, 0, 0));

        leftClimbMotor.setPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER, new PIDFCoefficients(KvP, KvI, KvD, KV));
        rightClimbMotor.setPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER, new PIDFCoefficients(KvP, KvI, KvD, KV));
    }

    public void manualClimb(double power){
        leftClimbMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightClimbMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

//        telemetry.addData("Left Motor:", leftClimbMotor.getCurrentPosition());
//        telemetry.addData("Right Motor:", rightClimbMotor.getCurrentPosition());

        leftClimbMotor.setPower(power);
        rightClimbMotor.setPower(power);
    }

    public void reportPosition(){
        telemetry.addData("Left Motor:", leftClimbMotor.getCurrentPosition());
        telemetry.addData("Right Motor:", rightClimbMotor.getCurrentPosition());
    }


}
