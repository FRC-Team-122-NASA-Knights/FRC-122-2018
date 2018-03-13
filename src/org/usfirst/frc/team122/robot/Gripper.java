package org.usfirst.frc.team122.robot;

import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.WPI_VictorSPX;

import edu.wpi.first.wpilibj.DoubleSolenoid;

public class Gripper
{
	/*
	 * Example of how a subsystem could be implemented using iterative code style.
	 * Files can be made and will run within the code structure (Rename as needed.
	 * Recommend "Intake" or "IntakeSubsystem")
	 */

	// Gripper Pneumatics
	private DoubleSolenoid pincher, wrist;
	
	private static DoubleSolenoid.Value open = DoubleSolenoid.Value.kReverse; // TODO check if this is proper
	private static DoubleSolenoid.Value closed = DoubleSolenoid.Value.kForward;
	
	private static DoubleSolenoid.Value up = DoubleSolenoid.Value.kReverse;
	private static DoubleSolenoid.Value down = DoubleSolenoid.Value.kForward;

	// Intake wheels
	private WPI_VictorSPX leftIntake, rightIntakeSlave;

	public Gripper(int PCM_CAN_Channel, int PCM_Port_1, int PCM_Port_2, int PCM_Port_3,
			int PCM_Port_4, int leftSpinny,	int rightSpinny)
	{
		// Gripper Solenoid
		this.pincher = new DoubleSolenoid(PCM_CAN_Channel, PCM_Port_1, PCM_Port_2);
		this.wrist = new DoubleSolenoid(PCM_CAN_Channel, PCM_Port_3, PCM_Port_4);

		// Intake/Eject wheels. Master/Slave pair with right wheel set inverted to allow
		// wheels to spin in opposite directions.
		this.leftIntake = new WPI_VictorSPX(leftSpinny);
		this.leftIntake.setNeutralMode(NeutralMode.Coast);
		
		this.rightIntakeSlave = new WPI_VictorSPX(rightSpinny);
		this.rightIntakeSlave.follow(leftIntake);
		this.rightIntakeSlave.setInverted(true);
	}

	// Pincher states
	public void pincherOpen()
	{
		pincher.set(open);
	}

	public void pincherClose()
	{
		pincher.set(closed);
	}
	
	// Wrist states
	public void wristUp()
	{
		wrist.set(up);
	}
	
	public void wristDown()
	{
		wrist.set(down);
	}
	
	// Intake wheels
	public void intake()
	{
		leftIntake.set(-.5);
	}

	public void eject()
	{
		leftIntake.set(0.5);
	}

	public void hold()
	{
		leftIntake.set(-0.15);
	}
}
