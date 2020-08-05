package org.open.address.repository;

import org.open.address.domain.Address;
import org.open.address.domain.AddressPK;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressRepository extends CrudRepository<Address, AddressPK>, AddressRepositoryCustom {
}
