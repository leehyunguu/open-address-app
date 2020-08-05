package org.open.address.controller;

import org.open.address.domain.Address;
import org.open.address.repository.AddressRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AddressController {

    private final AddressRepository addressRepository;

    public AddressController(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    @GetMapping(value = "/location")
    public List<Address> getLocation(@RequestParam String longitude, @RequestParam String latitude) {
        return addressRepository.search(longitude, latitude);
    }

}
