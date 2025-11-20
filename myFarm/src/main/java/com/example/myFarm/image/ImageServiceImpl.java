package com.example.myFarm.image;

import com.example.myFarm.command.ImageVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class ImageServiceImpl implements ImageService {

    @Value("${project.upload.path}") // 예: C:/Image
    private String uploadPath;

    @Autowired
    private ImageMapper imageMapper;

    @Override
    @Transactional
    public void saveItemImages(long itemId,
                               MultipartFile mainImage,
                               List<MultipartFile> detailImages) {

        System.out.println("이미지 삽입 디버그 메시지");

        int insertCount = 0;

        // ========= 1) MAIN 이미지 저장 =========
        if (mainImage != null && !mainImage.isEmpty()) {
            try {
                String mainUrl = saveOneFileAndGetUrl(mainImage); // dirPath/uuid_filename 형식

                ImageVO mainVO = new ImageVO();
                mainVO.setImageUrl(mainUrl);     // 예: 2511/uuid_file.jpg
                mainVO.setImageType("MAIN");     // ENUM('MAIN','DETAIL')
                mainVO.setItemId(itemId);

                insertCount += imageMapper.insertImage(mainVO);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("대표 이미지 저장 실패", e); // 실패시 롤백
            }
        }

        // ========= 2) DETAIL 이미지들 저장 =========
        if (detailImages != null) {
            // 최대 5개까지만 처리
            List<MultipartFile> limited = detailImages.size() > 5 ? detailImages.subList(0, 5) : detailImages;

            for (MultipartFile file : limited) {
                if (file == null || file.isEmpty()) continue;

                try {
                    String detailUrl = saveOneFileAndGetUrl(file);

                    ImageVO detailVO = new ImageVO();
                    detailVO.setImageUrl(detailUrl);
                    detailVO.setImageType("DETAIL");
                    detailVO.setItemId(itemId);

                    insertCount += imageMapper.insertImage(detailVO);
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException("상세 이미지 저장 실패", e);
                }
            }
        }
    }

    @Override
    public void deleteImagesByIds(List<Long> idsToDelete) {
        if (idsToDelete == null || idsToDelete.isEmpty()) return;
        imageMapper.deleteImagesByIds(idsToDelete);
    }

    @Override
    @Transactional
    public void appendNewImages(long itemId,
                                MultipartFile mainImage,
                                List<MultipartFile> detailImages) {

        // 1) 새 대표 이미지가 있으면 추가
        if (mainImage != null && !mainImage.isEmpty()) {
            try {
                String mainUrl = saveOneFileAndGetUrl(mainImage);

                ImageVO mainVO = new ImageVO();
                mainVO.setImageUrl(mainUrl);
                mainVO.setImageType("MAIN");
                mainVO.setItemId(itemId);

                imageMapper.insertImage(mainVO);
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("새 대표 이미지 저장 실패", e);
            }
        }

        // 2) 새 상세 이미지들 추가
        if (detailImages != null && !detailImages.isEmpty()) {
            // 최대 5장 제한을 백엔드에서도 한 번 더 방어하고 싶다면:
            List<MultipartFile> limited = detailImages.size() > 5
                    ? detailImages.subList(0, 5)
                    : detailImages;

            for (MultipartFile file : limited) {
                if (file == null || file.isEmpty()) continue;

                try {
                    String detailUrl = saveOneFileAndGetUrl(file);

                    ImageVO detailVO = new ImageVO();
                    detailVO.setImageUrl(detailUrl);
                    detailVO.setImageType("DETAIL");
                    detailVO.setItemId(itemId);

                    imageMapper.insertImage(detailVO);
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new RuntimeException("새 상세 이미지 저장 실패", e);
                }
            }
        }
    }

    @Override
    public List<ImageVO> getImagesByItemId(long itemId) {
        return imageMapper.selectImagesByItemId(itemId);
    }


    /**
     * 파일 1장을 디스크에 저장하고,
     * DB에 넣을 IMAGE_URL(상대경로: yymm/uuid_filename)을 반환.
     */
    private String saveOneFileAndGetUrl(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            originalFilename = "unknown";
        }

        // 브라우저마다 경로가 섞여 올 수 있으니 파일명만 분리 (\ 또는 / 기준)
        int pos = Math.max(originalFilename.lastIndexOf("\\"), originalFilename.lastIndexOf("/"));
        String filename = (pos == -1) ? originalFilename : originalFilename.substring(pos + 1);

        // 파일 이름: uuid_filename
        String uuid = UUID.randomUUID().toString();
        String savedName = uuid + "_" + filename;  // ✅ uuid_filname 형식

        // 디렉토리 이름: yymm (예: 2511)
        String dirPath = makeFolder();             // ✅ yymm

        // 실제 저장 경로: uploadPath/yymm/uuid_filename
        String fullDirPath = uploadPath + "/" + dirPath;
        String fullPath = fullDirPath + "/" + savedName; // ✅ 요구사항: uploadPath/dirPath/uuid_filname

        // 디렉토리 없으면 생성
        File saveDir = new File(fullDirPath);
        if (!saveDir.exists()) {
            saveDir.mkdirs();
        }

        File saveFile = new File(fullPath);
        file.transferTo(saveFile);

        // DB에 저장할 상대 경로 (yymm/uuid_filename)
        return dirPath + "/" + savedName;
    }

    /**
     * 디렉토리 이름: yymm (예: 2511)
     */
    private String makeFolder() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyMM"));
    }
}
