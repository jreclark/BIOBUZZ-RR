package org.firstinspires.ftc.teamcode.classes;

import android.graphics.Color;

import androidx.annotation.NonNull;

import com.ThermalEquilibrium.homeostasis.Controllers.Feedback.PIDEx;
import com.ThermalEquilibrium.homeostasis.Parameters.PIDCoefficientsEx;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Action;
import com.qualcomm.robotcore.hardware.AnalogInput;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.NormalizedColorSensor;
import com.qualcomm.robotcore.hardware.NormalizedRGBA;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;
import com.qualcomm.robotcore.hardware.TouchSensor;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.actions.ShootAllVariant;

public class Spindexer {
    private HardwareMap hardwareMap;
    private Telemetry telemetry;

    public DcMotorEx rotationMotor; //Expansion Hub Motor Port 3
    public PIDFCoefficients pidfCoefficients;
    public PIDFCoefficients pidfCoefficientsClose;

    private TouchSensor magSensor;
    private boolean hasMagSensor = true;

    //PIDEx Setup
    public static double Kp = 0.006;
    public static double Ki = 0;
    public static double Kd = 0;
    public static double Kv = 0; //1.1;
    public static double Ka = 0; //0.2;
    public static double Ks = 0.18; //0.001;
    public static double targetAccelTime = 0; //seconds

    PIDCoefficientsEx pidExCoeff = new PIDCoefficientsEx(Kp, Ki, Kd, 0.9, 10, 1);
    PIDEx motorController = new PIDEx(pidExCoeff);
    AnalogInput gobildaSensor;

//    Probably don't need FF
//    FeedforwardCoefficients ffCoeff = new FeedforwardCoefficients(Kv,Ka,Ks);
//    BasicFeedforward motorFFController = new BasicFeedforward(ffCoeff);


    /*
    * intakeSensor - Control Hub I2C Port 3
    * leftSensor - Expansion Hub I2C Port 3
    * rightSensor - Control Hub I2C Port 1
    * */
    public NormalizedColorSensor intakeSensor, leftSensor, rightSensor;
    public DistanceSensor distanceIntakeSensor;

    boolean pickedUp = false;

    public int spindexerStep = 179;
    public int maxSpindexerRot = 2;
    public int rot = 3*spindexerStep;
    public int spindexerInitialTol = 10;
    public int spindexerFinalTol = 2;

    public int waggleTarget = 0;
    public ElapsedTime waggleTimer = new ElapsedTime(ElapsedTime.Resolution.MILLISECONDS);
    public double waggleTimerStep = 0.2;
    public int waggleStep = 3;

    public double spinPwr = 0.5;

    public double intakeDistLimit = 95;

    public int[] intakeSpindexPos = {0, spindexerStep, -spindexerStep};
    public int[] shooterSpindexPos = {(int)Math.round(1.5*spindexerStep), (int)Math.round(-0.5*spindexerStep), (int)Math.round(0.5*spindexerStep)};

    int[] readCounter = new int[10];

    final float[] hsvValues = new float[3];

    public enum DetectedColor {
        GREEN,
        PURPLE,
        NONE,
        ANY
    }

    public DetectedColor[] spindexerSlots = {DetectedColor.NONE, DetectedColor.NONE,DetectedColor.NONE};

