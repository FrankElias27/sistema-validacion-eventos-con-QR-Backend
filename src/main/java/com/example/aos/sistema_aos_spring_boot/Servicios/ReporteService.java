package com.example.aos.sistema_aos_spring_boot.Servicios;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

import com.example.aos.sistema_aos_spring_boot.Modelo.Reporte;

import net.sf.jasperreports.engine.JRException;

public interface ReporteService {
    /**
     * @author <a href="mailto:4softwaredevelopers@gmail.com">Jordy Rodríguezr</a>
     * @date 24 sep. 2021
     * @description
     * @HU_CU_REQ
     * @param params
     * @return
     */
    Reporte obtenerReporteClientes(Map<String, Object> params) throws JRException, IOException, SQLException;
    Reporte obtenerReporteAsistencia(Map<String, Object> params) throws JRException, IOException, SQLException;
    Reporte obtenerReporteEstado(Map<String, Object> params) throws JRException, IOException, SQLException;
    Reporte obtenerReporteInvitados(Map<String, Object> params) throws JRException, IOException, SQLException;
}


