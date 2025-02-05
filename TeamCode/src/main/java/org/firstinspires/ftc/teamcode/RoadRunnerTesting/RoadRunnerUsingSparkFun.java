package org.firstinspires.ftc.teamcode.RoadRunnerTesting;

import androidx.annotation.NonNull;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Action;
import com.acmerobotics.roadrunner.AngularVelConstraint;
import com.acmerobotics.roadrunner.MinVelConstraint;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.ProfileAccelConstraint;
import com.acmerobotics.roadrunner.SequentialAction;
import com.acmerobotics.roadrunner.TrajectoryActionBuilder;
import com.acmerobotics.roadrunner.TranslationalVelConstraint;
import com.acmerobotics.roadrunner.Vector2d;
import com.acmerobotics.roadrunner.ftc.Actions;
import com.qualcomm.hardware.dfrobot.HuskyLens;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;
import com.sun.tools.javac.util.List;

import org.firstinspires.ftc.teamcode.ConstantsPackage.Constants;
import org.firstinspires.ftc.teamcode.TheoCode.MecanumDrive;
import org.firstinspires.ftc.teamcode.TheoCode.SparkFunOTOSDrive;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Autonomous(name = "RoadRunnerUsingSparkFun", group = "teamcode")
public class RoadRunnerUsingSparkFun extends LinearOpMode {

    public class DriveToPiece implements Action {

        double power;
        int millis;
        String direction;

        public DriveToPiece(double power, int millis, String direction) {
            this.power = power;
            this.millis = millis;
            this.direction = direction;
        }

        public void drive(String direction, double power, int millis) {
            switch(direction) {
                case "forward":
                    rightFront.setPower(power);
                    rightBack.setPower(power);
                    leftFront.setPower(power);
                    leftBack.setPower(power);
                    sleep(millis);
                    rightFront.setPower(0);
                    rightBack.setPower(0);
                    leftFront.setPower(0);
                    leftBack.setPower(0);
                    break;

                case "backward":
                    rightFront.setPower(-power);
                    rightBack.setPower(-power);
                    leftFront.setPower(-power);
                    leftBack.setPower(-power);
                    sleep(millis);
                    rightFront.setPower(0);
                    rightBack.setPower(0);
                    leftFront.setPower(0);
                    leftBack.setPower(0);
                    break;
            }
        }
        @Override
        public boolean run(@NonNull TelemetryPacket telemetryPacket) {
            drive(direction, power, millis);
            return false;
        }
    }

    public class LiftOrLowerElevator implements Action {
        String position;
        public LiftOrLowerElevator(String position) {
            this.position = position;
        }

        public void raiseOrLower(String position) {
            switch(position) {
                case "raise":
                    elevator.setTargetPosition(-2900);
                    elevator.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                    elevator.setPower(1);

                    telemetry.addData("Elevator Position", elevator.getCurrentPosition());
                    telemetry.update();

                    sleep(1400);

//                        if(elevator.getCurrentPosition() <= -3200 &&  elevator.getCurrentPosition() > -2800) {
//                            elevator.setPower(0);
//                        }

                    bucketServo.setPosition(0);

                    sleep(1500);
                    break;

                case "lower":
                    telemetry.addData("Ran", "this");
                    telemetry.update();
                    bucketServo.setPosition(1);

                    sleep(800);

                    elevator.setTargetPosition(-50);
                    elevator.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                    elevator.setPower(-1);

                    sleep(1400);

                    if(elevator.getCurrentPosition() < -100 && elevator.getCurrentPosition() > -50) {
                        elevator.setPower(0);
                    }

                    break;
            }
        }

        @Override
        public boolean run(@NonNull TelemetryPacket telemetryPacket) {
            raiseOrLower(position);
            return false;
        }
    }

    public class ExtensionAndRetraction implements Action {

        String position;

        public ExtensionAndRetraction(String position) {
            this.position = position;
        }

        public void extendOrRetract(String position) {
            switch(position) {
                case "extend":
                    leftExtend.setPosition(1);
                    rightExtend.setPosition(1);
                    break;

                case "retract":
                    leftExtend.setPosition(0.55);
                    rightExtend.setPosition(0.55);
                    break;
            }
        }

        @Override
        public boolean run(@NonNull TelemetryPacket telemetryPacket) {
            extendOrRetract(position);
            return false;
        }
    }

    public class SearchForPiece implements Action {

        public void setPower(double frontLeftPower, double frontRightPower, double backLeftPower, double backRightPower) {
            leftFront.setPower(frontLeftPower);
            rightFront.setPower(frontRightPower);
            leftBack.setPower(backLeftPower);
            rightBack.setPower(backRightPower);
        }

