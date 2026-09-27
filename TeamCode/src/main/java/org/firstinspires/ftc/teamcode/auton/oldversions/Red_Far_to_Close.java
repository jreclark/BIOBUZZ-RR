/* Copyright (c) 2017 FIRST. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted (subject to the limitations in the disclaimer below) provided that
 * the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list
 * of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * Neither the name of FIRST nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
 * LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.firstinspires.ftc.teamcode.auton.oldversions;

import android.annotation.SuppressLint;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Action;
import com.acmerobotics.roadrunner.AngularVelConstraint;
import com.acmerobotics.roadrunner.MinVelConstraint;
import com.acmerobotics.roadrunner.ParallelAction;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.RaceAction;
import com.acmerobotics.roadrunner.TrajectoryActionBuilder;
import com.acmerobotics.roadrunner.TranslationalVelConstraint;
import com.acmerobotics.roadrunner.Vector2d;
import com.acmerobotics.roadrunner.VelConstraint;
import com.acmerobotics.roadrunner.ftc.Actions;
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.actions.IntakeArtifact;
import org.firstinspires.ftc.teamcode.actions.ScanIntake;
import org.firstinspires.ftc.teamcode.actions.ShootAllVariant;
import org.firstinspires.ftc.teamcode.classes.HeadingStorage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/*
 * This file contains an minimal example of a Linear "OpMode". An OpMode is a 'program' that runs in either
 * the autonomous or the teleop period of an FTC match. The names of OpModes appear on the menu
 * of the FTC Driver Station. When a selection is made from the menu, the corresponding OpMode
 * class is instantiated on the Robot Controller and executed.
 *
 * This particular OpMode just executes a basic Tank Drive Teleop for a two wheeled robot
 * It includes all the skeletal structure that all linear OpModes contain.
 *
 * Use Android Studio to Copy this Class, and Paste it into your team's code folder with a new name.
 * Remove or comment out the @Disabled line to add this OpMode to the Driver Station OpMode list
 */

@Autonomous(name = "Red_Far_to_Close", group = "Comp")
@Disabled
public class Red_Far_to_Close extends LinearOpMode {

    enum PIDFVals {
        P,
        I,
        D,
        F;

        private static final PIDFVals[] vals = values();

        public PIDFVals next() {
            return vals[(this.ordinal() + 1) % vals.length];
        }

        public PIDFVals prev() {
            return vals[(this.ordinal() - 1 + vals.length) % vals.length];
        }
    }


