package com.boot3.myrestapi.lectures;

import com.boot3.myrestapi.common.errors.ErrorsResource;
import com.boot3.myrestapi.lectures.dto.LectureReqDto;
import com.boot3.myrestapi.lectures.dto.LectureResDto;
import com.boot3.myrestapi.lectures.dto.LectureResource;
import com.boot3.myrestapi.lectures.validator.LectureValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping(value = "/api/lectures", produces = MediaTypes.HAL_JSON_VALUE)
@RequiredArgsConstructor
public class LectureController {
    private final LectureRepository lectureRepository;
    private final ModelMapper modelMapper;
    private final LectureValidator lectureValidator;

    //    public LectureController(LectureRepository lectureRepository) {
//        this.lectureRepository = lectureRepository;
//    }

    @GetMapping
    public ResponseEntity<?> queryLectures(Pageable pageable, PagedResourcesAssembler<LectureResDto> assembler) {
        Page<Lecture> lecturePage = this.lectureRepository.findAll(pageable);
        // Page<Lecture> => Page<LectureResDto>
        Page<LectureResDto> lectureResDtoPage = lecturePage
                .map(lecture -> modelMapper.map(lecture, LectureResDto.class));
        // Page<LectureResDto> => PagedModel<EntityModel<LectureResDto>>
        //PagedModel<EntityModel<LectureResDto>> pagedModel = assembler.toModel(lectureResDtoPage);

//        PagedModel<LectureResource> pagedModel = assembler
//                .toModel(lectureResDtoPage, lectureResDto -> new LectureResource(lectureResDto));
        PagedModel<LectureResource> pagedModel = assembler.toModel(lectureResDtoPage, LectureResource::new);

        return ResponseEntity.ok(pagedModel);
    }

    @PostMapping
    public ResponseEntity<?> createLecture(@RequestBody @Valid LectureReqDto lectureReqDto, Errors errors) {
        //입력항목 검증오류 발생했는지 체크
        if (errors.hasErrors()) {
            return badRequest(errors);
        }

        //Biz로직의 입력항목 체크
        lectureValidator.validate(lectureReqDto, errors);
        if (errors.hasErrors()) {
            return badRequest(errors);
        }

        //ReqDto => Entity 매핑
        Lecture lecture = modelMapper.map(lectureReqDto, Lecture.class);

        //free, offline 정보 업데이트
        lecture.update();

        Lecture addLecture = this.lectureRepository.save(lecture);
        //Entity => ResDto 매핑
        LectureResDto lectureResDto = modelMapper.map(addLecture, LectureResDto.class);
        WebMvcLinkBuilder selfLinkBuilder = WebMvcLinkBuilder.linkTo(LectureController.class)
                .slash(lectureResDto.getId());
        URI createUri = selfLinkBuilder.toUri();

        LectureResource lectureResource = new LectureResource(lectureResDto);
        //relation 이름이 query-lectures  link 생성
        lectureResource.add(linkTo(LectureController.class).withRel("query-lectures"));
        //relation 이름이 update-lecture  link 생성
        lectureResource.add(selfLinkBuilder.withRel("update-lecture"));

        return ResponseEntity.created(createUri).body(lectureResource);
    }

    private static ResponseEntity<ErrorsResource> badRequest(Errors errors) {

        return ResponseEntity.badRequest().body(new ErrorsResource(errors));
    }
}