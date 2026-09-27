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
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.actions.IntakeArtifact;
import org.firstinspires.ftc.teamcode.actions.IntakeArtifactInOrder;
import org.firstinspires.ftc.teamcode.actions.ScanIntake;
import org.firstinspires.ftc.teamcode.actions.ShootAllVariant;
import org.firstinspires.ftc.teamcode.classes.ButtonState;

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

@TeleOp(name="TEST", group="TEST")
@Disabled
public class TEST_OpMode extends LinearOpMode {

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


        IntakeArtifactInOrder intakeAction = new IntakeArtifactInOrder(m_robot.intake, m_robot.spindexer, false);
        ShootAllVariant shootAction = new ShootAllVariant(m_robot.shooter, m_robot.spindexer);
        ScanIntake scanAction = new ScanIntake(m_robot.spindexer);

        //PIDFVals pidfSel = PIDFVals.P;

////        ButtonState liftTester =  new ButtonState(gamepad1, ButtonState.Button.a);
////        ButtonState loaderTest = new ButtonState(gamepad1, ButtonState.Button.b);
//        ButtonState spinUp = new ButtonState(gamepad2, ButtonState.Button.right_bumper);
        ButtonState shootAll = new ButtonState(gamepad1, ButtonState.Button.right_trigger);
//        ButtonState shootPattern = new ButtonState(gamepad1, ButtonState.Button.left_trigger);
        ButtonState shootGreen = new ButtonState(gamepad1, ButtonState.Button.left_stick_button);
        ButtonState shootPurple = new ButtonState(gamepad2, ButtonState.Button.right_stick_button);
//        ButtonState shootAny = new ButtonState(gamepad2, ButtonState.Button.left_bumper);
//        ButtonState scanIntake =new ButtonState(gamepad2, ButtonState.Button.back);

        ButtonState intakeArtifactGPP = new ButtonState(gamepad1, ButtonState.Button.y);
        ButtonState intakeArtifactPGP = new ButtonState(gamepad1, ButtonState.Button.b);
        ButtonState intakeArtifactPPG = new ButtonState(gamepad1, ButtonState.Button.a);
        ButtonState setShooter1 = new ButtonState(gamepad1, ButtonState.Button.dpad_up);
        ButtonState setShooter2 = new ButtonState(gamepad1, ButtonState.Button.dpad_right);
        ButtonState setShooter3 = new ButtonState(gamepad1, ButtonState.Button.dpad_down);

        ButtonState zeroSpindexer = new ButtonState(gamepad1, ButtonState.Button.x);
//        ButtonState stopIntake = new ButtonState(gamepad2, ButtonState.Button.b);
//        ButtonState reverseIntake = new ButtonState(gamepad2, ButtonState.Button.y);

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


        ButtonState alignToGoal = new ButtonState(gamepad1, ButtonState.Button.left_trigger);
//        ButtonState runLift = new ButtonState(gamepad1, ButtonState.Button.a);
//        ButtonState setRoboRel = new ButtonState(gamepad1, ButtonState.Button.x);
//        ButtonState setFieldRel = new ButtonState(gamepad1, ButtonState.Button.b);
//        ButtonState resetFieldRel = new ButtonState(gamepad1, ButtonState.Button.dpad_down);


        int shooterPos = 0;


        double powerScale=1;

        PoseVelocity2d driveControl;

        telemetry.addData("Status", "Initialized");
        telemetry.update();

        LLResult llResult;

        // Wait for the game to start (driver presses START)
        m_robot.intake.stop();
        m_robot.shooter.loadArtifact(0);
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
            m_robot.shooter.updateController();

            //m_robot.limelight.updateRobotOrientation(m_robot.drive.localizer.)
            llResult = m_robot.limelight.getLatestResult();
            if (llResult != null) {
                if (llResult.isValid()) {
                    // Access AprilTag results
                    List<LLResultTypes.FiducialResult> fiducialResults = llResult.getFiducialResults();
                    for (LLResultTypes.FiducialResult fr : fiducialResults) {
                        telemetry.addData("AprilTag", "ID: %d, Family: %s, X: %.2f, Y: %.2f", fr.getFiducialId(), fr.getFamily(), fr.getTargetXDegrees(), fr.getTargetYDegrees());
                    }
                } else {
                    telemetry.addData("Result not valid", 0);
                }
            } else {
                telemetry.addData("Limelight is Null", 0);
            }

            // Drive Code
            if (gamepad1.right_bumper) {
                powerScale=1;
            } else if (gamepad1.left_bumper) {
                powerScale=.3;
            }

                        Vector2d input = new Vector2d(
                    Math.pow(-gamepad1.left_stick_y, 3) * powerScale,
                    Math.pow(-gamepad1.left_stick_x, 3) * powerScale);

            m_robot.drive.localizer.update();

            telemetry.addData("Heading:", Math.toDegrees(m_robot.drive.localizer.getPose().heading.toDouble()));

            if(fieldRel){
                input = m_robot.rotatedVector(input, -m_robot.drive.localizer.getPose().heading.toDouble());
            }

            targetInfo = m_robot.getAprilTagInfo();

            distance = targetInfo.targetDistance;

            telemetry.addData("Heading:", Math.toDegrees(m_robot.drive.localizer.getPose().heading.toDouble()));
            telemetry.addData("Distance to Target: ", distance);

