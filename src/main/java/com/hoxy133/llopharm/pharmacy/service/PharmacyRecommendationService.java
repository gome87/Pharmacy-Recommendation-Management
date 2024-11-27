package com.hoxy133.llopharm.pharmacy.service;

import com.hoxy133.llopharm.api.dto.DocumentDto;
import com.hoxy133.llopharm.api.dto.KakaoApiResponseDto;
import com.hoxy133.llopharm.api.service.KakaoAddressSearchService;
import com.hoxy133.llopharm.direction.dto.OutputDto;
import com.hoxy133.llopharm.direction.entity.Direction;
import com.hoxy133.llopharm.direction.service.Base62Service;
import com.hoxy133.llopharm.direction.service.DirectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PharmacyRecommendationService {

    private final KakaoAddressSearchService kakaoAddressSearchService;
    private final DirectionService directionService;
    private final Base62Service base62Service;

    @Value("${pharmacy.recommendation.base.url}")
    private String baseUrl;

    @Value("${pharmacy.recommendation.road.url}")
    private String roadUrl;

    public List<OutputDto> recommendPharmacyList(String address){

        KakaoApiResponseDto kakaoApiResponseDto = kakaoAddressSearchService.requestAddressSearch(address);

        // 값 체크
        if(Objects.isNull(kakaoApiResponseDto) || CollectionUtils.isEmpty(kakaoApiResponseDto.getDocumentList())){
            log.error("(PharmacyRecommendationService - recommendPharmacyList fail] Input address : {}", address);
            return Collections.emptyList();
        }

        // 첫번째 값 사용
        DocumentDto documentDto = kakaoApiResponseDto.getDocumentList().get(0);

        // 가까운 약국 찾기
        // 공공기관 약국 데이터 및 거리계산 알고리즘 이용
        //List<Direction> directionList = directionService.buildDirectionList(documentDto);

        // kakao 카테고리를 이용한 장소 검색 api 이용
        List<Direction> directionList = directionService.buildDirectionListByCategoryApi(documentDto);

        return directionService.saveAll(directionList)
                .stream()
                .map(this::convertToOutputDto)
                .collect(Collectors.toList());
    }

    private OutputDto convertToOutputDto(Direction direction){
        return OutputDto.builder()
                .pharmacyName(direction.getTargetPharmacyName())
                .pharmacyAddress(direction.getTargetAddress())
                .directionUrl(baseUrl.concat(base62Service.encodeDirectionId(direction.getId())))
                .roadViewUrl(roadUrl.concat(base62Service.encodeDirectionId(direction.getId())))
                .distance(String.format("%.2f km", direction.getDistance()))
                .build();
    }

}
