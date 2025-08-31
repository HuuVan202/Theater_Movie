package movie_theater_gr4.project_gr4.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import movie_theater_gr4.project_gr4.model.Account;
import movie_theater_gr4.project_gr4.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    Cloudinary cloudinary;
    AccountRepository accountRepository;

    public CloudinaryService(Cloudinary cloudinary, AccountRepository accountRepository) {
        this.cloudinary = cloudinary;
        this.accountRepository = accountRepository;
    }

    public Account uploadAvatar(MultipartFile file, String username, String folder) throws IOException {
        try {
            // Upload ảnh lên Cloudinary
            Map data = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", folder,
                    "public_id", "avatar_" + username + "_" + System.currentTimeMillis()
            ));
            String url = (String) data.get("url");

            // Cập nhật avatarUrl trong Account
            Account account = accountRepository.findAccountByUsername(username);
            account.setAvatarUrl(url);
            return accountRepository.save(account);
        } catch (IOException e) {
            throw new RuntimeException("Upload hình ảnh thất bại", e);
        }
    }


    public String uploadAvatarMember(MultipartFile file, String username, String folder) throws IOException {
        try {
            Map data = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", folder,
                    "public_id", "avatar_" + username + "_" + System.currentTimeMillis()
            ));
            return (String) data.get("secure_url");
        } catch (IOException e) {
            throw new RuntimeException("Upload hình ảnh thất bại", e);
        }
    }

    public Map uploadAvatarEmployee(MultipartFile file) throws IOException {
        Map params = ObjectUtils.asMap(
                "folder", "employee_images"
        );
        return cloudinary.uploader().upload(file.getBytes(), params);
    }

}
