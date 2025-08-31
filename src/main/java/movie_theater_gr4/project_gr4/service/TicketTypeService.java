package movie_theater_gr4.project_gr4.service;

import movie_theater_gr4.project_gr4.model.TicketType;
import movie_theater_gr4.project_gr4.repository.TicketTypeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class TicketTypeService {
    private final TicketTypeRepository ticketTypeRepository;

    public TicketTypeService(TicketTypeRepository ticketTypeRepository) {
        this.ticketTypeRepository = ticketTypeRepository;
    }

public Map<Long, TicketType> getTicketTypesMap() {
        return ticketTypeRepository.findAll()
                .stream()
                .collect(Collectors.toMap(TicketType::getTicketTypeId, ticketType -> ticketType));
    }

    public List<TicketType> findAll() {
        return ticketTypeRepository.findAll();
    }
}