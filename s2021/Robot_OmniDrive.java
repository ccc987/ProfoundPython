package org.firstinspires.ftc.teamcode.s2021;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;

/**
 * This is NOT an opmode.
 * <p>
 * This class defines all the specific hardware for a three wheel omni-bot.
 * <p>
 * This hardware class assumes the following device names have been configured on the robot:
 * Note:  All names are lower case and some have single spaces between words.
 * <p>
 * Motor channel:  Left  drive motor:        "left drive"
 * Motor channel:  Right drive motor:        "right drive"
 * Motor channel:  Rear  drive motor:        "back drive"
 * <p>
 * These motors correspond to three drive locations spaced 120 degrees around a circular robot.
 * Each motor is attached to an omni-wheel. Two wheels are in front, and one is at the rear of the robot.
 * <p>
 * Robot motion is defined in three different axis motions:
 * - Axial    Forward/Backwards      +ve = Forward
 * - Lateral  Side to Side strafing  +ve = Right
 * - Yaw      Rotating               +ve = CCW
 */


public class Robot_OmniDrive {
    WebcamName webcamName = null;

    //private DcMotor  leftDrive      = null;
    //private DcMotor  rightDrive     = null;
    //private DcMotor  backDrive      = null;
    int cameraMonitorViewId = 0;
    ColorSensor color;
    // Private Members
    private LinearOpMode myOpMode;
    private DcMotor leftWheelF = null;               //Left Wheel Front
    private DcMotor leftWheelR = null;               //Left Wheel Back
    private DcMotor rightWheelF = null;              //Right Wheel Front
    private DcMotor rightWheelR = null;
    private Servo clawLeft = null;
    private Servo clawRight = null;
    private DcMotor duckWheel = null;
    private DcMotor returnMotor = null;
    private double driveAxial = 0;   // Positive is forward
    private double driveLateral = 0;   // Positive is right
    private double driveYaw = 0;   // Positive is CCW

    private double raise = 0;
    private double lower = 0;
    private double duck = 0;

    /* Constructor */
    public Robot_OmniDrive() {

    }


    /* Initialize standard Hardware interfaces */
    public void initDrive(LinearOpMode opMode) {

        // Save reference to Hardware map
        myOpMode = opMode;

        /*leftWheelF = myOpMode.hardwareMap.get(DcMotor.class, "D0");
        rightWheelF = myOpMode.hardwareMap.get(DcMotor.class, "D2");
        leftWheelR = myOpMode.hardwareMap.get(DcMotor.class, "D3");
        rightWheelR = myOpMode.hardwareMap.get(DcMotor.class, "D4");
*/
        // Define and Initialize Motors
        //leftDrive        = myOpMode.hardwareMap.get(DcMotor.class, "left drive");
        //rightDrive       = myOpMode.hardwareMap.get(DcMotor.class, "right drive");
        //backDrive        = myOpMode.hardwareMap.get(DcMotor.class, "back drive");

        leftWheelF = myOpMode.hardwareMap.dcMotor.get("D1");
        rightWheelF = myOpMode.hardwareMap.dcMotor.get("D2");
        leftWheelR = myOpMode.hardwareMap.dcMotor.get("D3");
        rightWheelR = myOpMode.hardwareMap.dcMotor.get("D4");
        //color = myOpMode.hardwareMap.get(ColorSensor.class, "Color");

        clawLeft = myOpMode.hardwareMap.get(Servo.class, "CL");
        clawRight = myOpMode.hardwareMap.get(Servo.class, "CR");
        duckWheel = myOpMode.hardwareMap.get(DcMotor.class, "duck");
        returnMotor = myOpMode.hardwareMap.get(DcMotor.class, "return");


        leftWheelF.setDirection(DcMotor.Direction.FORWARD);
        leftWheelR.setDirection(DcMotor.Direction.FORWARD);
        rightWheelF.setDirection(DcMotor.Direction.REVERSE);
        rightWheelR.setDirection(DcMotor.Direction.REVERSE);


        webcamName = myOpMode.hardwareMap.get(WebcamName.class, "Webcam 1");

        cameraMonitorViewId = myOpMode.hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", myOpMode.hardwareMap.appContext.getPackageName());

        //leftDrive.setDirection(DcMotor.Direction.FORWARD); // Positive input rotates counter clockwise
        //rightDrive.setDirection(DcMotor.Direction.FORWARD);// Positive input rotates counter clockwise
        //backDrive.setDirection(DcMotor.Direction.FORWARD); // Positive input rotates counter clockwise

        //use RUN_USING_ENCODERS because encoders are installed.
        //setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        // Stop all robot motion by setting each axis value to zero
        moveRobot(0, 0, 0);
    }

    public void manualDrive() {
        // In this mode the Left stick moves the robot fwd & back, and Right & Left.
        // The Right stick rotates CCW and CW.

        //  (note: The joystick goes negative when pushed forwards, so negate it)
        setAxial(-myOpMode.gamepad1.left_stick_y);
        setLateral(myOpMode.gamepad1.left_stick_x);
        setYaw(-myOpMode.gamepad1.right_stick_x);
        setRaise(myOpMode.gamepad2.right_trigger);
        setLower(myOpMode.gamepad2.left_trigger);
        setDuck(myOpMode.gamepad1.right_trigger);
    }


