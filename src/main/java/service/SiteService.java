package com.zidio.keystone.service;

import com.zidio.keystone.domain.Customer;
import com.zidio.keystone.domain.Site;
import com.zidio.keystone.repository.SiteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SiteService {

    private final SiteRepository siteRepository;

    public SiteService(SiteRepository siteRepository) {
        this.siteRepository = siteRepository;
    }

    // Create Site
    public Site createSite(Site site) {
        return siteRepository.save(site);
    }

    // Get All Sites
    public List<Site> getAllSites() {
        return siteRepository.findAll();
    }

    // Get Site By ID
    public Site getSiteById(Long id) {
        return siteRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Site not found"));
    }

    // Get Sites By Customer
    public List<Site> getSitesByCustomer(Customer customer) {
        return siteRepository.findByCustomer(customer);
    }

    // Update Site
    public Site updateSite(Long id, Site site) {

        Site existingSite = getSiteById(id);

        existingSite.setName(site.getName());
        existingSite.setAddress(site.getAddress());
        existingSite.setCustomer(site.getCustomer());

        return siteRepository.save(existingSite);
    }

    // Delete Site
    public void deleteSite(Long id) {

        if (!siteRepository.existsById(id)) {
            throw new RuntimeException("Site not found");
        }

        siteRepository.deleteById(id);
    }
}