    public Spindexer(HardwareMap hardwareMap, Telemetry telemetry) {
        this.hardwareMap = hardwareMap;
        this.telemetry = telemetry;

        //assign values based on what's configured on the robot
        intakeSensor = hardwareMap.get(NormalizedColorSensor.class, "intakeSensor");
        distanceIntakeSensor = hardwareMap.get(DistanceSensor.class, "intakeSensor");

        leftSensor = hardwareMap.get(NormalizedColorSensor.class, "leftSensor");
        rightSensor = hardwareMap.get(NormalizedColorSensor.class, "rightSensor");

        gobildaSensor = hardwareMap.get(AnalogInput.class, "gobildaSensor");

        rotationMotor = hardwareMap.get(DcMotorEx.class, "spindexerRotationMotor");
//        rotationMotor.setDirection(DcMotorSimple.Direction.REVERSE);
        rotationMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rotationMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rotationMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        if(hasMagSensor){
            magSensor = hardwareMap.get(TouchSensor.class, "magsensor");
        }

//        pidfCoefficients =  rotationMotor.getPIDFCoefficients(DcMotor.RunMode.RUN_TO_POSITION);
//        pidfCoefficients.p = 7.5;
//        pidfCoefficients.i = 1.5;
//        pidfCoefficients.d = 0.5;
//        setPIDF(pidfCoefficients);
//        pidfCoefficientsClose = pidfCoefficients;
//        pidfCoefficientsClose.p = 15;



        intakeSensor.setGain(100);
        leftSensor.setGain(100);
        rightSensor.setGain(100);

        clearAllSlots();
    }

    public void setAllColors() {
        DetectedColor intake = getDetectedColor(intakeSensor, telemetry);
        DetectedColor left = getDetectedColor(leftSensor, telemetry);
        DetectedColor right = getDetectedColor(rightSensor, telemetry);

            spindexerSlots[0] = intake;
            spindexerSlots[1] = left;
            spindexerSlots[2] = right;
    }

    public DetectedColor getDetectedColor(NormalizedColorSensor sensor, Telemetry telemetry) {
        //return red, green, blue, alpha (amount of light)
        NormalizedRGBA colors = sensor.getNormalizedColors();

        //take light out of the equation, making the color output consistent no matter the lighting
        float normRed, normGreen, normBlue;
        normRed = colors.red / colors.alpha;
        normGreen = colors.green / colors.alpha;
        normBlue = colors.blue / colors.alpha;

//        telemetry.addData("red", normRed);
//        telemetry.addData("green", normGreen);
//        telemetry.addData("blue", normBlue);


        if (sensor == leftSensor) {
            if (normRed < 1.1 && normGreen < 1.3 && normBlue > 0.8) {
                return DetectedColor.PURPLE;
            } else if (normRed < 0.7 && normGreen > 0.9 && normBlue > 0.7) {
                return DetectedColor.GREEN;
            } else {
                return DetectedColor.NONE;
            }
        } else if (sensor == intakeSensor) {
            if (normRed > 0.9 && normGreen < 2.5 && normBlue > 2.7) {
                return DetectedColor.PURPLE;
            } else if (normRed < 1.3 && normGreen > 3.1 && normBlue > 2.6) {
                return DetectedColor.GREEN;
            } else {
                return DetectedColor.NONE;
            }
        } else if(sensor == rightSensor) {
            if (normRed > 0.5 && normGreen > 1.2 && normBlue > 0.7) {
                return DetectedColor.PURPLE;
            } else if (normRed < 1.3 && normGreen < 1.4 && normBlue > 0.5) {
                return DetectedColor.GREEN;
            } else {
                return DetectedColor.NONE;
            }
        } else {
            return DetectedColor.NONE;
        }

    }

    public DetectedColor readIntakeHSV(){
        NormalizedRGBA colors = intakeSensor.getNormalizedColors();
        DetectedColor finalColor = DetectedColor.ANY;
        // Update the hsvValues array by passing it to Color.colorToHSV()
        Color.colorToHSV(colors.toColor(), hsvValues);

        if(hsvValues[0] > 185){
            finalColor = DetectedColor.PURPLE;
        } else  if(hsvValues[0] < 170 && hsvValues[2] > 0.07){
            finalColor = DetectedColor.GREEN;
        }

        telemetry.addLine()
                .addData("Hue", "%.3f", hsvValues[0])
                .addData("Saturation", "%.3f", hsvValues[1])
                .addData("Value", "%.3f", hsvValues[2])
                .addData("Detected Color: ", finalColor.toString());

        return finalColor;
    }

    public DetectedColor getIntakeColor(){
        return getDetectedColor(intakeSensor, telemetry);
    }

