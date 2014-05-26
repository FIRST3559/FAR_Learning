/*----------------------------------------------------------------------------*/
/* Copyright (c) FIRST 2008. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/
package edu.wpi.first.wpilibj.templates;

import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.SimpleRobot;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.Victor;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.Relay;
import edu.wpi.first.wpilibj.Talon;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the SimpleRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class RobotTemplate extends SimpleRobot {

    RobotDrive drive = new RobotDrive(1, 2); // y-con to 2left & 2right speed controllers
    Joystick leftStick = new Joystick(1); // tankDriveLeft
    Joystick rightStick = new Joystick(2); // tankDriveRight
    Joystick thirdStick = new Joystick(3); // Payload Operation
    Solenoid armOut = new Solenoid(1); // pushArmOut-cylinder extend
    Solenoid armIn = new Solenoid(3); // pullArmIn-clyinder retract
    Solenoid triggerGo = new Solenoid(4); // releaseTrigger-extend
    Solenoid triggerSet = new Solenoid(2); // resetTrigger-retract
    Compressor compressor = new Compressor(2, 2); // 6=digI/O 2=relay dI/O con sig(w) & gnd(blk)
    Talon winch = new Talon(8); // y-con to 2 speed controllers
    //Victor ballFeed = new Victor(6); // single speed controller for ball feed motor
    DigitalInput armLimit = new DigitalInput(7); // detect arm at bottom range of motion
    Relay feed = new Relay(3); //spike to turn ball feed fwd/rev/off
    boolean autoCounter = true; // variable to start and stop a while loop program after execution

    public void autonomous() {
        drive.setSafetyEnabled(false); // oddly enough, allows the motors to spin uninterrupted
        while (true && isEnabled() && isAutonomous()) { // checks to ensure autonomous is selected and enabled
            if (autoCounter == true) { //starts a loop to ensure autonomous doesn't repeat itself
                compressor.start(); // starts compressor, turns off at 120 psi, turns back on at 95 psi
                drive.tankDrive(0.6, 0.6); // sets drive motors to 60% power
                Timer.delay(4.7); // delays the program (in seconds)
                drive.tankDrive(0.0, 0.0); // stops drive motors
                armIn.set(true); /// pushes the arm out (contrary to name)
                Timer.delay(0.5); // delays the program (in seconds)
                feed.set(Relay.Value.kForward); // turns on the loader motor to prevent jamming the ball
                Timer.delay(0.5); // delays the program
                feed.set(Relay.Value.kOff); // stops the loader motor
                Timer.delay(0.8); // delays the program
                triggerGo.set(true); // releases the catapult trigger
                autoCounter = false; // stops the loop
            }
        }
    }

// operator control was moved from here to the end of the file
    public void test() {
        compressor.start(); //starts compressor, turns off at 120 psi, turns back on at 95 psi
        triggerGo.set(true); // releases the catapult trigger
    }

    public void driving() {
        drive.tankDrive(-(leftStick.getY(GenericHID.Hand.kLeft)),
                -(rightStick.getY(GenericHID.Hand.kLeft))); // allows robot to drive

    }

    public void windUp() {
        if (armLimit.get() == false) { // checks to see if limit switch is tripped
            winch.set(0.0); // turns off the winch if the switch is tripped
            triggerSet.set(true); // the catch pushes out and holds the catapult in place
            Timer.delay(0.2); // delays the program
            triggerSet.set(false); // releases the catch variable (catch still stays in place, but this is necessary to enable it to be pushed out later)
            unWind(); // runs the unWind void code
        } else {
            winch.set(0.4); // if limit switch isn't tripped, sets the winch to 40% power
        }
    }

    public void unWind() {
        /*
         * Simple operation to unwind the strap that pulls down the arm uses a 
         * timer to set how long the motor turn the other direction. Strech goal
         * will be to add feedback to increase precision.
         */
        winch.set(-0.2); // sets the winch to negative 20% power
        Timer.delay(3.8); // correct delay to allow the catapult to release
        winch.set(0.0); // turns off the winch
    }

    public void ballMotor() {
        /*
         * This function is called by a button press, ts(4) OR ts(5). Controls
         * the spike relay(3) and sets it fwd-ts(4) or rev-ts(5).
         * The motor turns off when the button ts(4) or ts(5) is released.
         */
        while (thirdStick.getRawButton(4) == true) {
            feed.set(Relay.Value.kForward);
            //ballFeed.set(0.5);
        }
        while (thirdStick.getRawButton(5) == true) {
            feed.set(Relay.Value.kReverse);
            //ballFeed.set(-0.5);
        }
        feed.set(Relay.Value.kOff);
        //ballFeed.set(0.0);
    }

    public void operatorControl() {
        compressor.start(); //start the compressor and let it do its thing
        drive.setSafetyEnabled(false);            //this command allows drive to work at the same time as the other motors

        while (true && isOperatorControl() && isEnabled()) { // all polling takes place in this while loop
            driving();      // drive system simple 

            if (thirdStick.getRawButton(8) == true) { // wind up the strap and pull the arm down
                windUp(); // executes code in the windUp void
            } else {
                winch.set(0.0); // sets winch to 0% power
            }

            if (thirdStick.getRawButton(9) == true) {
                unWind(); // runs code in the unWind void
            }

            if (thirdStick.getRawButton(4) == true) {
                feed.set(Relay.Value.kForward); // turns the loader motor on in the forward direction
            } else if (thirdStick.getRawButton(5) == true) { // put in place to prevent the code from clashin with each other (instead of the normal "else" or "if")
                feed.set(Relay.Value.kReverse); // turns the loader motor on in the backward direction
            } else {
                feed.set(Relay.Value.kOff); // turns the loader motor off
            }
//            }

            if (thirdStick.getRawButton(1) == true) {
                triggerGo.set(true); // releases the catapult catch and thus the catapult
            } else {
                triggerGo.set(false); // keeps the catch in place
            }

            if (thirdStick.getRawButton(2)) {
                armIn.set(true); // pushes the arm out, contrary to name (due to wiring)
            } else {
                armIn.set(false); // allows arm to go in after it goes out
            }

            if (thirdStick.getRawButton(3)) {
                armOut.set(true); // pulls the arm in, contrary to name (due to wiring)
            } else {
                armOut.set(false); // allows arm to go out after it goes in
            }
        }
    }
}
