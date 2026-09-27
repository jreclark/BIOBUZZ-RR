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

import static com.qualcomm.robotcore.util.Range.clip;

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

@TeleOp(name="Drive", group="Comp")
//@Disabled
public class Drive_OpMode extends LinearOpMode {

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


        IntakeArtifact intakeAction = new IntakeArtifact(m_robot.intake, m_robot.spindexer, false);
        ShootAllVariant shootAction = new ShootAllVariant(m_robot.shooter, m_robot.spindexer);
        ScanIntake scanAction = new ScanIntake(m_robot.spindexer);
        AlignSpindexer autoAlignAction = new AlignSpindexer(m_robot.spindexer, telemetry);

        //PIDFVals pidfSel = PIDFVals.P;

//        ButtonState liftTester =  new ButtonState(gamepad1, ButtonState.Button.a);
//        ButtonState loaderTest = new ButtonState(gamepad1, ButtonState.Button.b);
        ButtonState spinUp = new ButtonState(gamepad2, ButtonState.Button.right_bumper);
        ButtonState shootAll = new ButtonState(gamepad2, ButtonState.Button.right_trigger);
        ButtonState shootPattern = new ButtonState(gamepad2, ButtonState.Button.left_trigger);
        ButtonState shootGreen = new ButtonState(gamepad2, ButtonState.Button.left_stick_button);
        ButtonState shootPurple = new ButtonState(gamepad2, ButtonState.Button.right_stick_button);
        ButtonState shootAny = new ButtonState(gamepad2, ButtonState.Button.left_bumper);
        ButtonState scanIntake = new ButtonState(gamepad2, ButtonState.Button.back);
        ButtonState autoZeroSpindexer = new ButtonState(gamepad2, ButtonState.Button.dpad_up);

        ButtonState intakeArtifact = new ButtonState(gamepad2, ButtonState.Button.a);
        ButtonState zeroSpindexer = new ButtonState(gamepad2, ButtonState.Button.x);
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


        ButtonState alignToGoal = new ButtonState(gamepad1, ButtonState.Button.left_trigger);
        ButtonState runLift = new ButtonState(gamepad1, ButtonState.Button.a);
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
        m_robot.lights.setYellow();