    public float incrementIntakeGain(float increment){
        float newVal = intakeSensor.getGain() + increment;
        intakeSensor.setGain(newVal);
        return newVal;
    }

    public float decrementIntakeGain(float increment){
        float newVal = intakeSensor.getGain() - increment;
        intakeSensor.setGain(newVal);
        return newVal;
    }

    public void rotate(int pose) {
        rotationMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        rotationMotor.setTargetPosition(pose);
        //rotationMotor.setPower(1);
    }

    public void stop() {
        rotationMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rotationMotor.setPower(0);
    }

    public void initSpindexerforAuton(){
        spindexerSlots[0] = DetectedColor.PURPLE;
        spindexerSlots[1] = DetectedColor.PURPLE;
        spindexerSlots[2] = DetectedColor.GREEN;
    }

    public void initSpindexer(int greenPos){
        for (int i = 0; i <= 2; i++) {
            if(i==greenPos){
                setSlotGreen(i);
            } else {
                setSlotPurple(i);
            }
        }
    }

    public DetectedColor getSlotColor(int x){
        return spindexerSlots[x];
    }

    public int getSpindexerPos() {
        return rotationMotor.getCurrentPosition();
    }

    public void runSpindexerPos(int pos, double pwr){
        //Use motor TargetPosition to hold position even with custom PID
        //Maintains compatibility with old method
        rotationMotor.setTargetPosition(pos);
//        rotationMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
//        rotationMotor.setPower(pwr);
    }

    public void stopSpindexer(){
        rotationMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rotationMotor.setPower(0);
    }

    public void setSlot(int slotNum, DetectedColor color){
        spindexerSlots[slotNum] = color;
    }

    public void clearSlot(int slotNum){
        setSlot(slotNum, DetectedColor.NONE);
    }

    public void clearAllSlots(){
        for(int x = 0; x <=2 ; x++){
            clearSlot(x);
        }
    }

    public void setSlotGreen(int slotNum){
        setSlot(slotNum, DetectedColor.GREEN);
    }

    public void setSlotPurple(int slotNum){
        setSlot(slotNum, DetectedColor.PURPLE);
    }

    public void moveToIntakePos(int slotnum){
        int target = intakeSpindexPos[slotnum];
        int finalTarget = calcNearestPos(target);
        runSpindexerPos(finalTarget, spinPwr);
        waggleTarget = finalTarget;
        waggleTimer.reset();
    }

    public void moveToShooterPos(int slotnum){
        int target = shooterSpindexPos[slotnum];
        int finalTarget = calcNearestPos(target);
        runSpindexerPos(finalTarget, spinPwr);
        waggleTarget = finalTarget;
        waggleTimer.reset();
    }

    public int calcNearestPos(int target) {
        int currentPos = getSpindexerPos();

        int currentRot = currentPos/rot;
        int targetopt1 = target + currentRot * rot;
        int targetopt2;
        if(targetopt1 < currentPos){
            targetopt2 = target + (currentRot+1) * rot;
        } else {
            targetopt2 = target + (currentRot-1) * rot;
        }

        int distance1 = Math.abs(targetopt1 - currentPos);
        int distance2 = Math.abs(targetopt2 - currentPos);

        int finalTarget;

        if((distance1 <= distance2 && Math.abs(targetopt1/rot) <= maxSpindexerRot) || distance1==0){
            finalTarget = targetopt1;
        } else if((distance2 <= distance1 && Math.abs(targetopt2/rot) <= maxSpindexerRot) || distance2==0) {
            finalTarget = targetopt2;
        } else if (Math.abs(targetopt1/rot) <= maxSpindexerRot){
            finalTarget = targetopt1;
        } else {
            finalTarget = targetopt2;
        }

//        System.out.println("CurrentPos = " + currentPos);
//        System.out.println("Rot = " + currentRot);
//        System.out.println("target = " + target);
//        System.out.println("targetopt1 = " + targetopt1);
//        System.out.println("targetopt2 = " + targetopt2);
//        System.out.println("distance1 = " + distance1);
//        System.out.println("distance2 = " + distance2);
//
//        System.out.println("finaltarget = " + finalTarget);

        return finalTarget;
    }

