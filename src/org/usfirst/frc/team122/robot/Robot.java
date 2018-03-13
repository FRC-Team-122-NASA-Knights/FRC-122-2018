
package org.usfirst.frc.team5957.robot;

import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import com.kauailabs.navx.frc.AHRS;

import edu.wpi.cscore.UsbCamera;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.I2C.Port;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;

public class Robot extends IterativeRobot {

	// Controllers
	// Drive (can be made into a subsystem easily)
	WPI_TalonSRX leftDrive = new WPI_TalonSRX(1);
	WPI_TalonSRX leftDriveSlave = new WPI_TalonSRX(2);
	WPI_TalonSRX rightDrive = new WPI_TalonSRX(3);
	WPI_TalonSRX rightDriveSlave = new WPI_TalonSRX(4);
	DifferentialDrive drive = new DifferentialDrive(leftDrive, rightDrive);

	// Lift (Can be made into a subsystem easily)
	WPI_TalonSRX lift = new WPI_TalonSRX(6);
	WPI_TalonSRX leftLiftSlave = new WPI_TalonSRX(5);
	WPI_TalonSRX rightLiftSlave1 = new WPI_TalonSRX(7);
	WPI_TalonSRX rightLiftSlave2 = new WPI_TalonSRX(8);

	// Pneumatics
	int PCM = 40;
	Compressor compressor = new Compressor(PCM);
	DoubleSolenoid wrist = new DoubleSolenoid(PCM, 2, 3);
	DoubleSolenoid.Value open = DoubleSolenoid.Value.kForward; // TODO check if this is proper orientation
	DoubleSolenoid.Value closed = DoubleSolenoid.Value.kForward;

	// Intake
	IntakeSeparateFileExample gripper = new IntakeSeparateFileExample(PCM, 0, 1, 9, 10);

	// Sensors
	AHRS navx = new AHRS(Port.kMXP);
	// DigitalInput toplimit = new DigitalInput(4);
	// DigitalInput bottomLimit = new DigitalInput(5);
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

		/* Masters/slaves and adjustments to motor controllers */
		// Drive with master/slave pair on both sides. Master uses CTRE MagEncoder and
		// ramps over t = 0.4s. Do not need time out (set to 0) on any unless we begin
		// encountering errors
		leftDriveSlave.follow(leftDrive);
		leftDrive.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative, 0, 0);
		leftDrive.configOpenloopRamp(0.4, 0);
		rightDriveSlave.follow(rightDrive);
		rightDrive.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative, 0, 0);
		rightDrive.configOpenloopRamp(0.4, 0);

		// Lift with master and 3 slaves.
		// RIGHT MOTORS MUST BE INVERTED TO PREVENT SHEARING OF LIFT SHAFT!!!
		lift.setNeutralMode(NeutralMode.Brake);
		lift.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative, 0, 0);
		lift.config_kP(0, 0.05, 0);
		lift.config_kD(0, 0, 0);
		leftLiftSlave.follow(lift);
		rightLiftSlave1.follow(lift);
		rightLiftSlave1.setInverted(true);
		rightLiftSlave2.follow(lift);
		rightLiftSlave2.setInverted(true);

		// Default solenoid values
		wrist.set(open);

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
		drive.arcadeDrive(0.8, 0);
		lift.set(0.8);
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
		drive.arcadeDrive(driver.getRawAxis(1), driver.getRawAxis(4));

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
			lift.set(-0.8);
		} else if (getButton(operator, 4)) { // Up
			lift.set(1);
		} else {
			lift.set(0.05); // Stall
		}

		// Wrist (add
		if (getButton(operator, 1)) {
			if (wrist.get() == open) { // close if open
				wrist.set(closed);
			} else if (wrist.get() == closed) { // open if closed
				wrist.set(open);
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
		// Tests encoder value from lift
		System.out.println(lift.getSensorCollection().getQuadraturePosition());
	}

	// Used to shorten stetement for button selection on controller
	private boolean getButton(Joystick joystick, int button) {
		return joystick.getRawButton(button);
	}

}
