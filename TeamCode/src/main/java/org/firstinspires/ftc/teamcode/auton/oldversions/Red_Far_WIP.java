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
import com.acmerobotics.roadrunner.AccelConstraint;
import com.acmerobotics.roadrunner.Action;
import com.acmerobotics.roadrunner.ParallelAction;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.ProfileAccelConstraint;
import com.acmerobotics.roadrunner.RaceAction;
import com.acmerobotics.roadrunner.TrajectoryActionBuilder;
import com.acmerobotics.roadrunner.TranslationalVelConstraint;
import com.acmerobotics.roadrunner.Vector2d;
import com.acmerobotics.roadrunner.ftc.Actions;
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.actions.AutoTimeoutAction;
import org.firstinspires.ftc.teamcode.actions.IntakeArtifactInOrder;
import org.firstinspires.ftc.teamcode.actions.ScanIntake;
import org.firstinspires.ftc.teamcode.actions.ShootAllVariant;
import org.firstinspires.ftc.teamcode.classes.HeadingStorage;
import org.firstinspires.ftc.teamcode.classes.MatchInfo;

import java.util.ArrayList;
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

@Autonomous(name = "Red_Far_WIP", group = "Test")
@Disabled
public class Red_Far_WIP extends LinearOpMode {

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

        boolean runSecondPickup = true;

        TelemetryPacket packet = new TelemetryPacket();

        ElapsedTime timer = new ElapsedTime(ElapsedTime.Resolution.MILLISECONDS);

        int tagID = 0;
        int shooterRPM = 3250;

        telemetry.addData("Status", "Initialized");
        telemetry.update();

        LLResult llResult;

        double shotAngle = 160.5;

//        Pose2d initialPose = new Pose2d(0, 0, Math.toRadians(0));
        Pose2d initialPose = new Pose2d(64, 15, Math.toRadians(180));

        Pose2d firstShot = new Pose2d(new Vector2d(58, 15), Math.toRadians(shotAngle));

        Pose2d startPickup = new Pose2d(new Vector2d(37, 22), Math.toRadians(90));
        Pose2d finishPickup = new Pose2d(new Vector2d(37, 42), Math.toRadians(90));

        Pose2d start2ndPickup = new Pose2d(new Vector2d(11, 20), Math.toRadians(90));
        Pose2d finish2ndPickup = new Pose2d(new Vector2d(11, 42), Math.toRadians(90));

        Pose2d finalPos = new Pose2d(new Vector2d(0, 38), Math.toRadians(90));
        Pose2d parkHighPos = new Pose2d(new Vector2d(50, 26), Math.toRadians(160.5));

        TranslationalVelConstraint pickupVelConstraint = new TranslationalVelConstraint(4);
        AccelConstraint pickupAccelConstraint = new ProfileAccelConstraint(-50, 2);

        HeadingStorage.zeroOffset = initialPose.heading.log() - Math.toRadians(90);

        Robot m_robot = new Robot(hardwareMap, telemetry, initialPose);

        telemetry.setMsTransmissionInterval(11);
        m_robot.limelight.pipelineSwitch(0);
        m_robot.limelight.start();

//        IntakeArtifact intakeAction = new IntakeArtifact(m_robot.intake, m_robot.spindexer, true);
        IntakeArtifactInOrder intakeAction = new IntakeArtifactInOrder(m_robot.intake, m_robot.spindexer, true);
        ShootAllVariant shootAction = new ShootAllVariant(m_robot.shooter, m_robot.spindexer);
        ScanIntake scanAction = new ScanIntake(m_robot.spindexer);
        AutoTimeoutAction autoTimeoutAction = new AutoTimeoutAction(timer, 45);

        TrajectoryActionBuilder firstShotTraj = m_robot.drive.actionBuilder(initialPose)
                .lineToXLinearHeading(firstShot.position.x, firstShot.heading);

//        TrajectoryActionBuilder firstShotTraj = myBot.getDrive().actionBuilder(initialPose)
//                .splineTo(new Vector2d(48, 48), Math.toRadians(90));

        Action firstShotAction = firstShotTraj.build();

        TrajectoryActionBuilder pickupFirst = firstShotTraj.endTrajectory().fresh()
                .setTangent(Math.toRadians(shotAngle))
                .splineTo(startPickup.position, Math.toRadians(90))
                .splineTo(new Vector2d(startPickup.position.x, startPickup.position.y + 4), Math.toRadians(90), pickupVelConstraint)
                .splineTo(finishPickup.position, Math.toRadians(90), new TranslationalVelConstraint(50), pickupAccelConstraint);

        Action pickupFirstAction = pickupFirst.build();

        TrajectoryActionBuilder shootSecondTraj = pickupFirst.endTrajectory().fresh()
                .setTangent(Math.toRadians(-90))
                .splineToLinearHeading(firstShot, Math.toRadians(shotAngle) - Math.toRadians(180));

        Action shootSecondAction = shootSecondTraj.build();