    /***
     * void moveRobot(double axial, double lateral, double yaw)
     * Set speed levels to motors based on axes requests
     * @param axial     Speed in Fwd Direction
     * @param lateral   Speed in lateral direction (+ve to right)
     * @param yaw       Speed of Yaw rotation.  (+ve is CCW)
     */
    public void moveRobot(double axial, double lateral, double yaw) {
        setAxial(axial);
        setLateral(lateral);
        setYaw(yaw);
        moveRobot();
    }

    /***
     * void moveRobot()
     * This method will calculate the motor speeds required to move the robot according to the
     * speeds that are stored in the three Axis variables: driveAxial, driveLateral, driveYaw.
     * This code is setup for a three wheeled OMNI-drive but it could be modified for any sort of omni drive.
     *
     * The code assumes the following conventions.
     * 1) Positive speed on the Axial axis means move FORWARD.
     * 2) Positive speed on the Lateral axis means move RIGHT.
     * 3) Positive speed on the Yaw axis means rotate COUNTER CLOCKWISE.
     *
     * This convention should NOT be changed.  Any new drive system should be configured to react accordingly.
     */
    public void moveRobot() {
        // calculate required motor speeds to acheive axis motions
        //double back = driveYaw + driveLateral;
        //double left = driveYaw - driveAxial - (driveLateral * 0.5);
        //double right = driveYaw + driveAxial - (driveLateral * 0.5);

        double LeftF = driveAxial + driveLateral - driveYaw;
        double LeftR = driveAxial - driveLateral - driveYaw;

        double RightF = driveAxial - driveLateral + driveYaw;
        double RightR = driveAxial + driveLateral + driveYaw;

        double powerRaise;
        double powerLower;
        double powerDuck;

        powerRaise = raise;
        powerLower = -lower;
        powerDuck = duck;

        duckWheel.setPower(powerDuck);
        returnMotor.setPower(powerRaise);
        returnMotor.setPower(powerLower);

        // normalize all motor speeds so no values exceeds 100%.
        //double max = Math.max(Math.abs(back), Math.abs(right));
        //max = Math.max(max, Math.abs(left));

        /*double max = Math.max(Math.abs(LeftF), Math.abs(LeftR));
        max = Math.max(max, Math.abs(RightF));
        max = Math.max(max, Math.abs(RightR));

        if (max > 1.0)
        {
            LeftF /= max;
            LeftR /= max;
            RightF /= max;
            RightR /= max;
        }
*/
        // Set drive motor power levels.
        //backDrive.setPower(back);
        //leftDrive.setPower(left);
        //rightDrive.setPower(right);

        leftWheelF.setPower(LeftF);
        leftWheelR.setPower(LeftR);
        rightWheelF.setPower(RightF);
        rightWheelR.setPower(RightR);
        // Display Telemetry
        //myOpMode.telemetry.addData("Axes  ", "A[%+5.2f], L[%+5.2f], Y[%+5.2f]", driveAxial, driveLateral, driveYaw);
        //myOpMode.telemetry.addData("LeftF  ", LeftF);
        //myOpMode.telemetry.addData("LeftR  ", LeftR);
        //myOpMode.telemetry.addData("RightF  ", RightF);
        //myOpMode.telemetry.addData("RightR  ", RightR);


        //myOpMode.telemetry.addData("Wheels", "L[%+5.2f], R[%+5.2f], B[%+5.2f]", left, right, back);

        if (myOpMode.gamepad2.b) {
            clawLeft.setPosition(0);
            clawRight.setPosition(1);
        } else if (myOpMode.gamepad2.x) {
            clawLeft.setPosition(0.5);
            clawRight.setPosition(0.5);
        }
    }


    public void setAxial(double axial) {
        driveAxial = Range.clip(axial, -1, 1);
    }

    public void setLateral(double lateral) {
        driveLateral = Range.clip(lateral, -1, 1);
    }

    public void setYaw(double yaw) {
        driveYaw = Range.clip(yaw, -1, 1);
    }

    public void setRaise(double raise1) {
        raise = Range.clip(raise1, -1, 1);
    }

    public void setLower(double lower1) {
        lower = Range.clip(lower1, -1, 1);
    }

    public void setDuck(double duck1) {
        duck = Range.clip(duck1, -1, 1);
    }

    public void setGyro(double motor_output) {
        leftWheelF.setPower(1 * motor_output);
        leftWheelR.setPower(1 * motor_output);
        rightWheelF.setPower(-1 * motor_output);
        rightWheelR.setPower(-1 * motor_output);
    }


    /***
     * void setMode(DcMotor.RunMode mode ) Set all drive motors to same mode.
     * @param mode    Desired Motor mode.
     */
    public void setMode(DcMotor.RunMode mode) {
        leftWheelF.setMode(mode);
        leftWheelR.setMode(mode);
        rightWheelF.setMode(mode);
        rightWheelR.setMode(mode);
    }
}

