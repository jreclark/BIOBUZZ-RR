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

package org.firstinspires.ftc.teamcode.tuning;

import android.annotation.SuppressLint;

import com.ThermalEquilibrium.homeostasis.Parameters.FeedforwardCoefficients;
import com.ThermalEquilibrium.homeostasis.Parameters.PIDCoefficientsEx;
import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Action;
import com.acmerobotics.roadrunner.Pose2d;
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.Robot;
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
@Config
//@Disabled
@TeleOp(name = "Spindexer PIDF_Ex", group = "Tuning")
//@Disabled
public class Spindexer_PIDFEx extends LinearOpMode {

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

    public static int targetRPM = 3250;
    //PIDEx Setup
    public static double Kp = 0;
    public static double Ki = 0;
    public static double Kd = 0;
    public static double Kv = 0; //1.1;
    public static double Ka = 0; //0.2;
    public static double Ks = 0; //0.001;
    public static double targetAccelTime = 0.5; //seconds



    @SuppressLint("DefaultLocale")
    @Override
    public void runOpMode() {
        FtcDashboard dash = FtcDashboard.getInstance();
        List<Action> runningActions = new ArrayList<>();

        TelemetryPacket packet = new TelemetryPacket();

        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());

        Robot m_robot = new Robot(hardwareMap, telemetry, new Pose2d(0, 0, 0));

        ButtonState pos0 = new ButtonState(gamepad1, ButtonState.Button.x);
        ButtonState pos1 = new ButtonState(gamepad1, ButtonState.Button.y);
        ButtonState pos2 = new ButtonState(gamepad1, ButtonState.Button.b);
        ButtonState setVals = new ButtonState(gamepad1, ButtonState.Button.a);
//        ButtonState shoot = new ButtonState(gamepad1, ButtonState.Button.right_trigger);

        PIDCoefficientsEx pidExCoeff = new PIDCoefficientsEx(Kp, Ki, Kd, 0.9, 10, 1);
        FeedforwardCoefficients ffCoeff = new FeedforwardCoefficients(Kv,Ka,Ks);

        m_robot.shooter.setPIDFExCoeeficients(pidExCoeff, ffCoeff);

        int tagID = 0;

        telemetry.addData("Status", "Initialized");
        telemetry.update();

        LLResult llResult;

        // Wait for the game to start (driver presses START)
        m_robot.intake.stop();
        m_robot.shooter.loadArtifact(0);
        m_robot.spindexer.initSpindexerforAuton();
        m_robot.shooter.idle();


        waitForStart();

        while(opModeIsActive()){
            m_robot.spindexer.updateController();

            pidExCoeff = new PIDCoefficientsEx(Kp, Ki, Kd, 0.9, 10, 1);
            ffCoeff = new FeedforwardCoefficients(Kv,Ka,Ks);

            if(pos0.newPress()){
                m_robot.spindexer.moveToIntakePos(0);
            }
            if(pos1.newPress()){
                m_robot.spindexer.moveToIntakePos(1);
            }
            if(pos2.newPress()){
                m_robot.spindexer.moveToIntakePos(2);
            }
            if(setVals.newPress()){
                m_robot.spindexer.setPIDFExCoeeficients(pidExCoeff);
                m_robot.spindexer.Ks = this.Ks;
            }

//            if(shoot.getCurrentPress()){
//                m_robot.shooter.loadArtifact(1.0);
//            } else if (shoot.newRelease()){
//                m_robot.shooter.loadArtifact(0);
//            }

            telemetry.addData("Setpoint:", m_robot.spindexer.rotationMotor.getTargetPosition());
            telemetry.addData("Current Location:", m_robot.spindexer.getSpindexerPos());
            telemetry.addData("Kp:", Kp);
            telemetry.addData("Ki:", Ki);
            telemetry.addData("Kd:", Kd);
//            telemetry.addData("Kv:", Kv);
//            telemetry.addData("Ka:", Ka);
            telemetry.addData("Ks:", Ks);
//            telemetry.addData("targetAccelTime:", targetAccelTime);
            telemetry.update();

        }



    }


}
