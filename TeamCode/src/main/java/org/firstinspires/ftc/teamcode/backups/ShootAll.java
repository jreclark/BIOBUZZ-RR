package org.firstinspires.ftc.teamcode.backups;

import androidx.annotation.NonNull;

import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Action;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.classes.Shooter;
import org.firstinspires.ftc.teamcode.classes.Spindexer;

public class ShootAll implements Action {
    private boolean initialized = false;
    private boolean ammoRdy = false;
    private boolean running = true;
    private boolean atSpeed = false;
    private boolean safeMove = false;
    private boolean canceled = false;
    private boolean lastshot = false;
    private int targetSlot = -1;
    private double shotTime = 0.75;
    private boolean waiting = false;

    private ElapsedTime timer = new ElapsedTime(ElapsedTime.Resolution.MILLISECONDS);

    private Shooter shooter;
    private Spindexer spindexer;

    public ShootAll(Shooter shooter, Spindexer spindexer){
        this.shooter = shooter;
        this.spindexer = spindexer;
    }

    @Override
    public boolean run(@NonNull TelemetryPacket packet) {
        if (!canceled) {
            if (!initialized) {
                lastshot = false;
                waiting = false;
                shooter.spinUp(shooter.targetRPM);
                initialized = true;
                safeMove = false;
                targetSlot = spindexer.gotoClosestFullShooter(Spindexer.DetectedColor.ANY);
                running = true;
                timer.reset();
//                packet.put("Finished Init", 0);
            }

            atSpeed = shooter.atSpeed();

            if(targetSlot == -1){
                shooter.idle();
                shooter.loadArtifact(0);
                initialized = false;
                running = false;
                safeMove  = false;
                lastshot = false;
//                packet.put("No full slot", 0);
                return false;
            }

            if(!atSpeed){
                packet.put("Velocity", shooter.getSpeed());
            }
            if (atSpeed && !waiting && targetSlot != 3) {
                if (spindexer.atTarget()) {
                    packet.put("Executing launch", 0);
                    packet.put("Target Slot: ", targetSlot);
//                    sleep(2000);

                    shooter.loadArtifact(1.0);
                    spindexer.setSlot(targetSlot, Spindexer.DetectedColor.NONE);

//                    sleep(2000);

                    packet.put("Color 0: ", spindexer.spindexerSlots[0].name());
                    packet.put("Color 1: ", spindexer.spindexerSlots[1].name());
                    packet.put("Color 2: ", spindexer.spindexerSlots[2].name());

//                    sleep(2000);

                    targetSlot = spindexer.findFullShooterSlot(Spindexer.DetectedColor.ANY);
                    packet.put("Next target slot", targetSlot);

//                    sleep(2000);
                    if (targetSlot != -1) {
                        timer.reset();
                        waiting = true;
                        safeMove = false;
                    } else {
                        waiting = true;
                        lastshot = true;
                        targetSlot = 3; //Nonsense value
                        safeMove = false;
                    }
                }
            }
            if (waiting) {
                waiting = timer.seconds() <= shotTime;
                packet.put("Waiting", waiting);
                packet.put("Timer: ", timer.seconds());
                if (!waiting && lastshot) {
                    targetSlot = -1;
                } else if (!waiting) {
                    safeMove = true;
                }
            }

            if(safeMove){
                //spindexer.getAllDetectedColors();
                targetSlot = spindexer.gotoClosestFullShooter(Spindexer.DetectedColor.ANY);
                safeMove = false;
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
        atSpeed = false;
        shooter.loadArtifact(0);
        shooter.idle();
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