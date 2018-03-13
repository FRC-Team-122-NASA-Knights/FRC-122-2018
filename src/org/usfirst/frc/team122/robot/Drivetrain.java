package org.usfirst.frc.team122.robot;

import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;

import edu.wpi.first.wpilibj.drive.DifferentialDrive;

public class Drivetrain {
	private WPI_TalonSRX left;
	private WPI_TalonSRX leftSlave;
	private WPI_TalonSRX right;
	private WPI_TalonSRX rightSlave;
	private DifferentialDrive drive;

	private double maxSpeed = 1;
	private double maxRotation = 1;

	public Drivetrain(int frontLeft, int rearLeft, int frontRight, int rearRight, double rampTime) {

		/* Masters/slaves and adjustments to motor controllers */
		// Drive with master/slave pair on both sides. Master uses CTRE MagEncoder and
		// ramps over t = rampTime s. Do not need time out (set to 0) on any unless we
		// begin encountering errors

		left = new WPI_TalonSRX(frontLeft);
		leftSlave = new WPI_TalonSRX(rearLeft);
		leftSlave.follow(left);
		right = new WPI_TalonSRX(frontRight);
		rightSlave = new WPI_TalonSRX(rearRight);
		rightSlave.follow(right);

		left.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative, 0, 0);
		right.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative, 0, 0);
		setRamping(rampTime);

		drive = new DifferentialDrive(left, right);

	}

	public void setRamping(double seconds) {
		left.configOpenloopRamp(seconds, 0);
		right.configOpenloopRamp(seconds, 0);
	}

	public void setMaxSpeed(double speed) {
		maxSpeed = speed;
	}

	public void setMaxRotation(double speed) {
		maxRotation = speed;
	}

	public void drive(double speed, double rotation) {
		drive.arcadeDrive(maxSpeed * speed, maxRotation * rotation);
	}

}
