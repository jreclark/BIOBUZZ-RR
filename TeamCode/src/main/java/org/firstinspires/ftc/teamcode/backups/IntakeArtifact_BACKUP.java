package org.firstinspires.ftc.teamcode.backups;

import androidx.annotation.NonNull;

import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Action;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.classes.Intake;
import org.firstinspires.ftc.teamcode.classes.Spindexer;

public class IntakeArtifact_BACKUP implements Action {
    private boolean initialized = false;
    private boolean running = true;
    private boolean canceled = false;
    private int targetSlot = -1;
    private boolean isAuto = false;

    private double autoTimeout = 7.5;
    private ElapsedTime timer = new ElapsedTime(ElapsedTime.Resolution.MILLISECONDS);

    private Intake intake;
    private Spindexer spindexer;

    public IntakeArtifact_BACKUP(Intake intake, Spindexer spindexer, boolean isAuto){
        this.intake = intake;
        this.spindexer = spindexer;
        this.isAuto = isAuto;
    }

    @Override
    public boolean run(@NonNull TelemetryPacket packet) {
        if (!canceled) {
            if (!initialized) {
                targetSlot = spindexer.gotoClosestEmptyIntake();
            }
            if ((!initialized && targetSlot != -1)) {
                intake.intakeArtifact();
                initialized = true;
                running = true;
                timer.reset();
            } else if (targetSlot == -1) {
                intake.stop();
                running = false;
            }

            if(timer.seconds() >= autoTimeout && isAuto){
                intake.stop();
                running = false;
            }

            if (running) {
                if (spindexer.atTarget() && spindexer.isIntakeSlotFull()) {
                    spindexer.setSlot(targetSlot, spindexer.readIntakeHSV());
//                    int x = 0;
//                    while (tempColor == Spindexer.DetectedColor.NONE && x < 5){
//                        x++;
//                        spindexer.setSlot(targetSlot, tempColor);
//                        tempColor =  spindexer.getIntakeColor();
//                    }

                    if(spindexer.getSlotColor(targetSlot) == Spindexer.DetectedColor.NONE){
                        spindexer.setSlotPurple(targetSlot);
                    }

                    targetSlot = spindexer.gotoClosestEmptyIntake();
                    if (targetSlot != -1) {
                        intake.intakeArtifact();
                    } else {
                        intake.stop();
                        running = false;
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