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

import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;
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

@TeleOp(name="Lift Test", group="Test")
//@Disabled
public class Lift_Test extends LinearOpMode {

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
        Robot m_robot = new Robot(hardwareMap, telemetry, new Pose2d(0,0,0));

        int shotRPM = 3250;

        Robot.TargetInfo targetInfo;

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

        //PIDFVals pidfSel = PIDFVals.P;
//


        ButtonState alignToGoal = new ButtonState(gamepad1, ButtonState.Button.left_trigger);
        ButtonState runLift = new ButtonState(gamepad1, ButtonState.Button.a);
//        ButtonState setRoboRel = new ButtonState(gamepad1, ButtonState.Button.x);
        ButtonState runRight = new ButtonState(gamepad1, ButtonState.Button.b);
        ButtonState runLeft = new ButtonState(gamepad1, ButtonState.Button.x);
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

//        waitForStart();
        tasteTheRainbow = true;



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
            if(tasteTheRainbow && m_robot.lights != null){
                m_robot.lights.rainbow();
            }
        }
        tasteTheRainbow = false;
        m_robot.lights.setOff();

        // run until the end of the match (driver presses STOP)
        while (opModeIsActive()) {
            telemetry.addData("Alliance Color: ", MatchInfo.allianceColor.toString());

            if(runLift.getCurrentPress() && gamepad1.dpad_left){
                m_robot.lift.runLiftBalanced(false);
                tasteTheRainbow = true;
            } else {
                m_robot.lift.stopLift();
            }

            if(gamepad1.dpad_up){
                m_robot.lift.runLift();
            }

            if(runLeft.getCurrentPress()){
                m_robot.lift.leftClimbMotor.setPower(0.6);
            } else if(runLeft.newRelease()){
                m_robot.lift.leftClimbMotor.setPower(0);
            }
            if(runRight.getCurrentPress()){
                m_robot.lift.rightClimbMotor.setPower(0.6);
            } else if(runRight.newRelease()){
                m_robot.lift.rightClimbMotor.setPower(0);
            }

//            m_robot.lift.runLiftBalanced(true);
            telemetry.addData("Left Pos: ", m_robot.lift.leftClimbMotor.getCurrentPosition());
            telemetry.addData("Right Pos: ", m_robot.lift.rightClimbMotor.getCurrentPosition());

            telemetry.addData("Left Current: ", m_robot.lift.leftClimbMotor.getCurrent(CurrentUnit.MILLIAMPS));
            telemetry.addData("Right Current: ", m_robot.lift.rightClimbMotor.getCurrent(CurrentUnit.MILLIAMPS));

            telemetry.addData("Hood Pos", hoodPos);


            telemetry.update();

        }
    }
}
