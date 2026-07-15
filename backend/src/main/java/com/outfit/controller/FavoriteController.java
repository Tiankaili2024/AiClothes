package com.outfit.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.outfit.common.Result;
import com.outfit.entity.OutfitFavorite;
import com.outfit.entity.OutfitRecord;
import com.outfit.mapper.OutfitFavoriteMapper;
import com.outfit.mapper.OutfitRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/favorite")
public class FavoriteController {
    @Autowired private OutfitFavoriteMapper favoriteMapper;
    @Autowired private OutfitRecordMapper recordMapper;

    @PostMapping("/add/{recordId}")
    public Result<?> add(@PathVariable Long recordId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        OutfitFavorite fav = new OutfitFavorite();
        fav.setUserId(userId);
        fav.setRecordId(recordId);
        try {
            favoriteMapper.insert(fav);
            return Result.success();
        } catch (Exception e) {
            return Result.error("已收藏过该穿搭");
        }
    }

    @DeleteMapping("/remove/{recordId}")
    public Result<?> remove(@PathVariable Long recordId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        LambdaQueryWrapper<OutfitFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OutfitFavorite::getUserId, userId).eq(OutfitFavorite::getRecordId, recordId);
        favoriteMapper.delete(wrapper);
        return Result.success();
    }

    @GetMapping("/list")
    public Result<?> list(@RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "10") int size,
                          HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        LambdaQueryWrapper<OutfitFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OutfitFavorite::getUserId, userId).orderByDesc(OutfitFavorite::getCreatedAt);
        Page<OutfitFavorite> favPage = favoriteMapper.selectPage(new Page<>(page, size), wrapper);
        // 关联查询穿搭记录
        Page<OutfitRecord> recordPage = new Page<>();
        recordPage.setCurrent(favPage.getCurrent());
        recordPage.setSize(favPage.getSize());
        recordPage.setTotal(favPage.getTotal());
        recordPage.setRecords(favPage.getRecords().stream().map(fav -> {
            OutfitRecord record = recordMapper.selectById(fav.getRecordId());
            if (record != null) record.setId(fav.getRecordId());
            return record;
        }).filter(r -> r != null).collect(java.util.stream.Collectors.toList()));
        return Result.success(recordPage);
    }

    @GetMapping("/check/{recordId}")
    public Result<?> check(@PathVariable Long recordId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        LambdaQueryWrapper<OutfitFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OutfitFavorite::getUserId, userId).eq(OutfitFavorite::getRecordId, recordId);
        return Result.success(favoriteMapper.selectCount(wrapper) > 0);
    }
}
