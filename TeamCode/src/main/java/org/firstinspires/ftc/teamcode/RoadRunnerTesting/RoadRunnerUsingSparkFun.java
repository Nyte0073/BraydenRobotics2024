package org.firstinspires.ftc.teamcode.RoadRunnerTesting;

import static org.firstinspires.ftc.robotcontroller.internal.FtcRobotControllerActivity.TAG;

import android.util.Log;

import androidx.annotation.NonNull;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Action;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.SequentialAction;
import com.acmerobotics.roadrunner.TrajectoryActionBuilder;
import com.acmerobotics.roadrunner.Vector2d;
import com.acmerobotics.roadrunner.ftc.Actions;
import com.qualcomm.hardware.dfrobot.HuskyLens;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.ConstantsPackage.Constants;
import org.firstinspires.ftc.teamcode.TheoCode.SparkFunOTOSDrive;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

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
                    elevator.setTargetPosition(-2960); //-2790
                    elevator.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                    elevator.setPower(1);

                    sleep(1500);

                    bucketServo.setPosition(0);

                    sleep(1500);
                    break;

                case "lower":
                    telemetry.addData("Ran", "this");
                    telemetry.update();
                    bucketServo.setPosition(1);

                    sleep(800);

                    elevator.setTargetPosition(-110);
                    elevator.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                    elevator.setPower(-1);

                    sleep(1400);

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

        public void searchForPiece() {
                    while(opModeIsActive()) {
            if(huskyLens.blocks().length > 0) {
                HuskyLens.Block block = huskyLens.blocks().length == 0 ? null : huskyLens.blocks()[0];
                assert block != null;
                telemetry.addData("Block X", block.x);

               if(block.x < 160) {
                   telemetry.addData("Block", "is too far to the left.");
//                   leftFront.setPower(-0.2);
//                   rightFront.setPower(0.2);
//                   leftBack.setPower(0.2);
//                   rightBack.setPower(-0.2);

                   setPower(-0.2, 0.2, 0.2, -0.2);
               } else if(block.x > 170) {
                   telemetry.addData("Block", "is too far to the right.");
//                   leftFront.setPower(0.2);
//                   rightFront.setPower(-0.2);
//                   leftBack.setPower(-0.2);
//                   rightBack.setPower(0.2);

                   setPower(0.2, -0.2, -0.2, 0.2);
               } else if(block.x > 160 && block.x < 170) {
//                   leftWrist.setPosition(0.27);
//                   rightWrist.setPosition(0.27);
                   telemetry.addData("Block is centered! --> " + block.x, "Second Alignment beginning shortly.");
                   telemetry.update();
                   sleep(5000);
            setPower(0.3, 0.3, 0.3, 0.3);

                   while(opModeIsActive()) {
                       if(huskyLens.blocks().length > 0) {
                           block = huskyLens.blocks().length == 0 ? null : huskyLens.blocks()[0];
                           assert block != null;
                           telemetry.addData("Block Pos", block.y);
                           telemetry.update();
                           if(block.y > 200  ) {
            setPower(0, 0, 0, 0);
                               break;
                           }
                       }
                   }

                   while(opModeIsActive()) {
                       if(huskyLens.blocks().length > 0) {
                           block = huskyLens.blocks().length ==  0 ? null : huskyLens.blocks()[0];
                           assert block != null;
                           telemetry.addData("Block 2nd X", block.x);

                           if(block.x < 152) { //138
                               telemetry.addData("Block", "is too far to the left.");
            setPower(-0.3, 0.3, -0.3, 0.3);
                           } else if(block.x > 162) { //146
                               telemetry.addData("Block", "is too far to the right.");
            setPower(0.3, -0.3, 0.3, -0.3);
                           } else {
                               telemetry.addData("Block", "is centered!");
                               telemetry.update();

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
                     return;
                           }

                           telemetry.addData("Block Y", block.y);
                       }

                       telemetry.update();
                   }

//                   rightFront.setPower(0);
//                   rightBack.setPower(0);
//                   leftFront.setPower(0);
//                   leftBack.setPower(0);

                   telemetry.addData("Piece", "centered!");
                   telemetry.update();
                   sleep(5000);
               }

               telemetry.update();
            }
        }
        }

        @Override
        public boolean run(@NonNull TelemetryPacket telemetryPacket) {
            rightExtend.setPosition(1);
            leftExtend.setPosition(1);
            gear.setPosition(Constants.ServoConstants.gearDown);

            HuskyLens.Block block = null;

//            while(opModeIsActive()) {
//               if(huskyLens.blocks().length > 0) {
//                   block = huskyLens.blocks()[0];
//                   telemetry.addData("Block X", block.x);
//                   telemetry.addData("Block Y:", block.y);
//                   telemetry.update();
//
//                   sleep(5000);
//
//                   if(block.x < 160) { //140//0.34
//                       setPower(-0.28, 0.28, 0.28, -0.28);
//                   } else if(block.x > 170) {
//                       setPower(0.28, -0.28, -0.28, 0.28);
//                   } else {
//                     setPower(0.3,0.3, 0.3, 0.3);
//
//                     while(opModeIsActive()) {
//                       if(huskyLens.blocks().length > 0)  {
//                           block = huskyLens.blocks()[0];
//                           telemetry.addData("Y", block.y);
//                           telemetry.update();
//                           if(block.y > 180) {
//                               break;
//                           }
//                       }
//                     }
//
//                     setPower(0, 0, 0, 0);
//
//                     while(opModeIsActive()) {
//                         if(huskyLens.blocks().length > 0) {
//                             block = huskyLens.blocks().length == 0 ? null : huskyLens.blocks()[0];
//
//                             assert block != null;
//                             if(block.x < 152) {
//                                 rightFront.setPower(0.28);
//                                 rightBack.setPower(0.28);
//                                 leftFront.setPower(-0.28);
//                                 leftBack.setPower(-0.28);
//                             } else if(block.x > 162) {
//                                 rightFront.setPower(-0.28);
//                                 rightBack.setPower(-0.28);
//                                 leftFront.setPower(0.28);
//                                 leftBack.setPower(0.28);
//                             } else {
//                                 break;
//                             }
//                         }
//                     }
//
//                     gear.setPosition(Constants.ServoConstants.gearDownDown);
//                     sleep(500);
//
//                     leftWrist.setPosition(Constants.ServoConstants.wristDown);
//                     rightWrist.setPosition(Constants.ServoConstants.wristDown);
//                     sleep(500);
//
//                     intake.setPosition(Constants.ServoConstants.clawClosed);
//                     sleep(500);
//
//                     leftWrist.setPosition(Constants.ServoConstants.wristTransfer);
//                     rightWrist.setPosition(Constants.ServoConstants.wristTransfer);
//                     sleep(200);
//
//                     leftExtend.setPosition(Constants.ServoConstants.minExtension);
//                     rightExtend.setPosition(Constants.ServoConstants.minExtension);
//                     gear.setPosition(Constants.ServoConstants.gearTransfer);
//                     sleep(500);
//
//                     intake.setPosition(Constants.ServoConstants.clawOpen);
//                     break;
//                   }
//               }
//            }

//            searchForPiece();

           while(opModeIsActive()) {
               if(huskyLens.blocks().length > 0) {
                  block = huskyLens.blocks()[0];
                  break;
               }
           }

            telemetry.addData("Block X", block.x);
            telemetry.update();


            double millis = 0.5 * (block.x > 170 ? block.x - 170 : 170 - block.x);

           setPower(0, 0, 0,0);
           if(block.x > 170 && block.x < 250) {
               setPower(-0.3, 0.3, 0.3, -0.3);
           } else if(block.x > 275) {
               setPower(0.3, -0.3, -0.3, 0.3);
           }
           sleep((int) millis);
           setPower(0, 0, 0, 0);
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

            sleep(600);

            Log.i(TAG, "Go to this point in the method.");

            return false;
        }
    }

    DcMotorEx leftFront, leftBack, rightFront, rightBack;
    DcMotor elevator;
    HuskyLens huskyLens;
    Servo leftExtend, rightExtend, bucketServo, gear, intake, leftWrist, rightWrist, spin, sweeper;
    FtcDashboard dashboard = FtcDashboard.getInstance();
    volatile boolean isRaised = false;

    public void runAsync() {

        isRaised = false;

        gear.setPosition(Constants.ServoConstants.gearDown);
        rightExtend.setPosition(Constants.ServoConstants.maxExtension);
        leftExtend.setPosition(Constants.ServoConstants.maxExtension);
        leftWrist.setPosition(Constants.ServoConstants.wristHover);
        rightWrist.setPosition(Constants.ServoConstants.wristHover);
        spin.setPosition(Constants.ServoConstants.spinCenter);

        sleep(500);

        elevator.setTargetPosition(-2960); //-2790
        elevator.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        elevator.setPower(1);

        while(opModeIsActive()) {
            if(elevator.getCurrentPosition() >= elevator.getTargetPosition() -50) {
                break;
            }
        }

        sleep(2000);

        bucketServo.setPosition(0.15);

        sleep(1500);

        bucketServo.setPosition(1);

        sleep(700);

        elevator.setTargetPosition(-110);
        elevator.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        elevator.setPower(-1);

        isRaised = true;

        Log.i(TAG, "got to this point.");
    }


    @Override
    public void runOpMode() throws InterruptedException {
        AtomicBoolean raised = new AtomicBoolean(false);

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
        sweeper = hardwareMap.get(Servo.class, "sweeper");

        elevator.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        elevator.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        leftFront.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        leftBack.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rightFront.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rightBack.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        leftFront.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        leftBack.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightFront.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightBack.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        leftBack.setDirection(DcMotorSimple.Direction.REVERSE);
        leftFront.setDirection(DcMotorSimple.Direction.REVERSE);
        leftExtend.setDirection(Servo.Direction.REVERSE);
        leftWrist.setDirection(Servo.Direction.REVERSE);

        leftFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        leftBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        Pose2d initialPose = new Pose2d(41.9, 63, Math.toRadians(180));
        SparkFunOTOSDrive drive = new SparkFunOTOSDrive(hardwareMap, initialPose);
        TrajectoryActionBuilder tab1 = drive.actionBuilder(initialPose);

        SearchForPiece searchForPiece = new SearchForPiece();

        huskyLens.selectAlgorithm(HuskyLens.Algorithm.COLOR_RECOGNITION);

        telemetry.addData("HuskyLens Initialized", huskyLens.knock());
        telemetry.update();

        intake.setPosition(Constants.ServoConstants.clawOpen);

        waitForStart();

        Action goToBasket = tab1.endTrajectory().fresh()
//                .strafeTo(new Vector2d(51.6544, 54.9196)) //Y: 54.9196, X: 50.6544
//                .strafeTo(new Vector2d(49.6812, 51.4353))
                .strafeTo(new Vector2d(47.01, 51))
                .turnTo(Math.toRadians(-141))
                .build();

        Action goToBasket2 = tab1.endTrajectory().fresh()
//                .strafeTo(new Vector2d(51.6544, 54.9196)) //Y: 54.9196, X: 50.6544
//                .strafeTo(new Vector2d(49.6812, 51.4353))
                .strafeTo(new Vector2d(47.01, 51))
                .turnTo(Math.toRadians(-143))
                .build();

        Action goToBasket3 = tab1.endTrajectory().fresh()
//                .strafeTo(new Vector2d(51.6544, 54.9196)) //Y: 54.9196, X: 50.6544
//                .strafeTo(new Vector2d(49.6812, 51.4353))
                .strafeTo(new Vector2d(46.3, 51))
                .turnTo(Math.toRadians(-143))
                .build();

        Action goToFirstPiece = tab1.endTrajectory().fresh()
//                .turnTo(Math.toRadians(-88))
//                .waitSeconds(0.2)
                .turnTo(Math.toRadians(-88))
                .waitSeconds(0.5)
                .strafeTo(new Vector2d(43.1, 55.4))
                .build();
//
////        new Vector2d(39.1923, 55.0998)
//
        Action goToSecondPiece = tab1.endTrajectory().fresh()
                .strafeTo(new Vector2d(46.0888, 55.4))
                .turnTo(-88)
                .build();

        Action goToThirdPiece = tab1.endTrajectory().fresh()
                .strafeTo(new Vector2d(0, 0))
                .build();

//        CompletableFuture.runAsync(() -> {
//            while(opModeIsActive()) {
//                telemetry.addData("Elevator Pos", elevator.getCurrentPosition());
//                telemetry.update();
//            }
//        });

        Actions.runBlocking(
                new SequentialAction(
                        t -> {
                            raised.set(false);

                            gear.setPosition(Constants.ServoConstants.gearDown);
                            rightExtend.setPosition(Constants.ServoConstants.maxExtension);
                            leftExtend.setPosition(Constants.ServoConstants.maxExtension);
                            leftWrist.setPosition(Constants.ServoConstants.wristHover);
                            rightWrist.setPosition(Constants.ServoConstants.wristHover);
                            spin.setPosition(Constants.ServoConstants.spinCenter);

                            sleep(200);

                            CompletableFuture.runAsync(() -> {
                                elevator.setTargetPosition(-3000); //-2790, -2960
                                elevator.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                                elevator.setPower(1);

                                sleep(2000);

                                bucketServo.setPosition(0.15);

                                sleep(1500);

                                bucketServo.setPosition(1);

                                sleep(700);

                                elevator.setTargetPosition(-110);
                                elevator.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                                elevator.setPower(-1);

                                sleep(200);

                                raised.set(true);
                            });
                            return false;
                        },
                        goToBasket,
                        t -> {
                            while(opModeIsActive()) {
                                if(raised.get()) {
                                    break;
                                }
                            }
                            return false;
                        },
                        goToFirstPiece,
//                        t -> {
//                            intake.setPosition(Constants.ServoConstants.clawOpen);
//                            sleep(300);
//                            gear.setPosition(Constants.ServoConstants.gearDownDown);
//                            sleep(500);
//
//                            leftWrist.setPosition(Constants.ServoConstants.wristDown);
//                            rightWrist.setPosition(Constants.ServoConstants.wristDown);
//                            sleep(500);
//
//                            intake.setPosition(Constants.ServoConstants.clawClosed);
//                            sleep(500);
//
//                            leftWrist.setPosition(Constants.ServoConstants.wristTransfer);
//                            rightWrist.setPosition(Constants.ServoConstants.wristTransfer);
//                            sleep(200);
//
//                            leftExtend.setPosition(Constants.ServoConstants.minExtension);
//                            rightExtend.setPosition(Constants.ServoConstants.minExtension);
//                            gear.setPosition(Constants.ServoConstants.gearTransfer);
//                            sleep(500);
//
//                            intake.setPosition(Constants.ServoConstants.clawOpen);
//                            return false;
//                        }
                        searchForPiece,
                        t -> {
                            raised.set(false);

                            gear.setPosition(Constants.ServoConstants.gearDown);
                            rightExtend.setPosition(Constants.ServoConstants.maxExtension);
                            leftExtend.setPosition(Constants.ServoConstants.maxExtension);
                            leftWrist.setPosition(Constants.ServoConstants.wristHover);
                            rightWrist.setPosition(Constants.ServoConstants.wristHover);
                            spin.setPosition(Constants.ServoConstants.spinCenter);

                            sleep(200);

                            CompletableFuture.runAsync(() -> {
                                elevator.setTargetPosition(-2960); //-2790
                                elevator.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                                elevator.setPower(1);

                                sleep(2000);

                                bucketServo.setPosition(0.15);

                                sleep(1500);

                                bucketServo.setPosition(1);

                                sleep(700);

                                elevator.setTargetPosition(-110);
                                elevator.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                                elevator.setPower(-1);

                                sleep(400);

                                raised.set(true);
                            });
                            return false;
                        },
                        goToBasket2,
                        t -> {
                            while(opModeIsActive()) {
                                if(raised.get()) {
                                    break;
                                }
                            }
                            return false;
                        },
                        goToSecondPiece,
                        searchForPiece,

                        t -> {
                            raised.set(false);

                            gear.setPosition(Constants.ServoConstants.gearDown);
                            rightExtend.setPosition(Constants.ServoConstants.maxExtension);
                            leftExtend.setPosition(Constants.ServoConstants.maxExtension);
                            leftWrist.setPosition(Constants.ServoConstants.wristHover);
                            rightWrist.setPosition(Constants.ServoConstants.wristHover);
                            spin.setPosition(Constants.ServoConstants.spinCenter);

                            sleep(200);

                            CompletableFuture.runAsync(() -> {
                                elevator.setTargetPosition(-2960); //-2790
                                elevator.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                                elevator.setPower(1);

                                sleep(2000);

                                bucketServo.setPosition(0.15);

                                sleep(1500);

                                bucketServo.setPosition(1);

                                sleep(700);

                                elevator.setTargetPosition(-110);
                                elevator.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                                elevator.setPower(-1);

                                sleep(400);

                                raised.set(true);
                            });
                            return false;
                        },
                        goToBasket3
//                        t ->  {CompletableFuture.runAsync(this::runAsync); return false;},
//                        goToBasket,
//                        goToThirdPiece,
//                        searchForPiece,
//                        t ->  {CompletableFuture.runAsync(this::runAsync); return false;},
//                        goToBasket

                )
        );
    }
}
