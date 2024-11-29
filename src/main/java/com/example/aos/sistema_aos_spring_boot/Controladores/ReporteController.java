package com.example.aos.sistema_aos_spring_boot.Controladores;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

import com.example.aos.sistema_aos_spring_boot.Modelo.Reporte;
import com.example.aos.sistema_aos_spring_boot.Servicios.ReporteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.aos.sistema_aos_spring_boot.Enums.TipoReporteEnum;

import net.sf.jasperreports.engine.JRException;

@RestController
@RequestMapping("/api/report")
@CrossOrigin("*")
public class ReporteController {
    @Autowired
    private ReporteService reporteService;

    private static final Logger logger = LoggerFactory.getLogger(ReporteController.class);

    @GetMapping(path = "/clientes")
    public ResponseEntity<Resource> downloadClientes(@RequestParam Map<String, Object> params)
            throws JRException, IOException, SQLException {
        logger.info("Descargando reporte de clientes con parámetros: {}", params);
        return downloadReporte(reporteService.obtenerReporteClientes(params), params);
    }

    @GetMapping(path = "/asistencia")
    public ResponseEntity<Resource> downloadAsistencia(@RequestParam Map<String, Object> params)
            throws JRException, IOException, SQLException {
        logger.info("Descargando reporte de asistencia con parámetros: {}", params);
        return downloadReporte(reporteService.obtenerReporteAsistencia(params), params);
    }

    @GetMapping(path = "/invitados")
    public ResponseEntity<Resource> downloadEstado(@RequestParam Map<String, Object> params)
            throws JRException, IOException, SQLException {
        logger.info("Descargando reporte de estado con parámetros: {}", params);
        return downloadReporte(reporteService.obtenerReporteEstado(params), params);
    }

    @GetMapping(path = "/invitadoPorUsuario")
    public ResponseEntity<Resource> downloadInvitados(@RequestParam Map<String, Object> params)
            throws JRException, IOException, SQLException {
        logger.info("Descargando reporte de invitados con parámetros: {}", params);
        return downloadReporte(reporteService.obtenerReporteInvitados(params), params);
    }

    private ResponseEntity<Resource> downloadReporte(Reporte dto, Map<String, Object> params) {

        if (dto == null || dto.getStream() == null) {
            logger.error("El reporte está vacío o el stream es nulo.");
            return ResponseEntity.badRequest().body(null);
        }

        MediaType mediaType;
        try {
            String tipo = params.get("tipo").toString();
            mediaType = tipo.equalsIgnoreCase(TipoReporteEnum.EXCEL.name())
                    ? MediaType.APPLICATION_OCTET_STREAM
                    : MediaType.APPLICATION_PDF;
        } catch (Exception e) {
            logger.error("Error al determinar el tipo de reporte: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }

        InputStreamResource streamResource;
        try {
            streamResource = new InputStreamResource(dto.getStream());
        } catch (Exception e) {
            logger.error("Error al crear el InputStreamResource: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

        return ResponseEntity.ok()
                .header("Content-Disposition", "inline; filename=\"" + dto.getFileName() + "\"")
                .contentLength(dto.getLength())
                .contentType(mediaType)
                .body(streamResource);
    }
}
