package org.open.address.repository;

import org.open.address.domain.Address;

import java.util.List;

public interface AddressRepositoryCustom {

    List<Address> search(String longitude, String latitude);
}
