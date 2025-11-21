package bean;

import java.sql.Timestamp;

public class Jobs {
	public enum JobStatus {
		PENDING,
		QUEUED,
		PROCESSING,
		COMPLETED,
		FAILED
	}
	private int job_id;
	private int video_id;
	private String target_format;
	private JobStatus status;
	private int progress;
	private Timestamp created_at;
	private Timestamp updated_at;
	public int getJob_id() {
		return job_id;
	}
	public void setJob_id(int job_id) {
		this.job_id = job_id;
	}
	public int getVideo_id() {
		return video_id;
	}
	public void setVideo_id(int video_id) {
		this.video_id = video_id;
	}
	public String getTarget_format() {
		return target_format;
	}
	public void setTarget_format(String target_format) {
		this.target_format = target_format;
	}
	public JobStatus getStatus() {
		return status;
	}
	public void setStatus(JobStatus status) {
		this.status = status;
	}
	public int getProgress() {
		return progress;
	}
	public void setProgress(int progress) {
		this.progress = progress;
	}
	public Timestamp getCreated_at() {
		return created_at;
	}
	public void setCreated_at(Timestamp created_at) {
		this.created_at = created_at;
	}
	public Timestamp getUpdated_at() {
		return updated_at;
	}
	public void setUpdated_at(Timestamp updated_at) {
		this.updated_at = updated_at;
	}
		
}
