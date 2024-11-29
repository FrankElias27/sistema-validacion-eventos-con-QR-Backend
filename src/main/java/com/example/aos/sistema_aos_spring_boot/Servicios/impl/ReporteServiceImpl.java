package com.example.aos.sistema_aos_spring_boot.Servicios.impl;
import java.awt.image.BufferedImage;
import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.sql.DataSource;

import com.example.aos.sistema_aos_spring_boot.Servicios.ReporteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.example.aos.sistema_aos_spring_boot.Commons.JasperReportManager;
import com.example.aos.sistema_aos_spring_boot.Enums.TipoReporteEnum;
import com.example.aos.sistema_aos_spring_boot.Modelo.Reporte;
import com.example.aos.sistema_aos_spring_boot.Servicios.ReporteService;

import net.sf.jasperreports.engine.JRException;

/**
 * @author <a href="mailto:4softwaredevelopers@gmail.com">Jordy Rodríguezr</a>
 * @project demo-spring-boot-jasper
 * @class ReporteServiceImpl
 * @description
 * @HU_CU_REQ
 * @date 24 sep. 2021
 */
@Service
public class ReporteServiceImpl implements ReporteService {

    @Autowired
    private JasperReportManager reportManager;

    @Autowired
    private DataSource dataSource;

    @Override
    @Async
    @Cacheable("clientesReportes")
    public Reporte obtenerReporteClientes(Map<String, Object> params) throws JRException, IOException, SQLException {
        return generarReporte("ReporteClientes", params);
    }

    @Override
    @Async
    @Cacheable("asistenciaReportes")
    public Reporte obtenerReporteAsistencia(Map<String, Object> params) throws JRException, IOException, SQLException {
        return generarReporte("ReporteAsistencia", params);
    }

    @Override
    @Async
    @Cacheable("estadoReportes")
    public Reporte obtenerReporteEstado(Map<String, Object> params) throws JRException, IOException, SQLException {
        return generarReporte("ReporteBaneados", params);
    }

    @Override
    @Async
    @Cacheable("InvitadoReportes")
    public Reporte obtenerReporteInvitados(Map<String, Object> params) throws JRException, IOException, SQLException {
        return generarReporte("ReporteInvitados", params);
    }

    private Reporte generarReporte(String reportName, Map<String, Object> params) throws JRException, IOException, SQLException {
        Reporte dto = new Reporte();
        String extension = params.get("tipo").toString().equalsIgnoreCase(TipoReporteEnum.EXCEL.name()) ? ".xlsx" : ".pdf";
        dto.setFileName(reportName + extension);

        BufferedImage bufferedImage;
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("Images/Eva.png")) {
            if (is == null) {
                throw new IOException("No se encontró el archivo de imagen");
            }
            bufferedImage = ImageIO.read(is);
        }
        params.put("image", bufferedImage);

        try (Connection connection = dataSource.getConnection();
             ByteArrayOutputStream stream = reportManager.export(reportName, params.get("tipo").toString(), params, connection)) {

            byte[] bs = stream.toByteArray();
            dto.setStream(new ByteArrayInputStream(bs));
            dto.setLength(bs.length);
        } catch (SQLException | JRException e) {

            throw new SQLException("Error al generar el reporte: " + e.getMessage(), e);
        }

        return dto;
    }
}
