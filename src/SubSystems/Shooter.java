package SubSystems;

import Utilities.Constants;
import Utilities.Ports;
import Utilities.Util;
import edu.wpi.first.wpilibj.CANTalon;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.CANTalon.FeedbackDevice;
import edu.wpi.first.wpilibj.CANTalon.TalonControlMode;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Shooter 
{
    private CANTalon motor1;
    private CANTalon motor2;
    private CANTalon motor3;
    private CANTalon motor4;
    private CANTalon preloader_motor;
    private Solenoid hood;
    private int absolutePosition;
    private static Shooter instance = null;
    public static final int CLOSE = 0;
    public static final int FAR   = 1;
    private static final int STOPPED = 2;
    private int status = STOPPED;
    private boolean firing = false;
    public boolean fenderShot = false;
    private Elevator elevator;
    private Turret turret;
    private ShootingAction fireCommand;
    private double speed = 0;
    public static Shooter getInstance()
    {
        if( instance == null )
            instance = new Shooter();
        return instance;
    }
    
    public Shooter(){
    	motor1 = new CANTalon(Ports.SHOOTER_MOTOR_3);
    	absolutePosition = motor1.getPulseWidthPosition() & 0xFFF;
    	motor1.setEncPosition(absolutePosition);
    	motor1.setFeedbackDevice(FeedbackDevice.QuadEncoder);
    	motor1.reverseSensor(false);
    	motor1.configEncoderCodesPerRev(360);
    	motor1.configNominalOutputVoltage(+0f, -0f);
    	motor1.configPeakOutputVoltage(+12f, -12f);
    	motor1.setAllowableClosedLoopErr(0); 
    	motor1.changeControlMode(TalonControlMode.Speed);
    	motor1.set(motor1.getPosition());
//    	motor1.setPID(0.048, 0.0, 0.0, 0.048, 0, 0.0, 0);
//    	motor1.setPID(0.0, 0.0, 0.0, 0.048, 0, 0.0, 1);
    	motor1.setProfile(0);
        motor2 = new CANTalon(Ports.SHOOTER_MOTOR_1);
        motor2.changeControlMode(TalonControlMode.Follower);
        motor2.set(Ports.SHOOTER_MOTOR_3);
        motor3 = new CANTalon(Ports.SHOOTER_MOTOR_2);
        motor3.changeControlMode(TalonControlMode.Follower);
        motor3.set(Ports.SHOOTER_MOTOR_3);
        motor4 = new CANTalon(Ports.SHOOTER_MOTOR_4);
        motor4.changeControlMode(TalonControlMode.Follower);
        motor4.set(Ports.SHOOTER_MOTOR_3);
        preloader_motor = new CANTalon(Ports.PRELOAD);
        elevator = Elevator.getInstance();
        turret  = Turret.getInstance();
        hood = new Solenoid(Ports.HOOD);
    }
    public void update(){
    	SmartDashboard.putNumber("SHOOTER_SPEED", motor1.getSpeed());
    	SmartDashboard.putNumber("SHOOTER_GOAL", motor1.getSetpoint());
    	SmartDashboard.putNumber("SHOOTER_POWER", motor1.getOutputVoltage());
    	SmartDashboard.putNumber("SHOOTER_CURRENT", motor1.getOutputCurrent());
    	SmartDashboard.putNumber("SHOOTER_ERROR", motor1.getSetpoint()-motor1.getSpeed());
    	
    }
    public void setGoal(double goal){
    	speed = goal;
    }
    public void setPresetSpeed(int preset){
    	switch(preset){
    	case CLOSE:
    		status = CLOSE;
    		set(Constants.SHOOTER_CLOSE_SHOT);
    		break;
    	case FAR:
    		status = FAR;
    		set(Constants.SHOOTER_FAR_SHOT);
    		break;
    	default:
    		set(speed);
    		break;
    	}
    }
    public void set(double _speed){
    	motor1.setProfile(0);
    	motor1.set(_speed);
    }
    public void stop(){
    	motor1.setProfile(1);
    	motor1.set(0.0);
    }
    public void hoodExtend(){ 
    	double angle = turret.getAngle();
    	if((angle > Constants.TURRET_HOOD_MIN_ANGLE) && (angle < Constants.TURRET_HOOD_MAX_ANGLE) ){
    		turret.set(0.0);
    	}
    	hood.set(false); 
    } 
    public void hoodRetract(){ 
    	if((elevator.status() != Elevator.MOVING) || (elevator.status() != Elevator.STOP) )
    		hood.set(true); 
    }	
    public void preloader_forward(){
    	preloader_motor.set(-0.75);
    }
    public void preloader_reverse(){
    	preloader_motor.set(0.75);
    }
    public void preloader_stop(){
    	preloader_motor.set(0.0);
    }
    public void fire(){
    	if(!firing){
	    	fireCommand = new ShootingAction();
	    	fireCommand.start();
    	}
    }
    public class ShootingAction extends Thread{
    	boolean keeprunning = true;
		@Override
		public void run() {
			firing = true;
			System.out.println("Fire Started");
			while(keeprunning){
				System.out.println("Fire Checking 1");
				switch(status){
					case CLOSE:
						if(Util.onTarget(Constants.SHOOTER_CLOSE_SHOT, motor1.get(), 50.0) ){
							preloader_forward();
							Timer.delay(2.0);
							preloader_stop();
							
						}else{
							System.out.println("ERROR" + motor1.get());
						}
						keeprunning = false;
						break;
					case FAR:
						if(Util.onTarget(Constants.SHOOTER_FAR_SHOT, motor1.get(), 50.0) ){
							preloader_forward();
							Timer.delay(2.0);
							preloader_stop();
						}else{
							System.out.println("ERROR" + motor1.get());
						}
						keeprunning = false;
						break;
					default:
						System.out.println("ERROR2");
						keeprunning = false;
						break;
				}				
			}
			firing = false;			
		}
    	
    }
}