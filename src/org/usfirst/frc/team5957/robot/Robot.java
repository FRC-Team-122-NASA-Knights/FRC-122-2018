
package org.usfirst.frc.team5957.robot;

import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import com.ctre.phoenix.motorcontrol.can.WPI_VictorSPX;
import com.kauailabs.navx.frc.AHRS;

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
	// Drive
	WPI_TalonSRX leftDrive = new WPI_TalonSRX(1);
	WPI_TalonSRX leftDriveSlave = new WPI_TalonSRX(2);
	WPI_TalonSRX rightDrive = new WPI_TalonSRX(3);
	WPI_TalonSRX rightDriveSlave = new WPI_TalonSRX(4);
	DifferentialDrive drive = new DifferentialDrive(leftDrive, rightDrive);

	// Lift
	WPI_TalonSRX leftLift = new WPI_TalonSRX(5);
	WPI_TalonSRX leftLiftSlave = new WPI_TalonSRX(6);
	WPI_TalonSRX rightLift = new WPI_TalonSRX(7);
	WPI_TalonSRX rightLiftSlave = new WPI_TalonSRX(8);
	DifferentialDrive lift = new DifferentialDrive(leftLift, rightLift);

	// Intake
	WPI_VictorSPX spinny = new WPI_VictorSPX(9);
	WPI_VictorSPX spinnySlave = new WPI_VictorSPX(10);

	// Pneumatics
	int PCM = 0;
	Compressor compressor = new Compressor(PCM);
	DoubleSolenoid gripper = new DoubleSolenoid(PCM, 0, 1);
	DoubleSolenoid wrist = new DoubleSolenoid(PCM, 2, 3);
	DoubleSolenoid.Value forward = DoubleSolenoid.Value.kForward;
	DoubleSolenoid.Value reverse = DoubleSolenoid.Value.kForward;

	// Sensors
	AHRS navx = new AHRS(Port.kMXP);
	DigitalInput toplimit = new DigitalInput(4);
	DigitalInput bottomLimit = new DigitalInput(5);

	// OI
	Joystick driver = new Joystick(0);
	Joystick operator = new Joystick(1);

	// Constants

	// PID
	private double output;
	private double PCurrent;
	private double kP = 0.065;
	private double D;
	// 0.3015 too high
	private double kD = 0.304;
	private double PLast;
	private double targetAngle = 90;
	private double currentAngle;

	@Override
	public void robotInit() {
		// Masters/slaves and adjustments to motor controllers
		leftDrive.setNeutralMode(NeutralMode.Coast);
		leftDrive.configClosedloopRamp(0.25, 10);
		leftDrive.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Absolute, 0, 0);
		leftDriveSlave.follow(leftDrive);
		rightDrive.setNeutralMode(NeutralMode.Coast);
		rightDriveSlave.follow(rightDrive);
		leftLift.setNeutralMode(NeutralMode.Brake);
		leftLiftSlave.follow(leftLift);
		rightLift.setNeutralMode(NeutralMode.Brake);
		rightLiftSlave.follow(rightLift);
		spinny.setNeutralMode(NeutralMode.Coast);
		spinnySlave.follow(spinny);
		spinnySlave.setInverted(true);

		// Default solenoid values
		gripper.set(forward);
		wrist.set(reverse);

		// Others
		compressor.setClosedLoopControl(true);
		navx.reset();
	}

	@Override
	public void robotPeriodic() {
		Timer.delay(0.01);

	}

	@Override
	public void autonomousInit() {
		String gameData = DriverStation.getInstance().getGameSpecificMessage();
		double distanceFromCenter = 0;
		double distanceFromSwitch = 0;
		double driveSpeed = 0.5;

		/* Center auto */
		// move forward a foot
		targetAngle = 0;
		while (leftDrive.getSensorCollection().getQuadraturePosition() < distanceFromSwitch / 2) {
			drive.arcadeDrive(driveSpeed, output);
		}
		drive.arcadeDrive(0, 0);
		Timer.delay(1);

		// turn left or right respective switch
		targetAngle = gameData.charAt(0) == 'L' ? -90 : 90;
		while (output > 0.1) {
			drive.arcadeDrive(0, output);
		}
		drive.arcadeDrive(0, 0);
		Timer.delay(1);

		// move forward distanceFromCenter feet
		while (leftDrive.getSensorCollection().getQuadraturePosition() < distanceFromCenter / 2) {
			drive.arcadeDrive(driveSpeed, output);
		}
		drive.arcadeDrive(0, 0);
		Timer.delay(1);

		// turn towards switch
		targetAngle = gameData.charAt(0) == 'L' ? 90 : -90;
		while (output > 0.05) {
			drive.arcadeDrive(0, output);
		}
		drive.arcadeDrive(0, 0);
		Timer.delay(1);

		// Move to switch and raise elevator (doesnt raise rn) (Change drive speed until
		// lift has time to catch up or vice versa
		targetAngle = 0;
		while (leftDrive.getSensorCollection().getQuadraturePosition() < distanceFromSwitch / 2) {
			drive.arcadeDrive(driveSpeed, output);
		}
		drive.arcadeDrive(0, 0);
		Timer.delay(1);

		// Output cube
		spinny.set(0.3);
		Timer.delay(0.5);
	}

	@Override
	public void autonomousPeriodic() {
		currentAngle = navx.getYaw();
		PCurrent = targetAngle - currentAngle;
		D = PLast - PCurrent;
		output = (PCurrent * kP) - (D * kD) > 0.6 ? 0.6 : (PCurrent * kP) - (D * kD);
		PLast = PCurrent;
		Timer.delay(0.01);
	}

	@Override
	public void teleopPeriodic() {
		// Drive
		double speed = -0.7 * getAdjusted(driver.getRawAxis(1), 3);
		double rotation = 0.7 * getAdjusted(driver.getRawAxis(4), 2);
		speed = Math.abs(speed) < 0.10 ? 0 : speed;
		rotation = Math.abs(rotation) < 0.10 ? 0 : rotation;
		drive.arcadeDrive(speed, rotation);

		// Intake
		// A [hold down to spin wheels in, stop when released]
		// B [hold down to spin wheels out, stop when released]
		// C [hold down to open intake jaw, close when released] ( driver )
		// D [drop intake to horizontal position when pressed, raise intake to vertical
		// position when pressed again]
		// Spinnies
		// A & B
		if (getButton(operator, 1)) {
			spinny.set(0.5);
		} else if (getButton(operator, 2)) {
			spinny.set(-0.5);
		} else {
			spinny.set(0.1);
		}

		// C
		DoubleSolenoid.Value gripperPosition = (gripper.get() == forward) && getButton(operator, 6) ? reverse : forward;
		gripper.set(gripperPosition);

		// D
		if (getButton(operator, 6)) {
			wrist.set(reverse);
		} else if (getButton(operator, 7)) {
			wrist.set(forward);
		}

		// Lift
		double liftSpeed = 0.5 * operator.getRawAxis(1);
		if (toplimit.get() && liftSpeed < 0) {
			liftSpeed = 0;
		} else if (bottomLimit.get() && liftSpeed > 0) {
			liftSpeed = 0;
		} else {
			lift.tankDrive(liftSpeed, 0); // TODO test the directions for the lift
		}

		Timer.delay(0.01);
	}

	@Override
	public void testPeriodic() {
		System.out.println("Drive Quad Position: " + leftDrive.getSensorCollection().getQuadraturePosition());
		System.out.println("Drive PW Position: " + leftDrive.getSensorCollection().getPulseWidthPosition());

		Timer.delay(0.01);
	}

	// Adjusted value to make control less sensitive
	private double getAdjusted(double n, double exponent) {
		return n * Math.pow(Math.abs(n), exponent - 1);
	}

	private boolean getButton(Joystick joystick, int button) {
		return joystick.getRawButton(button);
	}

}
