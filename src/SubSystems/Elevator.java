package SubSystems;

import Utilities.Ports;
import edu.wpi.first.wpilibj.CANTalon;
import edu.wpi.first.wpilibj.CANTalon.TalonControlMode;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Elevator {
	private static Elevator instance = null;
    private CANTalon elevator_motor;
    public static final int UP = 0;
    public static final int DOWN = 1;
    public static final int STOP = 2;
    private int status = 1;
    public static Elevator getInstance()
    {
        if( instance == null )
            instance = new Elevator();
        return instance;
    }
    
    public Elevator(){
    	elevator_motor = new CANTalon(Ports.ELEVATOR);
    	elevator_motor.configNominalOutputVoltage(+0f, -0f);
    	elevator_motor.configPeakOutputVoltage(+12f, -12f);
    	elevator_motor.changeControlMode(TalonControlMode.Voltage);
//    	intake_arm_motor.setPID(4.0, 0.001, 240.0, 0.0, 0, 0.0, 0);
//    	intake_arm_motor.setPID(3.0, 0.0, 240.0, 0.0, 0, 0.0, 1);
    	elevator_motor.setProfile(0);
    	
    }
    public int status(){
    	return status;
    }
    public void up(){
    	status = 0;
    	setVoltage(-12.0);
    }
    public void down(){
    	status = 1;
    	setVoltage(12.0);
    }
    private void check(int direction){
    	switch(direction){
    	case UP:
    		if(elevator_motor.getOutputCurrent() > 20){
        		setVoltage(-1.0);
        	}
    		break;
    	case DOWN:
    		if(elevator_motor.getOutputCurrent() > 20){
        		setVoltage(0.5);
        	}
    	}
    	
    }
    public void stop(){
    	setVoltage(0.0);
    	status = STOP;
    }
    public void update(){
    	SmartDashboard.putNumber("ELE_DRAW", elevator_motor.getOutputCurrent());
    	SmartDashboard.putNumber("ELE_GOAL", elevator_motor.getSetpoint());
    	SmartDashboard.putNumber("ELE_POWER", elevator_motor.getOutputVoltage());
    	SmartDashboard.putNumber("ELE_P", elevator_motor.getP());
    	check(status);
    }
    
    public void setVoltage(double current){
    	elevator_motor.set(current);
    }
}