package com.hoxy133.llopharm.direction.controller;

import com.hoxy133.llopharm.direction.entity.Direction;
import com.hoxy133.llopharm.direction.service.DirectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Controller
@RequiredArgsConstructor
public class DirectionController {

    private final DirectionService directionService;

    @GetMapping("/dir/{encodedId}")
    public String searchDirection(@PathVariable("encodedId") String encodedId) {

        String result = directionService.findDirectionUrlById(encodedId);

        log.info("[DirectionController - searchDirection] direction url : {}", result);

        return "redirect:" + result;
    }

    @GetMapping("/road/{encodedId}")
    public String searchRoadView(@PathVariable("encodedId") String encodedId) {

        String result = directionService.findRoadViewUrlById(encodedId);

        log.info("[DirectionController - searchRoadView] Road View url : {}", result);

        return "redirect:" + result;
    }

}