        @Override
        public boolean run(@NonNull TelemetryPacket telemetryPacket) {
            rightExtend.setPosition(1);
            leftExtend.setPosition(1);
            gear.setPosition(Constants.ServoConstants.gearDown);

            HuskyLens.Block block;

            while(opModeIsActive()) {
               if(huskyLens.blocks().length > 0) {
                   block = huskyLens.blocks()[0];
                   telemetry.addData("Block X", block.x);
                   telemetry.update();

                   if(block.x < 140) {
                       setPower(-0.34, 0.34, 0.34, -0.34);
                   } else if(block.x > 200) {
                       setPower(0.34, -0.34, -0.34, 0.34);
                   } else {
                     setPower(0.4,0.4, 0.4, 0.4);

                     while(opModeIsActive()) {
                       if(huskyLens.blocks().length > 0)  {
                           block = huskyLens.blocks()[0];
                           telemetry.addData("Y", block.y);
                           telemetry.update();
                           if(block.y > 100) {
                               break;
                           }
                       }
                     }

                     setPower(0, 0, 0, 0);
                     intake.setPosition(Constants.ServoConstants.clawOpen);
                     sleep(500);
                     gear.setPosition(Constants.ServoConstants.gearDownDown);
                     sleep(500);
                     leftWrist.setPosition(Constants.ServoConstants.wristDown);
                     rightWrist.setPosition(Constants.ServoConstants.wristDown);
                     sleep(500);
                     intake.setPosition(Constants.ServoConstants.clawClosed);
                     sleep(500);
                     leftWrist.setPosition(Constants.ServoConstants.wristTransfer);
                     rightWrist.setPosition(Constants.ServoConstants.wristTransfer);
                     sleep(200);
                     leftExtend.setPosition(Constants.ServoConstants.minExtension);
                     rightExtend.setPosition(Constants.ServoConstants.minExtension);
                     gear.setPosition(Constants.ServoConstants.gearTransfer);
                     sleep(500);
                     intake.setPosition(Constants.ServoConstants.clawOpen);

                     return false;
                   }
               }
            }
            return false;
        }
    }

    DcMotorEx leftFront, leftBack, rightFront, rightBack;
    DcMotor elevator;
    HuskyLens huskyLens;
    Servo leftExtend, rightExtend, bucketServo, gear, intake, leftWrist, rightWrist, spin;
    FtcDashboard dashboard = FtcDashboard.getInstance();


    @Override
    public void runOpMode() throws InterruptedException {
        leftFront = hardwareMap.get(DcMotorEx.class, "left_front");
        leftBack = hardwareMap.get(DcMotorEx.class, "left_back");
        rightBack = hardwareMap.get(DcMotorEx.class, "right_back");
        rightFront = hardwareMap.get(DcMotorEx.class, "right_front");
        elevator = hardwareMap.get(DcMotor.class, "erect");
        huskyLens = hardwareMap.get(HuskyLens.class, "huskyLens");
        leftExtend = hardwareMap.get(Servo.class, "leftEx");
        rightExtend = hardwareMap.get(Servo.class, "rightEX");
        bucketServo = hardwareMap.get(Servo.class, "bust");
        gear = hardwareMap.get(Servo.class, "gear");
        intake = hardwareMap.get(Servo.class, "claw");
        leftWrist = hardwareMap.get(Servo.class, "leftWR");
        rightWrist = hardwareMap.get(Servo.class, "rightWR");
        spin = hardwareMap.get(Servo.class, "spin");

        elevator.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        elevator.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        leftBack.setDirection(DcMotorSimple.Direction.REVERSE);
        leftFront.setDirection(DcMotorSimple.Direction.REVERSE);
        leftExtend.setDirection(Servo.Direction.REVERSE);
        leftWrist.setDirection(Servo.Direction.REVERSE);

        leftFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        leftBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        Pose2d initialPose = new Pose2d(41.9, 63, Math.PI);
        SparkFunOTOSDrive drive = new SparkFunOTOSDrive(hardwareMap, initialPose);
        TrajectoryActionBuilder tab1 = drive.actionBuilder(initialPose);

        LiftOrLowerElevator raiseElevator = new LiftOrLowerElevator("raise");
        LiftOrLowerElevator lowerElevator = new LiftOrLowerElevator("lower");
        ExtensionAndRetraction retract = new ExtensionAndRetraction("retract");
        ExtensionAndRetraction extend = new ExtensionAndRetraction("extend");
        SearchForPiece searchForPiece = new SearchForPiece();

        huskyLens.selectAlgorithm(HuskyLens.Algorithm.COLOR_RECOGNITION);

        telemetry.addData("HuskyLens Initialized", huskyLens.knock());
        telemetry.update();

        gear.setPosition(Constants.ServoConstants.gearDown);
        rightExtend.setPosition(Constants.ServoConstants.maxExtension);
        leftExtend.setPosition(Constants.ServoConstants.maxExtension);
        leftWrist.setPosition(Constants.ServoConstants.wristHover);
        rightWrist.setPosition(Constants.ServoConstants.wristHover);
        spin.setPosition(Constants.ServoConstants.spinCenter);

        waitForStart();

        Action goToBasket = tab1.endTrajectory().fresh()
                .strafeTo(new Vector2d(50.6544, 54.9196))
                .turnTo(-141)
                .build();

        Action goToFirstPiece = tab1.endTrajectory().fresh()
                .strafeTo(new Vector2d(39, 53), new MinVelConstraint(List.of((MecanumDrive.kinematics.new WheelVelConstraint(80)))))
//                .turnTo(-108.2257)
                .turnTo(-87)
                .build();

//        new Vector2d(39.1923, 55.0998)

        Action goToSecondPiece = tab1.endTrajectory().fresh()
                .splineTo(new Vector2d(0, 0), Math.toRadians(35))
                .build();

        Action goToThirdPiece = tab1.endTrajectory().fresh()
                .splineTo(new Vector2d(0, 0), Math.toRadians(35))
                .build();

        Actions.runBlocking(
                new SequentialAction(
                        goToBasket,
                        raiseElevator,
                        lowerElevator,
                        goToFirstPiece,
                        searchForPiece,
                        goToBasket
                )
        );
    }
}
