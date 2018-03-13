package org.usfirst.frc.team122.robot;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;

public class Lift {

	private WPI_TalonSRX master;
	private WPI_TalonSRX leftSlave;
	private WPI_TalonSRX rightSlave1;
	private WPI_TalonSRX rightSlave2;

	private final double climbSpeed = 1;
	private final double descendSpeed = -1;
	private final double stallSpeed = 0.1;

	public Lift(int masterPort, int left2, int right1, int right2, int topLimit, int bottomLimit) {

		// Lift with master and 3 slaves.
		// RIGHT MOTORS MUST BE INVERTED TO PREVENT SHEARING OF LIFT SHAFT!!!
		master = new WPI_TalonSRX(masterPort);
		leftSlave = new WPI_TalonSRX(left2);
		rightSlave1 = new WPI_TalonSRX(right1);
		rightSlave2 = new WPI_TalonSRX(right2);
		master.setNeutralMode(NeutralMode.Brake);
		master.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative, 0, 0);
		master.config_kP(0, 0.05, 0);
		master.config_kD(0, 0, 0);
		leftSlave.follow(master);
		rightSlave1.follow(master);
		rightSlave1.setInverted(true);
		rightSlave2.follow(master);
		rightSlave2.setInverted(true);

	}

	public void climb() {
		master.set(climbSpeed);
	}

	public void descend() {
		// master.set(ControlMode.Velocity, descendSpeed);
		master.set(descendSpeed);
	}

	public void stall() {
		master.set(stallSpeed);
	}

	public void setHeight(double height) {
		master.set(ControlMode.Position, height);
	}
}