    public int findEmptyIntakeSlot(){
        int bestSlot = -1;
        int bestDistance = Integer.MAX_VALUE;
        int currentPos = getSpindexerPos();

        for (int i = 0; i <= 2; i++) {
            if(spindexerSlots[i] == DetectedColor.NONE){
                int target = intakeSpindexPos[i];
                int distance = Math.abs(calcNearestPos(target) - currentPos);
                if(distance < bestDistance){
                    bestSlot = i;
                    bestDistance = distance;
                }
            }
        }

        return bestSlot;
    }

    public int findFullShooterSlot(DetectedColor color){
        int bestSlot = -1;
        int bestDistance = Integer.MAX_VALUE;
        int currentPos = getSpindexerPos();
        boolean colorMatch = false;

        for (int i = 0; i <= 2; i++) {
            if(color == DetectedColor.ANY){
                colorMatch = spindexerSlots[i] != DetectedColor.NONE;
            } else {
                colorMatch = spindexerSlots[i] == color;
            }
            if(colorMatch){
                int target = shooterSpindexPos[i];
                int distance = Math.abs(calcNearestPos(target) - currentPos);
                if(distance < bestDistance){
                    bestSlot = i;
                    bestDistance = distance;
                }
            }
        }

        return bestSlot;
    }

    public int gotoClosestEmptyIntake(){
        int targetSlot = findEmptyIntakeSlot();
        if(targetSlot != -1){
            moveToIntakePos(targetSlot);
            return targetSlot;
        } else {
            return -1;
        }
    }

    public int gotoClosestFullShooter(DetectedColor color){
        int targetSlot = findFullShooterSlot(color);
        if(targetSlot != -1){
            moveToShooterPos(targetSlot);
            return targetSlot;
        } else {
            return -1;
        }
    }

    public boolean isIntakeSlotFull(){
        return distanceIntakeSensor.getDistance(DistanceUnit.MM) < intakeDistLimit;
    }

    public boolean isGobildaIntakeSlotFull(){
        return gobildaSensor.getVoltage() < 0.3;
    }

    public boolean atTarget(){
        int currentTarget = rotationMotor.getTargetPosition();
        int tol = Math.abs(currentTarget - rotationMotor.getCurrentPosition());
        telemetry.addData("Spindexer Tol: ", tol);
        boolean done = false;
//        if(tol < spindexerInitialTol){
//            rotationMotor.setPIDFCoefficients(DcMotor.RunMode.RUN_TO_POSITION, pidfCoefficientsClose);
//            runSpindexerPos(currentTarget, spinPwr);
//        }
        done = tol <= spindexerFinalTol;
//        if(done){
//            rotationMotor.setPIDFCoefficients(DcMotor.RunMode.RUN_TO_POSITION, pidfCoefficients);
//        }
        return done;
    }

    public PIDFCoefficients showPIDFVals(){
        PIDFCoefficients pidfCoefficients = rotationMotor.getPIDFCoefficients(DcMotor.RunMode.RUN_TO_POSITION);
//        telemetry.addData("P = ", pidfCoefficients.p);
//        telemetry.addData("I = ", pidfCoefficients.i);
//        telemetry.addData("D = ", pidfCoefficients.d);
//        telemetry.addData("F = ", pidfCoefficients.f);
        return pidfCoefficients;
    }
    public void setPIDF(PIDFCoefficients pidf){
        rotationMotor.setPIDFCoefficients(DcMotor.RunMode.RUN_TO_POSITION, pidf);
    }

    public void manualSpindexer(){
        rotationMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rotationMotor.setPower(0.15);
    }

    public void autoAlignMoveSpindexer(){
        rotationMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rotationMotor.setPower(-0.10);
    }

