package org.firstinspires.ftc.teamcode;


import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.tfod.Recognition;
import org.firstinspires.ftc.robotcore.external.tfod.TFObjectDetector;

import java.util.List;

@Autonomous(name = "DuckScan_Left", group = "Opmode RamEaters")
//@Disabled
public class DuckScan_Left extends LinearOpMode {
    private static final String TFOD_MODEL_ASSET = "FreightFrenzy_DM.tflite";
    private static final String[] LABELS = {
            "Ball",
            "Cube",
            "Duck",
            "Marker"
    };

    private static final String VUFORIA_KEY =
            "AXiCpJb/////AAABmUeqLpvfjkywirbDoSbnyFYKMf7uB24PIfaJZtIqcZO3L7rZVbsKVlz/fovHxEI6VgkUt3PBpXnp+YmHyLrWimMt2AKMFMYsYeZNRmz0p8jFT8DfQC7mmUgswQuPIm64qc8rxwV7vSb0et6Za96tPoDHYNHzhdiaxbI0UHpe4jCkqNTiRDFz8EVNds9kO7bCIXzxBfYfgTDdtjC5JRJ/drtM6DZnTXOqz3pdM85JEVgQqL9wBxUePSjbzyMo9e/FgxluCuWtxHraRJeeuvAlFwAb8wVAoV1cm02qIew0Vh0pDVJqy04gu62CJPhv/wwnXCKywUIEzVMbOLe7muycyHoT6ltpAn4O4s4Z82liWs9x";


    private VuforiaLocalizer vuforia;

    private TFObjectDetector tfod;

    @Override
    public void runOpMode() {

        initVuforia();
        initTfod();


        if (tfod != null) {
            tfod.activate();

            // The TensorFlow software will scale the input images from the camera to a lower resolution.
            // This can result in lower detection accuracy at longer distances (> 55cm or 22").
            // If your target is at distance greater than 50 cm (20") you can adjust the magnification value
            // to artificially zoom in to the center of image.  For best results, the "aspectRatio" argument
            // should be set to the value of the images used to create the TensorFlow Object Detection model
            // (typically 1.78 or 16/9).

            // Uncomment the following line if you want to adjust the magnification and/or the aspect ratio of the input images.
            //tfod.setZoom(3.5, 1.78);
            //Sets the number of pixels to obscure on the left, top, right, and bottom edges of each image passed to the TensorFlow object detector. The size of the images are not changed, but the pixels in the margins are colored black.
            tfod.setClippingMargins(120,160,120,160);
            tfod.setZoom(1.7, 2.5);
        }

        telemetry.update();
        waitForStart();

        if (opModeIsActive()) {
            int r1 = detectDuck();
            sleep(2000);

            telemetry.addData("===> r1", r1);

            telemetry.update();
            sleep(2000);
        }

        if (tfod != null) {
            tfod.shutdown();
        }
    }


    private int detectDuck() {

        int iTimeOut = 5;
        int j = 0;
        int r_pos = 1;

        while (opModeIsActive() && j < iTimeOut) {
            if (tfod != null) {
                // getUpdatedRecognitions() will return null if no new information is available since
                // the last time that call was made.
                List<Recognition> updatedRecognitions = tfod.getUpdatedRecognitions();

                if (updatedRecognitions != null) {
                    //telemetry.addData("# Object Detected", updatedRecognitions.size());
                    // step through the list of recognitions and display boundary info.
                    int i = 0;
                    for (Recognition recognition : updatedRecognitions) {
                        if (recognition.getWidth() < 100 && recognition.getHeight() < 100
                                && (0.75 < recognition.getWidth() / recognition.getHeight() || recognition.getWidth() / recognition.getHeight() < 1.25)) {

                            //telemetry.addData(String.format("label (%d)", i), recognition.getLabel());
                            telemetry.addData(String.format("===> right"), "%.03f"
                                    , recognition.getRight() * 1000);

                            // Robot is place on Left side
                            int pos1_left = 350000;
                            int pos2_left = 150000;

                            if (recognition.getRight() * 1000 >= pos1_left) {

                                // high
                                telemetry.addData("Left pos", 3);
                                r_pos = 3;
                                //return 3;

                            } else if (recognition.getRight() * 1000 >= pos2_left) {

                                //mid
                                telemetry.addData("Left pos", 2);
                                r_pos = 2;
                                //return 2;

                            } else {

                                telemetry.addData("Left pos", 1);
                                r_pos = 1;
                                //low
                                //return 1;

                            }

                            telemetry.update();


                        }
                    }
                }
            }
            sleep(500);
            j++;
        }

        telemetry.addData("r_pos", r_pos);
        return r_pos;
    }


    /**
     * Initialize the Vuforia localization engine.
     */
    private void initVuforia() {
        /*
         * Configure Vuforia by creating a Parameter object, and passing it to the Vuforia engine.
         */
        VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters();

        parameters.vuforiaLicenseKey = VUFORIA_KEY;
        parameters.cameraName = hardwareMap.get(WebcamName.class, "Webcam 1");

        //  Instantiate the Vuforia engine
        vuforia = ClassFactory.getInstance().createVuforia(parameters);

        // Loading trackables is not necessary for the TensorFlow Object Detection engine.
    }


    /**
     * Initialize the TensorFlow Object Detection engine.
     */
    private void initTfod() {
        int tfodMonitorViewId = hardwareMap.appContext.getResources().getIdentifier(
                "tfodMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        TFObjectDetector.Parameters tfodParameters = new TFObjectDetector.Parameters(tfodMonitorViewId);
        tfodParameters.minResultConfidence = 0.50f;
        tfodParameters.isModelTensorFlow2 = true;
        tfodParameters.inputSize = 320;
        tfod = ClassFactory.getInstance().createTFObjectDetector(tfodParameters, vuforia);
        tfod.loadModelFromAsset(TFOD_MODEL_ASSET, LABELS);
    }


}



