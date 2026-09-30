package org.firstinspires.ftc.teamcode.classes;

import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;

public class Intake_BIOBUZZ {
    private HardwareMap hardwareMap;
    private Telemetry telemetry;


    public DcMotorEx mainRoller; //Control Hub Motor Port 3

    public Intake_BIOBUZZ(HardwareMap hardwareMap, Telemetry telemetry) {
        this.hardwareMap = hardwareMap;
        this.telemetry = telemetry;

        mainRoller = hardwareMap.get(DcMotorEx.class, "intake");
        mainRoller.setDirection(DcMotorSimple.Direction.FORWARD);
    }

    public void intakeArtifact() {
        mainRoller.setPower(1);
    }

    public void spitArtifacts() {
        mainRoller.setPower(-1);
    }

    public void stop() {
        mainRoller.setPower(0);
    }

    public void autoIntake() {

    }


}