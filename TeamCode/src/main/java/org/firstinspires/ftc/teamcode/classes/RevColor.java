package org.firstinspires.ftc.teamcode.classes;

import android.graphics.Color;

import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.NormalizedColorSensor;
import com.qualcomm.robotcore.hardware.NormalizedRGBA;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RevColor {
    Telemetry telemetry;
    NormalizedRGBA colors;

    public enum Samples {
        RED,
        BLUE,
        YELLOW,
        NONE
    }

    private final double[] kBlueTarget = {0.143, 0.427, 0.429};
    private final double[] kRedTarget = {0.561, 0.232, 0.114};
    private final double[] kYellowTarget = {0.361, 0.524, 0.113};

    private final double kBlueHue = 220;
    private final double kRedHue = 20;
    private final double kYellowHue = 80;

    private List<double[]> ColorMatches = Arrays.asList(kBlueTarget, kRedTarget, kYellowTarget);

    private List<Samples> colorList = Arrays.asList(Samples.BLUE, Samples.RED, Samples.YELLOW);

    double matchDistance = 0.2;

    // Once per loop, we will update this hsvValues array. The first element (0) will contain the
    // hue, the second element (1) will contain the saturation, and the third element (2) will
    // contain the value. See http://web.archive.org/web/20190311170843/https://infohost.nmt.edu/tcc/help/pubs/colortheory/web/hsv.html
    // for an explanation of HSV color.
    final float[] hsvValues = new float[3];

    /**
     * The colorSensor field will contain a reference to our color sensor hardware object
     */
    NormalizedColorSensor colorSensor;

    boolean reportVals = false;



    Samples result = Samples.NONE;

    public RevColor(HardwareMap hardwareMap, Telemetry telemetry) {
        this.telemetry = telemetry;

        // You can give the sensor a gain value, will be multiplied by the sensor's raw value before the
        // normalized color values are calculated. Color sensors (especially the REV Color Sensor V3)
        // can give very low values (depending on the lighting conditions), which only use a small part
        // of the 0-1 range that is available for the red, green, and blue values. In brighter conditions,
        // you should use a smaller gain than in dark conditions. If your gain is too high, all of the
        // colors will report at or near 1, and you won't be able to determine what color you are
        // actually looking at. For this reason, it's better to err on the side of a lower gain
        // (but always greater than  or equal to 1).
        float gain = 15;

        // Get a reference to our sensor object. It's recommended to use NormalizedColorSensor over
        // ColorSensor, because NormalizedColorSensor consistently gives values between 0 and 1, while
        // the values you get from ColorSensor are dependent on the specific sensor you're using.
        colorSensor = hardwareMap.get(NormalizedColorSensor.class, "sensor_color");
        colorSensor.setGain(gain);

    }

    public Samples checkColor() {
        updateValues();
        return closestColor();
    }

    void updateValues() {
        // Get the normalized colors from the sensor
        colors = colorSensor.getNormalizedColors();

        /* Use telemetry to display feedback on the driver station. We show the red, green, and blue
         * normalized values from the sensor (in the range of 0 to 1), as well as the equivalent
         * HSV (hue, saturation and value) values. See http://web.archive.org/web/20190311170843/https://infohost.nmt.edu/tcc/help/pubs/colortheory/web/hsv.html
         * for an explanation of HSV color. */

        // Update the hsvValues array by passing it to Color.colorToHSV()
        Color.colorToHSV(colors.toColor(), hsvValues);

        if (reportVals) {
            telemetry.addLine()
                    .addData("Red", "%.3f", colors.red)
                    .addData("Green", "%.3f", colors.green)
                    .addData("Blue", "%.3f", colors.blue);
            telemetry.addLine()
                    .addData("Hue", "%.3f", hsvValues[0])
                    .addData("Saturation", "%.3f", hsvValues[1])
                    .addData("Value", "%.3f", hsvValues[2]);
            telemetry.addData("Alpha", "%.3f", colors.alpha);

            /* If this color sensor also has a distance sensor, display the measured distance.
             * Note that the reported distance is only useful at very close range, and is impacted by
             * ambient light and surface reflectivity. */
            if (colorSensor instanceof DistanceSensor) {
                telemetry.addData("Distance (cm)", "%.3f", ((DistanceSensor) colorSensor).getDistance(DistanceUnit.CM));
            }

//            telemetry.update();
        }
    }

    public Samples closestColor(){
//        int colorIndex = 0;
//        double bestDist = 100;
//        int j = 0;
//        for (double[] color : ColorMatches){
//            double distSum = 0;
//            for (int i = 0; i < 3; i++){
//                distSum = distSum + Math.pow((color[i] - hsvValues[i]),2);
//            }
//            double dist = Math.pow(distSum, 0.5);
//            if(dist < bestDist){
//                colorIndex = j;
//                bestDist = dist;
//            }
//            j++;
//        }
//        if(bestDist < matchDistance){
//            return colorList.get(colorIndex);
//        } else {
//            return Samples.NONE;
//        }
        if(hsvValues[0] > kBlueHue - 15 && hsvValues[0] < kBlueHue + 15){
            return Samples.BLUE;
        } else if (hsvValues[0] > kRedHue - 15 && hsvValues[0] < kRedHue + 15){
            return Samples.RED;
        } else if (hsvValues[0] > kYellowHue - 15 && hsvValues[0] < kYellowHue + 15){
            return Samples.YELLOW;
        } else {
            return Samples.NONE;
        }
    }
}
