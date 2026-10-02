package org.firstinspires.ftc.teamcode.actions;

import androidx.annotation.NonNull;

import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Action;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.classes.Shooter_BIOBUZZ;
import org.firstinspires.ftc.teamcode.classes.Spindexer;

public class ShootActionTimed implements Action {
    private boolean initialized = false;
    private boolean running = false;
    private boolean atSpeed = false;
    private boolean safeMove = false;
    private boolean canceled = false;
    private boolean lastshot = false;
    private int targetSlot = -1;
    private double shotTime = 0.75;
    private boolean waiting = false;

    private double duration = 4;

    private int totalShots = 3;
    private int currentShot = 0;

    //Timers
    private double firstShotHoldMin = 1.0;
    private double firstShotWait = 0.75;
    private double secondShotWait = 0.75;
    private double finalShotWait = 1.0;

    private ElapsedTime timer = new ElapsedTime(ElapsedTime.Resolution.MILLISECONDS);

    private Shooter_BIOBUZZ shooter;

    public enum ShotType{
        ShootGreen,
        ShootPurple,
        ShootAnySingle,
        ShootPattern,
        ShootAll
    }

    private ShotType shotType = ShotType.ShootAll;

    public ShootActionTimed(Shooter_BIOBUZZ shooter){
        this.shooter = shooter;
    }

    @Override
    public boolean run(@NonNull TelemetryPacket packet) {
        if (!canceled) {
            shooter.shooterIdle = false;
            shooter.setNotIdle();
            shooter.updateController();
            if (!initialized) {
                shooter.spinUp(shooter.targetRPM);
                timer.reset();
                lastshot = false;
                waiting = false;
                initialized = true;
                running = true;
                currentShot = 1;
            }

            if(timer.seconds() >= duration){
                canceled = true;
            }

            atSpeed = shooter.atSpeed();

            if (atSpeed && !waiting) {
                    packet.put("Executing launch", 0);
                    packet.put("Target Slot: ", targetSlot);

                    shooter.shoot();
            }

            return running;

        } else {
            canceled = false;
            cleanup();
            return false;
        }

    }

    public void cancel(){
        canceled = true;
    }

    public void cleanup(){
        shooter.idle();
        shooter.unshoot();
        initialized = false;
        running = false;
        safeMove  = false;
        lastshot = false;
    }

    public void clearCancel(){
        canceled = false;
    }

    public boolean isRunning(){
        return running;
    }

    public void setDuration(double seconds){
        duration = seconds;
    }

    public final void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }


}