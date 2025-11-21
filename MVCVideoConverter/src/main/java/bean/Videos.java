package bean;

import java.sql.Timestamp;

public class Videos {
	public enum VideoStatus {
		UPLOADED
	}
	
	private int video_id;
    private int client_id;
    private String original_filename;
    private String stored_path;
    private long size;                // dung lượng file (bytes)
    private double duration_seconds;
    private String mime_type;
    private Timestamp  created_at;
    private VideoStatus status;
	public int getVideo_id() {
		return video_id;
	}
	public void setVideo_id(int video_id) {
		this.video_id = video_id;
	}
	public int getClient_id() {
		return client_id;
	}
	public void setClient_id(int client_id) {
		this.client_id = client_id;
	}
	public String getOriginal_filename() {
		return original_filename;
	}
	public void setOriginal_filename(String original_filename) {
		this.original_filename = original_filename;
	}
	public String getStored_path() {
		return stored_path;
	}
	public void setStored_path(String stored_path) {
		this.stored_path = stored_path;
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
	public String getMime_type() {
		return mime_type;
	}
	public void setMime_type(String mime_type) {
		this.mime_type = mime_type;
	}
	public Timestamp getCreated_at() {
		return created_at;
	}
	public void setCreated_at(Timestamp created_at) {
		this.created_at = created_at;
	}
	public VideoStatus getStatus() {
		return status;
	}
	public void setStatus(VideoStatus status) {
		this.status = status;
	}
    
    
}
