package org.firstinspires.ftc.teamcode.classes;

public class MatchInfo {
    public enum AllianceColor {
        RED,
        BLUE
    }

    public static AllianceColor allianceColor = AllianceColor.RED;
    public static int patternGreenPos = 0;
    public static int targetTag = 24;

    public static void setAllianceColor(AllianceColor color){
        if(color == AllianceColor.RED){
            allianceColor = AllianceColor.RED;
            targetTag = 24;
        } else {
            allianceColor = AllianceColor.BLUE;
            targetTag = 20;
        }
    }

    public static void swapAllianceColor(){
        if(allianceColor == AllianceColor.RED){
            setAllianceColor(AllianceColor.BLUE);
        } else {
            setAllianceColor(AllianceColor.RED);
        }
    }
}
