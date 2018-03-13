package org.usfirst.frc.team122.robot;

import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;

public class Elevator
{
	/*
	 * Example of how a subsystem could be implemented using iterative code style.
	 * Files can be made and will run within the code structure (Rename as needed.
	 * Recommend "Intake" or "IntakeSubsystem")
	 */

	// Lift
	private WPI_TalonSRX leftLift, leftLiftSlave, rightLiftSlave1, rightLiftSlave2;

	public Elevator(int liftLeft1, int liftLeft2, int liftRight1, int liftRight2)
	{
		//Master/Slave pairs with right lift set inverted
		this.leftLift = new WPI_TalonSRX(liftLeft1);
		this.leftLift.setNeutralMode(NeutralMode.Brake);
		this.leftLift.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative, 0, 0);
		this.leftLift.config_kP(0, 0.05, 0);
		this.leftLift.config_kD(0, 0, 0);

		this.leftLiftSlave = new WPI_TalonSRX(liftLeft2);
		this.leftLiftSlave.follow(leftLift);
		
		this.rightLiftSlave1 = new WPI_TalonSRX(liftRight1);
		this.rightLiftSlave1.follow(leftLift);
		this.rightLiftSlave1.setInverted(true);
		
		this.rightLiftSlave2 = new WPI_TalonSRX(liftRight2);
		this.rightLiftSlave2.follow(leftLift);
		this.rightLiftSlave2.setInverted(true);
		
	}

	//Elevator Modes
	public void liftUp()
	{
		leftLift.set(0.5);
	}

	public void liftDown()
	{
		leftLift.set(-0.5);
	}

	public void liftStall()
	{
		leftLift.set(0.05);
	}
}