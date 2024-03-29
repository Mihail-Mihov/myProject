package com.example.myproject.service.impl;

import com.example.myproject.model.binding.OfferAddBindModel;
import com.example.myproject.model.entity.*;
import com.example.myproject.model.entity.UserRoleEnum;
import com.example.myproject.model.service.OfferAddServiceModel;
import com.example.myproject.model.service.OfferUpdateServiceModel;
import com.example.myproject.model.view.OfferDetailsView;
import com.example.myproject.repository.OfferRepository;
import com.example.myproject.repository.UserRepository;
import com.example.myproject.service.OfferService;
import com.example.myproject.web.exception.ObjectNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OfferServiceImpl implements OfferService {

    private final OfferRepository offerRepository;
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;

    public OfferServiceImpl(OfferRepository offerRepository, ModelMapper modelMapper, UserRepository userRepository) {
        this.offerRepository = offerRepository;
        this.modelMapper = modelMapper;
        this.userRepository = userRepository;
    }


    @Override
    public List<OfferEntity> getAllByAuthor(Long authorId) {
        return offerRepository.getAllByAuthor(authorId);
    }

    @Override
    @Transactional
    public List<OfferDetailsView> getByKeyword(String keyword) {
        return offerRepository.getAllByName(keyword.toUpperCase())
                .stream().map(o -> modelMapper.map(o, OfferDetailsView.class))
                .collect(Collectors.toList());
    }


    @Transactional
    @Override
    public List<OfferDetailsView> getAllOffers(String currentUser) {
        return offerRepository.
                findAllOffers().
                stream().
                map(o -> mapToDetailsView(null, o)).
                collect(Collectors.toList());
    }

    @Override
    public OfferDetailsView findOfferById(Long id, String currentUser) {
        return  offerRepository.
                findById(id).
                map(o -> mapToDetailsView(currentUser, o))
                .get();

    }
    private OfferDetailsView mapToDetailsView(String currentUser, OfferEntity offer) {

        return OfferDetailsView.builder()
                .id(offer.getId())
                .canDelete(hasPrivileges(currentUser, offer.getId()))
                .description(offer.getDescription())
                .category(offer.getCategory())
                .price(offer.getPrice())
                .imageUrl(offer.getImageUrl())
                .sellerFullName(offer.getAuthor().getFirstName() + " " + offer.getAuthor().getLastName())
                .build();
    }

    public boolean hasPrivileges(String userName, Long offerId) {
        Optional<OfferEntity> offer = offerRepository.
                findById(offerId);
        Optional<UserEntity> user = userRepository.
                findByUsername(userName);

        if (offer.isEmpty() || user.isEmpty()) {
            return false;
        } else {
            OfferEntity offerEntity = offer.get();

            return isAdmin(user.get()) ||
                    offerEntity.getAuthor().getUsername().equals(userName);
        }
    }

    private boolean isAdmin(UserEntity user) {
        return user.
                getRoles().
                stream().
                map(UserRoleEntity::getRole).
                anyMatch(r -> r == UserRoleEnum.ADMIN);
    }


    @Override
    public void deleteOffer(Long id) {
        offerRepository.deleteById(id);
    }

    @Override
    public void updateOffer(OfferUpdateServiceModel offerModel) {

        OfferEntity offerEntity  =offerRepository.findById(offerModel.getId()).orElseThrow(() ->
                new ObjectNotFoundException("There is no offer with id: " + offerModel.getId()));

        offerEntity.setName(offerModel.getName());
        offerEntity.setCategory(offerModel.getCategory());
        offerEntity.setDescription(offerModel.getDescription());
        offerEntity.setImageUrl(offerModel.getImageUrl());
        offerEntity.setPrice(offerModel.getPrice());

        offerRepository.save(offerEntity);
    }

    @Override
    public OfferAddServiceModel addOffer(OfferAddBindModel offerAddBindModel,String username) {
       UserEntity userEntity = userRepository.findByUsername(username).orElseThrow();
       OfferAddServiceModel offerAddServiceModel = modelMapper.map(offerAddBindModel, OfferAddServiceModel.class);
       OfferEntity offerEntity = modelMapper.map(offerAddServiceModel, OfferEntity.class);
       if(offerAddBindModel.getImageUrl().isEmpty()) {
           offerEntity.setImageUrl("https://www.theiasilver.com/index.php/images/products/162134606160a3c70d603fd.jpg");
       }
     //   offerEntity.setCreated(Instant.now());
        offerEntity.setAuthor(userEntity);
        // ModelEntity mozdel = modelRepository.getById(offerAddBindModel.getModelId());
        //newOffer.setModel(model);

        OfferEntity offer = offerRepository.save(offerEntity);
        return modelMapper.map(offer, OfferAddServiceModel.class);
    }


}
