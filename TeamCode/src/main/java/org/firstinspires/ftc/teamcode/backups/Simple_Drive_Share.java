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

package org.firstinspires.ftc.teamcode.backups;

import android.annotation.SuppressLint;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Action;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.PoseVelocity2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.actions.IntakeArtifact;
import org.firstinspires.ftc.teamcode.actions.ScanIntake;
import org.firstinspires.ftc.teamcode.actions.ShootAllVariant;
import org.firstinspires.ftc.teamcode.classes.ButtonState;
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

@TeleOp(name="DriveSimple", group="Test")
@Disabled
public class Simple_Drive_Share extends LinearOpMode {

    @SuppressLint("DefaultLocale")
    @Override
    public void runOpMode() {
        FtcDashboard dash = FtcDashboard.getInstance();
        List<Action> runningActions = new ArrayList<>();

        TelemetryPacket packet = new TelemetryPacket();
        Robot_Share m_robot = new Robot_Share(hardwareMap, telemetry, new Pose2d(0,0,0));

        int shotRPM = 3250;

        Robot_Share.TargetInfo targetInfo;

        telemetry.setMsTransmissionInterval(11);
        m_robot.limelight.pipelineSwitch(0);
        m_robot.limelight.start();
//
//        PIDFCoefficients pidf = m_robot.shooter.showPIDFVals();
//        pidf.p = 22;
//        pidf.i = 3;
//        pidf.d = 0;
//        pidf.f = 0;

        double hoodPos = 0;
        double hoodInc = 0.05;

        double distance;

        boolean fieldRel = false;
        boolean tasteTheRainbow = false;

        ButtonState alignToGoal = new ButtonState(gamepad1, ButtonState.Button.left_trigger);
        ButtonState setRoboRel = new ButtonState(gamepad1, ButtonState.Button.x);
        ButtonState setFieldRel = new ButtonState(gamepad1, ButtonState.Button.b);
        ButtonState resetFieldRel = new ButtonState(gamepad1, ButtonState.Button.dpad_down);
        ButtonState changeAlliance = new ButtonState(gamepad1, ButtonState.Button.back);

        int shooterPos = 0;


        double powerScale = 1;

        PoseVelocity2d driveControl;

        telemetry.addData("Status", "Initialized");
        telemetry.update();

        LLResult llResult;

        // Wait for the game to start (driver presses START)
        waitForStart();

        while (!isStarted() && !isStopRequested()) {
            llResult = m_robot.limelight.getLatestResult();
            if (llResult != null) {
                if (llResult.isValid()) {
                    // Access AprilTag results
                    List<LLResultTypes.FiducialResult> fiducialResults = llResult.getFiducialResults();
                    for (LLResultTypes.FiducialResult fr : fiducialResults) {
                        telemetry.addData("AprilTag", "ID: %d, Family: %s, X: %.2f, Y: %.2f", fr.getFiducialId(), fr.getFamily(), fr.getTargetXDegrees(), fr.getTargetYDegrees());
                    }
                }
            }
        }

        // run until the end of the match (driver presses STOP)
        while (opModeIsActive()) {
            telemetry.addData("Alliance Color: ", MatchInfo.allianceColor.toString());

            //Update limelight tag distance if visible
            targetInfo = m_robot.getTargetTagInfo();
            distance = targetInfo.targetDistance;

            // Drive Code
            if (gamepad1.right_bumper) {
                powerScale=1;
            } else if (gamepad1.left_bumper) {
                powerScale=.3;
            }

            if(setRoboRel.newPress()){
                fieldRel =false;
            } else if (setFieldRel.newPress()){
                fieldRel = true;
            }
            if(resetFieldRel.getCurrentPress()){
                Pose2d currentPose = m_robot.drive.localizer.getPose();
                m_robot.drive.localizer.setPose(new Pose2d(currentPose.position, 0));
            }

            Vector2d input = new Vector2d(
                    Math.pow(-gamepad1.left_stick_y, 3) * powerScale,
                    Math.pow(-gamepad1.left_stick_x, 3) * powerScale);

            m_robot.drive.localizer.update();

            telemetry.addData("Heading:", Math.toDegrees(m_robot.drive.localizer.getPose().heading.toDouble()));

            if(fieldRel){
                input = m_robot.rotatedVector(input, -m_robot.drive.localizer.getPose().heading.toDouble());
            }



            telemetry.addData("Heading:", Math.toDegrees(m_robot.drive.localizer.getPose().heading.toDouble()));
            telemetry.addData("Distance to Target: ", distance);

            double rotation = Math.pow(-gamepad1.right_stick_x, 3) * powerScale;

            if (alignToGoal.getCurrentPress() && targetInfo.tagID != 0) {
                double offset = targetInfo.targetXDegrees;
                rotation = -0.03 * offset;
            }

            driveControl = new PoseVelocity2d(input, rotation);

            m_robot.drive.setDrivePowers(driveControl);
    telemetry.update();

        }
    }
}
