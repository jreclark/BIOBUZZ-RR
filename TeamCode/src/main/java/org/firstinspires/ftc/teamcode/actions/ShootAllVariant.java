package org.firstinspires.ftc.teamcode.actions;

import androidx.annotation.NonNull;

import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Action;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.classes.Shooter;
import org.firstinspires.ftc.teamcode.classes.Spindexer;

public class ShootAllVariant implements Action {
    private boolean initialized = false;
    private boolean running = false;
    private boolean atSpeed = false;
    private boolean safeMove = false;
    private boolean canceled = false;
    private boolean lastshot = false;
    private int targetSlot = -1;
    private double shotTime = 0.75;
    private boolean waiting = false;

    //previously purple green purple
    public Spindexer.DetectedColor[] shotOrder = {Spindexer.DetectedColor.NONE, Spindexer.DetectedColor.NONE, Spindexer.DetectedColor.NONE};
    
    private int totalShots = 3;
    private int currentShot = 0;

    private Spindexer.DetectedColor colorTarget = Spindexer.DetectedColor.ANY;

    //Timers
    private double firstShotHoldMin = 1.0;
    private double firstShotWait = 0.75;
    private double secondShotWait = 0.75;
    private double finalShotWait = 1.0;

    private ElapsedTime timer = new ElapsedTime(ElapsedTime.Resolution.MILLISECONDS);

    private Shooter shooter;
    private Spindexer spindexer;

    public enum ShotType{
        ShootGreen,
        ShootPurple,
        ShootAnySingle,
        ShootPattern,
        ShootAll
    }

    private ShotType shotType = ShotType.ShootAll;

    public ShootAllVariant(Shooter shooter, Spindexer spindexer){
        this.shooter = shooter;
        this.spindexer = spindexer;
    }

    @Override
    public boolean run(@NonNull TelemetryPacket packet) {
        if (!canceled) {
            shooter.shooterIdle = false;
            shooter.updateController();
            spindexer.updateController();
            if (!initialized) {
                getNextColor(1);
                shooter.spinUp(shooter.targetRPM);
                timer.reset();
                lastshot = false;
                waiting = true;
                initialized = true;
                running = true;
                currentShot = 1;
                if (shotType == ShotType.ShootAll) {
                    targetSlot = 0;
                    spindexer.moveToShooterPos(targetSlot);
                } else {
                    targetSlot = spindexer.gotoClosestFullShooter(colorTarget);
//                    sleep(2000);
                }
            }
//            packet.put("Cunnret Condition: ", 0);
//            sleep(5);

            if(targetSlot == -1 && !lastshot){
                cleanup();
//                packet.put("No full slot", 0);
                return false;
            }

            atSpeed = shooter.atSpeed();

//            packet.put("CurrentShot:", currentShot);

            if(lastshot){
                waiting = timer.seconds() <= finalShotWait;
                if(!waiting){
                    cleanup();
                    return false;
                }
            } else {
                if (currentShot == 1 && waiting) {
                    waiting = timer.seconds() <= firstShotHoldMin;
                    spindexer.waggle();
                } else if (currentShot == 2 && atSpeed) {
                    waiting = timer.seconds() <= firstShotWait;
                    spindexer.waggle();
                    if (!waiting) {
                        spindexer.moveToShooterPos(targetSlot);
                    }
                } else if (currentShot == 3 && atSpeed) {
                    waiting = timer.seconds() <= secondShotWait;
                    spindexer.waggle();
                    if (!waiting) {
                        spindexer.moveToShooterPos(targetSlot);
                    }
                }
            }

//            packet.put("Cunnret Condition: ", 1);
//            sleep(5);
            
            if (atSpeed && !waiting) {
                if (spindexer.atTarget()) {
                    packet.put("Executing launch", 0);
                    packet.put("Target Slot: ", targetSlot);

                    shooter.loadArtifact(1.0);
                    spindexer.setSlot(targetSlot, Spindexer.DetectedColor.NONE);

                    packet.put("Color 0: ", spindexer.spindexerSlots[0].name());
                    packet.put("Color 1: ", spindexer.spindexerSlots[1].name());
                    packet.put("Color 2: ", spindexer.spindexerSlots[2].name());

//                    packet.put("Current Condition: ", 2);

                    if(currentShot < totalShots){
                        currentShot ++;
                        getNextColor(currentShot);
                        if (shotType == ShotType.ShootAll) {
                            targetSlot = currentShot - 1;
                        } else {
                            targetSlot = spindexer.findFullShooterSlot(colorTarget);
                        }
                    } else {
//                        targetSlot = -1;
                        lastshot = true;
                    }

//                    if(targetSlot == -1){
//                        lastshot = true;
//                    }

                    timer.reset();
                    waiting = true;

                    packet.put("Next target slot", targetSlot);
                }
            }

            return running;

        } else {
            canceled = false;
            cleanup();
            return false;
        }
//        packet.put("shooterVelocity", vel);

    }

    public void getNextColor(int shotNumber) {
        switch (shotType) {
            case ShootGreen:
                colorTarget = Spindexer.DetectedColor.GREEN;
                totalShots = 1;
                break;
            case ShootPurple:
                colorTarget = Spindexer.DetectedColor.PURPLE;
                totalShots = 1;
                break;
            case ShootAnySingle:
                colorTarget = Spindexer.DetectedColor.ANY;
                totalShots = 1;
                break;
            case ShootPattern:
                colorTarget = getColorTarget(shotNumber);
                totalShots = 3;
                break;
            case ShootAll:
                colorTarget = Spindexer.DetectedColor.ANY;
                totalShots = 3;
                break;
        }
    }

    public void cancel(){
        canceled = true;
    }

    public void cleanup(){
        shooter.idle();
        shooter.loadArtifact(0);
        initialized = false;
        running = false;
        safeMove  = false;
        lastshot = false;
    }

    public void clearCancel(){
        canceled = false;
    }

    public void selectShot(ShotType type){
        shotType = type;
    }

    public boolean isRunning(){
        return running;
    }

    public Spindexer.DetectedColor getColorTarget(int shotNum) {
        return shotOrder[shotNum-1];
    }

    public void setShotOrder(int greenPos){
        for(int x =0; x <=2; x++){
            if(x==greenPos){
                shotOrder[x] = Spindexer.DetectedColor.GREEN;
            } else {
                shotOrder[x] = Spindexer.DetectedColor.PURPLE;
            }
        }
    }

    public final void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }


}