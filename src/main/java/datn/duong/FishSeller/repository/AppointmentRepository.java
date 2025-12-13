package datn.duong.FishSeller.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import datn.duong.FishSeller.entity.AppointmentEntity;

public interface AppointmentRepository extends JpaRepository<AppointmentEntity, Long>{
    List<AppointmentEntity> findByAppointmentDate(LocalDate date); // Để check trùng lịch
}
