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
import org.firstinspires.ftc.teamcode.classes.Lift;
import org.firstinspires.ftc.teamcode.classes.Lights;
import org.firstinspires.ftc.teamcode.classes.MatchInfo;
import org.firstinspires.ftc.teamcode.classes.Shooter;
import org.firstinspires.ftc.teamcode.classes.Spindexer;

import java.util.List;

public class Robot {
    private HardwareMap hardwareMap;
    private Telemetry telemetry;

    public MecanumDrive drive;
    public Lift lift;
    public Shooter shooter;
    public Intake intake;
    public Spindexer spindexer;
    public Limelight3A limelight;
    public Lights lights = null;

    public boolean lightsInstalled = true;



    public class TargetInfo {
        public double targetXDegrees;
        public double targetDistance;
        public int tagID;

        public TargetInfo(LLResultTypes.FiducialResult results ){
            targetXDegrees = results.getTargetXDegrees();
            targetDistance = -results.getCameraPoseTargetSpace().getPosition().z;
            tagID = results.getFiducialId();
        }

        public TargetInfo(){
            targetDistance = 0;
            targetXDegrees = 0;
            tagID = 0;
        }

        public TargetInfo(int tagID, double targetDistance, double targetXDegrees){
            this.targetDistance = targetDistance;
            this.targetXDegrees = targetXDegrees;
            this.tagID = tagID;
        }

    }

    public TargetInfo targetInfo = new TargetInfo();
    public TargetInfo obelsikInfo = new TargetInfo();


    public Robot(HardwareMap hardwareMap, Telemetry telemetry, Pose2d pose){
        this.hardwareMap = hardwareMap;
        this.telemetry = telemetry;

        drive = new MecanumDrive(hardwareMap, pose);
        lift = new Lift(hardwareMap, telemetry);
        shooter = new Shooter(hardwareMap, telemetry);
        intake = new Intake(hardwareMap, telemetry);
        spindexer = new Spindexer(hardwareMap, telemetry);
        limelight = hardwareMap.get(Limelight3A.class, "limelight");

        if(lightsInstalled){
            lights = new Lights(hardwareMap,telemetry);
            lights.setYellow();
        }
    }

    public void initRobot(){
        intake.stop();
        shooter.loadArtifact(0);
        shooter.disable();
        spindexer.resetPos();
    }

    public TargetInfo getAprilTagInfo() {
        LLResult llResult;
        llResult = limelight.getLatestResult();
        if (llResult != null) {
            if (llResult.isValid()) {
                // Access AprilTag results
                List<LLResultTypes.FiducialResult> fiducialResults = llResult.getFiducialResults();
                for (LLResultTypes.FiducialResult fr : fiducialResults) {
                    if (fr.getFiducialId() == 20 || fr.getFiducialId() == 24) {
                        return new TargetInfo(fr);
                    }
                }
            }
        }
        return new TargetInfo();
    }

    public TargetInfo getTargetTagInfo() {
        LLResult llResult;
        llResult = limelight.getLatestResult();
        if (llResult != null) {
            if (llResult.isValid()) {
                // Access AprilTag results
                List<LLResultTypes.FiducialResult> fiducialResults = llResult.getFiducialResults();
                for (LLResultTypes.FiducialResult fr : fiducialResults) {
                    if (fr.getFiducialId() == MatchInfo.targetTag) {
                        targetInfo = new TargetInfo(fr);
                        return targetInfo;
                    }
                }
            }
        }
        targetInfo.tagID = 0;
        return targetInfo;
    }

    public TargetInfo getObeliskInfo() {
        LLResult llResult;
        llResult = limelight.getLatestResult();
        if (llResult != null) {
            if (llResult.isValid()) {
                // Access AprilTag results
                List<LLResultTypes.FiducialResult> fiducialResults = llResult.getFiducialResults();
                for (LLResultTypes.FiducialResult fr : fiducialResults) {
                    if (fr.getFiducialId() >= 21 && fr.getFiducialId() <= 23) {
                        telemetry.addData("Tag ID:", fr.getFiducialId());
                        telemetry.update();
                        obelsikInfo = new TargetInfo(fr);
                        return new TargetInfo(fr);
                    }
                }
            }
        }
        return new TargetInfo(0, 0,0);
    }

    public double getTurnToTargetRotation() {
        LLResult llResult;
        llResult = limelight.getLatestResult();
        if (llResult != null) {
            if (llResult.isValid()) {
                // Access AprilTag results
                List<LLResultTypes.FiducialResult> fiducialResults = llResult.getFiducialResults();
                for (LLResultTypes.FiducialResult fr : fiducialResults) {
                    if (fr.getFiducialId() == 20 || fr.getFiducialId() == 24) {
                        return fr.getTargetXDegrees();
                    }
                }
            }
        }
        return 0;
    }


    public Vector2d rotatedVector(Vector2d myVector, double angleRadians){
        // For reference, this is what rotateBy() does internally
//        double angleRadians = Math.toRadians(angleDegrees);
        double x_rotated = myVector.x * Math.cos(angleRadians) - myVector.y * Math.sin(angleRadians);
        double y_rotated = myVector.x * Math.sin(angleRadians) + myVector.y * Math.cos(angleRadians);
        return new Vector2d(x_rotated, y_rotated);
    }

    public Action updateLimelight() {
        return new Action() {
            private boolean initialized = false;

            @Override
            public boolean run(@NonNull TelemetryPacket packet) {
                getObeliskInfo();
                return true;
            }
        };
    }

    public TargetInfo getTargetInfo() {
        return targetInfo;
    }

    public TargetInfo getObeliskTag() {
        return obelsikInfo;
    }
}
