package org.firstinspires.ftc.teamcode.actions;

import androidx.annotation.NonNull;

import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Action;
import com.qualcomm.robotcore.util.ElapsedTime;

public class AutoTimeoutAction implements Action {
    ElapsedTime timer;
    double timeLimit;

    public AutoTimeoutAction(ElapsedTime timer, double timeLimit){
        this.timer = timer;
        this.timeLimit = timeLimit;
    }

    @Override
    public boolean run(@NonNull TelemetryPacket packet) {

        return timer.seconds() < timeLimit;
    }
}