        TrajectoryActionBuilder pickupSecond = shootSecondTraj.endTrajectory().fresh()
                .setTangent(Math.toRadians(shotAngle))
                .splineTo(start2ndPickup.position, Math.toRadians(90))
                .splineTo(new Vector2d(start2ndPickup.position.x, start2ndPickup.position.y + 4), Math.toRadians(90), pickupVelConstraint)
                .splineTo(finish2ndPickup.position, Math.toRadians(90), new TranslationalVelConstraint(50), pickupAccelConstraint);

//        TrajectoryActionBuilder pickupSecond = shootSecondTraj.endTrajectory().fresh()
//                .setTangent(Math.toRadians(180))
//                .splineToSplineHeading(start2ndPickup, Math.toRadians(90))
//                .splineToSplineHeading(finish2ndPickup, Math.toRadians(90), pickupVelConstraint, pickupAccelConstraint);

        Action pickupSecondAction = pickupSecond.build();

        TrajectoryActionBuilder finalPosTraj = pickupSecond.endTrajectory().fresh()
                .setTangent(Math.toRadians(-90))
                .splineToLinearHeading(finalPos, Math.toRadians(179.9));

        Action finalPosAction = finalPosTraj.build();

        //Third shot here
        TrajectoryActionBuilder thirdShotTraj = pickupSecond.endTrajectory().fresh()
                .setTangent(Math.toRadians(-90))
                .splineToLinearHeading(firstShot, Math.toRadians(shotAngle) - Math.toRadians(180));

        Action thirdShotAction = thirdShotTraj.build();

        TrajectoryActionBuilder shotParkTraj = thirdShotTraj.endTrajectory().fresh()
                .lineToX(firstShot.position.x + 10);

        Action shotParkAction = shotParkTraj.build();

        // Wait for the game to start (driver presses START)
        MatchInfo.setAllianceColor(MatchInfo.AllianceColor.RED);
        m_robot.initRobot();
        m_robot.spindexer.initSpindexerforAuton();

        if(m_robot.lights != null){
            m_robot.lights.setYellow();
        }

        m_robot.spindexer.showSlots();
//        sleep(5000);

        while (!isStarted() && !isStopRequested()) {
            if(m_robot.lights != null){
                m_robot.lights.rainbow();
            }
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
        }

    timer.reset();



        if(tagID <21 || tagID >23){
            tagID = 21;
        }

        MatchInfo.patternGreenPos = tagID - 21;

        shootAction.setShotOrder(tagID - 21);
        shootAction.selectShot(ShootAllVariant.ShotType.ShootPattern);
        m_robot.shooter.setTargetSpeed(shooterRPM);
        m_robot.shooter.updateController();

        Actions.runBlocking(new RaceAction(
                firstShotAction,
                m_robot.shooter.updateFlywheel()));
        Actions.runBlocking(shootAction);

//        m_robot.shooter.setHood(0.25);
////        sleep(2000);
//        m_robot.shooter.setHood(0);
        telemetry.addData("First Shot Done:", 0);
        m_robot.spindexer.showSlots();
        telemetry.update();
//        sleep(5000);

        Actions.runBlocking(new ParallelAction(
                intakeAction,
                pickupFirstAction
        ));

        m_robot.spindexer.initSpindexer(0);

        m_robot.spindexer.showSlots();
        telemetry.addData("Shot Type: ", m_robot.spindexer.selectAShot(shootAction).toString());
        telemetry.update();
//        sleep(5000);

        m_robot.shooter.setTargetSpeed(shooterRPM);
        m_robot.shooter.updateController();

        Actions.runBlocking(new RaceAction(
                shootSecondAction,
                m_robot.shooter.updateFlywheel()));
        Actions.runBlocking(shootAction);

        m_robot.spindexer.showSlots();
//        sleep(5000);

        if(runSecondPickup) {
            Actions.runBlocking(new ParallelAction(
                    intakeAction,
                    pickupSecondAction
            ));

            m_robot.spindexer.initSpindexer(1);

            m_robot.spindexer.showSlots();
//        sleep(5000);

            if (timer.seconds() >25) {
                Actions.runBlocking(finalPosAction);
            } else {
                Actions.runBlocking(new RaceAction(
                        autoTimeoutAction,
                        shootSecondAction,
                        m_robot.shooter.updateFlywheel()));
                Actions.runBlocking(new RaceAction(
                        autoTimeoutAction,
                        shootAction));
                Actions.runBlocking(shotParkAction);
            }
        } else {
            Actions.runBlocking(shotParkAction);
        }


//        m_robot.shooter.setTargetSpeed(shooterRPM);
//        m_robot.shooter.updateController();
//
//        m_robot.spindexer.selectAShot(shootAction);
//
//        Actions.runBlocking(new RaceAction(
//                shootThirdAction,
//                m_robot.shooter.updateFlywheel()));
//        Actions.runBlocking(shootAction);
//
//        Actions.runBlocking(finalPosAction);

    }




}
