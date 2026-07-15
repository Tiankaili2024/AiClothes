package com.outfit.controller;
import com.outfit.common.Result;
import com.outfit.dto.OutfitRequestDTO;
import com.outfit.dto.QuickRefineDTO;
import com.outfit.service.OutfitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
@RestController
@RequestMapping("/api/outfit")
public class OutfitController {
    @Autowired private OutfitService outfitService;
    @PostMapping("/generate")
    public Result<?> generate(@RequestBody OutfitRequestDTO dto, HttpServletRequest request) {
        try { return Result.success(outfitService.generateOutfit((Long)request.getAttribute("userId"), dto));
        } catch (Exception e) { return Result.error(e.getMessage()); }
    }
    @PostMapping("/refine")
    public Result<?> quickRefine(@RequestBody QuickRefineDTO dto, HttpServletRequest request) {
        try { return Result.success(outfitService.quickRefine((Long)request.getAttribute("userId"), dto));
        } catch (Exception e) { return Result.error(e.getMessage()); }
    }
    @GetMapping("/records")
    public Result<?> getRecords(@RequestParam(defaultValue="1") int page, @RequestParam(defaultValue="10") int size, HttpServletRequest request) {
        return Result.success(outfitService.getUserRecords((Long)request.getAttribute("userId"), page, size));
    }
    @GetMapping("/detail/{id}")
    public Result<?> getDetail(@PathVariable Long id, HttpServletRequest request) {
        try { return Result.success(outfitService.getRecordDetail(id, (Long)request.getAttribute("userId")));
        } catch (Exception e) { return Result.error(e.getMessage()); }
    }
    @GetMapping("/session/{sessionId}")
    public Result<?> getSessionRecords(@PathVariable String sessionId, HttpServletRequest request) {
        return Result.success(outfitService.getSessionRecords((Long)request.getAttribute("userId"), sessionId));
    }
    @DeleteMapping("/delete/{id}")
    public Result<?> delete(@PathVariable Long id, HttpServletRequest request) {
        outfitService.deleteRecord(id, (Long)request.getAttribute("userId")); return Result.success();
    }
    @DeleteMapping("/clear")
    public Result<?> clear(HttpServletRequest request) {
        outfitService.clearRecords((Long)request.getAttribute("userId")); return Result.success();
    }
    @GetMapping("/today-count")
    public Result<?> todayCount(HttpServletRequest request) {
        return Result.success(outfitService.getTodayCount((Long)request.getAttribute("userId")));
    }
    @GetMapping("/user-portrait")
    public Result<?> userPortrait(HttpServletRequest request) {
        try { return Result.success(outfitService.getUserPortrait((Long)request.getAttribute("userId")));
        } catch (Exception e) { return Result.error(e.getMessage()); }
    }
}
