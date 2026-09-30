package org.firstinspires.ftc.teamcode;

import androidx.annotation.NonNull;

import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Action;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.classes.Intake;
import org.firstinspires.ftc.teamcode.classes.Intake_BIOBUZZ;
import org.firstinspires.ftc.teamcode.classes.Lift;
import org.firstinspires.ftc.teamcode.classes.Lights;
import org.firstinspires.ftc.teamcode.classes.MatchInfo;
import org.firstinspires.ftc.teamcode.classes.Shooter;
import org.firstinspires.ftc.teamcode.classes.Shooter_BIOBUZZ;
import org.firstinspires.ftc.teamcode.classes.Spindexer;

import java.util.List;

public class Robot_BIOBUZZ {
    private HardwareMap hardwareMap;
    private Telemetry telemetry;

    public MecanumDrive drive;
    public Shooter_BIOBUZZ shooter;
    public Intake_BIOBUZZ intake;

    public boolean lightsInstalled = false;

    public Robot_BIOBUZZ(HardwareMap hardwareMap, Telemetry telemetry, Pose2d pose) {
        this.hardwareMap = hardwareMap;
        this.telemetry = telemetry;

        drive = new MecanumDrive(hardwareMap, pose);
//        lift = new Lift(hardwareMap, telemetry);
        shooter = new Shooter_BIOBUZZ(hardwareMap, telemetry);
        intake = new Intake_BIOBUZZ(hardwareMap, telemetry);
//        spindexer = new Spindexer(hardwareMap, telemetry);
//        limelight = hardwareMap.get(Limelight3A.class, "limelight");

    }

    public void initRobot() {
//        intake.stop();
        shooter.disable();
    }

    public Vector2d rotatedVector(Vector2d myVector, double angleRadians) {
        // For reference, this is what rotateBy() does internally
//        double angleRadians = Math.toRadians(angleDegrees);
        double x_rotated = myVector.x * Math.cos(angleRadians) - myVector.y * Math.sin(angleRadians);
        double y_rotated = myVector.x * Math.sin(angleRadians) + myVector.y * Math.cos(angleRadians);
        return new Vector2d(x_rotated, y_rotated);
    }


}
