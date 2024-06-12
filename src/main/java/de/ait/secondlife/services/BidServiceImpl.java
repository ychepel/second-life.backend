package de.ait.secondlife.services;

import de.ait.secondlife.domain.entity.Bid;
import de.ait.secondlife.repositories.BidRepository;
import de.ait.secondlife.services.interfaces.BidService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BidServiceImpl implements BidService {

    private final BidRepository bidRepository;

    @Override
    public Bid getById(Long id) {
        return bidRepository.findById(id).orElse(null);
    }
}
