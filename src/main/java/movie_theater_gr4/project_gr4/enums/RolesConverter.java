package movie_theater_gr4.project_gr4.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class RolesConverter implements AttributeConverter<Roles, Integer> {

    @Override
    public Integer convertToDatabaseColumn(Roles role) {
        if (role == null) {
            return null;
        }
        return role.getValue();
    }

    @Override
    public Roles convertToEntityAttribute(Integer value) {
        if (value == null) {
            return null;
        }
        return Roles.fromValue(value);
    }
}