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

package org.firstinspires.ftc.teamcode.auton;

import android.annotation.SuppressLint;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Action;
import com.acmerobotics.roadrunner.ParallelAction;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.RaceAction;
import com.acmerobotics.roadrunner.TrajectoryActionBuilder;
import com.acmerobotics.roadrunner.TranslationalVelConstraint;
import com.acmerobotics.roadrunner.Vector2d;
import com.acmerobotics.roadrunner.ftc.Actions;
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.actions.IntakeArtifactInOrder;
import org.firstinspires.ftc.teamcode.actions.ScanIntake;
import org.firstinspires.ftc.teamcode.actions.ShootAllVariant;
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

@Autonomous(name = "Motion_Test", group = "Test")
//@Disabled
public class Motion_Test extends LinearOpMode {

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

        Pose2d initialPose = new Pose2d(0, 0, Math.toRadians(180));

        Pose2d firstShot = new Pose2d(new Vector2d(0, 24), Math.toRadians(180));

        Pose2d oneByOne = new Pose2d(new Vector2d(-24, 24), Math.toRadians(180));


        Robot m_robot = new Robot(hardwareMap, telemetry, initialPose);



        TrajectoryActionBuilder firstShotTraj = m_robot.drive.actionBuilder(initialPose)
                .setTangent(Math.toRadians(180))
                .splineTo(oneByOne.position,Math.toRadians(90));

//        TrajectoryActionBuilder firstShotTraj =  m_robot.drive.actionBuilder(initialPose)
//                .setTangent(Math.toRadians(180))
//                .splineToConstantHeading(oneByOne.position,Math.toRadians(90));
//
//        TrajectoryActionBuilder firstShotTraj = m_robot.drive.actionBuilder(initialPose)
//                .setTangent(Math.toRadians(90))
//                .splineTo(firstShot.position, Math.toRadians(90));

        Action firstShotAction = firstShotTraj.build();

        // Wait for the game to start (driver presses START)
        MatchInfo.setAllianceColor(MatchInfo.AllianceColor.BLUE);
        m_robot.initRobot();
        m_robot.spindexer.initSpindexerforAuton();

        waitForStart();

        Actions.runBlocking(firstShotAction);


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