            double rotation = Math.pow(-gamepad1.right_stick_x, 3) * powerScale;

            if (alignToGoal.getCurrentPress() && llResult != null) {
                double offset = targetInfo.targetXDegrees;
                rotation = -0.03 * offset;
            }

            driveControl = new PoseVelocity2d(input, rotation);

            m_robot.drive.setDrivePowers(driveControl);

            if(intakeArtifactGPP.newPress()){
                m_robot.spindexer.initSpindexer(0);
                intakeAction.clearCancel();
                runningActions.add(intakeAction);
            } else if (intakeArtifactGPP.newRelease()){
                intakeAction.cancel();
            }

            if(intakeArtifactPGP.newPress()){
                m_robot.spindexer.initSpindexer(1);
                intakeAction.clearCancel();
                runningActions.add(intakeAction);
            } else if (intakeArtifactGPP.newRelease()){
                intakeAction.cancel();
            }

            if(intakeArtifactPPG.newPress()){
                m_robot.spindexer.initSpindexer(2);
                intakeAction.clearCancel();
                runningActions.add(intakeAction);
            } else if (intakeArtifactGPP.newRelease()){
                intakeAction.cancel();
            }

            if(zeroSpindexer.getCurrentPress()){
                m_robot.spindexer.manualSpindexer();
            } else if (zeroSpindexer.newRelease()){
                m_robot.spindexer.stop();
                m_robot.spindexer.zeroSpindexer();
            }

            if(shootAll.newPress()){
                shootAction.clearCancel();
                m_robot.shooter.setupShooter(distance);
                shootAction.selectShot(ShootAllVariant.ShotType.ShootAll);
                runningActions.add(shootAction);
            } else if (shootAll.newRelease()){
                shootAction.cancel();
            }

            if(setShooter1.newPress()){
                m_robot.spindexer.moveToShooterPos(0);
            }
            if(setShooter2.newPress()){
                m_robot.spindexer.moveToShooterPos(1);
            }
            if(setShooter3.newPress()){
                m_robot.spindexer.moveToShooterPos(2);
            }

//            if(shootPattern.newPress()){
//                shootAction.clearCancel();
//                shootAction.selectShot(ShootAllVariant.ShotType.ShootPattern);
//                runningActions.add(shootAction);
//            } else if (shootAll.newRelease()){
//                shootAction.cancel();
//            }

            if(shootGreen.newPress()){
                shootAction.clearCancel();
                shootAction.selectShot(ShootAllVariant.ShotType.ShootGreen);
                runningActions.add(shootAction);
            } else if (shootAll.newRelease()){
                shootAction.cancel();
            }

            if(shootPurple.newPress()){
                shootAction.clearCancel();
                shootAction.selectShot(ShootAllVariant.ShotType.ShootPurple);
                runningActions.add(shootAction);
            } else if (shootAll.newRelease()){
                shootAction.cancel();
            }




            // Telemetry Updates
            for(int x = 0; x <=2; x++){
                telemetry.addLine()
                        .addData("Slot[", x)
                        .addData("] ->", m_robot.spindexer.getSlotColor(x).name());
            }

//            telemetry.addData("Spindexer Pos", m_robot.spindexer.getSpindexerPos());

//
//            if(valUp.newPress()){
//                hoodPos = clip(hoodPos + hoodInc,-1, 1);
//                m_robot.shooter.setHood(hoodPos);
//            }
//            if(valDown.newPress()){
//                hoodPos = clip(hoodPos - hoodInc,-1, 1);
//                m_robot.shooter.setHood(hoodPos);
//            }

            telemetry.addData("Hood Pos", hoodPos);


//            telemetry.addData("Modifying ", pidfSel.name());
//
//            if(valUp.newPress()){
//                switch(pidfSel) {
//                    case P:
//                        pidf.p = pidf.p + 0.1;
//                        break;
//                    case I:
//                        pidf.i = pidf.i + 0.05;
//                        break;
//                    case D:
//                        pidf.d = pidf.d + 0.1;
//                        break;
//                    case F:
//                        pidf.f = pidf.f + 0.1;
//                        break;
//                }
//                m_robot.shooter.setPIDF(pidf);
//            }
//            if(valDown.newPress()){
//                switch(pidfSel) {
//                    case P:
//                        pidf.p = pidf.p - 0.1;
//                        break;
//                    case I:
//                        pidf.i = pidf.i - 0.05;
//                        break;
//                    case D:
//                        pidf.d = pidf.d - 0.1;
//                        break;
//                    case F:
//                        pidf.f = pidf.f - 0.1;
//                        break;
//                }
//                m_robot.shooter.setPIDF(pidf);
//            }
//            if(spinPwrUp.newPress()){
//                m_robot.spindexer.spinPwr = m_robot.spindexer.spinPwr +0.05;
//                if(m_robot.spindexer.spinPwr > 1.0) {m_robot.spindexer.spinPwr = 0.05;}
//            }
//            if (spinPwrDwn.newPress()) {
//                m_robot.spindexer.spinPwr = m_robot.spindexer.spinPwr -0.05;
//                if(m_robot.spindexer.spinPwr < 0.0) {m_robot.spindexer.spinPwr = 1.00;
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

            telemetry.update();

        }
    }
}
