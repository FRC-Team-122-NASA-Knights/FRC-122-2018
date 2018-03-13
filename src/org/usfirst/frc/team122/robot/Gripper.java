package org.usfirst.frc.team122.robot;

import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.WPI_VictorSPX;

import edu.wpi.first.wpilibj.DoubleSolenoid;

public class Gripper {

	// Gripper pneumatics (wrist not included)
	private DoubleSolenoid gripper;
	// TODO check if this is proper orientation
	private static DoubleSolenoid.Value open = DoubleSolenoid.Value.kReverse;
	private static DoubleSolenoid.Value closed = DoubleSolenoid.Value.kForward;

	DoubleSolenoid wrist;
	// TODO check if this is proper orientation
	DoubleSolenoid.Value stowed = DoubleSolenoid.Value.kForward;
	DoubleSolenoid.Value deployed = DoubleSolenoid.Value.kForward;

	// Intake wheels
	private WPI_VictorSPX spinny, spinnySlave;
	private final double ejectSpeed = 0.75;
	private final double intakeSpeed = -1;
	private final double stallSpeed = -0.15;

	public Gripper(int PCM_CAN_Channel, int PCM_Gripper_1, int PCM_Gripper_2, int PCM_Wrist_1, int PCM_Wrist_2,
			int leftSpinny, int rightSpinny) {

		// Solenoids
		this.gripper = new DoubleSolenoid(PCM_CAN_Channel, PCM_Gripper_1, PCM_Gripper_2);
		this.wrist = new DoubleSolenoid(PCM_CAN_Channel, PCM_Wrist_1, PCM_Wrist_2);

		// Intake/Eject wheels. Master/Slave pair with right wheel set inverted to allow
		// wheels to spin in opposite directions.
		this.spinny = new WPI_VictorSPX(leftSpinny);
		this.spinny.setNeutralMode(NeutralMode.Coast);
		this.spinnySlave = new WPI_VictorSPX(rightSpinny);
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

	public void intake() {
		spinny.set(intakeSpeed);
	}

	public void eject() {
		spinny.set(ejectSpeed);
	}

	public void stall() {
		spinny.set(stallSpeed);
	}

	public void stow() {
		wrist.set(stowed);
	}

	public void deploy() {
		wrist.set(deployed);
	}
}
