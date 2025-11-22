package bo;

import java.util.List;

import bean.Jobs;
import dao.JobDAO;

public class JobBO {
    private JobDAO jobDAO = new JobDAO();

    // QUAN TRỌNG: Phải trả về int (ID của job)
    public int addJob(Jobs job) {
        return jobDAO.addJob(job);
    }

    public boolean updateJob(Jobs job) {
        return jobDAO.updateJob(job);
    }
    
    public List<Jobs> getAllJobsByClientId(int clientId) {
    	return jobDAO.getAllJobs(clientId);
    }
    
    public Jobs getJob(int jobId) {
    	return jobDAO.getJob(jobId);
    }
    
}