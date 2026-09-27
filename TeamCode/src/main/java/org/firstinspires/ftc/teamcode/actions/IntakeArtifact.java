package org.firstinspires.ftc.teamcode.actions;

import androidx.annotation.NonNull;

import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Action;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.classes.Intake;
import org.firstinspires.ftc.teamcode.classes.Lights;
import org.firstinspires.ftc.teamcode.classes.Spindexer;

public class IntakeArtifact implements Action {
    private boolean initialized = false;
    private boolean running = true;
    private boolean canceled = false;
    private int targetSlot = -1;
    private boolean waiting = false;
    private boolean timerStarted = false;
    private boolean isAuto = false;

    private double intakeDelay = 0.4;

    private double autoTimeout = 7.5;
    private ElapsedTime timeoutTimer = new ElapsedTime(ElapsedTime.Resolution.MILLISECONDS);
    private ElapsedTime timer = new ElapsedTime(ElapsedTime.Resolution.MILLISECONDS);

    private Intake intake;
    private Spindexer spindexer;
    //private Lights leds;

    public IntakeArtifact(Intake intake, Spindexer spindexer, boolean isAuto){
        this.intake = intake;
        this.spindexer = spindexer;
        this.isAuto = isAuto;
        //this.leds = leds;
    }

    @Override
    public boolean run(@NonNull TelemetryPacket packet) {
        spindexer.updateController();
        if (!canceled) {
            if (!initialized) {
                targetSlot = spindexer.gotoClosestEmptyIntake();
            }
            if ((!initialized && targetSlot != -1)) {
                intake.intakeArtifact();
                initialized = true;
                running = true;
                waiting = true;
                timerStarted = false;
                timeoutTimer.reset();
            } else if (targetSlot == -1) {
                intake.stop();
                running = false;
            }

            if(timeoutTimer.seconds() >= autoTimeout && isAuto){
                intake.stop();
                running = false;
            }
            spindexer.updateController();

            if (running) {
                if(spindexer.atTarget()){
                    spindexer.waggle();
                }
                if (spindexer.atTarget() && spindexer.isGobildaIntakeSlotFull()) {
                    if (waiting) {
                        if (timerStarted) {
                            waiting = timer.seconds() <= intakeDelay;
                        } else {
                            timerStarted = true;
                            timer.reset();
                        }
                    }
                    if (!waiting) {
                        spindexer.setSlot(targetSlot, spindexer.readIntakeHSV());
//                    int x = 0;
//                    while (tempColor == Spindexer.DetectedColor.NONE && x < 5){
//                        x++;
//                        spindexer.setSlot(targetSlot, tempColor);
//                        tempColor =  spindexer.getIntakeColor();
//                    }

                        if (spindexer.getSlotColor(targetSlot) == Spindexer.DetectedColor.NONE) {
                            spindexer.setSlotPurple(targetSlot);
                        }

                        targetSlot = spindexer.gotoClosestEmptyIntake();
                        if (targetSlot != -1) {
                            intake.intakeArtifact();
                            //leds.showDetectedColor();
                        } else {
                            intake.stop();
                            running = false;
                        }
                    }
                }
            }
            if(!running){
                cleanup();
            }
            return running;
        } else {
            canceled = false;
            return false;
        }
//        packet.put("shooterVelocity", vel);

    }

    public void cancel(){
        canceled = true;
        cleanup();
    }

    public void cleanup(){
        running = false;
        intake.stop();
        initialized = false;
        if(!isAuto) {
            spindexer.moveToShooterPos(0);
        }
    }

    public void clearCancel(){
        canceled = false;
    }

    public final void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }



}