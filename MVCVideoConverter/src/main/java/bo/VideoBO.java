package bo;

import java.util.List;

import bean.Videos;
import bean.Videos.VideoStatus;
import dao.VideoDAO;

public class VideoBO {

    private VideoDAO videoDAO;

    public VideoBO() {
        this.videoDAO = new VideoDAO();
    }

    // Thêm video mới
    public int addVideo(Videos video) {
        return videoDAO.addVideo(video);
    }

    // Lấy video theo ID
    public Videos getVideoById(int videoId) {
        return videoDAO.getById(videoId);
    }

    // Lấy danh sách video theo client
    public List<Videos> getVideosByClientId(int clientId) {
        return videoDAO.getByClientId(clientId);
    }

    // Cập nhật trạng thái video
    public boolean updateVideoStatus(int videoId, VideoStatus status) {
        return videoDAO.updateStatus(videoId, status);
    }
}
