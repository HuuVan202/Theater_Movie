package movie_theater_gr4.project_gr4.mapper;

import movie_theater_gr4.project_gr4.dto.AccountGGDTO;
import movie_theater_gr4.project_gr4.dto.AccountProflieDTO;
import movie_theater_gr4.project_gr4.dto.AccountRegisterDTO;
import movie_theater_gr4.project_gr4.model.Account;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface AccountMapper {
//    @Mapping(target = "accountId", ignore = true)
//    @Mapping(target = "phoneNumber", ignore = true)
//    @Mapping(target = "address", ignore = true)
//    @Mapping(target = "dateOfBirth", ignore = true)
//    @Mapping(target = "gender", ignore = true)
//    @Mapping(target = "identityCard", ignore = true)
//    @Mapping(target = "registerDate", ignore = true)
//    @Mapping(target = "status", ignore = true)
//    @Mapping(target = "role", ignore = true)
    Account toAccountFromAccountRegisterDTO(AccountRegisterDTO accountRegisterDTO);

    Account toAccountFromAccountGGDTO(AccountGGDTO accountGGDTO);

    AccountProflieDTO toAccountProflieDTO(Account account);

    @Mapping(target = "accountId", ignore = true) // Bỏ qua ID khi ánh xạ
    void updateAccountFromAccountProflieDTO(AccountProflieDTO accountProflieDTO, @MappingTarget Account account);
}
