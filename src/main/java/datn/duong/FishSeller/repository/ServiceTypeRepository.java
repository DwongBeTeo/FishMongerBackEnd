package datn.duong.FishSeller.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import datn.duong.FishSeller.entity.ServiceTypeEntity;

public interface ServiceTypeRepository extends JpaRepository<ServiceTypeEntity, Long> {
    
}
