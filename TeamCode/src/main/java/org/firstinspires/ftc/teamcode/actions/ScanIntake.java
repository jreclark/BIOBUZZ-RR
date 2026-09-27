package org.firstinspires.ftc.teamcode.actions;

import androidx.annotation.NonNull;

import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Action;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.classes.Spindexer;

public class ScanIntake implements Action {
    private boolean initialized = false;
    private boolean running = true;
    private boolean waiting = false;
    private boolean canceled = false;
    private int targetSlot = 0;

    private Spindexer.DetectedColor color;
    private int readCount = 0;
    private Spindexer.DetectedColor[] detectedColors = new Spindexer.DetectedColor[10];

    private int purpleCount = 0;
    private int greenCount = 0;
    private int noneCount = 0;

    private double holdTime = 0.5;

    private ElapsedTime timer = new ElapsedTime(ElapsedTime.Resolution.MILLISECONDS);

    private Spindexer spindexer;

    public ScanIntake(Spindexer spindexer){
        this.spindexer = spindexer;
    }

    @Override
    public boolean run(@NonNull TelemetryPacket packet) {
        if (!canceled) {
            if (!initialized) {
                targetSlot = 0;
                spindexer.moveToIntakePos(0);
                initialized = true;
                running = true;
                waiting = true;
                timer.reset();
            }

            waiting = timer.seconds() < holdTime;

            if (running && spindexer.atTarget() && !waiting) {
                    if (readCount < 10) {
                        color = spindexer.getIntakeColor();

                        if (!spindexer.isIntakeSlotFull()) {
                            detectedColors[readCount] = Spindexer.DetectedColor.NONE;
                        } else {
                            detectedColors[readCount] = color;
                        }
                        readCount++;
                    } else if (readCount == 10) {
                        for (int i = 0; i < detectedColors.length - 1; i++) {
                            if(detectedColors[i] == Spindexer.DetectedColor.PURPLE) {
                                purpleCount++;
                            } else if (detectedColors[i] == Spindexer.DetectedColor.GREEN) {
                                greenCount++;
                            } else {
                                noneCount++;
                            }
                        }
                        if (purpleCount > greenCount && purpleCount > noneCount) {
                            spindexer.setSlot(targetSlot, Spindexer.DetectedColor.PURPLE);
                        } else if (greenCount > purpleCount && greenCount > noneCount) {
                            spindexer.setSlot(targetSlot, Spindexer.DetectedColor.GREEN);
                        } else {
                            spindexer.setSlot(targetSlot, Spindexer.DetectedColor.NONE);
                        }
                    }

                if(targetSlot < 2 && readCount == 10){
                    targetSlot ++;
                    spindexer.moveToIntakePos(targetSlot);
                    readCount = 0;
                    purpleCount = 0;
                    greenCount = 0;
                    noneCount = 0;
                    timer.reset();
                    waiting = true;
                } else {
                    running = false;
                    initialized = false;
                }
            }
            return running;
        } else {
            canceled = false;
            initialized = false;
            return false;
        }
//        packet.put("shooterVelocity", vel);

    }

    public void cancel(){
        canceled = true;
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