    @SuppressLint("DefaultLocale")
    @Override
    public void runOpMode() {
        FtcDashboard dash = FtcDashboard.getInstance();
        List<Action> runningActions = new ArrayList<>();

        TelemetryPacket packet = new TelemetryPacket();

        int tagID = 0;
        int shooterRPM = 3250;

        telemetry.addData("Status", "Initialized");
        telemetry.update();

        LLResult llResult;

        Pose2d initialPose = new Pose2d(64, 15, Math.toRadians(180));
        HeadingStorage.zeroOffset = initialPose.heading.log() - Math.toRadians(90);

        Pose2d firstShot = new Pose2d(new Vector2d(58, 15), Math.toRadians(160));
        Pose2d startPickup = new Pose2d(new Vector2d(35, 19), Math.toRadians(90));
        Pose2d finishPickup = new Pose2d(new Vector2d(35, 42), Math.toRadians(90));
        Pose2d start2ndPickup = new Pose2d(new Vector2d(11, 19), Math.toRadians(90));
        Pose2d finish2ndPickup = new Pose2d(new Vector2d(11, 42), Math.toRadians(90));
        Pose2d lastShot = new Pose2d(new Vector2d(0, 22), Math.toRadians(135));
        Pose2d finalPos = new Pose2d(new Vector2d(52, 21), Math.toRadians(160));

        VelConstraint pickupVelConstraint = new MinVelConstraint(Arrays.asList(
                new TranslationalVelConstraint(50.0),
                new AngularVelConstraint(Math.PI / 2)
        ));

        Robot m_robot = new Robot(hardwareMap, telemetry, initialPose);

        telemetry.setMsTransmissionInterval(11);
        m_robot.limelight.pipelineSwitch(0);
        m_robot.limelight.start();

        IntakeArtifact intakeAction = new IntakeArtifact(m_robot.intake, m_robot.spindexer, true);
        ShootAllVariant shootAction = new ShootAllVariant(m_robot.shooter, m_robot.spindexer);
        ScanIntake scanAction = new ScanIntake(m_robot.spindexer);

        TrajectoryActionBuilder firstShotTraj = m_robot.drive.actionBuilder(initialPose)
                .setTangent(Math.toRadians(180))
                .lineToXLinearHeading(firstShot.position.x, firstShot.heading);

        Action firstShotAction = firstShotTraj.build();

        TrajectoryActionBuilder pickupFirst = firstShotTraj.endTrajectory().fresh()
                .setTangent(Math.toRadians(180))
                .splineToSplineHeading(startPickup, Math.toRadians(90))
                .splineToSplineHeading(finishPickup, Math.toRadians(90), new TranslationalVelConstraint(7));

        Action pickupFirstAction = pickupFirst.build();

        TrajectoryActionBuilder shootSecondTraj = pickupFirst.endTrajectory().fresh()
                .setTangent(Math.toRadians(-90))
                .splineToLinearHeading(firstShot, Math.toRadians(160) - Math.toRadians(180));

        Action shootSecondAction = shootSecondTraj.build();

        TrajectoryActionBuilder pickupSecond = shootSecondTraj.endTrajectory().fresh()
                .setTangent(Math.toRadians(180))
                .splineToSplineHeading(start2ndPickup, Math.toRadians(90))
                .splineToSplineHeading(finish2ndPickup, Math.toRadians(90), new TranslationalVelConstraint(7));

        Action pickupSecondAction = pickupSecond.build();

        TrajectoryActionBuilder shootThirdTraj = pickupSecond.endTrajectory().fresh()
                .setTangent(Math.toRadians(-135))
                .splineToLinearHeading(lastShot, Math.toRadians(-135));

        Action shootThirdAction = shootThirdTraj.build();

        TrajectoryActionBuilder finalPosTraj = shootThirdTraj.endTrajectory().fresh()
                .setTangent(Math.toRadians(45))
                .splineToLinearHeading(finalPos, Math.toRadians(45));

        Action finalPosAction = finalPosTraj.build();


        // Wait for the game to start (driver presses START)
        m_robot.intake.stop();
        m_robot.shooter.loadArtifact(0);
        m_robot.spindexer.initSpindexerforAuton();

        while (!isStarted() && !isStopRequested()) {
            llResult = m_robot.limelight.getLatestResult();
            if (llResult != null) {
                if (llResult.isValid()) {
                    // Access AprilTag results
                    List<LLResultTypes.FiducialResult> fiducialResults = llResult.getFiducialResults();
                    for (LLResultTypes.FiducialResult fr : fiducialResults) {
                        telemetry.addData("AprilTag", "ID: %d, Family: %s, X: %.2f, Y: %.2f", fr.getFiducialId(), fr.getFamily(), fr.getTargetXDegrees(), fr.getTargetYDegrees());
                        telemetry.update();
                    }
                }
            }
        }

        llResult = m_robot.limelight.getLatestResult();
        if (llResult != null) {
            if (llResult.isValid()) {
                // Access AprilTag results
                List<LLResultTypes.FiducialResult> fiducialResults = llResult.getFiducialResults();
                for (LLResultTypes.FiducialResult fr : fiducialResults) {
                    if (fr.getFiducialId() >= 21 && fr.getFiducialId() <= 23) {
                        tagID = fr.getFiducialId();
                    }
                }
            }
        }

        if(tagID <21 || tagID >23){
            tagID = 21;
        }

        shootAction.setShotOrder(tagID - 21);
        shootAction.selectShot(ShootAllVariant.ShotType.ShootPattern);
        m_robot.shooter.setTargetSpeed(shooterRPM);
        m_robot.shooter.updateController();

        for(int x = 0; x <=2; x++){
            telemetry.addLine()
                    .addData("Slot[", x)
                    .addData("] ->", m_robot.spindexer.getSlotColor(x).name());
        }
        telemetry.update();

        Actions.runBlocking(new RaceAction(
                firstShotAction,
                m_robot.shooter.updateFlywheel()));
        Actions.runBlocking(shootAction);

        for(int x = 0; x <=2; x++){
            telemetry.addLine()
                    .addData("Slot[", x)
                    .addData("] ->", m_robot.spindexer.getSlotColor(x).name());
        }
        telemetry.update();

        m_robot.spindexer.selectAShot(shootAction);

        Actions.runBlocking(new ParallelAction(
                intakeAction,
                pickupFirstAction
        ));

        for(int x = 0; x <=2; x++){
            telemetry.addLine()
                    .addData("Slot[", x)
                    .addData("] ->", m_robot.spindexer.getSlotColor(x).name());
        }
        telemetry.update();

        m_robot.shooter.setTargetSpeed(shooterRPM);
        m_robot.shooter.updateController();

        Actions.runBlocking(new RaceAction(
                shootSecondAction,
                m_robot.shooter.updateFlywheel()));
        Actions.runBlocking(shootAction);

        for(int x = 0; x <=2; x++){
            telemetry.addLine()
                    .addData("Slot[", x)
                    .addData("] ->", m_robot.spindexer.getSlotColor(x).name());
        }
        telemetry.update();

        Actions.runBlocking(new ParallelAction(
                intakeAction,
                pickupSecondAction
        ));

        for(int x = 0; x <=2; x++){
            telemetry.addLine()
                    .addData("Slot[", x)
                    .addData("] ->", m_robot.spindexer.getSlotColor(x).name());
        }
        telemetry.update();

        m_robot.shooter.setTargetSpeed(shooterRPM);
        m_robot.shooter.updateController();

        m_robot.spindexer.selectAShot(shootAction);

        Actions.runBlocking(new RaceAction(
                shootThirdAction,
                m_robot.shooter.updateFlywheel()));
        Actions.runBlocking(shootAction);

        Actions.runBlocking(finalPosAction);

    }


}
