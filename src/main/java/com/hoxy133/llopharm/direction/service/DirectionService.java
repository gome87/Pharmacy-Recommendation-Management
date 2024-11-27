package com.hoxy133.llopharm.direction.service;

import com.hoxy133.llopharm.api.dto.DocumentDto;
import com.hoxy133.llopharm.api.dto.KakaoApiResponseDto;
import com.hoxy133.llopharm.api.service.KakaoCategorySearchService;
import com.hoxy133.llopharm.direction.entity.Direction;
import com.hoxy133.llopharm.direction.repository.DirectionRepository;
import com.hoxy133.llopharm.pharmacy.dto.PharmacyDto;
import com.hoxy133.llopharm.pharmacy.service.PharmacySearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DirectionService {

    // 약국 최대 검색 갯수
    private static final int MAX_SEARCH_COUNT = 3;
    // 반경 10 km
    private static final double RADIUS_KM = 10.0;

    // 길안내
    private static final String DIRECTION_BASE_URL = "https://map.kakao.com/link/map/";
    // 로드뷰
    private static final String ROAD_VIEW_BASE_URL = "https://map.kakao.com/link/roadview/";

    private final PharmacySearchService pharmacySearchService;
    private final DirectionRepository directionRepository;
    private final KakaoCategorySearchService kakaoCategorySearchService;
    private final Base62Service base62Service;

    @Transactional
    public List<Direction> saveAll(List<Direction> directionList){

        // 파라미터 체크
        if(CollectionUtils.isEmpty(directionList)){
            return Collections.emptyList();
        }
        return directionRepository.saveAll(directionList);
    }

    public String findDirectionUrlById(String encodedId){
        Long decodedId = base62Service.decodeDirectionId(encodedId);
        Direction direction = directionRepository.findById(decodedId).orElse(null);

        // 길안내 URL Param
        String sDirectionParam = String.join(",", direction.getTargetPharmacyName(), String.valueOf(direction.getTargetLatitude()), String.valueOf(direction.getTargetLongitude()));

        // 한글 인코딩
        String sDirectionUrl = UriComponentsBuilder.fromHttpUrl(DIRECTION_BASE_URL.concat(sDirectionParam)).toUriString();

        log.info("[DirectionService - findDirectionUrlById] Param : {}, DirectionUrl : {}", sDirectionParam, sDirectionUrl);

        return sDirectionUrl;
    }

    public List<Direction>  buildDirectionList(DocumentDto documentDto) {

        // 파라미터 null 체크
        if(Objects.isNull(documentDto)) {
            return Collections.emptyList();
        }

        // 약국 데이터 조회
        List<PharmacyDto> pharmacyDtos = pharmacySearchService.searchPharmacyDtoList();

        // 거리 계산 알고리즘을 이용하여, 고객과 약국 사이의 거리를 계산하고 sort
        return pharmacyDtos
                .stream()
                .map(pharmacyDto ->
                        Direction.builder()
                                .inputAddress(documentDto.getAddressName())
                                .inputLatitude(documentDto.getLatitude())
                                .inputLongitude(documentDto.getLongitude())
                                .targetPharmacyName(pharmacyDto.getPharmacyName())
                                .targetAddress(pharmacyDto.getPharmacyAddress())
                                .targetLatitude(pharmacyDto.getLatitude())
                                .targetLongitude(pharmacyDto.getLongitude())
                                .distance(calculateDistance( documentDto.getLatitude()
                                        , documentDto.getLongitude()
                                        , pharmacyDto.getLatitude()
                                        , pharmacyDto.getLongitude()
                                ))
                                .build()
                )
                .filter(direction -> direction.getDistance() <= RADIUS_KM)
                .sorted(Comparator.comparing(Direction::getDistance))
                .limit(MAX_SEARCH_COUNT)
                .collect(Collectors.toList());
    }

    // Pharmacy search by category kakao api
    public List<Direction> buildDirectionListByCategoryApi(DocumentDto inputDocumentDto) {
        // 파라미터 null 체크
        if(Objects.isNull(inputDocumentDto)) {
            return Collections.emptyList();
        }

        List<DocumentDto> categoryDtos = kakaoCategorySearchService.requestPharmacyCategorySearch(inputDocumentDto.getLatitude(), inputDocumentDto.getLongitude(), RADIUS_KM).getDocumentList();

        return categoryDtos
                .stream()
                .map(categoryDto ->
                        Direction.builder()
                                .inputAddress(inputDocumentDto.getAddressName())
                                .inputLatitude(inputDocumentDto.getLatitude())
                                .inputLongitude(inputDocumentDto.getLongitude())
                                .targetPharmacyName(categoryDto.getPlaceName())
                                .targetAddress(categoryDto.getAddressName())
                                .targetLatitude(categoryDto.getLatitude())
                                .targetLongitude(categoryDto.getLongitude())
                                .distance(categoryDto.getDistance() * 0.001) // km 단위
                                .build()
                )
                .limit(MAX_SEARCH_COUNT)
                .collect(Collectors.toList());
    }

    // Haversine formula
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        lat1 = Math.toRadians(lat1);
        lon1 = Math.toRadians(lon1);
        lat2 = Math.toRadians(lat2);
        lon2 = Math.toRadians(lon2);

        double earthRadius = 6371; //Kilometers
        return earthRadius * Math.acos(Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));
    }

}
