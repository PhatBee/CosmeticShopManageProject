package com.phatbee.cosmeticshopbackend.Service;

import com.phatbee.cosmeticshopbackend.dto.BannerDTO;

import java.util.List;

public interface BannerService {
    public List<BannerDTO> getAllActiveBanners();
    public BannerDTO getBanner(Long id);
    public BannerDTO createBanner(BannerDTO bannerDTO);
    public BannerDTO updateBanner(Long id, BannerDTO bannerDTO);
    public boolean deleteBanner(Long id);

}
