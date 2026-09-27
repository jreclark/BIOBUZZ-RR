package org.firstinspires.ftc.teamcode.backups;

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
import org.firstinspires.ftc.teamcode.MecanumDrive;
import org.firstinspires.ftc.teamcode.classes.Intake;
import org.firstinspires.ftc.teamcode.classes.Lift;
import org.firstinspires.ftc.teamcode.classes.Lights;
import org.firstinspires.ftc.teamcode.classes.MatchInfo;
import org.firstinspires.ftc.teamcode.classes.Shooter;
import org.firstinspires.ftc.teamcode.classes.Spindexer;

import java.util.List;

public class Robot_Share {
    private HardwareMap hardwareMap;
    private Telemetry telemetry;

    public MecanumDrive drive;
    public Limelight3A limelight;



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

    public TargetInfo targetInfo;


    public Robot_Share(HardwareMap hardwareMap, Telemetry telemetry, Pose2d pose){
        this.hardwareMap = hardwareMap;
        this.telemetry = telemetry;

        drive = new MecanumDrive(hardwareMap, pose);
        limelight = hardwareMap.get(Limelight3A.class, "limelight");

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
                        return new TargetInfo(fr);
                    }
                }
            }
        }
        return new TargetInfo(21, 0,0);
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
                targetInfo = getObeliskInfo();
                return true;
            }
        };
    }

    public TargetInfo getTargetInfo() {
        return targetInfo;
    }
}
