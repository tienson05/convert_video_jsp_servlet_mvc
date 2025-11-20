package task;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import bean.ConvertedFiles;
import bean.Jobs;
import bean.Videos;
import bean.Jobs.JobStatus;
import bo.JobBO;
import bo.VideoBO;
import bo.ConvertedFileBO;

// Phần của Toàn
// nếu có sửa tên hàm hoặc tham số  truyền vào thì qua controller sửa với nhé
// Còn thiếu code trong file JobBO/DAO, ConvertedFileBO/DAO: trong quá trình làm Toàn thêm luôn cho thuận lợi code luôn nha

public class Worker {
    // Thread pool cho convert video
    private static final ExecutorService executor = Executors.newFixedThreadPool(5); // tối đa 5 video convert cùng lúc

    public static void submitConversionTask(Videos video, String targetFormat) {
        executor.submit(() -> {
            try {
                // 1. Tạo bản ghi job ngay trong worker
                Jobs job = new Jobs();
                job.setVideo_id(video.getVideo_id());
                job.setStatus(JobStatus.PROCESSING);
                JobBO jobBO = new JobBO();
//                int jobId = jobBO.addJob(job);
//
//                // 2. Convert video
//                String outputPath = doConvert(video.getStored_path());
//
//                // 3. Tạo bản ghi converted_files
                ConvertedFiles cf = new ConvertedFiles();
//                cf.setVideo_id(video.getVideo_id());
//                cf.setFile_path(outputPath);
                ConvertedFileBO cfBO = new ConvertedFileBO();
//                cfBO.addConvertedFile(cf);
//
//                // 4. Cập nhật status video và job
//                video.setStatus(VideoStatus.COMPLETED);
//                VideoBO videoBO = new VideoBO();
//                videoBO.updateVideo(video);
//
//                job.setStatus("COMPLETED");
//                jobBO.updateJob(job);

            } catch(Exception e) {
                // cập nhật trạng thái FAILED
//                video.setStatus(VideoStatus.FAILED);
//                new VideoBO().updateVideo(video);
//
//                Jobs job = new Jobs();
//                job.setVideo_id(video.getVideo_id());
//                job.setStatus("FAILED");
//                new JobBO().updateJob(job);

                e.printStackTrace();
            }
        });
    }

}

