package org.firstinspires.ftc.teamcode.tuning;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.AnalogInput;
import com.qualcomm.robotcore.hardware.AnalogSensor;
import com.qualcomm.robotcore.hardware.NormalizedColorSensor;
import com.qualcomm.robotcore.hardware.NormalizedRGBA;
import com.sun.source.doctree.StartElementTree;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.classes.ButtonState;
import org.firstinspires.ftc.teamcode.classes.Spindexer;

@TeleOp(name = "Calibrate Color Sensors", group = "Calibration")
public class ColorSensorCalibration extends LinearOpMode {

    Spindexer spindexer;
    Spindexer.DetectedColor detectedColor;

    AnalogInput gobildaSensor;

    @Override
    public void runOpMode() throws InterruptedException {
        spindexer  = new Spindexer(hardwareMap, telemetry);

        gobildaSensor = hardwareMap.get(AnalogInput.class, "gobildaSensor");

        ButtonState readIntake = new ButtonState(gamepad1, ButtonState.Button.a);
        ButtonState readLeft = new ButtonState(gamepad1, ButtonState.Button.x);
        ButtonState readRight = new ButtonState(gamepad1, ButtonState.Button.b);

        ButtonState valUp = new ButtonState(gamepad1, ButtonState.Button.dpad_up);
        ButtonState valDown = new ButtonState(gamepad1, ButtonState.Button.dpad_down);

        NormalizedRGBA colors;

        float gain = 0;

        waitForStart();

        //spindexer.getDetectedColor(spindexer.intakeSensor, telemetry);
        //detectedColor = spindexer.getDetectedColor(spindexer.leftSensor, telemetry);
        //spindexer.getDetectedColor(spindexer.rightSensor, telemetry);

        while(opModeIsActive()) {
            telemetry.addData("color detected: ", detectedColor);

            if(readIntake.getCurrentPress()) {
                colors = spindexer.intakeSensor.getNormalizedColors();
                detectedColor = spindexer.getDetectedColor(spindexer.intakeSensor, telemetry);
                telemetry.addData("distance", spindexer.distanceIntakeSensor.getDistance(DistanceUnit.MM));
                telemetry.addData("red: ", (colors.red / colors.alpha));
                telemetry.addData("green: ", (colors.green / colors.alpha));
                telemetry.addData("blue: ", (colors.blue / colors.alpha));
            }
            if(readLeft.getCurrentPress()) {
                colors = spindexer.leftSensor.getNormalizedColors();
                detectedColor = spindexer.getDetectedColor(spindexer.leftSensor, telemetry);
                telemetry.addData("red: ", (colors.red / colors.alpha));
                telemetry.addData("green: ", (colors.green / colors.alpha));
                telemetry.addData("blue: ", (colors.blue / colors.alpha));
            }
            if(readRight.getCurrentPress()) {
                colors = spindexer.rightSensor.getNormalizedColors();
                detectedColor = spindexer.getDetectedColor(spindexer.rightSensor, telemetry);
                telemetry.addData("red: ", (colors.red / colors.alpha));
                telemetry.addData("green: ", (colors.green / colors.alpha));
                telemetry.addData("blue: ", (colors.blue / colors.alpha));
            }

            telemetry.addData("Spindexer Pos", spindexer.getSpindexerPos());
            telemetry.addData("Gobilda Sensor: ", gobildaSensor.getVoltage());

            float step = 5;

            if(valUp.newPress()){
                gain = spindexer.incrementIntakeGain(step);
            }

            if(valDown.newPress()){
                gain = spindexer.decrementIntakeGain(step);
            }
            telemetry.addData("Gain: ", gain);

            telemetry.addData("MagSensor", spindexer.readMagSensor());

            spindexer.readIntakeHSV();

            telemetry.update();
        }
    }
}