    public void manualInvertSpindexer(){
        rotationMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rotationMotor.setPower(-0.15);
    }

    public void zeroSpindexer(){
        DcMotor.RunMode mode = rotationMotor.getMode();
        rotationMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rotationMotor.setMode(mode);
    }

    public boolean hasGPP(){
        int greenCount = 0;
        int purpleCount = 0;

        for(int x = 0; x < 3; x++){
            if(spindexerSlots[x] == DetectedColor.GREEN){
                greenCount ++;
            } else if (spindexerSlots[x] == DetectedColor.PURPLE){
                purpleCount ++;
            }
        }

        if(purpleCount == 2 && greenCount ==1){
            return true;
        } else {
            return false;
        }
    }

    public ShootAllVariant.ShotType selectAShot(ShootAllVariant shootAction) {
        if (hasGPP()) {
            shootAction.selectShot(ShootAllVariant.ShotType.ShootPattern);
            return ShootAllVariant.ShotType.ShootPattern;
        } else {
            shootAction.selectShot(ShootAllVariant.ShotType.ShootAll);
            return ShootAllVariant.ShotType.ShootAll;
        }
    }

    public void showSlots(){
        for(int x = 0; x <=2; x++){
            telemetry.addLine()
                    .addData("Slot[", x)
                    .addData("] ->", getSlotColor(x).name());
        }
//        telemetry.update();
    }

    public void updateController(){
        rotationMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        double targetPos = rotationMotor.getTargetPosition();
        double currentPos = getSpindexerPos();
        double pidOutput = motorController.calculate(targetPos, currentPos);
//        double ffOutput = motorFFController.calculate(0, rotationMotor, targetAccel);
        rotationMotor.setPower(Range.clip(pidOutput + calcKs(), -1.0, 1.0));

        telemetry.addData("Output: ", Range.clip(pidOutput + calcKs(), -1.0, 1.0));

//        telemetry.addData("currentSpeed: ", currentSpeed);
//        telemetry.addData("targetSpeed: ", targetSpeed);
//        telemetry.addData("targetAccel: ", targetAccel);
//        telemetry.addData("pidOutput: ", pidOutput);
//        telemetry.addData("ffoutput: ", ffOutput);
    }
    public Action updateSpindexer() {
        return new Action() {
            private boolean initialized = false;

            @Override
            public boolean run(@NonNull TelemetryPacket packet) {
                updateController();
                return true;
            }
        };
    }

    public void setPIDFExCoeeficients(PIDCoefficientsEx pidExCoeff){
        motorController = new PIDEx(pidExCoeff);
//        motorFFController = new BasicFeedforward(ffCoeff);
    }

    public void resetPos(){
        rotationMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rotationMotor.setTargetPosition(0);
        rotationMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
    }

    public double calcKs(){
        int sensitivity = 1;
        int error = rotationMotor.getCurrentPosition() - rotationMotor.getTargetPosition();
        if(error > sensitivity){
            return -Ks;
        } else if(error < -sensitivity){
            return Ks;
        } else {
            return 0;
        }
    }

    public void waggle(){
        if(waggleTimer.seconds() >= waggleTimerStep){
            waggleTimer.reset();
            if(rotationMotor.getTargetPosition() < waggleTarget){
                rotationMotor.setTargetPosition(waggleTarget + waggleStep);
            } else {
                rotationMotor.setTargetPosition(waggleTarget - waggleStep);
            }
        }
    }

    public boolean getHasMagSensor(){
        return hasMagSensor;
    }

    public boolean readMagSensor(){
        if(hasMagSensor){
            return magSensor.isPressed();
        } else {
            telemetry.addData("Somethings wrong!!", 0);
            return false;
        }
    }

    public void gotoAlignPos(int targetSlot){
        int alignOffset = rot/4;
        moveToIntakePos(targetSlot);
        rotationMotor.setTargetPosition(rotationMotor.getTargetPosition() - alignOffset);
    }

}
