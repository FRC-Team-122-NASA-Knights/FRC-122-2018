
package org.usfirst.frc.team122.robot;

import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
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
import edu.wpi.first.wpilibj.drive.DifferentialDrive;

public class Robot extends IterativeRobot
{
	// Controllers
	// Drive (can be made into a subsystem easily)
	WPI_TalonSRX leftDrive = new WPI_TalonSRX(1);
	WPI_TalonSRX leftDriveSlave = new WPI_TalonSRX(2);
	WPI_TalonSRX rightDrive = new WPI_TalonSRX(3);
	WPI_TalonSRX rightDriveSlave = new WPI_TalonSRX(4);
	DifferentialDrive drive = new DifferentialDrive(leftDrive, rightDrive);

	// Lift (Can be made into a subsystem easily)
	Elevator lift = new Elevator(6, 5, 7, 8);

	// Pneumatics
	int PCM = 40;
	Compressor compressor = new Compressor(PCM);

	// Intake
	Gripper gripper = new Gripper(PCM, 0, 1, 2, 3, 9, 10);

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
	public void robotInit()
	{
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


		// Default solenoid values
		gripper.pincherClose();
		gripper.wristUp();

		// Compressor. This is the only thing that has to be done with it.
		compressor.setClosedLoopControl(true);

		// Auto Switch for selecting position.
		if (leftAuto.get() && !leftAuto.get())
		{
			selectedAuto = 'L';
		}	else if (!leftAuto.get() && rightAuto.get())
			{
				selectedAuto = 'R';
			}	else
				{
					selectedAuto = 'C';
				}
	}

	@Override
	public void robotPeriodic()
	{}

	@Override
	public void autonomousInit()
	{
		// Temp Auto (forward drop ugliness)
		// drives Forward for 3 seconds into switch, wrist stays up, cube is output
		// upwards and (hopefully) falls into the switch
		String gameData = DriverStation.getInstance().getGameSpecificMessage();
		drive.arcadeDrive(0.8, 0);
		lift.liftUp();
		Timer.delay(3);
		if (selectedAuto == gameData.charAt(0))
		{
			gripper.eject();
			Timer.delay(2);
		}
	}

	@Override
	public void autonomousPeriodic()
	{}

	@Override
	public void teleopPeriodic()
	{
		// Drive (standard, no correction, ramps for 0.4 seconds (adjust as needed)]
		drive.arcadeDrive(driver.getRawAxis(1), driver.getRawAxis(4));

		// Intake wheels
		if (getButton(operator, 5))
		{
			gripper.eject();
		}	else if (getButton(operator, 6))
			{
				gripper.intake();
			}	else
				{
					gripper.hold();
				}

		// Lift
		if (getButton(operator, 2))
		{
			lift.liftDown();
		}	else if (getButton(operator, 4))
			{
				lift.liftUp();
			}	else
				{
					lift.liftStall(); // Stall
				}

		// Wrist
		if (getButton(operator, 1))
		{
			gripper.wristDown();
		}	else if (getButton(operator, 3))
			{
				gripper.wristUp();
			}	else
				{
					return;
				}

		// Pincher 
		if (getButton(driver, 6))
		{ 
			gripper.pincherOpen();
		}	else
			{
				gripper.pincherClose();
			}
			
		
	}

	@Override
	public void testPeriodic()
	{
		// Tests encoder value from lift
		// System.out.println(lift.getSensorCollection().getQuadraturePosition());
	}

	// Used to shorten statement for button selection on controller
	private boolean getButton(Joystick joystick, int button)
	{
		return joystick.getRawButton(button);
	}
}