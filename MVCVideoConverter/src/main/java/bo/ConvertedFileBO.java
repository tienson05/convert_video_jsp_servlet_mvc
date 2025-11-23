package bo;

import java.sql.SQLException;

import bean.ConvertedFiles;
import dao.ConvertedFileDAO;

public class ConvertedFileBO {
    
    private ConvertedFileDAO cfDAO = new ConvertedFileDAO();

    // Hàm được gọi trong Worker.java
    public boolean addConvertedFile(ConvertedFiles cf) {
        return cfDAO.addConvertedFile(cf);
    }
    
    public ConvertedFiles getFileByJobId(int jobId) throws SQLException {
    	return cfDAO.getFileByJobId(jobId);
    }
}