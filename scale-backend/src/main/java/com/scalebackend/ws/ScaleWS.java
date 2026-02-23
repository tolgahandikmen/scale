package com.scalebackend.ws;

import com.scalebackend.dto.*;
import com.scalebackend.dto.request.CreateSheetRequestDTO;
import com.scalebackend.dto.request.CreateTemplateVersionRequestDTO;
import com.scalebackend.dto.request.SaveFieldsVersionRequestDTO;
import com.scalebackend.dto.response.ServiceMessageResponse;
import com.scalebackend.service.ScaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/scale")
@CrossOrigin(origins = "*")
public class ScaleWS {

    @Autowired
    private ScaleService scaleService;

    @PostMapping("/createType")
    public ResponseEntity<ServiceMessageResponse<TypeDTO>> createType(@RequestBody TypeDTO dto) {
        try {
            TypeDTO created = scaleService.createType(dto);
            return ok("type is created successfully.", created);
        } catch (Exception ex) {
            return error("type can not be created. " + ex.getMessage());
        }
    }

    @GetMapping("/items/tree")
    public ResponseEntity<ServiceMessageResponse<List<ItemTreeNodeDTO>>> getItemsTree() {
        try {
            return ok("item tree fetched successfully.", scaleService.getItemsTree());
        } catch (Exception ex) {
            return error("item tree can not be fetched. " + ex.getMessage());
        }
    }

    @GetMapping("/items/{itemId}")
    public ResponseEntity<ServiceMessageResponse<ItemDTO>> getItemById(@PathVariable Integer itemId) {
        try {
            return ok("item fetched successfully.", scaleService.getItemById(itemId));
        } catch (Exception ex) {
            return error("item can not be fetched. " + ex.getMessage());
        }
    }

    @GetMapping("/items/partIds")
    public ResponseEntity<ServiceMessageResponse<List<String>>> listPartIds() {
        try {
            return ok("part ids fetched successfully.", scaleService.listPartIds());
        } catch (Exception ex) {
            return error("part ids can not be fetched. " + ex.getMessage());
        }
    }

    @PostMapping("/sheets")
    public ResponseEntity<ServiceMessageResponse<SheetDTO>> createSheet(@RequestBody CreateSheetRequestDTO request) {
        try {
            return ok("sheet is created successfully.", scaleService.createSheet(request));
        } catch (DataIntegrityViolationException ex) {
            return error(HttpStatus.CONFLICT, "sheet can not be created due to duplicate or invalid relation.");
        } catch (Exception ex) {
            return error("sheet can not be created. " + ex.getMessage());
        }
    }

    @GetMapping("/sheets/{sheetId}")
    public ResponseEntity<ServiceMessageResponse<SheetDetailDTO>> getSheet(@PathVariable Integer sheetId) {
        try {
            return ok("sheet fetched successfully.", scaleService.getSheet(sheetId));
        } catch (Exception ex) {
            return error("sheet can not be fetched. " + ex.getMessage());
        }
    }

    @GetMapping("/sheets")
    public ResponseEntity<ServiceMessageResponse<List<SheetDTO>>> listItemSheets(@RequestParam Integer itemId, @RequestParam String kind) {
        try {
            return ok("item sheets fetched successfully.", scaleService.listItemSheets(itemId, kind));
        } catch (Exception ex) {
            return error("item sheets can not be fetched. " + ex.getMessage());
        }
    }

    @GetMapping("/sheets/{inputSheetId}/outputs")
    public ResponseEntity<ServiceMessageResponse<List<SheetDTO>>> listInputOutputs(@PathVariable Integer inputSheetId) {
        try {
            return ok("output sheets fetched successfully.", scaleService.listInputOutputs(inputSheetId));
        } catch (Exception ex) {
            return error("output sheets can not be fetched. " + ex.getMessage());
        }
    }

    @GetMapping("/templates")
    public ResponseEntity<ServiceMessageResponse<List<TemplateDTO>>> getTemplates(@RequestParam(required = false) String kind) {
        try {
            return ok("templates fetched successfully.", scaleService.getTemplates(kind));
        } catch (Exception ex) {
            return error("templates can not be fetched. " + ex.getMessage());
        }
    }

    @GetMapping("/templates/{templateId}/fields")
    public ResponseEntity<ServiceMessageResponse<List<FieldDefinitionDTO>>> getTemplateFields(@PathVariable Integer templateId) {
        try {
            return ok("template fields fetched successfully.", scaleService.getTemplateFields(templateId));
        } catch (Exception ex) {
            return error("template fields can not be fetched. " + ex.getMessage());
        }
    }

    @PostMapping("/templates/version")
    public ResponseEntity<ServiceMessageResponse<TemplateDTO>> createTemplateVersion(@RequestBody CreateTemplateVersionRequestDTO request) {
        try {
            return ok("template version is created successfully.", scaleService.createTemplateVersion(request));
        } catch (Exception ex) {
            return error("template version can not be created. " + ex.getMessage());
        }
    }

    @PostMapping("/templates/fields/version")
    public ResponseEntity<ServiceMessageResponse<TemplateDTO>> saveFieldsAsNewVersion(@RequestBody SaveFieldsVersionRequestDTO request) {
        try {
            return ok("fields are saved as new version successfully.", scaleService.saveFieldsAsNewVersion(request));
        } catch (Exception ex) {
            return error("fields can not be saved as new version. " + ex.getMessage());
        }
    }

    @GetMapping("/part-template-mappings")
    public ResponseEntity<ServiceMessageResponse<List<PartTemplateMappingDTO>>> listPartTemplateMappings() {
        try {
            return ok("part-template mappings fetched successfully.", scaleService.listPartTemplateMappings());
        } catch (Exception ex) {
            return error("part-template mappings can not be fetched. " + ex.getMessage());
        }
    }

    @GetMapping("/part-template-mappings/{partId}")
    public ResponseEntity<ServiceMessageResponse<TemplatesForPartDTO>> getTemplatesForPart(@PathVariable String partId) {
        try {
            return ok("part template rules fetched successfully.", scaleService.getTemplatesForPart(partId));
        } catch (Exception ex) {
            return error("part template rules can not be fetched. " + ex.getMessage());
        }
    }

    @PostMapping("/part-template-mappings")
    public ResponseEntity<ServiceMessageResponse<PartTemplateMappingDTO>> savePartTemplateMapping(@RequestBody PartTemplateMappingDTO dto) {
        try {
            return ok("part-template mapping is saved successfully.", scaleService.savePartTemplateMapping(dto));
        } catch (Exception ex) {
            return error("part-template mapping can not be saved. " + ex.getMessage());
        }
    }

    private <T> ResponseEntity<ServiceMessageResponse<T>> ok(String detail, T data) {
        return ResponseEntity.ok(ServiceMessageResponse.success(detail, data));
    }

    private <T> ResponseEntity<ServiceMessageResponse<T>> error(String detail) {
        return ResponseEntity.internalServerError().body(ServiceMessageResponse.error(detail));
    }

    private <T> ResponseEntity<ServiceMessageResponse<T>> error(HttpStatus status, String detail) {
        return ResponseEntity.status(status).body(ServiceMessageResponse.error(detail));
    }
}
