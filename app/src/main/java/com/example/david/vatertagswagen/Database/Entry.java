package com.example.david.vatertagswagen.Database;

public class Entry {
	private long ID;
	private String DeviceName;
	private String DeviceMAC;
	public long getID() {
		return ID;
	}
	public void setID(long ID) {
		this.ID = ID;
	}
	public String getDeviceName() {
		return DeviceName;
	}
	public void setDeviceName (String DeviceName) {
	this.DeviceName = DeviceName;
	}
	public String getDeviceMAC() {
	return DeviceMAC;
	}
	public void setDeviceMAC(String DeviceMAC) {
	this.DeviceMAC = DeviceMAC;
	}
}