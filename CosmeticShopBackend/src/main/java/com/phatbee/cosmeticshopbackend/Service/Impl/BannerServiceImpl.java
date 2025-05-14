package com.phatbee.cosmeticshopbackend.Service.Impl;

import com.phatbee.cosmeticshopbackend.Entity.Banner;
import com.phatbee.cosmeticshopbackend.Repository.BannerRepository;
import com.phatbee.cosmeticshopbackend.Service.BannerService;
import com.phatbee.cosmeticshopbackend.dto.BannerDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BannerServiceImpl implements BannerService {
    private final BannerRepository bannerRepository;
    public BannerServiceImpl(BannerRepository bannerRepository) {
        this.bannerRepository = bannerRepository;
    }


    @Override
    public List<BannerDTO> getAllActiveBanners() {
        List<Banner> banners = bannerRepository.findActiveOrderByDisplayOrder();
        return banners.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public BannerDTO getBanner(Long id) {
        return bannerRepository.findById(Math.toIntExact(id))
                .map(this::convertToDTO)
                .orElse(null);
    }

    @Override
    public BannerDTO createBanner(BannerDTO bannerDTO) {
        Banner banner = convertToEntity(bannerDTO);
        Banner savedBanner = bannerRepository.save(banner);
        return convertToDTO(savedBanner);
    }

    @Override
    public BannerDTO updateBanner(Long id, BannerDTO bannerDTO) {
        return bannerRepository.findById(Math.toIntExact(id))
                .map(existingBanner -> {
                    // Only copy non-null properties from DTO to entity
                    if (bannerDTO.getTitle() != null) existingBanner.setTitle(bannerDTO.getTitle());
                    if (bannerDTO.getImageUrl() != null) existingBanner.setImageUrl(bannerDTO.getImageUrl());
                    if (bannerDTO.getActionUrl() != null) existingBanner.setActionUrl(bannerDTO.getActionUrl());
                    if (bannerDTO.getDisplayOrder() != null) existingBanner.setDisplayOrder(bannerDTO.getDisplayOrder());
                    if (bannerDTO.getActive() != null) existingBanner.setActive(bannerDTO.getActive());

                    Banner updatedBanner = bannerRepository.save(existingBanner);
                    return convertToDTO(updatedBanner);
                })
                .orElse(null);
    }

    @Override
    public boolean deleteBanner(Long id) {
        if (bannerRepository.existsById(Math.toIntExact(id))) {
            bannerRepository.deleteById(Math.toIntExact(id));
            return true;
        }
        return false;
    }

    private BannerDTO convertToDTO(Banner banner) {
        BannerDTO bannerDTO = new BannerDTO();
        BeanUtils.copyProperties(banner, bannerDTO);
        return bannerDTO;
    }

    private Banner convertToEntity(BannerDTO bannerDTO) {
        Banner banner = new Banner();
        BeanUtils.copyProperties(bannerDTO, banner);
        return banner;
    }
}