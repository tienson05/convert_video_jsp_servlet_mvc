package bean;

import java.sql.Timestamp;

public class ConvertedFiles {
	private int id;
	private int job_id;
	private String output_filename;
	private String output_path;
	private long size;
	private double duration_seconds;
	private Timestamp created_at;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getJob_id() {
		return job_id;
	}
	public void setJob_id(int job_id) {
		this.job_id = job_id;
	}
	public String getOutput_filename() {
		return output_filename;
	}
	public void setOutput_filename(String output_filename) {
		this.output_filename = output_filename;
	}
	public String getOutput_path() {
		return output_path;
	}
	public void setOutput_path(String output_path) {
		this.output_path = output_path;
	}
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}
	public double getDuration_seconds() {
		return duration_seconds;
	}
	public void setDuration_seconds(double duration_seconds) {
		this.duration_seconds = duration_seconds;
	}
	public Timestamp getCreated_at() {
		return created_at;
	}
	public void setCreated_at(Timestamp created_at) {
		this.created_at = created_at;
	}

}
