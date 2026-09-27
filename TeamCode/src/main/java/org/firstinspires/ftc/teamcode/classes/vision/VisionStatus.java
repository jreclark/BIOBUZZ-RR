package org.firstinspires.ftc.teamcode.classes.vision;

import com.qualcomm.hardware.limelightvision.LLResult;

/**
 * Read-only view of the Vision implementation state.
 */
public interface VisionStatus {
    Vision.Pipeline getCurrentPipeline();

    LLResult getLatestResult();

    Vision.TargetData getLastTargetData();

    int getConsecutiveNoTargetFrames();
}
