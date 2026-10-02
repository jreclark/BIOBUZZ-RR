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

package org.firstinspires.ftc.teamcode;

import android.annotation.SuppressLint;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Action;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.PoseVelocity2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.actions.AlignSpindexer;
import org.firstinspires.ftc.teamcode.actions.IntakeArtifact;
import org.firstinspires.ftc.teamcode.actions.ScanIntake;
import org.firstinspires.ftc.teamcode.actions.ShootAction;
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

@TeleOp(name="BIOBUZZ Drive", group="Comp")
//@Disabled
public class BIOBUZZ_Drive extends LinearOpMode {

    @SuppressLint("DefaultLocale")
    @Override
    public void runOpMode() {
        FtcDashboard dash = FtcDashboard.getInstance();
        List<Action> runningActions = new ArrayList<>();

        TelemetryPacket packet = new TelemetryPacket();
        Robot_BIOBUZZ m_robot = new Robot_BIOBUZZ(hardwareMap, telemetry, new Pose2d(0,0,0));

        int shotRPM = 3250;

        boolean fieldRel = false;
        boolean shooting = false;

        ShootAction shootAction = new ShootAction(m_robot.shooter);

//
//        PIDFCoefficients pidf = m_robot.shooter.showPIDFVals();
//        pidf.p = 22;
//        pidf.i = 3;
//        pidf.d = 0;
//        pidf.f = 0;



        //PIDFVals pidfSel = PIDFVals.P;

////        ButtonState liftTester =  new ButtonState(gamepad1, ButtonState.Button.a);
////        ButtonState loaderTest = new ButtonState(gamepad1, ButtonState.Button.b);
        ButtonState spinUp = new ButtonState(gamepad2, ButtonState.Button.right_bumper);
        ButtonState shootAll = new ButtonState(gamepad2, ButtonState.Button.right_trigger);
        ButtonState motorTest = new ButtonState(gamepad2, ButtonState.Button.left_trigger);
//        ButtonState shootGreen = new ButtonState(gamepad2, ButtonState.Button.left_stick_button);
//        ButtonState shootPurple = new ButtonState(gamepad2, ButtonState.Button.right_stick_button);
//        ButtonState shootAny = new ButtonState(gamepad2, ButtonState.Button.left_bumper);
//        ButtonState scanIntake = new ButtonState(gamepad2, ButtonState.Button.back);
//        ButtonState autoZeroSpindexer = new ButtonState(gamepad2, ButtonState.Button.dpad_up);
//
        ButtonState intakeArtifact = new ButtonState(gamepad2, ButtonState.Button.a);
//        ButtonState zeroSpindexer = new ButtonState(gamepad2, ButtonState.Button.x);
        ButtonState stopIntake = new ButtonState(gamepad2, ButtonState.Button.b);
        ButtonState reverseIntake = new ButtonState(gamepad2, ButtonState.Button.y);

//        ButtonState highRollerTest = new ButtonState(gamepad2, ButtonState.Button.y);
//        ButtonState leftMidRollerTest = new ButtonState(gamepad2, ButtonState.Button.x);
//        ButtonState rightMidRollerTest = new ButtonState(gamepad2, ButtonState.Button.b);
//        ButtonState leftLowRollerTest = new ButtonState(gamepad2, ButtonState.Button.dpad_left);
//        ButtonState rightLowRollerTest = new ButtonState(gamepad2, ButtonState.Button.dpad_right);

//        ButtonState intake0 = new ButtonState(gamepad2, ButtonState.Button.dpad_up);
//        ButtonState intake1 = new ButtonState(gamepad2, ButtonState.Button.dpad_right);
//        ButtonState intake2 = new ButtonState(gamepad2, ButtonState.Button.dpad_down);
//
//        ButtonState selectValUp = new ButtonState(gamepad1, ButtonState.Button.dpad_up);
//        ButtonState selectValDown = new ButtonState(gamepad1, ButtonState.Button.dpad_down);
//        ButtonState valUp = new ButtonState(gamepad1, ButtonState.Button.dpad_right);
//        ButtonState valDown = new ButtonState(gamepad1, ButtonState.Button.dpad_left);
//
//
//
//        ButtonState alignToGoal = new ButtonState(gamepad1, ButtonState.Button.left_trigger);
//        ButtonState runLift = new ButtonState(gamepad1, ButtonState.Button.a);
        ButtonState setRoboRel = new ButtonState(gamepad1, ButtonState.Button.x);
        ButtonState setFieldRel = new ButtonState(gamepad1, ButtonState.Button.b);
        ButtonState resetFieldRel = new ButtonState(gamepad1, ButtonState.Button.dpad_down);
        ButtonState changeAlliance = new ButtonState(gamepad1, ButtonState.Button.back);


        int shooterPos = 0;


        double powerScale=1;

        PoseVelocity2d driveControl;

        telemetry.addData("Status", "Initialized");
        telemetry.update();

        LLResult llResult;

        // Wait for the game to start (driver presses START)
        m_robot.initRobot();

        waitForStart();


        // run until the end of the match (driver presses STOP)
        while (opModeIsActive()) {
            telemetry.addData("Alliance Color: ", MatchInfo.allianceColor.toString());
//            telemetry.addData("Flywheel: ", m_robot.shooter.getSpeed());

            m_robot.shooter.updateController();




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

            if(fieldRel){
                input = m_robot.rotatedVector(input, -m_robot.drive.localizer.getPose().heading.toDouble());
            }


//            m_robot.shooter.setupShooter();

            double rotation = Math.pow(-gamepad1.right_stick_x, 3) * powerScale;


            driveControl = new PoseVelocity2d(input, rotation);

            m_robot.drive.setDrivePowers(driveControl);

            if (changeAlliance.newPress()) {
                MatchInfo.swapAllianceColor();
            }

            if(intakeArtifact.newPress()){
                m_robot.intake.intakeArtifact();
            } else if (intakeArtifact.newRelease()){
                m_robot.intake.stop();
            }

            if(stopIntake.newPress()){
                m_robot.intake.stop();
            } else if (stopIntake.newRelease()){
                m_robot.intake.stop();
            }

            if(reverseIntake.newPress()){
                m_robot.intake.spitArtifacts();
            } else if (reverseIntake.newRelease()){
                m_robot.intake.stop();
            }

            if(spinUp.newPress()){
                m_robot.shooter.spinUp(shotRPM);
            } else if (spinUp.newRelease()){
                {
                    m_robot.shooter.idle();
                }
            }

            if(shootAll.newPress()){
                shootAction.clearCancel();
                m_robot.shooter.setNotIdle();
                m_robot.shooter.setupShooter(shotRPM);
                runningActions.add(shootAction);
            } else if (shootAll.newRelease()){
                shootAction.cancel();
            }

//            if(motorTest.newPress()){
//                m_robot.shooter.motorTest(0.5);
//            } else if (motorTest.newRelease()){
//                m_robot.shooter.motorTest(0);
//            }

            // update running actions
            List<Action> newActions = new ArrayList<>();
            for (Action action : runningActions) {
                action.preview(packet.fieldOverlay());
                if (action.run(packet)) {
                    newActions.add(action);
                }
            }
            runningActions = newActions;

            dash.sendTelemetryPacket(packet);

//            telemetry.addData("currentSpeed: ", m_robot.shooter.getSpeed());

            telemetry.update();

        }
    }
}
