package com.luxe.store.repository;
import com.luxe.store.model.ArqueoCaja;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
public interface ArqueoCajaRepository extends JpaRepository<ArqueoCaja,Long> {
 List<ArqueoCaja> findByVendedorIdAndFechaOrderByIdDesc(Long vendedorId,LocalDate fecha);
 List<ArqueoCaja> findByVendedorIdAndFechaBetweenOrderByFechaDescIdDesc(Long vendedorId,LocalDate desde,LocalDate hasta);
}
