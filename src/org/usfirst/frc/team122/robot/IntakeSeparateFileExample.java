package org.usfirst.frc.team5957.robot;

import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;

import edu.wpi.first.wpilibj.DoubleSolenoid;

public class IntakeSeparateFileExample {

	/*
	 * Example of how a subsystem could be implemented using iterative code style.
	 * Files can be made and will run within the code structure (Rename as needed.
	 * Recommend "Intake" or "IntakeSubsystem")
	 */

	// Gripper pneumatics (wrist not included)
	private DoubleSolenoid gripper;
	private static DoubleSolenoid.Value open = DoubleSolenoid.Value.kReverse; // TODO check if this is proper
																				// orientation
	private static DoubleSolenoid.Value closed = DoubleSolenoid.Value.kForward;

	// Intake wheels
	private WPI_TalonSRX spinny, spinnySlave;

	public IntakeSeparateFileExample(int PCM_CAN_Channel, int PCM_Port_1, int PCM_Port_2, int leftSpinny,
			int rightSpinny) {

		// Gripper Solenoid
		this.gripper = new DoubleSolenoid(PCM_CAN_Channel, PCM_Port_1, PCM_Port_2);

		// Intake/Eject wheels. Master/Slave pair with right wheel set inverted to allow
		// sheels to spin in opposite directions.
		this.spinny = new WPI_TalonSRX(leftSpinny);
		this.spinny.setNeutralMode(NeutralMode.Coast);
		this.spinnySlave = new WPI_TalonSRX(rightSpinny);
		this.spinnySlave.follow(spinny);
		this.spinnySlave.setInverted(true);
	}

	// Gripper states
	public void open() {
		gripper.set(open);
	}

	public void close() {
		gripper.set(closed);
	}

	// Intake wheels (adjust negation as needed)
	public void intake() {
		spinny.set(-1);
	}

	public void eject() {
		spinny.set(0.75);
	}

	public void stall() {
		spinny.set(-0.15);
	}

}