        // run until the end of the match (driver presses STOP)
        while (opModeIsActive()) {
            telemetry.addData("Alliance Color: ", MatchInfo.allianceColor.toString());

            //Update custom PID here
            m_robot.shooter.updateController();
//            m_robot.spindexer.updateController(); //Spindexer updated in reset button tree

            //Update limelight tag distance if visible
            targetInfo = m_robot.getTargetTagInfo();
            distance = targetInfo.targetDistance;
            shootAction.setShotOrder(MatchInfo.patternGreenPos - 21);


            //m_robot.limelight.updateRobotOrientation(m_robot.drive.localizer.)
//            llResult = m_robot.limelight.getLatestResult();
//            if (llResult != null) {
//                if (llResult.isValid()) {
//                    // Access AprilTag results
//                    List<LLResultTypes.FiducialResult> fiducialResults = llResult.getFiducialResults();
//                    for (LLResultTypes.FiducialResult fr : fiducialResults) {
//                        telemetry.addData("AprilTag", "ID: %d, Family: %s, X: %.2f, Y: %.2f", fr.getFiducialId(), fr.getFamily(), fr.getTargetXDegrees(), fr.getTargetYDegrees());
//                    }
//                } else {
//                    telemetry.addData("Result not valid", 0);
//                }
//            } else {
//                telemetry.addData("Limelight is Null", 0);
//            }

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


            m_robot.shooter.setupShooter(distance);
            telemetry.addData("Heading:", Math.toDegrees(m_robot.drive.localizer.getPose().heading.toDouble()));
            telemetry.addData("Distance to Target: ", distance);
            telemetry.addData("Target RPM: ", m_robot.shooter.getTargetRPM());

            double rotation = Math.pow(-gamepad1.right_stick_x, 3) * powerScale;

            if (alignToGoal.getCurrentPress() && targetInfo.tagID != 0) {
                double offset = targetInfo.targetXDegrees;
                rotation = -0.03 * offset;
                if (rotation < 0.08 && m_robot.lights != null) {
                    m_robot.lights.setRed();
                } else if (m_robot.lights != null) {
                    m_robot.lights.setYellow();
                }
            } else if (m_robot.lights != null && !tasteTheRainbow) {
                m_robot.lights.setYellow();
            }

            driveControl = new PoseVelocity2d(input, rotation);

            m_robot.drive.setDrivePowers(driveControl);

            if(runLift.getCurrentPress() && gamepad1.dpad_left){
                m_robot.lift.runLiftBalanced(false);
//                tasteTheRainbow = true;
            } else {
                m_robot.lift.stopLift();
            }

//            if(gamepad1.dpad_up){
//                m_robot.lift.runLiftBalanced(true);
//            }

            if (changeAlliance.newPress()) {
                MatchInfo.swapAllianceColor();
            }

            if(tasteTheRainbow && m_robot.lights != null){
                m_robot.lights.rainbow();
            }

            if(intakeArtifact.newPress()){
                intakeAction.clearCancel();
                runningActions.add(intakeAction);
            } else if (intakeArtifact.newRelease()){
                intakeAction.cancel();
            }

//            if(scanIntake.newPress()){
//                scanAction.clearCancel();
//                runningActions.add(scanAction);
//            } else if (scanIntake.newRelease()){
//                scanAction.cancel();
//            }

            if(stopIntake.newPress()){
                m_robot.spindexer.manualSpindexer();
            } else if (stopIntake.newRelease()){
                m_robot.spindexer.stop();
            }

            if(reverseIntake.newPress()){
                m_robot.intake.spitArtifacts();
            } else if (reverseIntake.newRelease()){
                m_robot.intake.stop();
            }

            if(autoZeroSpindexer.newPress()){
                autoAlignAction.clearCancel();
                runningActions.add(autoAlignAction);
            } else if(autoZeroSpindexer.newRelease()){
                autoAlignAction.cancel();
                m_robot.spindexer.resetPos();
                m_robot.spindexer.clearAllSlots();
            }

            if(zeroSpindexer.getCurrentPress()){
                m_robot.spindexer.manualSpindexer();
            } else if (zeroSpindexer.newRelease()){
                m_robot.spindexer.stop();
                m_robot.spindexer.resetPos();
            } else if(scanIntake.getCurrentPress()){
                m_robot.spindexer.manualInvertSpindexer();
            } else if (scanIntake.newRelease()){
                m_robot.spindexer.stop();
                m_robot.spindexer.resetPos();
            } else if(!autoZeroSpindexer.getCurrentPress()) {
                telemetry.addData("Updating Spindexer",0);
                m_robot.spindexer.updateController();
            }



//            if(readColors.getCurrentPress()) {
//                m_robot.spindexer.getSpindexerPos();
//
//            }

            if(spinUp.newPress()){
                m_robot.shooter.spinUp(shotRPM);
            } else if (spinUp.newRelease()){
                if(!shootAction.isRunning()){
                    m_robot.shooter.idle();
                }
            }

            if(shootAll.newPress()){
                shootAction.clearCancel();
                m_robot.shooter.setupShooter(distance);
                shootAction.selectShot(ShootAllVariant.ShotType.ShootAll);
                runningActions.add(shootAction);
            } else if (shootAll.newRelease()){
                shootAction.cancel();
            }

            if(shootPattern.newPress()){
                shootAction.clearCancel();
                shootAction.selectShot(ShootAllVariant.ShotType.ShootPattern);
                runningActions.add(shootAction);
            } else if (shootAll.newRelease()){
                shootAction.cancel();
            }

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

            if(shootAny.newPress()){
                shootAction.clearCancel();
                shootAction.selectShot(ShootAllVariant.ShotType.ShootAnySingle);
                runningActions.add(shootAction);
            } else if (shootAll.newRelease()){
                shootAction.cancel();
            }

            /***
             * Test Buttons
             ***/
            //TODO: Remove these when done testing!!

//            if(intake0.newPress()){
//                m_robot.spindexer.moveToIntakePos(0);
//            }
//            if(intake1.newPress()){
//                m_robot.spindexer.moveToIntakePos(1);
//            }
//            if(intake2.newPress()){
//                m_robot.spindexer.moveToIntakePos(2);
//            }




//            if (liftTester.getCurrentPress()) {
//                m_robot.lift.manualClimb(0.2);
//            } else {
//                m_robot.lift.stopLift();
//            }

//            if (loaderTest.newPress()) {
//                m_robot.shooter.loadArtifact(1.0);
//            } else if (loaderTest.newRelease()){
//                m_robot.shooter.loadArtifact(-1.0);
//            }
//            if (liftTester.getCurrentPress()) {
//                m_robot.shooter.loadArtifact(0);
//            }


//            if(shootAll.getCurrentPress()){
//                m_robot.shooter.shoot();
//            } else {
//                m_robot.shooter.stop();
//            }



            /*******************************
             * Servo Direction Test
             *******************************/
//            if(highRollerTest.getCurrentPress()){
//                m_robot.intake.highRoller.setPower(1);
//            } else {
//                m_robot.intake.highRoller.setPower(0);
//            }
//            if(leftMidRollerTest.getCurrentPress()){
//                m_robot.intake.leftMidRoller.setPower(1);
//            } else {
//                m_robot.intake.leftMidRoller.setPower(0);
//            }
//            if(rightMidRollerTest.getCurrentPress()){
//                m_robot.intake.rightMidRoller.setPower(1);
//            } else {
//                m_robot.intake.rightMidRoller.setPower(0);
//            }
//            if(leftLowRollerTest.getCurrentPress()){
//                m_robot.intake.leftLowRoller.setPower(1);
//            } else {
//                m_robot.intake.leftLowRoller.setPower(0);
//            }
//            if(rightLowRollerTest.getCurrentPress()){
//                m_robot.intake.rightLowRoller.setPower(1);
//            } else {
//                m_robot.intake.rightLowRoller.setPower(0);
//            }



            // Telemetry Updates
            for(int x = 0; x <=2; x++){
                telemetry.addLine()
                        .addData("Slot[", x)
                        .addData("] ->", m_robot.spindexer.getSlotColor(x).name());
            }

            telemetry.addData("Spindexer Pos", m_robot.spindexer.getSpindexerPos());

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
