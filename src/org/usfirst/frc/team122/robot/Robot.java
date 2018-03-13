
package org.usfirst.frc.team122.robot;

import com.kauailabs.navx.frc.AHRS;

import edu.wpi.cscore.UsbCamera;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.I2C.Port;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Timer;

public class Robot extends IterativeRobot {

	Drivetrain drive = new Drivetrain(1, 2, 3, 4, 0.4);
	Lift lift = new Lift(5, 6, 7, 8, 0, 1);
	Gripper gripper = new Gripper(40, 0, 1, 2, 3, 9, 10);
	boolean deployed;

	// Pneumatics
	int PCM = 40;
	Compressor compressor = new Compressor(PCM);

	// Intake

	// Sensors
	AHRS navx = new AHRS(Port.kMXP);

	DigitalInput leftAuto = new DigitalInput(0);
	DigitalInput rightAuto = new DigitalInput(1);
	char selectedAuto;

	// OI
	Joystick driver = new Joystick(0);
	Joystick operator = new Joystick(1);

	// Camera
	UsbCamera camera = CameraServer.getInstance().startAutomaticCapture();

	@Override
	public void robotInit() {

		// Default solenoid values

		// Compressor. This is the only thing that has to be done with it.
		compressor.setClosedLoopControl(true);

		// Auto Switch for selecting position.
		if (leftAuto.get() && !leftAuto.get()) {
			selectedAuto = 'L';
		} else if (!leftAuto.get() && rightAuto.get()) {
			selectedAuto = 'R';
		} else {
			selectedAuto = 'C';
		}
	}

	@Override
	public void robotPeriodic() {

	}

	@Override
	public void autonomousInit() {
		// Temp Auto (forward drop ugliness)
		// drives Forward for 3 seconds into switch, wrist stays up, cube is output
		// upwards and (hopefully) falls into the switch
		String gameData = DriverStation.getInstance().getGameSpecificMessage();
		drive.drive(0.8, 0);
		lift.climb();
		Timer.delay(3);
		if (selectedAuto == gameData.charAt(0)) {
			gripper.eject();
			Timer.delay(2);
		}
	}

	@Override
	public void autonomousPeriodic() {
	}

	@Override
	public void teleopPeriodic() {
		// Drive (standard, no correction, ramps for 0.4 seconds (adjust as needed)]
		drive.drive(driver.getRawAxis(1), driver.getRawAxis(4));

		// Intake wheels
		if (getButton(operator, 5)) {
			gripper.eject();
		} else if (getButton(operator, 6)) {
			gripper.intake();
		} else {
			gripper.stall();
		}

		// Lift
		if (getButton(operator, 2)) { // Down
			lift.descend();
		} else if (getButton(operator, 4)) { // Up
			lift.climb();
		} else {
			lift.stall();
			// Stall
		}

		// Wrist
		if (getButton(operator, 1)) {
			if (deployed) {
				gripper.stow();
			} else {
				gripper.deploy();
			}
		}

		// Gripper (replace solenoid or solenoid wires possibly)
		if (getButton(driver, 6)) { // open if button pressed
			gripper.open();
		} else { // close if button not pressed
			gripper.close();
		}
	}

	@Override
	public void testPeriodic() {
	}

	// Used to shorten statement for button selection on controller
	private boolean getButton(Joystick joystick, int button) {
		return joystick.getRawButton(button);
	}

}
